package com.mopidy.dz0ny.mopidy.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.mopidy.dz0ny.mopidy.api.Mopidy;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;

import timber.log.Timber;

public class Discovery extends Service {
    public static final String SERVICE_TYPE = "_mopidy-http._tcp.";
    public static String OnRefresh = "DiscoveryHelperRefresh";
    ArrayList<Mopidy> devices = new ArrayList<Mopidy>();
    NsdManager mNsdManager;
    NsdManager.DiscoveryListener mDiscoveryListener;
    private NsdManager.ResolveListener mResolveListener;

    @Override
    public void onCreate() {
        super.onCreate();
        mNsdManager = (NsdManager) this.getSystemService(Context.NSD_SERVICE);
        initializeResolveListener();
        initializeDiscoveryListener();
        mNsdManager.discoverServices(
                SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        refreshListeners();
        return super.onStartCommand(intent, flags, startId);
    }

    public void initializeDiscoveryListener() {
        mDiscoveryListener = new NsdManager.DiscoveryListener() {

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                mNsdManager.resolveService(service, mResolveListener);
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                ArrayList<Mopidy> toremove = new ArrayList<Mopidy>();
                for (Mopidy iapp : devices) {
                    if (iapp.getName().equalsIgnoreCase(service.getServiceName())) {
                        toremove.add(iapp);
                    }
                }
                devices.removeAll(toremove);
                refreshListeners();
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onDiscoveryStarted(String s) {

            }

            @Override
            public void onDiscoveryStopped(String s) {

            }
        };
    }

    public void initializeResolveListener() {
        mResolveListener = new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Timber.i("Failed resolving %s with %d", serviceInfo, errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {

                int port = serviceInfo.getPort();
                InetAddress host = serviceInfo.getHost();
                Timber.i("Discovered  %s", host);
                try {
                    if (host.isReachable(15) && !host.getHostAddress().contains(":")) {
                        Mopidy app = new Mopidy(serviceInfo.getServiceName().replaceAll("\\\\\\\\032", " "), host.getHostAddress(), port);
                        for (Mopidy iapp : devices) {
                            if (iapp.getURL().equalsIgnoreCase(app.getURL())) {
                                return;
                            }
                        }
                        devices.add(app);
                        refreshListeners();
                    }
                } catch (IOException e) {
                    Timber.i("Failed reaching %s", host);
                }

            }
        };
    }

    private void refreshListeners() {
        Intent intent = new Intent(OnRefresh);
        intent.putExtra("devices", devices);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


    @Override
    public void onDestroy() {
        mNsdManager.stopServiceDiscovery(mDiscoveryListener);
        super.onDestroy();
    }

    public static void Stop(Context c){
        Timber.i("Stopping Discovery");
        Intent intent = new Intent(c, Discovery.class);
        c.stopService(intent);
    }

    public static void Start(Context c){
        Timber.i("Starting Discovery");
        Intent intent = new Intent(c, Discovery.class);
        c.startService(intent);
    }
}

package com.mopidy.dz0ny.mopidy.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.mopidy.dz0ny.mopidy.api.Mopidy;

import java.io.IOException;
import java.net.InetAddress;

import timber.log.Timber;

public class Discovery extends Service {
    public static final String SERVICE_TYPE = "_mopidy-http._tcp.";
    public static String OnRefresh = "DiscoveryHelperRefresh";
    public static String OnStop = "DiscoveryHelperStop";
    public static String OnStart = "DiscoveryHelperStart";

    NsdManager mNsdManager;
    NsdManager.DiscoveryListener mDiscoveryListener;
    Handler handler = new Handler();
    private NsdManager.ResolveListener mResolveListener;
    private Runnable autostop = new Runnable() {
        @Override
        public void run() {
            Discovery.Stop(getContext());
        }
    };

    public static void Stop(Context c) {
        Timber.i("Stopping Discovery");
        Intent intent = new Intent(c, Discovery.class);
        c.stopService(intent);
    }

    public static void Start(Context c) {
        Timber.i("Starting Discovery");
        Intent intent = new Intent(c, Discovery.class);
        c.startService(intent);
    }

    private Context getContext() {
        return this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mNsdManager = (NsdManager) this.getSystemService(Context.NSD_SERVICE);
        initializeResolveListener();
        initializeDiscoveryListener();
        handler.postDelayed(autostop, 1000 * 15);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return START_NOT_STICKY;
        }
        handler.removeCallbacks(autostop);
        handler.postDelayed(autostop, 1000 * 15);
        mNsdManager.discoverServices(
                SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
        return START_NOT_STICKY;
    }

    public void initializeDiscoveryListener() {
        mDiscoveryListener = new NsdManager.DiscoveryListener() {

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                mNsdManager.resolveService(service, mResolveListener);
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {

            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {

            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {

            }

            @Override
            public void onDiscoveryStarted(String s) {
                LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent(OnStart));
            }

            @Override
            public void onDiscoveryStopped(String s) {
                LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent(OnStop));
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
                        refreshListeners(app);
                    }
                } catch (IOException e) {
                    Timber.i("Failed reaching %s", host);
                }

            }
        };
    }

    private void refreshListeners(Mopidy app) {
        Intent intent = new Intent(OnRefresh);
        intent.putExtra("app", app);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        mNsdManager.stopServiceDiscovery(mDiscoveryListener);
        Timber.i("Stopping service  %s", mDiscoveryListener);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent(OnStop));
        super.onDestroy();
    }
}

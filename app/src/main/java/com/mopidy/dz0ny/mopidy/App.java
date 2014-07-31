package com.mopidy.dz0ny.mopidy;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.joshdholtz.sentry.Sentry;
import com.mopidy.dz0ny.mopidy.api.AutoUpdate;
import com.mopidy.dz0ny.mopidy.services.Discovery;

import timber.log.Timber;

import static timber.log.Timber.DebugTree;

public class App extends Application {

    private Tracker tracker;

    @Override
    public void onCreate() {
        super.onCreate();

        Sentry.init(this, getString(R.string.sentry_dsn));
        AutoUpdate.init(this, getString(R.string.update_url));

        if (BuildConfig.DEBUG) {
            Timber.plant(new DebugTree());
        }
        getTracker().enableAutoActivityTracking(true);
        getTracker().send(new HitBuilders.EventBuilder()
                .setCategory("app")
                .setAction("startup")
                .setLabel("new")
                .build());
    }
    synchronized Tracker getTracker() {
        if (tracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            tracker = analytics.newTracker(R.xml.tracker);
        }
        return tracker;
    }
    @Override
    public void onTerminate() {
        Discovery.Stop(this);
        super.onTerminate();
    }
}


package com.dz0ny.mopidy;

import android.app.Application;

import com.dz0ny.mopidy.api.AutoUpdate;
import com.dz0ny.mopidy.services.Discovery;
import com.joshdholtz.sentry.Sentry;

import timber.log.Timber;

import static timber.log.Timber.DebugTree;

public class App extends Application {


    @Override
    public void onCreate() {
        super.onCreate();

        try {
            AutoUpdate.init(this, getString(R.string.update_url));
        } catch (Exception e) {
            e.printStackTrace();
        }


        if (BuildConfig.DEBUG) {
            Timber.plant(new DebugTree());
        } else {
            Sentry.init(this, getString(R.string.sentry_dsn));
        }
    }

    @Override
    public void onTerminate() {
        Discovery.Stop(this);
        super.onTerminate();
    }
}


package com.mopidy.dz0ny.mopidy;

import android.app.Application;
import android.content.Intent;

import com.joshdholtz.sentry.Sentry;
import com.mopidy.dz0ny.mopidy.api.AutoUpdate;
import com.mopidy.dz0ny.mopidy.services.Discovery;

import timber.log.Timber;

import static timber.log.Timber.DebugTree;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Sentry.init(this, getString(R.string.sentry_dsn));
        AutoUpdate.init(this, getString(R.string.update_url));

        if (BuildConfig.DEBUG) {
            Timber.plant(new DebugTree());
        }
        Intent intent = new Intent(this, Discovery.class);
        startService(intent);
    }

    @Override
    public void onTerminate() {
        Intent intent = new Intent(this, Discovery.class);
        stopService(intent);
        super.onTerminate();
    }
}


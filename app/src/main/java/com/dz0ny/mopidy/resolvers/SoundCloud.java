package com.dz0ny.mopidy.resolvers;

import android.content.Context;

import com.dz0ny.mopidy.api.Mopidy;

import java.net.URI;

public class SoundCloud implements Resolver {

    private Context ctx;

    public SoundCloud(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    public boolean canResolve(URI content_url) {
        return content_url.getHost().contains("soundcloud");
    }

    @Override
    public boolean canPlay(Mopidy app) {
        return app.getSchemes(ctx).contains("soundcloud");
    }

    @Override
    public String resolvedURI(String content_url) {
        return "sc:" + content_url;
    }
}

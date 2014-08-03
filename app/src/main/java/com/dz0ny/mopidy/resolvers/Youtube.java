package com.dz0ny.mopidy.resolvers;

import android.app.Activity;

import com.dz0ny.mopidy.api.Mopidy;

import java.net.URI;

public class Youtube implements Resolver {

    private Activity ctx;

    public Youtube(Activity ctx) {
        this.ctx = ctx;
    }

    @Override
    public boolean canResolve(URI content_url) {
        return content_url.getHost().contains("youtube");
    }

    @Override
    public boolean canPlay(Mopidy app) {
        return app.getSchemes(ctx).contains("youtube");
    }

    @Override
    public String resolvedURI(String content_url) {
        return "yt:" + content_url;
    }


}

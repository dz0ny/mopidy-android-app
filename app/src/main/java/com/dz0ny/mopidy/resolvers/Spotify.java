package com.dz0ny.mopidy.resolvers;

import android.content.Context;

import com.dz0ny.mopidy.api.Mopidy;
import com.koushikdutta.ion.Ion;

import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.MULTILINE;

public class Spotify implements Resolver {

    private Context ctx;
    private Pattern curl = Pattern.compile(("property=\"og:audio\" content=\"([\\w:]+)\""), MULTILINE);

    public Spotify(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    public boolean canResolve(URI content_url) {
        return content_url.getHost().contains("spotify");
    }

    @Override
    public boolean canPlay(Mopidy app) {
        return app.getSchemes(ctx).contains("spotify");
    }

    @Override
    public String resolvedURI(String content_url) {
        try {
            String document = Ion.with(ctx).load(content_url).setTimeout(3000).asString().get();
            return curl.matcher(document).group(0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

}

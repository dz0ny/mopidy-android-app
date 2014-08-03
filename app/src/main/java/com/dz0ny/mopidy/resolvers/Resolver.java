package com.dz0ny.mopidy.resolvers;

import com.dz0ny.mopidy.api.Mopidy;

import java.net.URI;

public interface Resolver {
    public boolean canResolve(URI content_url);

    public boolean canPlay(Mopidy app);

    public String resolvedURI(String content_url);
}

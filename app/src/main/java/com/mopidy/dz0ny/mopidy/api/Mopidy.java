package com.mopidy.dz0ny.mopidy.api;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.JsonObject;
import com.koushikdutta.ion.Ion;

import java.util.concurrent.ExecutionException;


public class Mopidy implements Parcelable {

    private String name;
    private String host;
    private int port;
    private String url;

    public Mopidy(String name, String host, int port) {
        this.name = name;
        this.host = host;
        this.port = port;
        this.url = "http://" + host + ":" + port;
        ;
    }

    @Override
    public boolean equals(Object object) {
        boolean sameSame = false;

        if (object != null && object instanceof Mopidy) {
            sameSame = this.url == ((Mopidy) object).url;
        }

        return sameSame;
    }

    public String getURL() {
        return url;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.name);
        parcel.writeString(this.host);
        parcel.writeInt(this.port);
    }

    public String getVersion(Context ctx) {

        try {
            return this.callApi(ctx, "core.get_version").get("result").getAsString();
        } catch (InterruptedException e) {
            return "InterruptedException";
        } catch (NullPointerException e) {
            return "NullPointerException";
        } catch (ExecutionException e) {
            return "ExecutionException";
        }

    }

    private JsonObject callApi(Context ctx, String cmd) throws ExecutionException, InterruptedException {
        return Ion.with(ctx)
                .load(this.getRPCUrl())
                .setJsonObjectBody(new JSONRPC(cmd, null))
                .asJsonObject().get();
    }

    public String getRPCUrl() {
        return this.getURL() + "/mopidy/rpc";
    }

    public String getName() {
        return name;
    }

    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }
}

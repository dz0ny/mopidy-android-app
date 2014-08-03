package com.dz0ny.mopidy.api;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.koushikdutta.ion.Ion;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;

import timber.log.Timber;


public class Mopidy implements Parcelable {

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Mopidy createFromParcel(Parcel in) {
            return new Mopidy(in);
        }

        public Mopidy[] newArray(int size) {
            return new Mopidy[size];
        }
    };
    public String name;
    public String host;
    public int port;
    private String url;

    public Mopidy(String name, String host, int port) {
        this.name = name;
        this.host = host;
        this.port = port;
        this.url = "http://" + this.host + ":" + this.port;
    }

    public Mopidy(Parcel in) {
        this.name = in.readString();
        this.host = in.readString();
        this.port = in.readInt();
        this.url = "http://" + this.host + ":" + this.port;
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
            return this.callApi(ctx, "core.get_version", null).get("result").getAsString();
        } catch (InterruptedException e) {
            return "Unknown";
        } catch (NullPointerException e) {
            return "Unknown";
        } catch (ExecutionException e) {
            return "Unknown";
        }

    }

    public HashSet<String> getSchemes(Context ctx) {

        HashSet<String> schemes = new HashSet<String>();

        JsonArray list = null;
        try {
            list = this.callApi(ctx, "core.get_uri_schemes", null).get("result").getAsJsonArray();
        } catch (Exception e) {
            Timber.i(e.getMessage());
        }

        for (JsonElement scheme: list){
            schemes.add(scheme.getAsString());
        }
        return schemes;

    }

    public Boolean tracklistClear(Context ctx) {
        Timber.i("tracklistClear called");
        Boolean result = null;
        try {
            result = this.callApi(ctx, "core.tracklist.clear", null).get("result").getAsBoolean();
        } catch (Exception e) {
            Timber.i(e.getMessage());
        }
        return result;

    }

    public Boolean tracklistAdd(Context ctx, String url) {
        Timber.i("Adding track %s, %s", url, ctx);
        HashMap<String,String> params = new HashMap<> ();
        try {
            params.put("uri", url);
            Timber.i("Params %s", params);
            return this.callApi(ctx, "core.tracklist.add", params).get("result").getAsBoolean();
        } catch (Exception e) {
            Timber.i(e.getMessage());
        }
        return false;

    }

    public Boolean play(Context ctx) {

        try {
            return this.callApi(ctx, "core.playback.play", null).get("result").getAsBoolean();
        } catch (Exception e) {
            Timber.i(e.getMessage());
        }
        return false;

    }

    private JsonObject callApi(Context ctx, String cmd, HashMap<String,String> params) throws ExecutionException, InterruptedException {
        JSONRPC json = new JSONRPC(cmd, params);
        Timber.i(new Gson().toJson(json));
        return Ion.with(ctx)
                .load(this.getRPCUrl())
                .setTimeout(1500)
                //.proxy("127.0.0.1", 8888)
                .setJsonObjectBody(json)
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

    public String getJSON() {
        Gson gson = new GsonBuilder().create();
        return gson.toJson(this);
    }

}

package com.dz0ny.mopidy.api;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.koushikdutta.ion.Ion;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
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

    public ArrayList<String> getSchemes(Context ctx) {

        String schemes = null;
        try {
            schemes = this.callApi(ctx, "core.get_uri_schemes", null).get("result").getAsString();
        } catch (ExecutionException e) {
            Timber.i(e.getMessage());
        } catch (InterruptedException e) {
            Timber.i(e.getMessage());
        }
        return (ArrayList<String>) new Gson().fromJson(schemes, ArrayList.class);

    }

    public Boolean tracklistClear(Context ctx) {

        Boolean result = null;
        try {
            result = this.callApi(ctx, "core.tracklist.clear", null).get("result").getAsBoolean();
        } catch (ExecutionException e) {
            Timber.i(e.getMessage());
        } catch (InterruptedException e) {
            Timber.i(e.getMessage());
        }
        return result;

    }

    public Boolean tracklistAdd(Context ctx, String url) {
        JSONObject params = new JSONObject();
        try {
            params.put("uri", url);
            try {
                return this.callApi(ctx, "core.tracklist.add", params).get("result").getAsBoolean();
            } catch (ExecutionException e) {
                Timber.i(e.getMessage());
            } catch (InterruptedException e) {
                Timber.i(e.getMessage());
            }

        } catch (JSONException e) {
            Timber.i(e.getMessage());
        }
        return false;

    }

    private JsonObject callApi(Context ctx, String cmd, JSONObject params) throws ExecutionException, InterruptedException {
        return Ion.with(ctx)
                .load(this.getRPCUrl())
                .setTimeout(1500)
                .setJsonObjectBody(new JSONRPC(cmd, params))
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

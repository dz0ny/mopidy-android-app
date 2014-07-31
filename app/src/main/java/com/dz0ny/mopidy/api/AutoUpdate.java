package com.dz0ny.mopidy.api;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;


class Version implements Comparable<Version> {

    private String version;

    public Version(String version) {
        if (version == null)
            throw new IllegalArgumentException("Version can not be null");

        version = version.replace("v", "");
        if (!version.matches("[0-9]+(\\.[0-9]+)*"))
            throw new IllegalArgumentException("Invalid version format");
        this.version = version;
    }

    public final String get() {
        return this.version;
    }

    @Override
    public int compareTo(Version that) {
        if (that == null)
            return 1;
        String[] thisParts = this.get().split("\\.");
        String[] thatParts = that.get().split("\\.");
        int length = Math.max(thisParts.length, thatParts.length);
        for (int i = 0; i < length; i++) {
            int thisPart = i < thisParts.length ?
                    Integer.parseInt(thisParts[i]) : 0;
            int thatPart = i < thatParts.length ?
                    Integer.parseInt(thatParts[i]) : 0;
            if (thisPart < thatPart)
                return -1;
            if (thisPart > thatPart)
                return 1;
        }
        return 0;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that)
            return true;
        if (that == null)
            return false;
        if (this.getClass() != that.getClass())
            return false;
        return this.compareTo((Version) that) == 0;
    }

}

public class AutoUpdate {
    private String update_url;
    private Context context;

    private AutoUpdate() {

    }

    private static AutoUpdate getInstance() {
        return LazyHolder.instance;
    }

    public static void init(Context context, String update_url) {
        AutoUpdate.getInstance().context = context;
        AutoUpdate.getInstance().update_url = update_url;
    }

    public static void check(final Context context) {
        PackageInfo pinfo = null;
        try {
            pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            Timber.e(e.getMessage());
            return;
        }
        final String versionName = pinfo.versionName; // v1.0
        Ion.with(AutoUpdate.getInstance().context)
                .load(AutoUpdate.getInstance().update_url)
                .as(new TypeToken<List<Release>>() {
                })
                .setCallback(new FutureCallback<List<Release>>() {
                    @Override
                    public void onCompleted(Exception e, List<Release> releases) {
                        Release release = releases.get(0);
                        String download_url = null;
                        Timber.i("Release %s", release.getTagName());
                        //-1 (current<online)
                        //1 (current>online)
                        int shouldUpdate = new Version(versionName).compareTo(new Version(release.getTagName()));
                        Timber.i("shouldUpdate %d", shouldUpdate);
                        if (shouldUpdate >= 0) {
                            return;
                        }
                        for (Asset asset : release.getAssets()) {
                            Timber.i("Asset %s (%d) %s", asset.getName(), asset.getSize(), asset.getBrowserDownloadUrl());
                            if (asset.getContentType().equalsIgnoreCase("application/vnd.android.package-archive")) {

                                download_url = asset.getBrowserDownloadUrl();
                            }
                        }
                        if (download_url != null) {

                            show(context, release.getBody(), download_url, release.getTagName());
                        }
                    }
                });
    }

    private static void show(final Context context, String body, final String downloadUrl, String version) {
        try {

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
            String appName = null;
            try {
                appName = (String) context.getPackageManager().getApplicationLabel(context.getPackageManager().getApplicationInfo(context.getPackageName(), 0));
            } catch (PackageManager.NameNotFoundException ignored) {
            }
            alertDialogBuilder.setTitle(String.format("New Update for %s to %s", appName, version));
            alertDialogBuilder.setMessage(body)
                    .setCancelable(true)
                    .setPositiveButton("Download", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            download(context, downloadUrl);
                            dialog.cancel();
                        }
                    })
                    .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        } catch (NullPointerException activityClosed) {
        /*   Happens when the library tries to open a dialog,
             but the activity is already closed, so generates a NullPointerException or IllegalStateException.
			 In this way, a force close is avoided.*/
        } catch (IllegalStateException activityClosed) {
            // See up
        }
    }

    private static void download(Context mContext, String uri) {
        mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));

    }

    private static class LazyHolder {
        private static AutoUpdate instance = new AutoUpdate();
    }
}


class Release {


    @SerializedName("tag_name")
    @Expose
    private String tagName;
    @Expose
    private String name;
    @Expose
    private Boolean draft;
    @Expose
    private Boolean prerelease;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("published_at")
    @Expose
    private String publishedAt;
    @Expose
    private List<Asset> assets = new ArrayList<Asset>();
    @Expose
    private String body;

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public Boolean getPrerelease() {
        return prerelease;
    }

    public void setPrerelease(Boolean prerelease) {
        this.prerelease = prerelease;
    }

    public Boolean getDraft() {
        return draft;
    }

    public void setDraft(Boolean draft) {
        this.draft = draft;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }

    public List<Asset> getAssets() {
        return assets;
    }

    public void setAssets(List<Asset> assets) {
        this.assets = assets;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}


class Asset {

    @Expose
    private String name;
    @SerializedName("content_type")
    @Expose
    private String contentType;
    @Expose
    private Integer size;
    @SerializedName("download_count")
    @Expose
    private Integer downloadCount;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("browser_download_url")
    @Expose
    private String browserDownloadUrl;

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Integer getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(Integer downloadCount) {
        this.downloadCount = downloadCount;
    }

    public String getBrowserDownloadUrl() {
        return browserDownloadUrl;
    }

    public void setBrowserDownloadUrl(String browserDownloadUrl) {
        this.browserDownloadUrl = browserDownloadUrl;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}


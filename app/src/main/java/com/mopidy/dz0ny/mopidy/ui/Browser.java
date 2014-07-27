package com.mopidy.dz0ny.mopidy.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.mopidy.dz0ny.mopidy.R;
import com.mopidy.dz0ny.mopidy.api.Mopidy;
import com.mopidy.dz0ny.mopidy.shim.WebSocketFactory;

import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import timber.log.Timber;

public class Browser extends Activity {
    @InjectView(R.id.web_view)
    WebView wv;
    private Mopidy app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);
        ButterKnife.inject(this);

        Intent intent = getIntent();
        app = intent.getParcelableExtra("app");
        setTitle(getString(R.string.app_name) + " - " +app.getName());
        wv.getSettings().setJavaScriptEnabled(true);
        wv.setWebChromeClient(new WebChromeClient() {
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Timber.i("Console: %s", consoleMessage.message());
                return true;
            }
        });
        wv.setWebViewClient(
                new WebViewClient() {
                    @Override
                    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {

                        if (url.endsWith("/mopidy/mopidy.js") || url.endsWith("/mopidy/mopidy.min.js")){

                            try {
                                Timber.i("Loading hijacked: %s", url);
                                return new WebResourceResponse("application/javascript", "utf-8", getAssets().open("mopidy.js"));
                            } catch (IOException e) {
                                Timber.i("Failed loading hijacked: %s", url);
                                return super.shouldInterceptRequest(view, url);
                            }

                        }
                        return null;
                    }

                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        Timber.i("Loading: %s", url);
                        view.loadUrl(url);
                        return false;
                    }
                }
        );
        wv.loadUrl(app.getURL());
        wv.addJavascriptInterface(new WebSocketFactory(wv), "WebSocketFactory");
    }
}

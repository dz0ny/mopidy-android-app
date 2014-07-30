package com.mopidy.dz0ny.mopidy.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
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
    private boolean fullScreen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);
        ButterKnife.inject(this);

        Intent intent = getIntent();
        app = intent.getParcelableExtra("app");
        setTitle(getString(R.string.app_name) + " - " + app.getName());
        wv.getSettings().setJavaScriptEnabled(true);
        wv.setWebChromeClient(new WebChromeClient() {
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Timber.i("Console: %s", consoleMessage.message());
                return true;
            }
        });
        wv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (fullScreen)
                    ToggleFullScreen(false);
                return true;
            }
        });
        wv.setWebViewClient(
                new WebViewClient() {
                    @Override
                    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {

                        if (url.endsWith("/mopidy/mopidy.js") || url.endsWith("/mopidy/mopidy.min.js")) {

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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.browser, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_full_screen:
                ToggleFullScreen(false);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void ToggleFullScreen(Boolean force) {
        if (!fullScreen || force) {
            if (Build.VERSION.SDK_INT < 16) { //ye olde method
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);
            } else { // Jellybean and up, new hotness
                View decorView = getWindow().getDecorView();
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
                getActionBar().hide();
            }
        } else {
            if (Build.VERSION.SDK_INT < 16) { //ye olde method
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            } else { // Jellybean and up, new hotness
                View decorView = getWindow().getDecorView();
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                getActionBar().show();
            }
        }
        fullScreen = !fullScreen;
    }

    @Override
    protected void onResume() {
        if (fullScreen)
            ToggleFullScreen(true);
        super.onResume();
    }
}

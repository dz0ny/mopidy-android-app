package com.dz0ny.mopidy.shim;

/*
 * Copyright (c) 2010 Nathan Rajlich (https://github.com/TooTallNate)
 * Copyright (c) 2010 Animesh Kumar (https://github.com/anismiles)
 * Copyright (c) 2010 Strumsoft (https://strumsoft.com)
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */


import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import org.apache.http.util.ByteArrayBuffer;
import org.eclipse.jetty.websocket.WebSocketClient;
import org.eclipse.jetty.websocket.WebSocketClientFactory;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;

/**
 * @author bzero
 */
public class WebSocket implements
        org.eclipse.jetty.websocket.WebSocket.OnTextMessage,
        org.eclipse.jetty.websocket.WebSocket.OnBinaryMessage,
        org.eclipse.jetty.websocket.WebSocket.OnFrame {

    /**
     * An empty string
     */
    private static String BLANK_MESSAGE = "";

    /**
     * The javascript method name for onOpen event.
     */
    private static String EVENT_ON_OPEN = "onopen";

    /**
     * The javascript method name for onMessage event.
     */
    private static String EVENT_ON_MESSAGE = "onmessage";
    private final WebView appView;
    private URI uri = null;
    private String id = null;
    private WebSocketClient client;
    private WebSocketClientFactory factory;
    private Connection conn;
    private FrameConnection frame;
    private boolean binary;
    private ByteArrayBuffer buffer;

    public WebSocket(WebView appView, URI uri, String id) {
        this.id = id;
        this.uri = uri;
        this.appView = appView;

        try {
            factory = new WebSocketClientFactory();
            factory.start();

            client = factory.newWebSocketClient();
            client.setMaxTextMessageSize(512 * 1024);
            client.setMaxBinaryMessageSize(-1);

        } catch (Exception e) {
            Log.e("WebSocket", "WebSocket -> error: ", e);
        }
    }

    public void connect() {
        try {
            client.open(this.uri, this, 20000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            Log.e("WebSocket", "WebSocket -> connect error: ", e);
        }
    }

    public void stop() {
        Log.d("WebSocket", "WebSocket -> stop");
    }

    @JavascriptInterface
    public String getId() {
        return this.id;
    }

    @JavascriptInterface
    public void send(final String text) {
        try {
            conn.sendMessage(text);
        } catch (IOException e) {
            Log.e("WebSocket", "WebSocket -> send error", e);
        }
    }

    /**
     * Release resources.
     */
    private void release() {
        buffer.clear();
        buffer = null;
        if (frame.isOpen()) {
            frame.close();
        }
        frame = null;
    }

    /**
     * Builds text for javascript engine to invoke proper event method with
     * proper data.
     *
     * @param event websocket event (onOpen, onMessage etc.)
     * @param msg   Text message received from websocket server
     * @return
     */
    private String buildJavaScriptData(String event, String msg) {

        String json = "javascript:WebSocket." + event + "({\"_target\":\"" + this.id + "\",\"data\":%s})";
        return String.format(json, JSONObject.quote(msg));
    }

    @Override
    public boolean onFrame(byte flags, byte opcode, byte[] data, int offset, int length) {

        if (frame.isBinary(opcode) || (frame.isContinuation(opcode) && binary)) {
            binary = true;

            buffer.append(data, offset, length);

            if (frame.isMessageComplete(flags)) {
                binary = false;
                this.onMessage(buffer.buffer(), 0, buffer.length());
                buffer.clear();
            }

            return true;
        } else if (frame.isClose(opcode)) {
            release();
        }

        return false;
    }

    @Override
    public void onOpen(Connection conn) {
        this.conn = conn;

        appView.post(new Runnable() {
            @Override
            public void run() {
                appView.loadUrl(buildJavaScriptData(EVENT_ON_OPEN, BLANK_MESSAGE));
            }
        });
    }

    @Override
    public void onClose(int code, String reason) {
        Log.d("WebSocket", "WebSocket -> onClose");
    }

    @Override
    public void onMessage(byte[] data, int offset, int length) {
        Log.d("WebSocket", "WebSocket -> onMessage(byte[] data, int offset, int length)");
    }

    @Override
    public void onMessage(final String data) {
        appView.post(new Runnable() {
            @Override
            public void run() {
                appView.loadUrl(buildJavaScriptData(EVENT_ON_MESSAGE, data));
            }
        });

    }

    @Override
    public void onHandshake(FrameConnection connection) {
        frame = connection;
    }

}
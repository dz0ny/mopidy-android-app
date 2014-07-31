package com.dz0ny.mopidy.api;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by dz0ny on 26.7.2014.
 */


public class JSONRPC {
    /*
    curl -X POST -H Content-Type:application/json -d '{
            "method": "core.get_version",
            "jsonrpc": "2.0",
            "params": {},
            "id": 1
    }' http://localhost:6680/mopidy/rpc
    */
    String method;
    JSONObject params;
    String jsonrpc = "2.0";
    String id = "1";

    public JSONRPC(String cmd, Map<String, String> map) {
        method = cmd;
        id = MopidyRPC.getID();
        if (map != null) {
            params = new JSONObject();
            for (Map.Entry<String, String> entry : map.entrySet()) {
                try {
                    params.put(entry.getKey(), entry.getValue());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}

class MopidyRPC {
    public static int apiCallID = 0;

    public static String getID() {
        MopidyRPC.apiCallID++;
        return ((Number) MopidyRPC.apiCallID).toString();
    }
}
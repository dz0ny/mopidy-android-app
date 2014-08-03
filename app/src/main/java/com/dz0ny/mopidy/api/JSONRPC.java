package com.dz0ny.mopidy.api;

import org.json.JSONObject;


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

    public JSONRPC(String cmd, JSONObject params) {
        method = cmd;
        id = MopidyRPC.getID();
        params = params;
    }
}

class MopidyRPC {
    public static int apiCallID = 0;

    public static String getID() {
        MopidyRPC.apiCallID++;
        return ((Number) MopidyRPC.apiCallID).toString();
    }
}
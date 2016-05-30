package com.d360.sdk;

import android.net.ConnectivityManager;
import android.util.ArrayMap;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by Thomas on 30/05/2016.
 */
public class D360Event {

    private static final String TAG = "D360Event";
    private static int sEventNoCpt = 0;

    private Map<String, Object> mMapData;
    private Map<String, Object> mMapMeta;

    private String mName;
    private long mTimeStamp;
    private D360SDK.ConnectionType connectionType;
    private int eventNo;

    private boolean mActivateName = false;
    private boolean mActivateTimeStamp = false;
    private boolean mActivateConnectionType = false;

    /**
     * Creates an empty event.
     */
    public D360Event() {
        mTimeStamp = System.currentTimeMillis() / 1000L;
        mMapData = new ArrayMap<>();
        mMapMeta = new ArrayMap<>();
    }

    /**
     * Creates the event with "data" and "meta" values already set.
     *
     * @param mapData
     * @param mapMeta
     */
    public D360Event(Map<String, Object> mapData, Map<String, Object> mapMeta) {
        mMapData = mapData;
        mMapMeta = mapMeta;
    }

    public JSONObject getJSon() throws JSONException {
        if (mMapData.isEmpty())
            throw new JSONException("Data values cannot be empty.");
        Gson gson = new Gson();
        String s = "{\"data\":";
        s += gson.toJson(mMapData);
        s += ",\"meta\":";
        s += gson.toJson(mMapMeta);
        s += "}";
        return new JSONObject(s);
    }

    public void addMetaParameters(String key, Object value) {
        mMapMeta.put(key, value);
    }

    public void addDataParameters(String key, Object value) {
        mMapData.put(key, value);
    }

    /**
     * Get the current connection type when the message is sent, which means it varies depending
     * if the message was first sent offline.
     * @return
     */
    public D360SDK.ConnectionType getConnectionType() {
        return D360SDK.get().getConnectionType();
    }
}

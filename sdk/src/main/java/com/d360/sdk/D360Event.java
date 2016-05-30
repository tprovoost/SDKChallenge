package com.d360.sdk;

import android.support.v4.util.ArrayMap;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Thomas on 30/05/2016.
 */
public class D360Event {

    private static final String TAG = "D360Event";

    private static final String EVENT_KEY_NAME = "name";
    private static final String EVENT_KEY_EVENT_NO = "eventNo";
    private static final String EVENT_KEY_TIMESTAMP = "localTimeStamp";

    private static int sEventNoCpt = 0;

    private Map<String, Object> mMapData;
    private Map<String, Object> mMapMeta;

    private String mName;
    private long mTimeStamp;
    private D360RequestManager.ConnectionType connectionType;
    private int mEventNo;

    private boolean mActivateTimeStamp = false;
    private boolean mActivateConnectionType = false;

    /**
     * Creates an empty event with a name.
     */
    public D360Event(String name) {
        this(name, new ArrayMap<String, Object>(), new ArrayMap<String, Object>());
    }

    /**
     * Creates the event with "data" and "meta" values already set.
     *
     * @param name    name of the event.
     * @param mapData
     * @param mapMeta
     */
    public D360Event(String name, Map<String, Object> mapData, Map<String, Object> mapMeta) {
        mName = name;
        mEventNo = sEventNoCpt++;
        mTimeStamp = System.currentTimeMillis() / 1000L;
        mMapData = mapData;
        mMapMeta = mapMeta;
        addName();
    }

    public JSONObject getJSon() throws JSONException {
        if (!checkName())
            throw new JSONException("Name must be non null and " +
                    "respect the following pattern: '^ev_[A-Za-z0-9]+'");

        String s = getJsonString();
        return new JSONObject(s);
    }

    public String getJsonString() {
        Gson gson = new Gson();
        String s = "{\"data\":";
        s += gson.toJson(mMapData);
        s += ",\"meta\":";
        s += gson.toJson(mMapMeta);
        s += "}";
        return s;
    }

    public void fromJSon(JSONObject json) throws JSONException {
        JSONObject data = (JSONObject) json.get("data");
        JSONObject meta = (JSONObject) json.get("meta");

        Gson gson = new Gson();
        mMapData = gson.fromJson(data.toString(), Map.class);
        mMapMeta = gson.fromJson(meta.toString(), Map.class);
        for (String s : mMapMeta.keySet()) {
            switch(s) {
               case EVENT_KEY_EVENT_NO:
                    mEventNo = ((Double) mMapMeta.get(s)).intValue();
                    break;
                case EVENT_KEY_TIMESTAMP:
                    mTimeStamp = ((Double)mMapMeta.get(s)).longValue();
                    break;
                case EVENT_KEY_NAME:
                    mName = (String) mMapMeta.get(s);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Check if the name set is correct or not for the server.
     *
     * @return
     */
    public boolean checkName() {
        if (mName == null || mName.isEmpty())
            return false;
        Pattern pattern = Pattern.compile("^ev_[A-Za-z0-9]+");
        Matcher matcher = pattern.matcher(mName);
        if (matcher.matches())
            return true;
        return false;
    }

    public void addMetaParameter(String key, Object value) {
        mMapMeta.put(key, value);
    }

    public void addDataParameter(String key, Object value) {
        mMapData.put(key, value);
    }

    private void addName() {
        addMetaParameter(EVENT_KEY_NAME, mName);
    }

    public void addTimeStamp() {
        addMetaParameter(EVENT_KEY_TIMESTAMP, 1458578476);
    }

    public void addConnectionType() {
        addMetaParameter("connectionInfo", getConnectionType().toString().toLowerCase());
    }

    public void addEventNo() {
        addMetaParameter(EVENT_KEY_EVENT_NO, mEventNo);
    }

    public String getName() {
        return mName;
    }

    public void setConnectionType(D360RequestManager.ConnectionType connectionType) {
        this.connectionType = connectionType;
    }

    public long getTimeStamp() {
        return mTimeStamp;
    }

    public int getEventNo() {
        return mEventNo;
    }

    /**
     * Get the current connection type when the message is sent, which means it varies depending
     * if the message was first sent offline.
     *
     * @return
     */
    public D360RequestManager.ConnectionType getConnectionType() {
        return D360SDK.get().getRequestManager().getConnectionType();
    }

    @Override
    public String toString() {
        return getJsonString();
    }
}

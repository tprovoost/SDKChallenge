package com.d360.sdk;

import android.support.v4.util.ArrayMap;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
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
    private static final String EVENT_KEY_CONNECTION_INFO = "connectionInfo";

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
    public D360Event() {
        this("ev_NoName: " + sEventNoCpt, new ArrayMap<String, Object>(), new ArrayMap<String, Object>());
    }

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

    /**
     * Returns the JSON equivalent of this object.
     *
     * @return
     * @throws JSONException
     */
    public JSONObject getJSon() throws JSONException {
        if (!checkEventName(mName))
            throw new JSONException("Name must be non null and " +
                    "respect the following pattern: '^ev_[A-Za-z0-9]+'");

        String s = getJsonString();
        return new JSONObject(s);
    }

    /**
     * Returns the String version of this object. May variate from an object to another because
     * of the internal maps used.
     *
     * @return
     */
    public String getJsonString() {
        Gson gson = new Gson();
        String s = "{\"data\":";
        s += gson.toJson(mMapData);
        s += ",\"meta\":";
        s += gson.toJson(mMapMeta);
        s += "}";
        return s;
    }

    /**
     * Populates this object with the json parameter content.
     *
     * @param json
     * @throws JSONException
     */
    public void fromJSon(JSONObject json) throws JSONException {
        JSONObject data = (JSONObject) json.get("data");
        JSONObject meta = (JSONObject) json.get("meta");

        Gson gson = new Gson();
        mMapData = gson.fromJson(data.toString(), Map.class);
        mMapMeta = gson.fromJson(meta.toString(), Map.class);
        for (String s : mMapMeta.keySet()) {
            switch (s) {
                case EVENT_KEY_EVENT_NO:
                    mEventNo = ((Double) mMapMeta.get(s)).intValue();
                    mMapMeta.put(s, mEventNo);
                    break;
                case EVENT_KEY_TIMESTAMP:
                    mTimeStamp = ((Double) mMapMeta.get(s)).longValue();
                    mMapMeta.put(s, mTimeStamp);
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
    public static boolean checkEventName(String mName) {
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
        addMetaParameter(EVENT_KEY_CONNECTION_INFO, getConnectionType().toString().toLowerCase());
    }

    public void addEventNo() {
        addMetaParameter(EVENT_KEY_EVENT_NO, mEventNo);
    }

    /**
     * Generates an event based on a JSON String. <br/>
     * Example: <code>String jsonString = new String("{'data':'coucou'}");</code>
     *
     * @param jsonString
     * @return
     * @throws JSONException
     */
    public static D360Event generateEvent(String jsonString) throws JSONException {
        JSONObject eventTest = new JSONObject(jsonString);
        D360Event event = new D360Event();
        event.fromJSon(eventTest);
        return event;
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
        D360SDK sdk = D360SDK.getInstance();
        return D360RequestManager.getConnectionType(sdk.getContext());
    }

    @Override
    public String toString() {
        return getJsonString();
    }

    @Override
    public boolean equals(Object o) {
        if (super.equals(o))
            return true;
        if (!(o instanceof D360Event))
            return false;
        return sameMap(mMapData, ((D360Event) o).mMapData)
                && sameMap(mMapMeta, ((D360Event) o).mMapMeta);
    }

    /**
     * Returns true if two maps are equivalent.
     *
     * @param map1
     * @param map2
     * @return
     */
    public static boolean sameMap(Map<String, Object> map1, Map<String, Object> map2) {
        if (map1 == null || map2 == null) {
            if (map1 == null && map2 == null)
                return true;
            return false;
        }
        if (map1.size() != map2.size())
            return false;
        for (String s : map1.keySet()) {
            if (!map2.containsKey(s))
                return false;
            Object data = map1.get(s);
            if (!map1.get(s).equals(map2.get(s)))
                return false;
        }
        return true;
    }

    public static boolean compareQueues(Queue<D360Event> queue1, Queue<D360Event> queue2) {
        if (queue1 == null || queue2 == null) {
            if (queue1 == null && queue2 == null)
                return true;
            return false;
        }
        if (queue1.size() != queue2.size())
            return false;
        if (queue1.size() == 0)
            return true;
        Queue<D360Event> queue1Copy = new ArrayDeque<>(queue1);
        Queue<D360Event> queue2Copy = new ArrayDeque<>(queue2);
        D360Event event1 = null;
        D360Event event2 = null;
        do {
            event1 = queue1Copy.poll();
            event2 = queue2Copy.poll();
            if (event1 != null && !event1.equals(event2))
                return false;
        } while (event1 != null);
        return true;
    }
}

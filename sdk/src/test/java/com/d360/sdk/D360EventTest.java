package com.d360.sdk;

import android.support.v4.util.ArrayMap;

import junit.framework.TestCase;

import org.json.JSONObject;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Created by Thomas on 30/05/2016.
 */
public class D360EventTest extends TestCase {

    public static final String DATA_TEST = "{" +
            "\"data\":{\"foo\":\"bar\"}," +
            "\"meta\":{" +
            "\"localTimeStamp\":1458578476," +
            "\"eventNo\":150," +
            "\"name\":\"ev_MyCustomEvent\"," +
            "\"connectionInfo\":\"wifi\"}}";

    protected JSONObject eventTest;
    protected Map<String, Object> mapData;
    protected Map<String, Object> mapMeta;

    @Override
    protected void setUp() throws Exception {
        eventTest = new JSONObject(DATA_TEST);

        mapData = new ArrayMap<>();
        mapData.put("foo", "bar");

        mapMeta = new ArrayMap<>();
        mapMeta.put("eventNo", 150);
        mapMeta.put("localTimeStamp", 1458578476);
        mapMeta.put("name", "ev_MyCustomEvent");
        mapMeta.put("connectionInfo", "wifi");
    }

    @Test
    public void testEventToString() throws Exception {
        D360Event event = new D360Event("ev_MyCustomEvent", mapData, mapMeta);
        assertEquals(DATA_TEST, event.toString());
    }

    @Test
    public void testImportEventFromJson() throws Exception {
        D360Event event = new D360Event("");
        event.fromJSon(eventTest);
        assertEquals("ev_MyCustomEvent", event.getName());
        assertEquals(1458578476L, event.getTimeStamp());
        assertEquals(150, event.getEventNo());
    }
}


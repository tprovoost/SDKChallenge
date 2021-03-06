package com.d360.sdk;

import android.support.v4.util.ArrayMap;
import android.test.suitebuilder.annotation.SmallTest;

import junit.framework.TestCase;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import static org.junit.Assert.assertEquals;

/**
 * Created by Thomas on 30/05/2016.
 */
@SmallTest
public class D360EventTest extends TestCase {

    public static final String DATA_TEST = "{" +
            "\"data\":{\"foo\":\"bar\"}," +
            "\"meta\":{" +
            "\"localTimeStamp\":1458578476," +
            "\"eventNo\":150," +
            "\"name\":\"ev_MyCustomEvent\"," +
            "\"connectionInfo\":\"wifi\"}}";

    public static final String DATA_TEST2 = "{" +
            "\"data\":{\"foo\":\"bar\"}," +
            "\"meta\":{" +
            "\"localTimeStamp\":1458578476," +
            "\"eventNo\":150.2," +
            "\"name\":\"ev_MyCustomEvent\"," +
            "\"connectionInfo\":\"wifi\"}}";

    private JSONObject eventTest;
    private JSONObject eventTest2;
    private Map<String, Object> mapData;
    private Map<String, Object> mapMeta;
    private Queue<D360Event> mEvents;

    @Override
    protected void setUp() throws Exception {
        eventTest = new JSONObject(DATA_TEST);
        eventTest2 = new JSONObject(DATA_TEST2);

        mapData = new ArrayMap<>();
        mapData.put("foo", "bar");

        mapMeta = new ArrayMap<>();
        mapMeta.put("eventNo", (int)(150));
        mapMeta.put("localTimeStamp", 1458578476L);
        mapMeta.put("name", "ev_MyCustomEvent");
        mapMeta.put("connectionInfo", "wifi");

        mEvents = new ArrayDeque<>();
        mEvents.add(D360Event.generateEvent(DATA_TEST));
        mEvents.add(D360Event.generateEvent(DATA_TEST2));
    }

    @Test
    public void testEventToString() throws Exception {
        D360Event event = new D360Event("ev_MyCustomEvent", mapData, mapMeta);
        assertEquals(DATA_TEST, event.toString());
    }

    @Test
    public void testImportEventFromJson() throws Exception {
        D360Event eventTarget = new D360Event("ev_MyCustomEvent", mapData, mapMeta);
        D360Event event = new D360Event();
        event.fromJSon(eventTest);
        assertEquals(eventTarget, event);
    }

    @Test
    public void testGetJSon() throws Exception {
        D360Event event = D360Event.generateEvent(DATA_TEST);
        event.getJSon();
        event = new D360Event("bad name");
        try {
            event.getJSon();
            throw new Exception("Name was wrong but no error.");
        } catch (Exception e) {
            // normal case
        }
    }

    @Test
    public void testSameMaps() throws Exception {
        Map<String, Object> map1 = new HashMap<>();
        Map<String, Object> map2 = new HashMap<>();
        Map<String, Object> map3 = new HashMap<>();

        map1.put("TestA", 12);
        map1.put("TestB", "Test");
        map1.put("TestC", 1235L);

        map2.put("TestA", 12);
        map2.put("TestB", "Test");
        map2.put("TestC", 1235L);

        map3.put("TestA", 12);
        map3.put("TestB", 12);
        map3.put("TestC", 12);
        map3.put("TestD", 12);

        assertEquals(true, D360Event.sameMap(map1, map2));

        map2.put("TestA", "Ok");
        assertEquals(false, D360Event.sameMap(map1, map2));
        assertEquals(false, D360Event.sameMap(map1, null));
        assertEquals(false, D360Event.sameMap(null, map2));
        assertEquals(false, D360Event.sameMap(map3, map2));
        assertEquals(true, D360Event.sameMap(null, null));
    }

    @Test
    public void testCheckName() {
        assertEquals(true, D360Event.checkEventName("ev_0456"));
        assertEquals(true, D360Event.checkEventName("ev_MyCustomName78"));
        assertEquals(false, D360Event.checkEventName("ev_MyCustom_Name78"));
        assertEquals(false, D360Event.checkEventName("HelloEvent"));
        assertEquals(false, D360Event.checkEventName("ev_MyCustomName78+"));
        assertEquals(false, D360Event.checkEventName("ev_-za-')]{]"));
        assertEquals(false, D360Event.checkEventName("ev_\"test"));
        assertEquals(false, D360Event.checkEventName(null));
    }

    @Test
    public void testCompareQueues() throws Exception {
        Queue<D360Event> events2 = new ArrayDeque<>();
        events2.add(D360Event.generateEvent(DATA_TEST));
        assertFalse(D360Event.compareQueues(mEvents, events2));

        events2.add(D360Event.generateEvent(DATA_TEST2));
        assertTrue(D360Event.compareQueues(mEvents, events2));
    }

    @Test
    public void testGenerateEvent() throws Exception {
        D360Event.generateEvent(DATA_TEST);
        D360Event.generateEvent(DATA_TEST2);

        // test corruption of data
        try {
            D360Event.generateEvent("aico,v");
            throw new Exception("The parsing should have had a problem");
        } catch (JSONException e) {
            // do nothing, normal behavior
        }

        try {
            D360Event.generateEvent("{ \"a\": \"b\"}");
            throw new Exception("The parsing should have had a problem");
        } catch (JSONException e) {
            // do nothing, normal behavior
        }
    }
}


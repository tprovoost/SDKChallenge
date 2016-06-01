package com.d360.sdk;

import android.support.test.runner.AndroidJUnit4;
import android.test.InstrumentationTestCase;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Created by Thomas on 31/05/2016.
 */

@RunWith(AndroidJUnit4.class)
public class D360PersistenceInstrumentationTest extends InstrumentationTestCase {

    public static final String DATA_1 = "{" +
            "\"data\":{\"foo\":\"bar\"}," +
            "\"meta\":{" +
            "\"localTimeStamp\":1458578476," +
            "\"eventNo\":150," +
            "\"name\":\"ev_MyCustomEvent\"," +
            "\"connectionInfo\":\"wifi\"}}";

    public static final String DATA_2 = "{" +
            "\"data\":{\"foo\":\"bar\"}," +
            "\"meta\":{" +
            "\"localTimeStamp\":1458578476," +
            "\"eventNo\":150," +
            "\"name\":\"ev_MyCustomEvent\"," +
            "\"connectionInfo\":\"wifi\"}}";

    private static final String CACHE_FILE_PATH = "/data/data/com.d360.sdk.test/cache/.D360queues";

    private Queue<D360Event> mEvents;
    private Queue<D360Event> mEventsBig;
    private Queue<D360Event> mEventsHuge;

    @Override
    protected void setUp() throws Exception {
        mEvents = new ArrayDeque<>();
        mEventsBig = new ArrayDeque<>();
        mEventsHuge = new ArrayDeque<>();

        D360Event event1 = D360Event.generateEvent(DATA_1);
        D360Event event2 = D360Event.generateEvent(DATA_2);

        mEvents.add(event1);
        mEvents.add(event2);

        for (int i = 0; i < 500; ++i) {
            mEventsBig.add(event1);
        }
        for (int i = 0; i < 5000; ++i) {
            mEventsHuge.add(event1);
        }
    }

    @Test
    public void testAccessToCacheFile() throws Exception {
        File f = D360Persistence.getCacheFile(getInstrumentation().getContext());
        assertEquals(CACHE_FILE_PATH, f.getAbsolutePath());
    }

    @Test
    public void testWritingEventToDisk() throws Exception {
        D360Persistence.storeEvent(getInstrumentation().getTargetContext(), mEvents.peek());
        Queue<D360Event> events = D360Persistence.getQueue(getInstrumentation().getContext());
        assertEquals(mEvents.peek(), events.peek());
    }

    @Test
    public void testWritingQueueToDisk() throws Exception {
        D360Persistence.storeQueue(getInstrumentation().getTargetContext(), mEventsBig);
        Queue<D360Event> events = D360Persistence.getQueue(getInstrumentation().getContext());
        assertEquals(true, D360Event.compareQueues(events, mEventsBig));
    }

    @Test
    public void testWritingBigQueueToDisk() throws Exception {
        // last time = 1m 40s 61ms
        D360Persistence.storeQueue(getInstrumentation().getTargetContext(), mEventsHuge);
        Queue<D360Event> events = D360Persistence.getQueue(getInstrumentation().getContext());
        assertEquals(true, D360Event.compareQueues(events, mEventsHuge));
    }

}

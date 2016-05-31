package com.d360.sdk;

import android.support.test.runner.AndroidJUnit4;
import android.test.InstrumentationTestCase;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.ArrayDeque;
import java.util.LinkedList;
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

    @Override
    protected void setUp() throws Exception {
        mEvents = new ArrayDeque<>();

        D360Event event1 = D360Event.generateEvent(DATA_1);
        D360Event event2 = D360Event.generateEvent(DATA_2);

        mEvents.add(event1);
        mEvents.add(event2);
    }

    @Test
    public void testAccessToCacheFile() throws Exception {
        File f = D360Persistence.getCacheFile(getInstrumentation().getTargetContext());
        assertEquals(CACHE_FILE_PATH, f.getAbsolutePath());
    }

    @Test
    public void testWritingToDisk() throws Exception {
        D360Persistence.storeQueue(getInstrumentation().getTargetContext(), mEvents);
        Queue<D360Event> events = D360Persistence.getQueue(getInstrumentation().getTargetContext());
        assertEquals(true, testSameQueues(events, mEvents));
    }

    public static boolean testSameQueues(Queue<D360Event> queue1, Queue<D360Event> queue2) {
        if (queue1 == null || queue2 == null) {
            if (queue1 == null && queue2 == null)
                return true;
            return false;
        }
        if (queue1.size() != queue2.size())
            return false;
        if (queue1.size() == 0)
            return true;
        Queue<D360Event> queue1Copy = new LinkedList<>(queue1);
        Queue<D360Event> queue2Copy = new LinkedList<>(queue2);
        D360Event event1 = null;
        D360Event event2 = null;
        do {
            event1 = queue1Copy.poll();
            event2 = queue2Copy.poll();
            if (event1 != null && !event1.equals(event2))
                return false;
        } while(event1 != null);
        return true;
    }

}

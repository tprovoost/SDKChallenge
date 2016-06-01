package com.d360.sdk;

import android.content.Context;
import android.test.AndroidTestCase;
import android.test.InstrumentationTestCase;
import android.test.mock.MockContext;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.ArrayMap;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

/**
 * Created by Thomas on 31/05/2016.
 */
@SmallTest
public class D360PersistenceTest extends TestCase {

    public static final String DATA_1 = "{" +
            "\"data\":{\"foo\":\"bar\"}," +
            "\"meta\":{" +
            "\"localTimeStamp\":1458578476," +
            "\"eventNo\":150," +
            "\"name\":\"ev_MyCustomEvent\"," +
            "\"connectionInfo\":\"wifi\"}}";

    public static final String DATA_2 = "{" +
            "\"data\":{\"foo2\":\"bar2\"}," +
            "\"meta\":{" +
            "\"localTimeStamp\":2586578476," +
            "\"eventNo\":151," +
            "\"name\":\"ev_MyCustomEvent2\"," +
            "\"connectionInfo\":\"mobile\"}}";

    private Queue<D360Event> mEvents;
    private Queue<D360Event> mEventsBig;
    private Queue<D360Event> mEventsHuge;

    private D360Event event1;
    private D360Event event2;

    @Override
    protected void setUp() throws Exception {
        mEvents = new ArrayDeque<>();
        mEventsBig = new ArrayDeque<>();
        mEventsHuge = new ArrayDeque<>();

        event1 = D360Event.generateEvent(DATA_1);
        event2 = D360Event.generateEvent(DATA_2);

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
    public void testParseString() throws Exception {
        Queue<D360Event> queueResult = new ArrayDeque<>();
        D360Persistence.getQueueFromString(DATA_1 + "<|>" + DATA_2, queueResult);
        assertEquals(2 , queueResult.size());
        D360Event e1 = queueResult.poll();
        D360Event e2 = queueResult.poll();
        assertEquals(event1, e1);
        assertEquals(event2, e2);
    }

    @Test
    public void testWriteOneEvent() throws Exception {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        D360Persistence.storeEvent(baos, event1, true);
        baos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        D360Event event = D360Persistence.getQueue(bais).poll();
        bais.close();
        assertEquals(event1, event);
    }

    @Test
    public void testWriteTwoEvents() throws Exception {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        D360Persistence.storeEvent(baos, event1, true);
        D360Persistence.storeEvent(baos, event2);
        baos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        Queue<D360Event> queue = D360Persistence.getQueue(bais);
        D360Event e1 = queue.poll();
        D360Event e2 = queue.poll();
        bais.close();
        assertEquals(event1, e1);
        assertEquals(event2, e2);
    }

    @Test
    public void testQueueOneElement() throws Exception {
        Queue<D360Event> simpleQueue = new ArrayDeque<>();
        simpleQueue.add(event1);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        D360Persistence.storeQueue(baos, simpleQueue);
        baos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        Queue<D360Event> eventQueue = D360Persistence.getQueue(bais);
        bais.close();
        assertEquals(simpleQueue.size(), eventQueue.size());
        D360Event e1 = mEvents.poll();
        assertEquals(event1, e1);
    }

    @SmallTest
    public void testQueueTwoElements() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        D360Persistence.storeQueue(baos, mEvents);
        baos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        Queue<D360Event> eventQueue = D360Persistence.getQueue(bais);
        bais.close();
        assertEquals(mEvents.size(), eventQueue.size());
        D360Event e1 = mEvents.poll();
        D360Event e2 = mEvents.poll();

        assertEquals(event1, e1);
        assertEquals(event2, e2);
    }

    @SmallTest
    public void testQueueBigQueue() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int written = D360Persistence.storeQueue(baos, mEventsBig);
        baos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        Queue<D360Event> eventQueue = D360Persistence.getQueue(bais);
        bais.close();
        assertEquals(mEventsBig.size(), eventQueue.size());
    }

    @SmallTest
    public void testQueueHugeQueue() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int written = D360Persistence.storeQueue(baos, mEventsHuge);
        baos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        Queue<D360Event> eventQueue = D360Persistence.getQueue(bais);
        bais.close();
        assertEquals(mEventsHuge.size(), eventQueue.size());
    }
}

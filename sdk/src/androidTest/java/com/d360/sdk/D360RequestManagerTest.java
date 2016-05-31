package com.d360.sdk;

import android.app.Instrumentation;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;
import android.test.AndroidTestCase;
import android.test.InstrumentationTestCase;
import android.test.IsolatedContext;
import android.test.suitebuilder.annotation.MediumTest;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

/**
 * Created by Thomas on 30/05/2016.
 */
@RunWith(AndroidJUnit4.class)
@MediumTest
public class D360RequestManagerTest extends InstrumentationTestCase {

    public static final String DATA_TEST = "{" +
            "\"data\":{\"foo\":\"bar\"}," +
            "\"meta\":{" +
            "\"localTimeStamp\":1458578476," +
            "\"eventNo\":150," +
            "\"name\":\"ev_MyCustomEvent\"," +
            "\"connectionInfo\":\"wifi\"}}";

    private static final String FAKE_KEY = "FakeKey";

    private D360RequestManager mManager;

    @Override
    protected void setUp() throws Exception {
        mManager = new D360RequestManager(getInstrumentation().getTargetContext(), FAKE_KEY);
    }

    @Test
    public void testConnectionOn() {
        Context context = getInstrumentation().getTargetContext();
        D360RequestManager.checkConnectivity(context);
    }

    @Test
    public void testManager() throws Exception {
        assertEquals(4, 2 + 2 );
    }
}

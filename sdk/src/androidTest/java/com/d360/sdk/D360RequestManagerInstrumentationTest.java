package com.d360.sdk;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.test.runner.AndroidJUnit4;
import android.test.AndroidTestCase;
import android.test.InstrumentationTestCase;
import android.test.mock.MockContext;
import android.test.suitebuilder.annotation.MediumTest;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.*;

/**
 * Created by Thomas on 30/05/2016.
 */
@RunWith(AndroidJUnit4.class)
@MediumTest
public class D360RequestManagerInstrumentationTest extends AndroidTestCase {

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
        mManager = new D360RequestManager(getContext(), FAKE_KEY);
    }

    @Test
    public void testGetConnexionType() {
        Context context = new MockContext();
        ConnectivityManager cm = mock(ConnectivityManager.class);
        NetworkInfo activeNetwork = mock(NetworkInfo.class);

        when(context.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(cm);
        when(cm.getActiveNetworkInfo()).thenReturn(activeNetwork);
        when(activeNetwork.isConnectedOrConnecting()).thenReturn(true);

        assertEquals(true, D360RequestManager.checkConnectivity(context));
    }

}

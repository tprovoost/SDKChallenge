package com.d360.sdk;

import android.content.Context;
import android.util.ArrayMap;

import com.android.volley.RequestQueue;
import com.android.volley.Response;

import junit.framework.TestCase;

import org.json.JSONObject;
import org.junit.Test;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;

import static org.mockito.Mockito.*;

/**
 * Created by Thomas on 01/06/2016.
 */
public class D360RequestManagerTest extends TestCase {

    private static final String URL = "http://www.test.com";
    private Context mContext;
    private RequestQueue mRequestQueue;
    private D360Event mEvent;
    private D360Request mRequest;

    private Queue<D360Request> mRequests;

    @Override
    protected void setUp() throws Exception {
        mRequests = new ArrayDeque<>();
        mContext = mock(Context.class);

        // setup request
        mEvent = new D360Event("ev_Test1");
        Response.Listener listener = mock(Response.Listener.class);
        Response.ErrorListener errorListener = mock(Response.ErrorListener.class);
        JSONObject parameters = mEvent.getJSon();
        Map<String, String> headers = new ArrayMap<>();

        mRequest = mock(D360Request.class);
        mRequestQueue = mock(RequestQueue.class);

        when(mRequestQueue.add(mRequest)).thenReturn(mRequest);
    }

    @Test
    public void testSendEvent() throws Exception {
    }

    @Test
    public void testRegisterEvent() throws Exception {

    }

    @Test
    public void testOnErrorResponse() throws Exception {

    }

    @Test
    public void testCheckConnectivity() throws Exception {

    }

    @Test
    public void testGetConnectionType() throws Exception {

    }

    @Test
    public void testOnReceive() throws Exception {

    }
}
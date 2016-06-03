package com.d360.sdk;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.util.ArrayMap;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.ExecutorDelivery;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.ResponseDelivery;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.RequestFuture;

import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowLog;
import org.robolectric.shadows.ShadowSettings;
import org.robolectric.shadows.httpclient.FakeHttp;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

/**
 * Created by Thomas on 02/06/2016.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class D360RequestManagerTest {

    private static final String TEST_KEY = "myCustomKey";
    private static final String TEST_KEY_2 = "myCustomKey2";
    private static final String TAG = "D360RequestManagerTest";

    private static final int STATUS_OK = 201;
    private static final int STATUS_ERROR = 400;

    private static final String MESSAGE_CREATED = "{\"developer\":\"CREATED\"}";
    private static final String MESSAGE_ERROR = "{\"error\":\"error in JSON\"}";

    private D360RequestManager mManager;
    private Context mContext = RuntimeEnvironment.application;
    private D360Event mEvent;
    private Queue<D360Event> mEventQueue;
    private D360Request mRequest;

    public static final String DATA_TEST = "{" +
            "\"data\":{\"foo\":\"bar\"}," +
            "\"meta\":{" +
            "\"localTimeStamp\":1458578476," +
            "\"eventNo\":150," +
            "\"name\":\"ev_MyCustomEvent\"," +
            "\"connectionInfo\":\"wifi\"}}";

    @Before
    public void setup() throws Exception {
        ShadowLog.stream = System.out;
        mManager = new D360RequestManager(mContext, TEST_KEY);
        mEvent = D360Event.generateEvent(DATA_TEST);
        mRequest = new D360Request("test.com", null, mEvent.getJSon(), null, null);
        mEventQueue = new ArrayDeque<>();
    }

    @Test
    public void testConnectivity() throws Exception {
        setConnectivity(false);
        assertEquals(false, mManager.checkConnectivity(mContext));
        setConnectivity(true);
        assertEquals(true, mManager.checkConnectivity(mContext));
    }

    @Test
    public void testResponseOk() throws Exception {
        FakeHttp.getFakeHttpLayer().interceptHttpRequests(false);
        RequestQueue queue = newRequestQueueForTest(
                mContext, STATUS_OK, MESSAGE_CREATED);
        RequestFuture<JSONObject> future = RequestFuture.newFuture();

        JSONObject parameters = new JSONObject();
        Map<String, String> header = new ArrayMap<>();
        JsonRequest request = new D360Request(
                "test.com", null, parameters, future, future);

        mManager.registerEvent(queue, mEvent);

        CountDownLatch signal = new CountDownLatch(1);
        signal.await(1, TimeUnit.SECONDS);
        List<ShadowLog.LogItem> logs = ShadowLog.getLogsForTag("D360RequestManager");

        assertEquals(3, logs.size());
        assertEquals("No events in file.", logs.get(0).msg);
        assertEquals(true, logs.get(2).msg.startsWith("Request Successfully sent:"));
    }

    @Test
    public void testResponseNotOk() throws Exception {
        FakeHttp.getFakeHttpLayer().interceptHttpRequests(false);
        RequestQueue queue = newRequestQueueForTest(
                mContext, STATUS_ERROR, MESSAGE_ERROR);
        RequestFuture<JSONObject> future = RequestFuture.newFuture();

        JSONObject parameters = new JSONObject();
        Map<String, String> header = new ArrayMap<>();
        JsonRequest request = new D360Request(
                "test.com", null, parameters, future, future);

        mManager.registerEvent(queue, mEvent);

        CountDownLatch signal = new CountDownLatch(1);
        signal.await(2, TimeUnit.SECONDS);
        List<ShadowLog.LogItem> logs = ShadowLog.getLogsForTag("D360RequestManager");

        assertEquals(3, logs.size());
        assertEquals("No events in file to reload.", logs.get(0).msg);
        assertEquals(true, logs.get(2).msg.startsWith("Error:"));
    }

    @Test
    public void testRegisterEvent() throws Exception {
        FakeHttp.getFakeHttpLayer().interceptHttpRequests(false);

        RequestQueue queue = newRequestQueueForTest(mContext, STATUS_OK, MESSAGE_CREATED);

        mManager.registerEvent(queue, mEvent);

        CountDownLatch signal = new CountDownLatch(1);
        signal.await(1, TimeUnit.SECONDS);
    }

    @Test
    public void testRegisterEventNull() throws Exception {
        mManager.registerEvent(null);
    }

    @Test
    public void testRegisterBroadcaster() throws Exception {
        int n = 5;
        populateQueue(mEventQueue, n);
        RequestQueue queue = newRequestQueueForTest(mContext, STATUS_OK, MESSAGE_CREATED);

        setConnectivity(false);
        for (int i = 0; i < n; ++i) {
            mManager.registerEvent(mEventQueue.poll());
        }
        setConnectivity(true);
        mManager.resumeDownloadFromFiles(queue);
    }

    /**
     * Generates a RequestQueue for testing purposes: it is based on<br/>
     * <ul>
     * <li>A tweaked network that answers only with the parameters.</li>
     * <li>A single thread executor.</li>
     * </ul>
     *
     * @param context         Context to run the test with.
     * @param responseCode    Type of code needed.
     * @param responseMessage Message response.
     * @return The generated queue.
     */
    private static RequestQueue newRequestQueueForTest(
            final Context context,
            final int responseCode,
            final String responseMessage) {

        final File cacheDir = new File(context.getCacheDir(), "volley");

        final Network network = new BasicNetwork(new HurlStack() {
            @Override
            public HttpResponse performRequest(
                    Request<?> request, Map<String,
                    String> additionalHeaders) throws IOException, AuthFailureError {
                return buildResponse(responseCode, responseMessage);
            }
        });

        final ResponseDelivery responseDelivery =
                new ExecutorDelivery(Executors.newSingleThreadExecutor());

        final RequestQueue queue =
                new RequestQueue(
                        new DiskBasedCache(cacheDir),
                        network,
                        1,
                        responseDelivery);

        queue.start();
        return queue;
    }

    /**
     * Populates the event queue with <code>n</code> events.
     * @param eventQueue
     * @param n
     * @throws Exception
     */
    private void populateQueue(Queue<D360Event> eventQueue, int n) throws Exception{
        for (int i = 0 ; i < n; ++i) {
            mEventQueue.add(D360Event.generateEvent(DATA_TEST));
        }
    }

    /**
     * Builds the Http response based on the response code and the response message. By
     * default, the protocol version is Http 1.1.
     * @param responseCode code for the http response
     * @param responseMessage body for the message
     * @return the response contains in his entity an InputStream referencing the response message
     */
    private static HttpResponse buildResponse(int responseCode, String responseMessage) {
        ProtocolVersion protocolVersion = new ProtocolVersion("HTTP", 1, 1);
        StatusLine statusline = new BasicStatusLine(
                protocolVersion, responseCode, "Finished with: " + responseCode);
        HttpResponse response = new BasicHttpResponse(statusline);
        BasicHttpEntity entity = new BasicHttpEntity();
        entity.setContent(new ByteArrayInputStream(responseMessage.getBytes()));
        response.setEntity(entity);
        return response;
    }

    private static void setConnectivity(boolean enabled) throws Exception {
        Context context = RuntimeEnvironment.application.getApplicationContext();

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        Shadows.shadowOf(wifiManager).setWifiEnabled(enabled);
        wifiManager = (WifiManager) RuntimeEnvironment.application.getSystemService(Context.WIFI_SERVICE);
        Shadows.shadowOf(wifiManager).setWifiEnabled(enabled);

        ConnectivityManager dataManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Shadows.shadowOf(dataManager).setBackgroundDataSetting(enabled);
        dataManager = (ConnectivityManager) RuntimeEnvironment.application.getSystemService(Context.CONNECTIVITY_SERVICE);
        Shadows.shadowOf(dataManager).setBackgroundDataSetting(enabled);

        Shadows.shadowOf(dataManager.getActiveNetworkInfo()).setConnectionStatus(enabled);

        Intent connIntent = new Intent(ConnectivityManager.CONNECTIVITY_ACTION);
        connIntent.putExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, enabled);
        ShadowApplication.getInstance().sendBroadcast(connIntent);

        ShadowSettings shadowSettings = new ShadowSettings();
        shadowSettings.setWifiOn(enabled);
        shadowSettings.setAirplaneMode(enabled);
    }
}

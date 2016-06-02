package com.d360.sdk;

import android.util.ArrayMap;

import com.android.volley.Response;

import junit.framework.TestCase;

import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * Created by Thomas on 02/06/2016.
 */
public class D360RequestTest extends TestCase {

    private static final String url = "http://www.test.com";
    private static final String request = "";

    private static final String DEV_URL = "http://api.dev.staging.crm.slace.me/v2/events";

    public static final String DATA_TEST = "{" +
            "\"data\":{\"foo\":\"bar\"}," +
            "\"meta\":{" +
            "\"localTimeStamp\":1458578476," +
            "\"eventNo\":150," +
            "\"name\":\"ev_MyCustomEvent\"," +
            "\"connectionInfo\":\"wifi\"}}";

    private D360Request input;
    private Map<String, String> mHeaders;
    private Response.Listener mListener;
    private Response.ErrorListener mErrorListener;

    @Override
    protected void setUp() throws Exception {
        mHeaders = new ArrayMap<>();

        // input = new D360Request(url, mHeaders, new JSONObject(request), mListener, mErrorListener);
    }

    @Test
    public void testGenerateRequest() throws Exception {
        URL url = null;
        try {
            url = new URL(DEV_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);//this is in milliseconds
            conn.setConnectTimeout(15000);//this is in milliseconds
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("D360-Api-Key", "kilctuhEeONbf");
            conn.connect();

            byte[] outputBytes  = DATA_TEST.getBytes("UTF-8");
            OutputStream out = conn.getOutputStream();
            out.write(outputBytes);
            out.flush();
            out.close();

            int status = conn.getResponseCode();
            assertEquals(HttpURLConnection.HTTP_ACCEPTED, status);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
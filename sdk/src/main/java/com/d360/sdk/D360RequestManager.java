package com.d360.sdk;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.ArrayMap;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;

/**
 * Created by Thomas on 30/05/2016.
 */
public class D360RequestManager {

    public enum ConnectionType {NONE, WIFI, MOBILE, OTHER}

    private static final String TAG = "D360RequestManager";
    private static final String DEV_URL = "http://api.dev.staging.crm.slace.me/v2/events";

    private String message;
    private Context mContext;
    private final Queue<D360Event> mQueueEvents;

    D360RequestManager(Context context) {
        mContext = context;
        mQueueEvents = new ArrayDeque<>();
    }

    public void sendEvent(String name, D360Event event) {

        RequestQueue queue = Volley.newRequestQueue(mContext);

        final Map<String, String> mHeaders = new ArrayMap<>();
        mHeaders.put("D360-Api-Key", D360SDK.get().getKey());
        mHeaders.put("Content-Type", "application/json");

        final JSONObject parameters;
        try {
            parameters = event.getJSon();
        } catch (JSONException je) {
            Log.e(TAG, "Error while parsing JSon: " + je);
            return;
        }
        Log.i(TAG, "Sending event" + name + "with parameters " + parameters.toString());
        final D360Request request = new D360Request(
                DEV_URL,
                mHeaders,
                parameters,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, "Request Successfully sent: " + response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                handleError(parameters);
                Log.e(TAG, "Error: " + error.getMessage());
            }
        });

        queue.add(request);
    }

    private void handleError(JSONObject parameters) {
        // TODO handle error : test network state, then retry
    }

    public ConnectionType getConnectionType() {
        ConnectivityManager cm =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if (isConnected) {
            switch (activeNetwork.getType()) {
                case ConnectivityManager.TYPE_WIFI:
                    return ConnectionType.WIFI;
                case ConnectivityManager.TYPE_MOBILE:
                    return ConnectionType.MOBILE;
                default:
                    return ConnectionType.OTHER;
            }
        }
        return ConnectionType.NONE;
    }

}

package com.d360.sdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.ArrayMap;
import android.util.Log;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;

/**
 * Created by Thomas on 30/05/2016.
 */
public class D360RequestManager extends BroadcastReceiver {

    public enum ConnectionType {NONE, WIFI, MOBILE, OTHER}

    private static final String TAG = "D360RequestManager";
    private static final String DEV_URL = "http://api.dev.staging.crm.slace.me/v2/events";

    /**
     * The headers used to communicate with the server.
     */
    private final Map<String, String> mHeaders;

    private Context mContext;
    private final String mApiKey;
    private final Queue<D360Event> mQueueEvents;

    /**
     * The event of {@link ConnectivityManager#CONNECTIVITY_ACTION} can arrive multiple times,
     * this variable helps preventing it.
     */
    private boolean mAlreadyResuming = false;
    private boolean mRegistered = false;

    public D360RequestManager(Context context, String apiKey) {
        mContext = context;
        mApiKey = apiKey;
        mQueueEvents = new ArrayDeque<>();

        mHeaders = new ArrayMap<>();
        mHeaders.put("D360-Api-Key", apiKey);
        mHeaders.put("Content-Type", "application/json");
    }

    public void sendEvent(final D360Event event) {

        RequestQueue queue = Volley.newRequestQueue(mContext);

        final JSONObject parameters;
        try {
            parameters = event.getJSon();
        } catch (JSONException je) {
            Log.e(TAG, "Error while parsing JSon: " + je);
            return;
        }
        mQueueEvents.add(event);
        if (mRegistered) {
            Log.i(TAG, "Offline, sending later event " + event.getName() + " with parameters "
                    + parameters.toString());
            return;
        }
        Log.i(TAG, "Sending event " + event.getName() + " with parameters " + parameters.toString());
        final D360Request request = new D360Request(
                DEV_URL,
                mHeaders,
                parameters,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, "Request Successfully sent: " + response);
                        mQueueEvents.poll();
                        if (mQueueEvents.isEmpty() && mRegistered) {
                            mContext.unregisterReceiver(D360RequestManager.this);
                            mRegistered = false;
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                handleError(error, event);
                Log.e(TAG, "Error: " + error.getMessage());
            }
        });
        queue.add(request);
    }

    private void handleError(VolleyError error, D360Event event) {
        if (getConnectionType() == ConnectionType.NONE) {
            D360Persistence.setLastKey(mContext, mApiKey);

            // register this class as a receiver for when the connection comes back
            mRegistered = true;
            IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
            mContext.registerReceiver(this, intentFilter);
        }
    }

    private void handleResponse(JSONObject response) {

    }

    /**
     * Detects if the application is connected or not.
     *
     * @return If connected, returns if on wifi or mobile. Returns none in the other cases.
     */
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
            }
        }
        return ConnectionType.NONE;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (getConnectionType() != ConnectionType.NONE && !mAlreadyResuming) {
            Log.v(TAG, "Connection is back on, resuming event sending...");
            mAlreadyResuming = true;
            for (D360Event event : new ArrayDeque<>(mQueueEvents)) {
                event.addConnectionType();
                sendEvent(event);
            }
        } else {
            boolean running = false;
        }
    }

}

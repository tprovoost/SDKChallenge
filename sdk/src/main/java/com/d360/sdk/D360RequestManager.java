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
import com.android.volley.NoConnectionError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
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
    private Map<String, String> mHeaders;

    /**
     * The queue used by volley to send the events
     */
    private RequestQueue mRequestQueue;

    private Queue<D360Event> mQueueEvents;

    private Context mContext;
    private final String mApiKey;

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

        initializeVolleyRequestQueue();
        initializeHeaders(apiKey);

        // Check if a connection is present, if it is, upload
        // all the downloads
        if (checkConnectivity(context)) {
            resumeDownloadFromFiles();
        } else {
            registerReceiver();
        }
    }

    private void initializeHeaders(String apiKey) {
        mHeaders = new ArrayMap<>();
        mHeaders.put("D360-Api-Key", apiKey);
        mHeaders.put("Content-Type", "application/json");
    }

    private void initializeVolleyRequestQueue() {
        // Instantiate the cache
        Cache cache = new DiskBasedCache(mContext.getCacheDir(), 1024 * 1024); // 1MB cap

        // Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());

        // Instantiate the RequestQueue with the cache and network.
        mRequestQueue = new RequestQueue(cache, network);
    }

    /**
     * Reloads all queues in the cache file into memory and send them.
     */
    private void resumeDownloadFromFiles() {
        try {
            Queue<D360Event> mQueueEvents = D360Persistence.getQueue(mContext);
            if (mQueueEvents.size() > 0) {
                Log.v(TAG, "Reloads all events from file.");
                mRequestQueue.start();
                for (D360Event event : mQueueEvents) {
                    registerEvent(event);
                }
            } else {
                Log.v(TAG, "No events in file.");
            }
        } catch (IOException ioe) {
            Log.e(TAG, "Cache file could not be read: ", ioe);
        }
    }

    public void registerEvent(D360Event event) {
        if (checkConnectivity(mContext)) {
            Log.i(TAG, "Offline, sending later event " + event.getName());
            try {
                D360Persistence.storeEvent(mContext, event);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            mQueueEvents.add(event);
            sendEvent(event);
        }
    }

    /**
     * This method sends the event onto the REST server. While doing so, it pushes it in a queue
     * in case the connection is not available.
     *
     * @param event
     */
    private void sendEvent(final D360Event event) {
        final JSONObject parameters;
        try {
            parameters = event.getJSon();
        } catch (JSONException je) {
            Log.e(TAG, "Error while parsing JSon: " + je);
            return;
        }
        Log.i(TAG, "Sending event " + event.getName() +
                " with parameters " + parameters.toString());
        final D360Request request = new D360Request(
                DEV_URL,
                mHeaders,
                parameters,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, "Request Successfully sent: " + response);
                        // TODO handle response
                        mQueueEvents.remove(event);
                        if (mQueueEvents.isEmpty() && mRegistered) {
                            unregisterReceiver();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        try {
                            D360RequestManager.this.onErrorResponse(error, event);
                        } catch (IOException ioe) {
                            Log.d(TAG, "Could not handle response: ", ioe);
                        }
                    }
                });
        mRequestQueue.add(request);
    }

    public void onErrorResponse(VolleyError error, D360Event event) throws IOException {
        if (!checkConnectivity(mContext)) {
            if (!mRegistered) {
                Log.i(TAG, "Registering " + TAG + " as a BroadCastReceiver");
                D360Persistence.setLastKey(mContext, mApiKey);
                registerReceiver();

                D360Persistence.storeQueue(mContext, mQueueEvents);
                mQueueEvents.clear();
            }
        } else {
            if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                Log.d(TAG, "Server Timed out: ", error);
                D360Persistence.storeEvent(mContext, event);
            } else {
                Log.e(TAG, "Error: ", error);
            }
            mQueueEvents.remove(event);
        }
    }

    /**
     * Register this class as a receiver for when the connection comes back
     */
    private void registerReceiver() {
        Log.i(TAG, "Register as a receiver");
        mRegistered = true;
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        mContext.registerReceiver(this, intentFilter);
    }

    /**
     * Register this class as a receiver for when the connection comes back
     */
    private void unregisterReceiver() {
        Log.i(TAG, "Unregister as a receiver");
        mRegistered = false;
        mContext.unregisterReceiver(this);
    }

    /**
     * This method returns if yes or no a connection is available. <br/>
     * Warning: only works for Wifi and mobile.
     *
     * @param context the context to test the connection with. Typically an application.
     * @return
     */
    public static boolean checkConnectivity(Context context) {
        return getConnectionType(context) != ConnectionType.NONE;
    }

    /**
     * Detects if the application is connected or not.
     *
     * @return If connected, returns if on wifi or mobile. Returns none in the other cases.
     */
    public static ConnectionType getConnectionType(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

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
        boolean connected = checkConnectivity(context);
        if (connected) {
            unregisterReceiver();
            Log.v(TAG, "Connection is back on, resuming event sending...");
            resumeDownloadFromFiles();
        }
    }

}

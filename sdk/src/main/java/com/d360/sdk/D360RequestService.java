package com.d360.sdk;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.util.ArrayMap;
import android.util.Log;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;

/**
 * Created by Thomas on 30/05/2016.
 */
public class D360RequestService extends IntentService {

    public enum ConnectionType { NONE, WIFI, MOBILE }

    private static final String TAG = "D360RequestService";
    private static final String EXTRA_API_KEY = "com.d360.sdk.apikey";

    public static Intent newIntent(Context context, String apiKey) {
        Intent intent = new Intent(context, D360RequestService.class);
        intent.putExtra(EXTRA_API_KEY, apiKey);
        return intent;
    }

     public D360RequestService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "Received an intent" + intent);
        String apiKey = intent.getStringExtra(EXTRA_API_KEY);
        Context context = getApplicationContext();
        D360RequestManager manager = new D360RequestManager(context, apiKey);

        // Check if a connection is present, if it is, upload
        // all the downloads
        if (D360RequestManager.checkConnectivity(context)) {
            manager.resumeDownloadFromFiles();
        } else {
            registerReceiver();
        }
    }

    private void registerReceiver() {

    }

    private void unregisterReceiver() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // unregisterReceiver(receiver);
    }


}

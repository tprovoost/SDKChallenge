package com.d360.sdk;

import android.content.Context;
import android.util.ArrayMap;
import android.util.Log;

import java.util.Map;

/**
 * Created by janchaloupecky on 04/05/16.
 */

public class D360SDK {

    private static final String TAG = "D360SDK";
    private static Map<Context, D360SDK> allSdks;
    private final D360RequestManager mRequestManager;

    private D360SDK(Context context, String apiKey) {
        mRequestManager = new D360RequestManager(context, apiKey);
    }

    public static synchronized void init(Context context, String apiKey) {
        Log.i(TAG, "Starting SDK with mApiKey" + apiKey);
        if (allSdks == null) {
            allSdks = new ArrayMap<>();
        }
        D360SDK sdk = allSdks.get(context);
        if (sdk == null) {
            sdk = new D360SDK(context, apiKey);
            allSdks.put(context, sdk);
        }
    }

    /**
     * Convenience method to send an event without calling {@link D360SDK#getRequestManager()}.
     * @param event
     */
    public static void sendEvent(Context context, D360Event event) {
        D360SDK sdk = allSdks.get(context);
        sdk.getRequestManager().registerEvent(event);
    }

    /**
     * The request manager is used to properly send events to the REST server.
     * @return the request manager
     */
    public D360RequestManager getRequestManager() {
        return mRequestManager;
    }

}

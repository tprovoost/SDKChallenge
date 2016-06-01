package com.d360.sdk;

import android.content.Context;
import android.util.Log;

/**
 * Created by janchaloupecky on 04/05/16.
 */

public class D360SDK {

    private static final String TAG = "D360SDK";

    private static D360SDK sD360sdk;

    private final D360RequestManager mD360RequestManager;

    private Context mContext;

    private String mApiKey;

    private D360SDK(Context context, String apiKey) {
        mContext = context;
        mD360RequestManager = new D360RequestManager(context, apiKey);
    }

    public static void init(Context context, String apiKey) {
        Log.i(TAG, "Starting SDK with mApiKey" + apiKey);
        if (sD360sdk == null) {
            sD360sdk = new D360SDK(context, apiKey);
        }
        sD360sdk.mApiKey = apiKey;
    }

    /**
     * Convenience method to send an event without calling {@link D360SDK#getRequestManager()}.
     * @param event
     */
    public void sendEvent(D360Event event) {
        mD360RequestManager.registerEvent(event);
    }

    /**
     * Returns the singleton of the class {@link D360SDK}.
     * Be careful, method {@link D360SDK#init(Context, String)}
     * has to be called first.
     *
     * @return A reference to the singleton. Can be null if not initialized.
     */
    public static D360SDK getInstance() {
        return sD360sdk;
    }

    /**
     * The request manager is used to properly send events to the REST server.
     * @return the request manager
     */
    public D360RequestManager getRequestManager() {
        return mD360RequestManager;
    }

    public Context getContext() {
        return mContext;
    }
}

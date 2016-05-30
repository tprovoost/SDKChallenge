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

    private String key;

    private D360SDK(Context context) {
        mContext = context;
        mD360RequestManager = new D360RequestManager(context);
    }

    public static void init(Context context, String apiKey) {
        Log.i(TAG, "Starting SDK with key" + apiKey);
        if (sD360sdk == null) {
            sD360sdk = new D360SDK(context);
        }
        sD360sdk.key = apiKey;
    }

    public void sendEvent(String name, D360Event event) {
        mD360RequestManager.sendEvent(name, event);
    }

    /**
     * Returns the singleton of the class {@link D360SDK}.
     * Be careful, method {@link D360SDK#init(Context, String)}
     * has to be called first.
     *
     * @return A reference to the singleton.
     */
    public static D360SDK get() {
        return sD360sdk;
    }

    public String getKey() {
        return key;
    }

}

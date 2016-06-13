package com.d360.sdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.util.Log;

/**
 * Created by Thomas on 11/06/2016.
 */
public class D360RequestBroadcaster extends BroadcastReceiver {

    private static final String TAG = "D360RequestBroadcaster";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Received broadcast");
        String key = D360Persistence.getLastKey(context);
        if (key != null) {
            Intent requestService = D360RequestService.newIntent(context, key);
            context.startService(requestService);
        }
    }

}

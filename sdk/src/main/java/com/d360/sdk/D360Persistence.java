package com.d360.sdk;

import android.content.Context;
import android.preference.Preference;
import android.preference.PreferenceManager;

/**
 * Created by Thomas on 30/05/2016.
 */
public class D360Persistence {

    private static final String PREF_LAST_KEY = "lastKey";

    public static String getLastKey(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_LAST_KEY, null);
    }

    public static void setLastKey(Context context, String key) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_LAST_KEY, key)
                .apply();
    }

}

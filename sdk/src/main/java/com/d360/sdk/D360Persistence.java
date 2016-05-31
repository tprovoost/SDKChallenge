package com.d360.sdk;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Created by Thomas on 30/05/2016.
 */
public class D360Persistence {

    private static final String TAG = "D360Persistence";
    private static final String PREF_LAST_KEY = "lastKey";
    private static final String PERSISTENCE_FILE_QUEUE = ".D360queues";
    private static final String JSON_SEPARATOR = "<|>";

    public static String getLastKey(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_LAST_KEY, null);
    }

    public static void setLastKey(Context context, String key) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_LAST_KEY, key)
                .apply();
    }

    /**
     * Stores the queue of events into the cache directory in a temporary file.
     * @param context needed to access the cache of the app.
     * @param queueEvents the queue of events.
     * @throws IOException an error is raised if the file could not be written.
     */
    public static void storeQueue(Context context, Queue<D360Event> queueEvents) throws IOException {
        File file = getCacheFile(context);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            storeQueue(fos, queueEvents);
        } finally {
            if (fos != null)
                fos.close();
        }
    }

    /**
     * Stores the queue of events into the file.
     * @param outputStream the stream to save the queue of events in to.
     * @param queueEvents the queue of events, provided by {@link D360RequestManager}.
     * @throws IOException
     */
    public static void storeQueue(OutputStream outputStream, Queue<D360Event> queueEvents) throws IOException {
        String allJsons = "";
        int i = 0;
        for (D360Event event : queueEvents) {
            if (i != 0)
                allJsons += JSON_SEPARATOR;
            allJsons += event.getJsonString();
            ++i;
        }
        byte[] byteData = allJsons.getBytes();
        outputStream.write(byteData, 0, byteData.length);
    }

    /**
     * Retrieves the queue of events of the application.
     * @param context needed to access the cache of the app.
     * @return
     * @throws IOException
     */
    public static Queue<D360Event> getQueue(Context context) throws IOException {
        File file = getCacheFile(context);
        if (file.exists()) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                return getQueue(fis);
            } finally {
                if (fis != null)
                    fis.close();
            }
        }
        return new ArrayDeque<>();
    }

    /**
     *
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static Queue<D360Event> getQueue(InputStream inputStream) throws IOException {
        Queue<String> queueS = new ArrayDeque<>();
        Queue<D360Event> queueEvents = new ArrayDeque<>();

        byte[] data = new byte[inputStream.available()];
        inputStream.read(data);

        String dataString = new String(data);
        getQueueFromString(dataString, queueEvents);

        return queueEvents;
    }

    public static void getQueueFromString(String dataString, Queue<D360Event> queueEvents) {
        String[] arrayDataString = dataString.split(JSON_SEPARATOR);
        for (String s : arrayDataString) {
            try {
                D360Event event = D360Event.generateEvent(s);
                queueEvents.add(event);
            }
            catch (JSONException jse) {
                Log.e(TAG, "Error while parsing string: ", jse);
            }
        }
    }

    public static File getCacheFile(Context context) throws IOException {
        File file = new File(context.getCacheDir(), PERSISTENCE_FILE_QUEUE);
        if (!file.exists())
            file.createNewFile();
        return file;
    }

}

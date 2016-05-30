package com.d360.application;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.d360.sdk.D360Event;
import com.d360.sdk.D360SDK;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    public static final String DATA_TEST = "{" +
            "\"data\":{\"foo\":\"bar\"}," +
            "\"meta\":{" +
            "\"eventNo\":150," +
            "\"localTimeStamp\":1458578476," +
            "\"name\":\"ev_MyCustomEvent\"," +
            "\"connectionInfo\":\"wifi\"}}";
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        D360SDK.init(this, "kilctuhEeONbf-V1JMH7");

        // JSONObject parameters = new JSONObject(DATA_TEST);
        D360Event event = new D360Event();
        event.addDataParameters("foo", "bar");
        event.addMetaParameters("eventNo", 150);
        event.addMetaParameters("localTimeStamp", 1458578476);
        event.addMetaParameters("name", "ev_MyCustomEvent");
        event.addMetaParameters("connectionInfo", "wifi");
        D360SDK.get().sendEvent("EventTest", event);

    }
}

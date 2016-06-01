package com.d360.application;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.d360.sdk.D360Event;
import com.d360.sdk.D360SDK;

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
        for (int i = 0; i < 100; ++i) {
            D360Event event = new D360Event("ev_MyCustomEvent" + i);
            event.addDataParameter("foo" + i, "bar" + i);
            event.addEventNo();
            event.addTimeStamp();
            event.addConnectionType();
            D360SDK.getInstance().sendEvent(event);
        }
    }
}

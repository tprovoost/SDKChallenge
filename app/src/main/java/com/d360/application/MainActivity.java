package com.d360.application;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.d360.sdk.D360Event;
import com.d360.sdk.D360Persistence;
import com.d360.sdk.D360SDK;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    public static final String DATA_TEST = "{" +
            "\"data\":{\"foo\":\"bar\"}," +
            "\"meta\":{" +
            "\"eventNo\":150," +
            "\"localTimeStamp\":1458578476," +
            "\"name\":\"ev_MyCustomEvent\"," +
            "\"connectionInfo\":\"wifi\"}}";

    private static final String DEV_URL = "http://api.dev.staging.crm.slace.me/v2/events";
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        D360SDK.init(this, "kilctuhEeONbf-V1JMH7");
        Button b = (Button)findViewById(R.id.button);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < 100; ++i) {
                    D360Event event = new D360Event("ev_MyCustomEvent" + i);
                    event.addDataParameter("foo" + i, "bar" + i);
                    event.addEventNo();
                    event.addTimeStamp();
                    event.addConnectionType();
                    D360SDK.getInstance().sendEvent(event);
                }
            }
        });
    }
}

package com.d360.sdk;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.Map;

/**
 * Created by Thomas on 30/05/2016.
 */
public class D360Request extends JsonObjectRequest {
    private final Gson gson = new Gson();
    private final Map<String, String> mHeaders;
    private final Response.Listener<JSONObject> listener;

    /**
     * Make a POST request and return a parsed object from JSON.
     *
     * @param url URL of the request to make
     * @param headers Map of request mHeaders. If null, uses default mHeaders.
     * @param jsonRequest The request containing data and meta, as a {@link JSONObject}.
     */
    public D360Request(String url, Map<String, String> headers, JSONObject jsonRequest,
                       Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(Request.Method.POST, url, jsonRequest, listener, errorListener);
        this.mHeaders = headers;
        this.listener = listener;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return mHeaders != null ? mHeaders : super.getHeaders();
    }

}

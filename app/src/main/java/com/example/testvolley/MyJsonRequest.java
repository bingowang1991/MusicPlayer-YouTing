package com.example.testvolley;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.JsonRequest;

public class MyJsonRequest extends JsonRequest{

    public MyJsonRequest(int method, String url, String requestBody,
                         Listener listener, ErrorListener errorListener) {
        super(method, url, requestBody, listener, errorListener);
        // TODO Auto-generated constructor stub
    }

    public int compareTo(Object another) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    protected Response parseNetworkResponse(NetworkResponse response) {
        // TODO Auto-generated method stub
        return null;
    }

}

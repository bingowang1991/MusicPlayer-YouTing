package com.example.util;

import android.content.Context;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.request.StringRequest;
import com.example.testvolley.MyApplication;

public class UserHelper {
    private MyApplication application;
    private static RequestQueue mQueue;
    private Context context;
    private static final String TAG = UserHelper.class.getSimpleName();
    private static final String add_friend_url = "http://121.42.164.7/index.php/Home/Index/add_friend";
    private static final String delete_friend_url = "http://121.42.164.7/index.php/HOme/Index/delete_friend";
    private static final String add_lover_url = "http://121.42.164.7/index.php/Home/Index/add_lover";
    private static final String delete_lover_url = "http://121.42.164.7/index.php/Home/Index/delete_lover";
    public UserHelper(Context context){
        this.context = context;
        application = MyApplication.get();
        mQueue = application.getRequestQueue();
    }
    public static void add_friend(long uid){
        String url = add_friend_url +"?uid="+uid;
        StringRequest request = new StringRequest(Method.GET,url,null,new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                // TODO Auto-generated method stub

            }
        },new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub

            }
        });
        mQueue.add(request);
    }
    public static void delete_friend(long uid){
        String url = delete_friend_url +"?uid="+uid;
        StringRequest request = new StringRequest(Method.GET,url,null,new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                // TODO Auto-generated method stub

            }
        },new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub

            }
        });
        mQueue.add(request);
    }
    public static void add_lover(long uid){
        String url = add_lover_url +"?uid="+uid;
        StringRequest request = new StringRequest(Method.GET,url,null,new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                // TODO Auto-generated method stub

            }
        },new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub

            }
        });
        mQueue.add(request);
    }
    public static void delelte_lover(long uid){
        String url = delete_lover_url +"?uid="+uid;
        StringRequest request = new StringRequest(Method.GET,url,null,new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                // TODO Auto-generated method stub

            }
        },new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub

            }
        });
        mQueue.add(request);
    }
}

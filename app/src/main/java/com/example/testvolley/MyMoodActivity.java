package com.example.testvolley;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Request.Method;
import com.android.volley.VolleyError;
import com.example.request.JsonObjectRequest;
import com.example.util.ExitApplication;

import org.json.JSONException;
import org.json.JSONObject;

import de.greenrobot.daoexample.User;

/**
 * Created by Administrator on 2016/5/13.
 */
public class MyMoodActivity extends AppCompatActivity {

    private EditText content_mood;
    private ImageButton tv_return;
    private TextView send_mood;
    private MyApplication application;
    private RequestQueue mQueue;
    private String mood;

    private final String TAG = "MyMood";
    private final String changeMood_url = "http://121.42.164.7/index.php/Home/Index/change_mood";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_mood);

        application = (MyApplication)this.getApplicationContext();
        mQueue = application.getRequestQueue();
        ExitApplication.getInstance().addActivity(this);
        tv_return = (ImageButton)findViewById(R.id.tv_return);
        content_mood = (EditText)findViewById(R.id.mood_edit);
        send_mood = (TextView)findViewById(R.id.send_mood);
        mood = application.getLoginUser().getMood();
        if (mood != null && !mood.equals(application.DEFAULT_MOOD)){
            content_mood.setText(mood);
        }

        //返回
        tv_return.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                finish();
            }
        });

        //修改Mood
        send_mood.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                change_mood();
            }
        });
    }

    public void change_mood(){
        mood = content_mood.getText().toString().trim();

        if (mood.equals("")){
            mood = "~暂无心情~";
        }
        JSONObject jObject = new JSONObject();
        try {
            jObject.put("mood", mood);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        JsonObjectRequest changeMood = new JsonObjectRequest(Method.POST,changeMood_url,jObject,null,new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                // TODO Auto-generated method stub
                String status = null;
                try {
                    status = response.getString("status");
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if(status.equals("success")){
                    User user = application.getLoginUser();
                    user.setMood(mood);
                    application.setLoginUser(user);
                    onBackPressed();
                }
            }
        },new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        mQueue.add(changeMood);
    }

}

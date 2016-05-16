package com.example.testvolley;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.example.util.ExitApplication;

import java.util.ArrayList;

import de.greenrobot.daoexample.User;

public class RemarkActivity extends AppCompatActivity {
    private ImageButton tv_return;
    private TextView tv_complete;
    private EditText name_edit;
    private int list_position;
    private long uid;
    private String name;
    private String mood;
    private String avatar;
    private String sex;
    private User user;
    private int position=-1;
    private String remark_name;

    private MyApplication application;
    private RequestQueue mQueue;
    public static ArrayList<User> friendList = new ArrayList<User>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remark);
        ExitApplication.getInstance().addActivity(this);
        application = (MyApplication)this.getApplicationContext();
        mQueue = application.getRequestQueue();
        friendList=application.getFriendList();

        Intent intent=getIntent();
        uid=intent.getLongExtra("user_id",-1);
        name=intent.getStringExtra("name");
        mood=intent.getStringExtra("mood");
        avatar=intent.getStringExtra("avatar");
        sex=intent.getStringExtra("sex");
        user=new User(uid,name,sex,mood,avatar);
        for(int i=0;i<friendList.size();i++){
            if(friendList.get(i).getUid()==uid){
                position=i;
                break;
            }
        }

        tv_return=(ImageButton)findViewById(R.id.tv_return);
        tv_complete=(TextView)findViewById(R.id.tv_complete);
        name_edit=(EditText)findViewById(R.id.name_edit);

        name_edit.setText("name");

        //งฺง็จ[งแ
        tv_return.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                finish();
            }
        });

        //งฺงเก่ง๎
        tv_complete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                //User user_old=friendList.get(list_position);
                //User user_new=user_old;
                remark_name=name_edit.getText().toString().trim();
                if(remark_name!=""){
                    if(position>=0){
                        friendList.get(position).setName(remark_name);
                        //user.setName(remark_name);
                        Intent intent=new Intent();
                        intent.setClass(RemarkActivity.this,FriendInfoActivity.class);
                        intent.putExtra("user_id",user.getUid());
                        intent.putExtra("name",remark_name);
                        intent.putExtra("mood", user.getMood());
                        intent.putExtra("avatar", user.getAvatar());
                        intent.putExtra("sex", user.getSex());
                        startActivity(intent);
                        finish();
                    }else{
                        finish();
                    }
                }
            }
        });


    }
}


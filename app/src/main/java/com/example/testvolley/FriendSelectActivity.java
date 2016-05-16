package com.example.testvolley;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.example.util.ExitApplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.daoexample.User;

/**
 * Created by Administrator on 2016/5/13.
 */
public class FriendSelectActivity extends AppCompatActivity {

    private ImageButton tv_return;
    private TextView tv_name;
    private ImageButton tv_continue;
    private TextView tv_fs;
    private long music_id;
    private String artist;
    private String name;
    private String pic_url;
    private ImageView iv_selected;

    private ArrayList<User> friendList = new ArrayList<User>();
    private MyApplication application;
    private RequestQueue mQueue;

    private ArrayList<String> Names=new ArrayList<String>();
    private ArrayList<String> pic_urls=new ArrayList<String>();
    private ArrayList<String> selectfriends=new ArrayList<String>();
    private ArrayList<Boolean> click_num=new ArrayList<Boolean>();
    private ArrayList<Long> select_uid=new ArrayList<Long>();

    private final static String TAG = "FriendSelectActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_select);
        ExitApplication.getInstance().addActivity(this);
        application = (MyApplication)this.getApplicationContext();
        mQueue = application.getRequestQueue();
        friendList=application.getFriendList();

        tv_return = (ImageButton) findViewById(R.id.tv_return);
        tv_continue = (ImageButton) findViewById(R.id.tv_continue);
        tv_fs = (TextView) findViewById(R.id.tv_fs);
        ListView list=(ListView) findViewById(R.id.lv_fs);

        Intent intent=getIntent();
        name=intent.getStringExtra("name");
        artist=intent.getStringExtra("artist");
        pic_url=intent.getStringExtra("pic_url");
        music_id = intent.getLongExtra("music_id",-1);
        for(int i=0;i<friendList.size();i++){
            Names.add(friendList.get(i).getName());
            pic_urls.add(friendList.get(i).getAvatar());
            boolean a=false;
            click_num.add(a);
        }

        List<Map<String,Object>> listitems=new ArrayList<Map<String,Object>>();
        for(int i=0;i<friendList.size();i++){
            Map<String,Object> listitem=new HashMap<String,Object>();
            listitem.put("header", R.mipmap.default_face);
            listitem.put("name", Names.get(i));
            listitem.put("stateselect", R.mipmap.list_icn_checkbox_ok);
            listitems.add(listitem);
        }
        MySimpleAdapter simpleAdapter=new MySimpleAdapter(this,listitems,R.layout.list_item_friendselect,
                new String[]{"header","name","stateselect"},
                new int[]{R.id.iv_pic,R.id.tv_name,R.id.iv_selected});


        list.setAdapter(simpleAdapter);


        //List不同item的点击响应函数
        list.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

                iv_selected = (ImageView) arg1.findViewById(R.id.iv_selected);
                boolean temp=!click_num.get(position);
                click_num.set(position, temp);
                if(click_num.get(position)==true){
                    iv_selected.setVisibility(View.VISIBLE);
                    tv_name = (TextView) arg1.findViewById(R.id.tv_name);

                    selectfriends.add(tv_name.getText()+";");
                    select_uid.add(friendList.get(position).getUid());

                    String hasselec="";
                    for(int i=0;i<selectfriends.size();i++){
                        hasselec=hasselec+selectfriends.get(i);
                    }
                    tv_fs.setText(hasselec);
                }
                else{
                    iv_selected.setVisibility(View.GONE);
                    tv_name = (TextView) arg1.findViewById(R.id.tv_name);
                    selectfriends.remove(tv_name.getText()+";");
                    select_uid.remove(friendList.get(position).getUid());

                    String hasselec="";
                    for(int i=0;i<selectfriends.size();i++){
                        hasselec=hasselec+selectfriends.get(i);
                    }
                    tv_fs.setText(hasselec);
                }
            }
        });

        //返回
        tv_return.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                finish();
            }
        });

        //下一步
        tv_continue.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                String fs="";
                long [] s_uid = null;

                for(int i=0;i<selectfriends.size();i++){
                    fs=fs+selectfriends.get(i);
                }

                if(fs!=""){

                    s_uid=new long[select_uid.size()];
                    for(int i=0;i<select_uid.size();i++){
                        s_uid[i]=select_uid.get(i);
                    }
                    Log.v(TAG,"s_uid"+s_uid[0]);
                    Intent intent=new Intent();
                    intent.setClass(FriendSelectActivity.this,MyShareActivity.class);
                    intent.putExtra("tofriends", fs);
                    intent.putExtra("select_uid", s_uid);
                    intent.putExtra("share_mode", 1);
                    intent.putExtra("name", name);
                    intent.putExtra("artist", artist);
                    intent.putExtra("pic_url", pic_url);
                    intent.putExtra("music_id",music_id);
                    startActivity(intent);
                    finish();
                }
            }
        });

    }

    public class MySimpleAdapter extends SimpleAdapter {
        Context context;
        public MySimpleAdapter(Context context, List<? extends Map<String, ?>> data,
                               int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
            this.context = context;
        }
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = super.getView(position, convertView, parent);
            final ImageView iv=(ImageView) v.findViewById(R.id.iv_pic);

            //设置好友头像
            if(pic_urls.get(position)!= null){
                ImageRequest avatarRequest=new ImageRequest(pic_urls.get(position), new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap arg0) {
                        // TODO Auto-generated method stub
                        Log.v("succ","111");
                        iv.setImageBitmap(arg0);
                    }
                }, 50, 50, Config.RGB_565, new ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError arg0) {
                        // TODO Auto-generated method stub
                        iv.setImageResource(R.mipmap.default_face);
                    }
                });
                mQueue.add(avatarRequest);
            }else{
                iv.setImageResource(R.mipmap.default_face);
            }

            return v;
        }
    }

}

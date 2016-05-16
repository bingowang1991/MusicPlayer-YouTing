package com.example.testvolley;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SimpleAdapter;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.example.request.StringRequest;
import com.example.util.ExitApplication;
import com.example.util.PinyinComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.daoexample.User;

/**
 * Created by Administrator on 2016/5/13.
 */
public class MyFriendsActivity extends AppCompatActivity {

    private SwipeMenuListView mListView;
    private ImageButton tv_return;
    private ArrayList<User> friendList = new ArrayList<User>();
    private User user;

    private MyApplication application;
    private RequestQueue mQueue;

    private MySimpleAdapter simpleAdapter;
    private ArrayList<String> Names=new ArrayList<String>();
    private ArrayList<String> pic_urls=new ArrayList<String>();
    private ArrayList<Map<String,Object>> listitems =new ArrayList<Map<String,Object>>();
    private PinyinComparator pinyinComparator;
    private static final String delete_friend_url = "http://121.42.164.7/index.php/Home/Index/delete_friend";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_friends);
        ExitApplication.getInstance().addActivity(this);
        application = (MyApplication)this.getApplicationContext();
        mQueue = application.getRequestQueue();
        friendList=application.getFriendList();
        pinyinComparator = new PinyinComparator();
        Collections.sort(friendList,pinyinComparator);
        tv_return=(ImageButton)findViewById(R.id.tv_return_myfriend);
        mListView = (SwipeMenuListView) findViewById(R.id.listView_friend);

        for(int i=0;i<friendList.size();i++){
            Names.add(friendList.get(i).getName());
            pic_urls.add(friendList.get(i).getAvatar());
        }

        for(int i=0;i<friendList.size();i++){
            Map<String,Object> listitem=new HashMap<String,Object>();
            listitem.put("header", R.mipmap.default_face);
            listitem.put("name", Names.get(i));
            listitems.add(listitem);
        }
        simpleAdapter=new MySimpleAdapter(this,listitems,R.layout.item_list_friends,
                new String[]{"header","name"},
                new int[]{R.id.friend_photo,R.id.friend_name});

        mListView.setAdapter(simpleAdapter);

        //返回
        tv_return.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                finish();
            }
        });
    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    public void delete_friend(long uid,final int position){
        final String url = delete_friend_url +"?friend_id="+uid;
        StringRequest request = new StringRequest(Method.GET,url,null,new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                // TODO Auto-generated method stub
                Log.v("myfriends",url);
                Log.v("myFriends","response"+response.toString());
                listitems.remove(position);
                user=friendList.get(position);
                application.removeFromFriendList(user);

                Names.remove(position);
                pic_urls.remove(position);
                simpleAdapter.notifyDataSetChanged();
            }
        },new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        mQueue.add(request);
    }

    public class MySimpleAdapter extends SimpleAdapter {
        Context context;
        public MySimpleAdapter(Context context, List<? extends Map<String, ?>> data,
                               int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
            this.context = context;
        }
        public View getView(final int position, View convertView, ViewGroup parent) {
            View v = super.getView(position, convertView, parent);
            final ImageView iv=(ImageView) v.findViewById(R.id.friend_photo);
            final ImageButton chat = (ImageButton)v.findViewById(R.id.chat);
            final ImageButton delete = (ImageButton)v.findViewById(R.id.delete);

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

            //跳到好友信息
            iv.setOnClickListener(new OnClickListener(){

                @Override
                public void onClick(final View v) {
                    Intent intent=new Intent();
                    intent.setClass(MyFriendsActivity.this,FriendInfoActivity.class);
                    intent.putExtra("user_id",friendList.get(position).getUid());
                    intent.putExtra("name",friendList.get(position).getName());
                    intent.putExtra("mood", friendList.get(position).getMood());
                    intent.putExtra("avatar", friendList.get(position).getAvatar());
                    intent.putExtra("sex", friendList.get(position).getSex());
                    startActivity(intent);
                }

            });

            //聊天
            chat.setOnClickListener(new OnClickListener(){

                @Override
                public void onClick(final View v) {
                    Intent intent = new Intent();
                    intent.putExtra("targetID", friendList.get(position).getName());
                    intent.setClass(MyFriendsActivity.this, ChatActivity.class);
                    startActivity(intent);
                }

            });
            //删除
            delete.setOnClickListener(new OnClickListener(){

                @Override
                public void onClick(final View v) {
                    user = friendList.get(position);
                    delete_friend(user.getUid(),position);}
            });
            return v;
        }
    }
}

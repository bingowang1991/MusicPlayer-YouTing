package com.example.testvolley;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.example.request.StringRequest;

import java.util.ArrayList;

import de.greenrobot.daoexample.MusicMessage;
import de.greenrobot.daoexample.User;

/**
 * Created by Administrator on 2016/5/15.
 */
public class SearchUserAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<User> userList,friendList;
    private MyApplication application;
    private RequestQueue mQueue;
    private ImageLoader imageLoader;
    private StringRequest addRequest;
    private MusicMessage mm;
    private ProgressDialog mDialog;
    private User user;
    private int[] user_status;

    private static final String add_friend = "http://121.42.164.7/index.php/Home/Index/add_friend";
    private static final String TAG = "SearchUserAdapter";


    public SearchUserAdapter(Context context, ArrayList<User> userList){
        this.context = context;
        this.userList = userList;

        application = MyApplication.get();
        friendList = application.getFriendList();
        mQueue = application.getRequestQueue();
        imageLoader = new ImageLoader(mQueue,new BitmapCache());
        mDialog = new ProgressDialog(context);
        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mDialog.setMessage("正在发送请求");
        user_status = new int[userList.size()];
        for(int i = 0;i<userList.size();i++){
            user_status[i] = isFriendListContain(userList.get(i));
        }
    }

    @Override
    public int getCount() {
        return userList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public View getView(final  int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(context).inflate(R.layout.item_search_user, null);
        final Button add_friend_button = (Button)convertView.findViewById(R.id.add_friend);
        TextView name_view = (TextView)convertView.findViewById(R.id.name);
        NetworkImageView avatar_view = (NetworkImageView)convertView.findViewById(R.id.avatar);
        avatar_view.setDefaultImageResId(R.mipmap.ic_launcher);
        avatar_view.setErrorImageResId(R.mipmap.ic_launcher);
        // 判断是否是好友，这里有3种状态，1，未添加 2.已经是好友 3.请求命令已发送，尚未回应
        user = userList.get(position);
        name_view.setText(user.getName());
        long uid = user.getUid();

        switch(user_status[position]){
            case 0:
                // 用户自己
                add_friend_button.setBackgroundColor(000000);
                add_friend_button.setClickable(false);
                add_friend_button.setTextColor(context.getResources().getColor(R.color.gainsboro));
                add_friend_button.setEnabled(false);
                break;
            case 1:
                // 已添加
                add_friend_button.setText("已添加");
                add_friend_button.setBackgroundColor(000000);
                add_friend_button.setTextColor(context.getResources().getColor(R.color.gainsboro));
                add_friend_button.setEnabled(false);
                break;
            case 2:
                //未添加
                break;
            case 3:
                //已发送好友请求
                add_friend_button.setText("已发送");
                add_friend_button.setBackgroundColor(000000);
                add_friend_button.setEnabled(false);
                add_friend_button.setTextColor(context.getResources().getColor(R.color.gainsboro));
                break;
        }
        String avatar = user.getAvatar();
        if(!avatar.equals("null")){
            avatar_view.setImageUrl(avatar,imageLoader);
            Log.v(TAG,"avatar:"+avatar);
        }

        avatar_view.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                // 点击头像，跳转到FriendInfo界面
                Intent intent = new Intent();
                intent.setClass(context,FriendInfoActivity.class);
                intent.putExtra("name", user.getName());
                intent.putExtra("mood", user.getMood());
                intent.putExtra("uid", user.getUid());
                intent.putExtra("avatar",user.getAvatar());
                context.startActivity(intent);
            }

        });
        add_friend_button.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(final View v) {
                // TODO Auto-generated method stub
                String add_friend_url = add_friend+"?friend_id="+user.getUid();
                addRequest = new StringRequest(Method.GET,add_friend_url,null,new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        // TODO Auto-generated method stub
                        Log.v(TAG,response);
                        mDialog.dismiss();
                        ((Button)v).setText("已发送");
                        v.setBackgroundColor(000000);
                        v.setClickable(false);
                        user_status[position] = 3;
                    }
                },new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        Toast.makeText(context, "发送请求失败", Toast.LENGTH_SHORT).show();
                        mDialog.dismiss();
                    }
                });
                mDialog.show();
                mQueue.add(addRequest);
            }

        });
        return convertView;
    }

    public int isFriendListContain(User user){

        for(int i=0;i<friendList.size();i++){
            if(user.getUid() == application.getLoginUser().getUid()){
                return 0;
            }
            if(user.getUid() == friendList.get(i).getUid()){
                return 1;
            }

        }
        return 2;
    }
}

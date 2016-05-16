package com.example.testvolley;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.example.request.StringRequest;
import com.example.util.TimeHelper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;

import de.greenrobot.dao.query.QueryBuilder;
import de.greenrobot.daoexample.DaoSession;
import de.greenrobot.daoexample.SystemMessage;
import de.greenrobot.daoexample.SystemMessageDao;
import de.greenrobot.daoexample.User;

/**
 * Created by Administrator on 2016/5/15.
 */
public class MyMessageAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<SystemMessage> smList;
    private ArrayList<User> userList;
    private MyApplication application;
    private RequestQueue mQueue;
    private ImageLoader imageLoader;
    private SystemMessage sm;
    private ProgressDialog mDialog;
    private DaoSession daoSession;
    private SystemMessageDao systemMessageDao;
    private QueryBuilder qb;
    WeakReference<Activity> weak; // 定义弱引用变量

    private static final String confirm_friend_url = "http://121.42.164.7/index.php/Home/Index/confirm_friend";
    private static final String confirm_lover_url = "http://121.42.164.7/index.php/Home/Index/confirm_lover";
    private static final String TAG = "MyMessageAdapter";

    public MyMessageAdapter(Context context, ArrayList<User> userList, ArrayList<SystemMessage> smList){
        this.context = context;
        this.userList = userList;
        this.smList = smList;
        this.weak = new WeakReference<Activity>((Activity)context);
        application = MyApplication.get();
        mQueue = application.getRequestQueue();
        imageLoader = new ImageLoader(mQueue,new BitmapCache());
        mDialog = new ProgressDialog(context);
        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mDialog.setMessage("正在添加好友");
        daoSession = application.getDaoSession(context);
        systemMessageDao = daoSession.getSystemMessageDao();
    }

    @Override
    public int getCount() {
        return smList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(context).inflate(R.layout.item_my_message, null);
        final Button add_button = (Button)convertView.findViewById(R.id.add);
        TextView message_view = (TextView)convertView.findViewById(R.id.message);
        TextView category_view = (TextView)convertView.findViewById(R.id.category);
        TextView time_view = (TextView)convertView.findViewById(R.id.time);
        TextView name_view = (TextView)convertView.findViewById(R.id.name);
        NetworkImageView avatar_view = (NetworkImageView)convertView.findViewById(R.id.avatar);
        avatar_view.setDefaultImageResId(R.mipmap.ic_launcher);
        avatar_view.setErrorImageResId(R.mipmap.ic_launcher);

        sm = smList.get(position);
        Date date = sm.getCreate_time();
        time_view.setText(TimeHelper.dateToFormatString(date));
        String category = sm.getCategory();
        int act = sm.getAct();
        if(category.equals("add_friend")){
            category_view.setText("请求添加你为好友");
            if(act == 1){
                add_button.setBackgroundColor(0x000000);
                add_button.setText("已添加");
                add_button.setClickable(false);
            }
        }else if(category.equals("confirm_friend")){
            category_view.setText("已经添加你为好友");
            message_view.setVisibility(View.GONE);
            add_button.setVisibility(View.GONE);
        }else if(category.equals("add_lover")){
            category_view.setText("请求添加你为情侣");
            if(act == 1){
                add_button.setBackgroundColor(0x000000);
                add_button.setText("已添加");
                add_button.setClickable(false);
            }
        }else if(category.equals("confirm_lover")){
            category_view.setText("已经添加你为情侣");
            message_view.setVisibility(View.GONE);
            add_button.setVisibility(View.GONE);
        }
        final User user = userList.get(position);
        message_view.setText(sm.getMessage());
        name_view.setText(user.getName());
        String avatar = user.getAvatar();
        if(!avatar.equals("null")){
            avatar_view.setImageUrl(avatar,imageLoader);
            Log.v(TAG,"avatar:"+avatar);
        }
        add_button.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                long user_id = sm.getUser_id();
                final long friend_id = sm.getSender_id();
                int confirm = 1;
                String friend_url = confirm_friend_url+"?user_id="+user_id+"&friend_id="+friend_id+"&confirm="+confirm;
                String lover_url = confirm_lover_url +"?friend_id="+friend_id+"&confirm="+confirm ;
                StringRequest confirmFriendRequest = new StringRequest(Method.GET,friend_url,null,new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.v(TAG,response);
                        mDialog.dismiss();
                        add_button.setBackgroundColor(00000000);
                        add_button.setText("已添加");
                        add_button.setClickable(false);
                        //设定此消息为已处理
                        sm.setAct(1);

                        systemMessageDao.update(sm);
                        // 添加进好友列表
                        Log.v(TAG,"addToFriendList:"+userList.get(position).getUid());
                        if(!isExistInFriendList(friend_id)){
                            application.addToFriendList(userList.get(position));
                        }

                    }
                },new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        Log.v(TAG,error.toString());
                        mDialog.dismiss();
                    }
                });
                StringRequest confirmLoverRequest = new StringRequest(Method.GET,lover_url,null,new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.v(TAG,response);
                        mDialog.dismiss();
                        add_button.setBackgroundColor(00000000);
                        add_button.setText("已添加");
                        add_button.setClickable(false);
                        //设定此消息为已处理
                        sm.setAct(1);
                        systemMessageDao.update(sm);
                        // 添加lover
                        application.setLover(userList.get(position));
                        application.setIsExistLover(true);
                    }
                },new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        Log.v(TAG,error.toString());
                        mDialog.dismiss();
                    }
                });
                if(smList.get(position).getCategory().equals("add_friend")){
                    mQueue.add(confirmFriendRequest);
                }else{
                    mQueue.add(confirmLoverRequest);
                }
                mDialog.show();
            }
        });

        name_view.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setClass(weak.get(),FriendInfoActivity.class);
                intent.putExtra("user_id",user.getUid());
                intent.putExtra("name", user.getName());
                intent.putExtra("mood", user.getMood());
                intent.putExtra("avatar", user.getAvatar());
                intent.putExtra("sex", user.getSex());
                intent.putExtra("SystemMessageID", smList.get(position).getId());

                context.startActivity(intent);
            }
        });

        return convertView;
    }

    private boolean isExistInFriendList(long user_id){
        for(int i =0;i<application.getFriendList().size();i++){
            if(user_id == application.getFriendList().get(i).getUid()){
                return true;
            }
        }
        return false;
    }
}

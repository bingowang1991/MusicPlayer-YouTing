package com.example.testvolley;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ListView;

import com.android.volley.RequestQueue;
import com.example.util.ExitApplication;

import java.util.ArrayList;

import de.greenrobot.dao.query.QueryBuilder;
import de.greenrobot.daoexample.DaoSession;
import de.greenrobot.daoexample.SystemMessage;
import de.greenrobot.daoexample.SystemMessageDao;
import de.greenrobot.daoexample.User;
import de.greenrobot.daoexample.UserDao;
import de.greenrobot.daoexample.SystemMessageDao.Properties;

/**
 * Created by Administrator on 2016/5/13.
 */
public class MyMessageActivity extends AppCompatActivity {

    public static boolean hasmessage=true;
    private ListView systemListView;
    private ImageButton iv_return;
    private MyApplication application;
    private RequestQueue mQueue;
    private DaoSession daoSession;
    private QueryBuilder qb;
    private SystemMessageDao systemMessageDao;
    private UserDao userDao;
    private MyMessageAdapter adapter;

    private ArrayList<SystemMessage> smList = new ArrayList<SystemMessage>();
    private ArrayList<User> userList = new ArrayList<User>();

    private final String TAG = "MyMessageActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_message);

        ExitApplication.getInstance().addActivity(this);
        application = (MyApplication)this.getApplicationContext();
        application.setMessage(false);
        mQueue = application.getRequestQueue();
        daoSession = application.getDaoSession(getApplicationContext());
        userDao = daoSession.getUserDao();
        systemMessageDao = daoSession.getSystemMessageDao();
        qb = systemMessageDao.queryBuilder();

        initView();

        initAdapter();

        iv_return=(ImageButton)findViewById(R.id.iv_return);
        iv_return.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                finish();

            }
        });
    }

    public void initView(){
        systemListView = (ListView)findViewById(R.id.systemListView);
    }

    public void initAdapter(){
        long uid = application.getLoginUser().getUid();
        qb.where(Properties.User_id.eq(uid)).orderDesc(Properties.Create_time);
        long count = qb.buildCount().count();
        if(count>0){
            smList = (ArrayList<SystemMessage>) qb.list();
            Log.v(TAG,"smList:"+smList.size());
            for(int i=0;i<smList.size();i++){

                long sender_id = smList.get(i).getSender_id();
                qb = userDao.queryBuilder();
                qb.where(de.greenrobot.daoexample.UserDao.Properties.Uid.eq(sender_id));

                User user = (User) qb.unique();
                userList.add(user);

            }
        }
        adapter = new MyMessageAdapter(this,userList,smList);
        systemListView.setAdapter(adapter);
    }
    public void ReturnToMain(View view){
        this.finish();
    }

}

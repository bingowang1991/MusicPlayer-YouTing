package com.example.testvolley;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.request.JsonObjectRequest;
import com.example.util.ExitApplication;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.api.BasicCallback;
import de.greenrobot.dao.query.QueryBuilder;
import de.greenrobot.daoexample.DaoSession;
import de.greenrobot.daoexample.User;
import de.greenrobot.daoexample.UserDao;
import de.greenrobot.daoexample.UserDao.Properties;

/**
 * Created by Administrator on 2016/5/13.
 */
public class MyLoginActivity extends AppCompatActivity {

    private Button loginBtn;
    private Button registerBtn;
    private EditText inputUsername;
    private EditText inputPassword;
    private CheckBox saveInfoItem;
    private ProgressDialog mDialog;
    private String responseMsg = "";
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private String username,password,status,uid;
    private MyApplication application;
    private RequestQueue mQueue;
    private ImageView tv_return;
    private final static String TAG = "MyLoginActivity";
    private final static String login_url = "http://121.42.164.7/index.php/Home/Index/login";
    private DaoSession daoSession;
    private QueryBuilder qb;
    private UserDao userDao;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_login);

        ExitApplication.getInstance().addActivity(this);
        application = (MyApplication)this.getApplicationContext();
        mQueue = application.getRequestQueue();

        daoSession = application.getDaoSession(getApplicationContext());
        userDao = daoSession.getUserDao();
        loginBtn = (Button)findViewById(R.id.login_btn_login);
        registerBtn = (Button)findViewById(R.id.login_btn_zhuce);
        inputUsername = (EditText)findViewById(R.id.login_edit_account);
        inputPassword = (EditText)findViewById(R.id.login_edit_pwd);
        saveInfoItem = (CheckBox)findViewById(R.id.login_cb_savepwd);
        tv_return = (ImageButton)findViewById(R.id.my_login_return);
        sp = getSharedPreferences("youting",MODE_PRIVATE);
        editor = sp.edit();

        //初始化数据
        LoadUserdata();

        //检查网络
        CheckNetworkState();

        //监听记住密码选项
        saveInfoItem.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //载入用户信息

//                if(saveInfoItem.isChecked())
//                {
//                     //获取已经存在的用户名和密码
//                    String realUsername = sp.getString("username", "");
//                    String realPassword = sp.getString("password", "");
//                    editor.putBoolean("checkstatus", true);
//                    editor.commit();
//
//                     if((!realUsername.equals(""))&&!(realUsername==null)||(!realPassword.equals(""))||!(realPassword==null))
//                     {
//                        //清空输入框
//                        inputUsername.setText("");
//                          inputPassword.setText("");
//                          //设置已有值
//                         inputUsername.setText(realUsername);
//                         inputPassword.setText(realPassword);
//                     }
//                }else
//                {
//                    editor.putBoolean("checkstatus", false);
//                    editor.commit();
//                    //清空输入框
//                    inputUsername.setText("");
//                     inputPassword.setText("");
//                }
            }
        });

        //登录
        loginBtn.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View arg0) {
                //显示登录状态框
                mDialog = new ProgressDialog(MyLoginActivity.this);
                mDialog.setTitle("登陆");
                mDialog.setMessage("正在登陆服务器，请稍后...");
                mDialog.show();

                username = inputUsername.getText().toString();
                password = md5(inputPassword.getText().toString());

                //检查输入

                JSONObject jObject = new JSONObject();
                try {
                    jObject.put("name", username);
                    jObject.put("password", password);
                } catch (JSONException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

                JsonObjectRequest jRequest = new JsonObjectRequest(Request.Method.POST, login_url, jObject,null,new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject j) {
                        editor.putString("user", j.toString());
                        Log.v(TAG+"json",j.toString());

                        try {
                            status = j.getString("status");
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        if (status.equals("success")){
                            // 登陆成功！
                            // 注册jpush的alias
                            editor.putBoolean("login", true);
                            editor.commit();
                            try {
                                uid = j.getString("uid");
                                String name = j.getString("name");
                                String mood = j.getString("mood");
                                String sex = j.getString("sex");
                                String avatar = j.getString("avatar");
                                User user = new User(Long.parseLong(uid),name,sex,mood,avatar);
                                editor.putString("USER_NAME", name);
                                editor.putString("PASSWORD", j.getString("password"));
                                editor.commit();
                                application.setLoginUser(user);
                                application.setIsLogin(true);
                                //将user添加到数据库中
                                qb = userDao.queryBuilder();
                                qb.where(Properties.Uid.eq(uid));
                                if(qb.buildCount().count() > 0){
                                    userDao.update(user);
                                }else{
                                    userDao.insert(user);
                                }
                                //       						Log.v(TAG+"login",application.getLoginUser().getAvatar());
                            } catch (JSONException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            WelcomeActivity.getWelcomeActivityCallBack().setAlias(uid);
                            setIMLogin();
                            WelcomeActivity.getWelcomeActivityCallBack().setFriendList(uid);
                            WelcomeActivity.getWelcomeActivityCallBack().setMyMusic(uid);
                            WelcomeActivity.getWelcomeActivityCallBack().setFriendMusic(uid);
                            WelcomeActivity.getWelcomeActivityCallBack().setLover();
                            new Handler().postDelayed(new Runnable(){
                                public void run() {
                                    mDialog.dismiss();
                                    Intent intent=new Intent();
                                    intent.setClass(MyLoginActivity.this, MainActivity.class);
                                    startActivity(intent);
                                }
                            }, 2000);


                        }else{
                            Log.v(TAG,"log fail");
                            editor.putBoolean("login", false);
                            editor.commit();
                            mDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "用户名和密码不匹配", Toast.LENGTH_LONG).show();

                        }
                    }
                },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                String sb = volleyError.toString();
                                Log.v(TAG+"login",sb);
                                mDialog.dismiss();
                                Toast.makeText(getApplicationContext(), sb, Toast.LENGTH_SHORT).show();
//        	        	WelcomeActivity.getWelcomeActivityCallBack().setMyMusic(null);
//        	        	WelcomeActivity.getWelcomeActivityCallBack().setFriendMusic(null);
                            }
                        });
                mQueue.add(jRequest);
                //发起登录线程
//                Thread loginThread = new Thread(new LoginThread());
//                loginThread.start();
            }
        });

        //返回
        tv_return.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                finish();
            }
        });

        //若点击注册则跳回注册界面
        registerBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent();
                intent.setClass(MyLoginActivity.this, MyRegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    //向服务器端进行验证，并返回服务器响应状态
    private boolean loginServer(String username, String password){
        boolean loginValidate = false;

        /*            //添加用户名和密码

        try
        {
            //设置请求参数项

            //判断是否请求成功
            if()
            {
                loginValidate = true;
                //获得响应信息
                responseMsg = "";
            }
        }catch(Exception e)
        {
            e.printStackTrace();
        }*/

        return loginValidate;
    }

    //初始化用户数据
    private void LoadUserdata(){
        boolean checkstatus = sp.getBoolean("checkstatus", false);
        //若勾选了记住密码，则载入已存的用户名和密码，否则提示输入
        if(checkstatus){
            saveInfoItem.setChecked(true);
            //载入用户信息
            //获取已经存在的用户名和密码
            String realUsername = sp.getString("username", "");
            String realPassword = sp.getString("password", "");
            if((!realUsername.equals(""))&&!(realUsername==null)||(!realPassword.equals(""))||!(realPassword==null)) {
                inputUsername.setText("");
                inputPassword.setText("");
                inputUsername.setText(realUsername);
                inputPassword.setText(realPassword);
            }
        }else {
            saveInfoItem.setChecked(false);
            inputUsername.setText("");
            inputPassword.setText("");
        }
    }

    //检查网络状态
    public void CheckNetworkState()
    {
        ConnectivityManager manager = (ConnectivityManager)getSystemService(
                Context.CONNECTIVITY_SERVICE);
        State mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
        State wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
        //如果3G、wifi、2G等网络状态是连接的，则退出，否则显示提示信息进入网络设置界面
        if(mobile == State.CONNECTED||mobile==State.CONNECTING)
            return;
        if(wifi == State.CONNECTED||wifi==State.CONNECTING)
            return;
        showTips();
    }

    private void showTips(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setTitle("没有可用网络");
        builder.setMessage("当前网络不可用，是否设置网络？");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 如果没有网络连接，则进入网络设置界面
                startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                MyLoginActivity.this.finish();
            }
        });
        builder.create();
        builder.show();
    }

    //Handler
    Handler handler = new Handler(){
        public void handleMessage(Message msg){
            switch(msg.what){
                case 0:
                    mDialog.cancel();
                    Toast.makeText(getApplicationContext(), "登录成功！", Toast.LENGTH_SHORT).show();
                    //登录成功跳转到主界面
                    Intent intent = new Intent();
                    intent.setClass(MyLoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                case 1:
                    mDialog.cancel();
                    Toast.makeText(getApplicationContext(), "密码错误", Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    mDialog.cancel();
                    Toast.makeText(getApplicationContext(), "URL验证失败", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    //LoginThread线程类
    class LoginThread implements Runnable {
        @Override
        public void run() {
            String username = inputUsername.getText().toString();
            String password = inputPassword.getText().toString();
            boolean checkstatus = sp.getBoolean("checkstatus", false);
            if(checkstatus){
                //获取已经存在的用户名和密码
                String realUsername = sp.getString("username", "");
                String realPassword = sp.getString("password", "");
                if((!realUsername.equals(""))&&!(realUsername==null)||(!realPassword.equals(""))||!(realPassword==null)) {
                    if(username.equals(realUsername)&&password.equals(realPassword)) {
                        username = inputUsername.getText().toString();
                        password = inputPassword.getText().toString();
                    }
                }
            }else {
                password = md5(password);
            }
            System.out.println("username="+username+":password="+password);

            //URL合法，但是这一步并不验证密码是否正确
            //boolean loginValidate = loginServer(username, password);
            boolean loginValidate = true;
            System.out.println("----------------------------bool is :"+loginValidate+"----------response:"+responseMsg);
            Message msg = handler.obtainMessage();
            if(loginValidate){
                if(responseMsg.equals("success")){
                    msg.what = 0;
                    handler.sendMessage(msg);
                }else{
                    msg.what = 0;
                    handler.sendMessage(msg);
                }
            }else{
                msg.what = 2;
                handler.sendMessage(msg);
            }
        }
    }

    /**
     * MD5单向加密，32位，用于加密密码，因为明文密码在信道中传输不安全，明文保存在本地也不安全
     * @param str
     * @return
     */
    public static String md5(String str){
        MessageDigest md5 = null;
        try{
            md5 = MessageDigest.getInstance("MD5");
        }catch(Exception e){
            e.printStackTrace();
            return "";
        }

        char[] charArray = str.toCharArray();
        byte[] byteArray = new byte[charArray.length];

        for(int i = 0; i < charArray.length; i++){
            byteArray[i] = (byte)charArray[i];
        }
        byte[] md5Bytes = md5.digest(byteArray);

        StringBuffer hexValue = new StringBuffer();
        for( int i = 0; i < md5Bytes.length; i++){
            int val = ((int)md5Bytes[i])&0xff;
            if(val < 16){
                hexValue.append("0");
            }
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString();
    }

    public void setIMLogin(){
        Log.v(TAG,"IMLogin");
        JMessageClient.login(application.getLoginUser().getName(), password,
                new BasicCallback() {
                    @Override
                    public void gotResult(final int status, final String desc) {
                        MyLoginActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (status == 0) {
                                    //后台拿UserInfo
                                    JMessageClient.getUserInfo(application.getLoginUser().getName(), null);

                                } else {

                                    Log.i("LoginController", "status = " + status);

                                }
                            }
                        });
                    }
                });
    }

}

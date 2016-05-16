package com.example.testvolley;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

/**
 * Created by Administrator on 2016/5/13.
 */
public class MyRegisterActivity extends AppCompatActivity {

    private MyApplication application;
    private RequestQueue mQueue;
    private EditText newUser,newPassword,confirmPassword;
    private Button registerBtn, clearBtn;
    private ProgressDialog mDialog;
    private String responseMsg = "";
    public static boolean regstate=false;
    private SharedPreferences sharepre;
    private final static String TAG = "MyRegisterActivity";
    private MyRegisterActivity mContext;
    private final static String register_url = "http://121.42.164.7/index.php/Home/Index/register";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_register);

        ExitApplication.getInstance().addActivity(this);
        mContext = this;
        application =  (MyApplication)this.getApplicationContext();
        mQueue = application.getRequestQueue();
        newUser = (EditText)findViewById(R.id.newUser_input);
        newPassword = (EditText)findViewById(R.id.newPassword_input);
        confirmPassword = (EditText)findViewById(R.id.Confirm_input);
        registerBtn = (Button)findViewById(R.id.registerbtn);
        clearBtn = (Button)findViewById(R.id.clearbtn);

        sharepre = getSharedPreferences("userdata",0);

        registerBtn.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                //获取用户名、密码、确认密码
                String newusername = newUser.getText().toString();
                String newpassword = md5(newPassword.getText().toString());
                String confirmpwd = md5(confirmPassword.getText().toString());
                // 判断用户名字符数不小于4个
                if(newusername.length() < 4){
                    Toast.makeText(getApplicationContext(), "用户名不得小于4个字符", Toast.LENGTH_SHORT).show();
                }else if(newPassword.getText().toString().length() < 6){
                    Toast.makeText(getApplicationContext(), "密码不得小于6个字符", Toast.LENGTH_SHORT).show();
                }else if(newPassword.getText().toString().length() > 26){
                    Toast.makeText(getApplicationContext(), "密码不得大于26个字符", Toast.LENGTH_SHORT).show();
                }
                else{
                    //若密码和确认密码相等，则向服务器发起注册请求，否则打印错误信息
                    if(newpassword.equals(confirmpwd)){

                        //状态显示框
                        mDialog = new ProgressDialog(MyRegisterActivity.this);
                        mDialog.setTitle("登陆");
                        mDialog.setMessage("正在登陆服务器，请稍后...");
                        mDialog.show();
                        JSONObject jObject = new JSONObject();
                        try {
                            jObject.put("name", newusername);
                            jObject.put("password", newpassword);
                        } catch (JSONException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }

                        JsonObjectRequest registerRequest = new JsonObjectRequest(Request.Method.POST, register_url, jObject,null,new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject j) {
                                Log.v(TAG,j.toString());
                                String status = null;
                                try {
                                    status = j.getString("status");
                                } catch (JSONException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                                if(status.equals("success")){

                                    mDialog.dismiss();
                                    // 向JMessage注册信息
                                    JMessageClient.register(newUser.getText().toString(), md5(newPassword.getText().toString()), new BasicCallback() {

                                        @Override
                                        public void gotResult(final int status, final String desc) {
                                            mContext.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {

                                                    if (status == 0) {
                                                        Intent intent=new Intent(MyRegisterActivity.this,MyLoginActivity.class);
                                                        startActivity(intent);
                                                        MyRegisterActivity.this.finish();

                                                    } else {
                                                        Intent intent=new Intent(MyRegisterActivity.this,MyLoginActivity.class);
                                                        startActivity(intent);
                                                        MyRegisterActivity.this.finish();
                                                    }
                                                }
                                            });
                                        }
                                    });

                                }else if(status.equals("fail")){
                                    mDialog.dismiss();
                                    Toast.makeText(getApplicationContext(), "用户名重复", Toast.LENGTH_SHORT).show();
                                }
                            }
                        },new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                Log.v(TAG,volleyError.toString());
                                mDialog.dismiss();
                                Toast.makeText(getApplicationContext(), "网络错误", Toast.LENGTH_SHORT).show();
                            }
                        });
                        mQueue.add(registerRequest);
                        //发起注册新线程
                        //                   Thread loginThread = new Thread(new RegisterThread());
                        //                  loginThread.start();
                    }else {
                        Toast.makeText(getApplicationContext(), "您两次输入的密码不一致！", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        clearBtn.setOnClickListener(new Button.OnClickListener(){
            //重置按钮清空编辑框
            @Override
            public void onClick(View v) {
                newUser.setText("");
                newPassword.setText("");
                confirmPassword.setText("");
            }
        });
    }

    //返回注册状态
    boolean getRegState(){
        return regstate;
    }

    //向服务器端进行验证，并返回服务器响应状态
    private boolean registerServer(String username, String password){
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

    //Handler
    Handler handler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            Editor editor = sharepre.edit();
            switch(msg.what)
            {
                case 0:
                    mDialog.cancel();
                    Toast.makeText(getApplicationContext(), "注册成功", Toast.LENGTH_SHORT).show();
                    MyRegisterActivity.regstate=true;
                    editor.putBoolean("regstate", true);

                    //跳转到登陆界面
                    Intent intent=new Intent(MyRegisterActivity.this,MyLoginActivity.class);
                    startActivity(intent);
                    MyRegisterActivity.this.finish();
                    break;
                case 1:
                    mDialog.cancel();
                    editor.putBoolean("regstate", false);
                    Toast.makeText(getApplicationContext(), "注册失败", Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    mDialog.cancel();
                    editor.putBoolean("regstate", false);
                    Toast.makeText(getApplicationContext(), "URL验证失败", Toast.LENGTH_SHORT).show();
                    break;
            }
            editor.commit();
        }
    };

    //RegisterThread线程类
    class RegisterThread implements Runnable
    {

        @Override
        public void run() {
            String username = newUser.getText().toString();
            String password = md5(newPassword.getText().toString());

            //URL合法，但是这一步并不验证密码是否正确
            //boolean registerValidate = registerServer(username, password);
            boolean registerValidate = true;
            Message msg = handler.obtainMessage();
            if(registerValidate)
            {
                if(responseMsg.equals("success"))
                {
                    msg.what = 0;
                    handler.sendMessage(msg);
                }else
                {
                    msg.what = 0;
                    handler.sendMessage(msg);
                }

            }else
            {
                msg.what = 2;
                handler.sendMessage(msg);
            }
        }

    }

    public void ReturnToMain(View view){
        this.finish();
    }

    /**
     * MD5单向加密，32位，用于加密密码，因为明文密码在信道中传输不安全，明文保存在本地也不安全
     * @param str
     * @return
     */
    public static String md5(String str)
    {
        MessageDigest md5 = null;
        try
        {
            md5 = MessageDigest.getInstance("MD5");
        }catch(Exception e)
        {
            e.printStackTrace();
            return "";
        }

        char[] charArray = str.toCharArray();
        byte[] byteArray = new byte[charArray.length];

        for(int i = 0; i < charArray.length; i++)
        {
            byteArray[i] = (byte)charArray[i];
        }
        byte[] md5Bytes = md5.digest(byteArray);

        StringBuffer hexValue = new StringBuffer();
        for( int i = 0; i < md5Bytes.length; i++)
        {
            int val = ((int)md5Bytes[i])&0xff;
            if(val < 16)
            {
                hexValue.append("0");
            }
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString();
    }

}

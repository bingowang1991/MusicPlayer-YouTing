package com.example.testvolley;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.example.myview.CustomImageView;
import com.example.myview.Tools;
import com.example.request.JsonObjectRequest;
import com.example.request.StringRequest;
import com.example.util.ExitApplication;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UpProgressHandler;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.storage.UploadOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import cn.jpush.im.android.api.JMessageClient;
import de.greenrobot.daoexample.User;

/**
 * Created by Administrator on 2016/5/13.
 */
public class UserInfoSettingActivity extends AppCompatActivity {

    private Button btn_loginout;
    private SharedPreferences.Editor editor;
    private ImageButton iv_return;
    private CustomImageView faceImage;
    private Button uploadImage;
    private TextView userName;
    private TextView title;
    private TextView userMood;
    private ProgressDialog mDialog;
    private MyApplication application;
    private SharedPreferences sp;
    private RequestQueue mQueue;
    private String token,key;
    private User user;
    private Bitmap photo;

    private final static String TAG = "UserInfoSettingActivity";
    private final static String token_url = "http://121.42.164.7/index.php/Home/index/getToken";
    private final static String changeAvatar_url = "http://121.42.164.7/index.php/Home/index/change_avatar";
    private final static String logout_url = "http://121.42.164.7/index.php/Home/index/logout";
    private final String qiniu_url = "http://7xi2lw.com1.z0.glb.clouddn.com/";

    private String[] items = new String[] { "选择本地图片", "拍照" };
    Map<String,String> uploadParams = new HashMap<String,String>();
    /* 头像名称 */
    private static final String IMAGE_FILE_NAME = "faceImage.jpg";

    /* 请求码 */
    private static final int IMAGE_REQUEST_CODE = 0;
    private static final int CAMERA_REQUEST_CODE = 1;
    private static final int RESULT_REQUEST_CODE = 2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info_setting);
        ExitApplication.getInstance().addActivity(this);
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        Log.v(TAG,"onresume");
        initViews();
    }

    private void initViews() {
        application =(MyApplication) this.getApplicationContext();
        mQueue = application.getRequestQueue();
        sp = getSharedPreferences("youting",MODE_PRIVATE);
        user = application.getLoginUser();
        String temp_name=user.getName();
        String mood = user.getMood();
        String avatar = user.getAvatar();
        iv_return = (ImageButton) findViewById(R.id.my_userinfo_return);
        faceImage = (CustomImageView) findViewById(R.id.iv_face);
        uploadImage = (Button)findViewById(R.id.set_head);
        userName = (TextView)findViewById(R.id.user_name);
        userMood = (TextView)findViewById(R.id.user_signature);
        title = (TextView)findViewById(R.id.title);
        btn_loginout=(Button)findViewById(R.id.btn_loginout);
        if(mood.equals(MyApplication.DEFAULT_MOOD)){
            mood = "~暂无心情~";
        }
        userName.setText( temp_name);
        userMood.setText(mood);
        title.setText(temp_name);
        if(!avatar.equals("null")){
            ImageRequest avatarRequest = new ImageRequest(avatar,new Response.Listener<Bitmap>() {

                @Override
                public void onResponse(Bitmap response) {
                    // TODO Auto-generated method stub
                    faceImage.setImageBitmap(response);
                }
            },300,200,Config.ARGB_8888,new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    // TODO Auto-generated method stub

                }
            });
            mQueue.add(avatarRequest);
        }

        iv_return.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                MainActivity.getMainActivityCallBack().setUserInfo();
                finish();

            }
        });

        userMood.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent();
                intent.setClass(UserInfoSettingActivity.this, MyMoodActivity.class);
                startActivity(intent);
                finish();
            }
        });

        uploadImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                showDialog();
            }
        });

        btn_loginout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                StringRequest logoutRequest = new StringRequest(Method.GET,logout_url,null,new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        // TODO Auto-generated method stub
                        editor = sp.edit();
                        editor.putBoolean("login", false);
                        editor.commit();
                        application.initList();
                        application.setLoginUser(null);
                        application.setIsLogin(false);
                        WelcomeActivity.getWelcomeActivityCallBack().setAlias("0");
                        JMessageClient.logout();
                        finish();
                    }
                },new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        Toast.makeText(getApplicationContext(),"注销失败",Toast.LENGTH_SHORT).show();
                    }
                });
                mQueue.add(logoutRequest);
            }
        });
    }

    /**
     * 显示选择对话框
     */
    private void showDialog() {
        new AlertDialog.Builder(this)
                .setTitle("设置头像")
                .setItems(items, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                Intent intentFromGallery = new Intent();
                                intentFromGallery.setType("image/*"); // 设置文件类型
                                intentFromGallery
                                        .setAction(Intent.ACTION_GET_CONTENT);
                                startActivityForResult(intentFromGallery,
                                        IMAGE_REQUEST_CODE);
                                break;
                            case 1:
                                Intent intentFromCapture = new Intent(
                                        MediaStore.ACTION_IMAGE_CAPTURE);
                                // 判断存储卡是否可以用，可用进行存储
                                if (Tools.hasSdcard()) {
                                    intentFromCapture.putExtra(
                                            MediaStore.EXTRA_OUTPUT,
                                            Uri.fromFile(new File(Environment
                                                    .getExternalStorageDirectory(),
                                                    IMAGE_FILE_NAME)));
                                }
                                startActivityForResult(intentFromCapture,
                                        CAMERA_REQUEST_CODE);
                                break;
                        }
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //结果码不等于取消时候
        if (resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case IMAGE_REQUEST_CODE:
                    startPhotoZoom(data.getData());
                    break;
                case CAMERA_REQUEST_CODE:
                    if (Tools.hasSdcard()) {
                        File tempFile = new File(
                                Environment.getExternalStorageDirectory()
                                        + IMAGE_FILE_NAME);
                        startPhotoZoom(Uri.fromFile(tempFile));
                    } else {
                        Toast.makeText(UserInfoSettingActivity.this, "未找到存储卡，无法存储照片！",
                                Toast.LENGTH_LONG).show();
                    }
                    break;
                case RESULT_REQUEST_CODE:
                    if (data != null) {
                        getImageToView(data);
                    }
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 裁剪图片方法实现
     *
     * @param uri
     */
    public void startPhotoZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // 设置裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 320);
        intent.putExtra("outputY", 320);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, 2);
    }

    /**
     * 保存裁剪之后的图片数据
     *
     * @param data
     */
    private void getImageToView(Intent data) {
        Bundle extras = data.getExtras();
        if (extras != null) {
            photo = extras.getParcelable("data");
            byte[] img = Bitmap2Bytes(photo);

            mDialog = new ProgressDialog(UserInfoSettingActivity.this);
            mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mDialog.setMessage("正在上传图片");
            mDialog.show();
            uploadPic(img);
            //成功上传后再设置
            Log.v(TAG,"succ set");
            /** 保存到SD卡 */
//			  File f = new File("/sdcard/DCIM/", "zzzzz");
//			  if (f.exists()) {
//			   f.delete();
//			  }
//			  try {
//			   FileOutputStream out = new FileOutputStream(f);
//			   photo.compress(Bitmap.CompressFormat.PNG, 90, out);
//			   out.flush();
//			   out.close();
//			  } catch (FileNotFoundException e) {
//			   e.printStackTrace();
//			  } catch (IOException e) {
//			   e.printStackTrace();
//			  }
        }
    }

    //上传图片到云端
    public void uploadPic(byte[] i){
        final byte[] img = i;
        key = "avatar-"+user.getUid()+"v0.png";
        if(user.getAvatar() != "null"){
            int start = user.getAvatar().lastIndexOf("v")+1;
            int end = user.getAvatar().lastIndexOf(".");
            int version = Integer.parseInt(user.getAvatar().substring(start, end))+1;
            Log.v(TAG,"version:"+version);
            key = "avatar-"+user.getUid()+"v"+version+".png";
        }
//		uploadParams.put("x:scope", "youting:"+key);
//		uploadParams.put("x:deadline", ""+3600);
//		uploadParams.put("x:mimeLimit", "image/jpeg");

        Log.v(TAG,uploadParams.toString());
        //获取token
        StringRequest tokenRequest = new StringRequest(Method.POST,token_url,null,new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                // TODO Auto-generated method stub
                token = response;
                Log.v(TAG,token);
                //upload
                UploadManager uploadManager = new UploadManager();
                uploadManager.put(img, key, token,
                        new UpCompletionHandler() {
                            @Override
                            public void complete(String arg0, ResponseInfo arg1,
                                                 JSONObject arg2) {
                                // 更新数据库
                                Log.v(TAG,"upload succ"+arg0+"response:"+arg1);
                                String avatar = qiniu_url+key;
                                user.setAvatar(avatar);
                                application.setLoginUser(user);
                                changeAvatar();
                            }
                        }, new UploadOptions(null, null, false,
                                new UpProgressHandler(){
                                    public void progress(String key, double percent){
                                        int progress = (int)percent*100;
                                        mDialog.setProgress(progress);
                                        Log.i("qiniu", key + ": " + percent);
                                    }
                                }, null));
            }
        },new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub
                Log.v(TAG,error.toString());
            }
        });
        mQueue.add(tokenRequest);
    }

    public void changeAvatar(){
        JSONObject jObject = new JSONObject();
        try {
            jObject.put("avatar", qiniu_url+key);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        JsonObjectRequest changeAvatar = new JsonObjectRequest(Method.POST,changeAvatar_url,jObject,null,new Response.Listener<JSONObject>() {

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
                    mDialog.dismiss();

                    faceImage.setImageBitmap(photo);
                    Toast.makeText(getApplicationContext(), "修改头像成功", Toast.LENGTH_SHORT).show();
                }
            }
        },new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub

            }
        });
        mQueue.add(changeAvatar);
    }

    private byte[] Bitmap2Bytes(Bitmap bm){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

}

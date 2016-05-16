package com.example.testvolley;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.example.myview.Bimp;
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
import java.io.IOException;

import de.greenrobot.daoexample.Music;

public class MyShareActivity extends AppCompatActivity {
    private EditText mysong_mood;
    private ImageView iv_share;
    private ImageButton tv_return;
    private ImageButton tv_share;
    private TextView song_info;
    private TextView ToFriends;
    long music_id;
    private String artist;
    private String name;
    private String pic_url;
    private String tofriends;
    private long [] select_uid;
    private Music music;
    private String song_mood;
    private MyApplication application;
    private RequestQueue mQueue;
    private int share_mode = 0;
    private final int share_one_mode = 1;
    private final int share_all_mode = 0;

    private GridView noScrollgridview;
    private GridAdapter adapter;

    private static final int TAKE_PICTURE = 0x000000;
    private String path = "";
    private String share_pic_url = null;
    private final String TAG = "MyShareActivity";
    private final String share_all_url = "http://121.42.164.7/index.php/Home/Index/share_all";
    private final String share_one_url = "http://121.42.164.7/index.php/Home/Index/share_one";
    private final static String token_url = "http://121.42.164.7/index.php/Home/index/getToken";
    private final String qiniu_url = "http://7xi2lw.com1.z0.glb.clouddn.com/";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_share);
        ExitApplication.getInstance().addActivity(this);
        Init();

        application = (MyApplication)this.getApplicationContext();
        mQueue = application.getRequestQueue();
//		music = application.getService().getMusic();
//		artist = music.getArtist();
//		pic_url = music.getPic_url();
//		name = music.getName();

        tv_return=(ImageButton) findViewById(R.id.tv_return);
        tv_share=(ImageButton) findViewById(R.id.tv_share);
        iv_share=(ImageView) findViewById(R.id.iv_share);
        ToFriends=(TextView) findViewById(R.id.tofriends);
        song_info=(TextView) findViewById(R.id.song_info);
        mysong_mood=(EditText) findViewById(R.id.mysong_mood);

        Intent intent=getIntent();
        tofriends=intent.getStringExtra("tofriends");
        select_uid=intent.getLongArrayExtra("select_uid");
        music_id = intent.getLongExtra("music_id",-1);
        share_mode=intent.getIntExtra("share_mode", share_all_mode);
        artist = intent.getStringExtra("artist");
        pic_url = intent.getStringExtra("pic_url");
        name = intent.getStringExtra("name");
        song_info.setText(artist+"-"+name);
        ToFriends.setText("分享给："+tofriends);

        //设置专辑图片

        ImageRequest avatarRequest=new ImageRequest(pic_url, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap arg0) {
                // TODO Auto-generated method stub
                Log.v("succ","111");
                iv_share.setImageBitmap(arg0);
            }
        }, 50, 50, Config.RGB_565, new ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError arg0) {
                // TODO Auto-generated method stub
            }
        });
        mQueue.add(avatarRequest);



        //返回
        tv_return.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                finish();
            }
        });

        //分享
        tv_share.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // 判断有没有图片
                if(Bimp.max != 0){
                    byte[] b = Bitmap2Bytes(Bimp.bmp.get(0));
                    uploadPic(b);
                }else{

                    switch(share_mode){
                        case share_one_mode:
                            share_one();


                            Bimp.max=0;
                            Bimp.act_bool=true;
                            Bimp.bmp.clear();
                            Bimp.drr.clear();

                            break;
                        case share_all_mode:
                            share_all();

                            Bimp.max=0;
                            Bimp.act_bool=true;
                            Bimp.bmp.clear();
                            Bimp.drr.clear();

                            break;
                    }
                }
            }
        });
    }

    public void share_one(){
        final int length = select_uid.length;
        song_mood = mysong_mood.getText().toString().trim();

        for(int i=0;i<length;i++){
            final int cycle = i;
            JSONObject jObject = new JSONObject();
            try {
                if (!song_mood.equals("")){
                    jObject.put("message", song_mood);
                }
                jObject.put("music_id", music_id);
                jObject.put("receiver_id",select_uid[i]);
                Log.v(TAG,"share_pic_url"+share_pic_url);
                if(share_pic_url != null){
                    jObject.put("pic_url", share_pic_url);
                }



            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            Log.v(TAG,jObject.toString());
            JsonObjectRequest share_one_request = new JsonObjectRequest(Method.POST,share_one_url,jObject,null,new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    // TODO Auto-generated method stub

                    if(cycle == length -1){
                        finish();
                        Toast.makeText(getApplicationContext(),"分享成功",Toast.LENGTH_SHORT).show();
                    }

                }
            },new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    // TODO Auto-generated method stub
                    Toast.makeText(getApplicationContext(),"分享失败",Toast.LENGTH_SHORT).show();
                }
            });
            mQueue.add(share_one_request);
        }


    }
    public void share_all(){
        song_mood = mysong_mood.getText().toString().trim();

        JSONObject jObject = new JSONObject();
        try {
            jObject.put("music_id",music_id);
            if (!song_mood.equals("")){
                jObject.put("message", song_mood);
            }
            if(share_pic_url != null){
                jObject.put("pic_url", share_pic_url);
            }

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Log.v(TAG,jObject.toString());
        JsonObjectRequest share_all_request = new JsonObjectRequest(Method.POST,share_all_url,jObject,null,new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                // TODO Auto-generated method stub
                Log.v(TAG,response.toString());
                //onBackPressed();
                finish();
                Toast.makeText(getApplicationContext(),"分享成功",Toast.LENGTH_SHORT).show();

            }
        },new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub
                Log.v(TAG,error.toString());
                Toast.makeText(getApplicationContext(),"分享失败",Toast.LENGTH_SHORT).show();
            }
        });
        mQueue.add(share_all_request);
    }


    @SuppressLint("HandlerLeak")
    public class GridAdapter extends BaseAdapter {
        private LayoutInflater inflater; // 视图容器
        private int selectedPosition = -1;// 选中的位置
        private boolean shape;

        public boolean isShape() {
            return shape;
        }

        public void setShape(boolean shape) {
            this.shape = shape;
        }

        public GridAdapter(Context context) {
            inflater = LayoutInflater.from(context);
        }

        public void update() {
            loading();
        }

        public int getCount() {
            return (Bimp.bmp.size() + 1);
        }

        public Object getItem(int arg0) {

            return null;
        }

        public long getItemId(int arg0) {

            return 0;
        }

        public void setSelectedPosition(int position) {
            selectedPosition = position;
        }

        public int getSelectedPosition() {
            return selectedPosition;
        }

        /**
         * ListView Item设置
         */
        public View getView(int position, View convertView, ViewGroup parent) {
            final int coord = position;
            ViewHolder holder = null;
            if (convertView == null) {

                convertView = inflater.inflate(R.layout.item_published_grida,
                        parent, false);
                holder = new ViewHolder();
                holder.image = (ImageView) convertView
                        .findViewById(R.id.item_grida_image);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            if (position == Bimp.bmp.size()) {
                holder.image.setImageBitmap(BitmapFactory.decodeResource(
                        getResources(), R.mipmap.icon_addpic_unfocused));
                if (position == 1) {
                    holder.image.setVisibility(View.GONE);
                }
            } else {
                holder.image.setImageBitmap(Bimp.bmp.get(position));
            }

            return convertView;
        }

        public class ViewHolder {
            public ImageView image;
        }

        Handler handler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        adapter.notifyDataSetChanged();
                        break;
                }
                super.handleMessage(msg);
            }
        };

        public void loading() {
            new Thread(new Runnable() {
                public void run() {
                    while (true) {
                        if (Bimp.max == Bimp.drr.size()) {

                            Message message = new Message();
                            message.what = 1;
                            handler.sendMessage(message);
                            break;
                        } else {
                            try {
                                String path = Bimp.drr.get(Bimp.max);
                                System.out.println(path);
                                Bitmap bm = Bimp.revitionImageSize(path);
                                Bimp.bmp.add(bm);
                                String newStr = path.substring(
                                        path.lastIndexOf("/") + 1,
                                        path.lastIndexOf("."));
                                FileUtils.saveBitmap(bm, "" + newStr);
                                Bimp.max += 1;
                                Message message = new Message();
                                message.what = 1;
                                handler.sendMessage(message);
                            } catch (IOException e) {

                                e.printStackTrace();
                            }
                        }
                    }
                }
            }).start();
        }
    }

    protected void onRestart() {
        adapter.update();
        super.onRestart();
    }



    public void Init() {


        noScrollgridview = (GridView) findViewById(R.id.noScrollgridview);
        noScrollgridview.setSelector(new ColorDrawable(Color.TRANSPARENT));
        adapter = new GridAdapter(this);
        adapter.update();
        noScrollgridview.setAdapter(adapter);
        noScrollgridview.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                if (arg2 == Bimp.bmp.size()) {
                    new PopupWindows(MyShareActivity.this, noScrollgridview);
                } else {
                    Intent intent = new Intent(MyShareActivity.this,
                            PhotoActivity.class);
                    intent.putExtra("ID", arg2);
                    startActivity(intent);
                }
            }
        });
/*	activity_selectimg_send = (TextView) findViewById(R.id.activity_selectimg_send);
	activity_selectimg_send.setOnClickListener(new OnClickListener() {

		public void onClick(View v) {
			List<String> list = new ArrayList<String>();
			for (int i = 0; i < Bimp.drr.size(); i++) {
				String Str = Bimp.drr.get(i).substring(
						Bimp.drr.get(i).lastIndexOf("/") + 1,
						Bimp.drr.get(i).lastIndexOf("."));
				list.add(FileUtils.SDPATH+Str+".JPEG");
			}
			// 高清的压缩图片全部就在  list 路径里面了
			// 高清的压缩过的 bmp 对象  都在 Bimp.bmp里面
			// 完成上传服务器后 .........
			FileUtils.deleteDir();
		}
	});*/
    }

    public class PopupWindows extends PopupWindow {

        public PopupWindows(Context mContext, View parent) {

            View view = View
                    .inflate(mContext, R.layout.item_popupwindows, null);
            view.startAnimation(AnimationUtils.loadAnimation(mContext,
                    R.anim.fade_ins));
            LinearLayout ll_popup = (LinearLayout) view
                    .findViewById(R.id.ll_popup);
            ll_popup.startAnimation(AnimationUtils.loadAnimation(mContext,
                    R.anim.push_bottom_in_2));

            setWidth(LayoutParams.MATCH_PARENT);
            setHeight(LayoutParams.MATCH_PARENT);
            setBackgroundDrawable(new BitmapDrawable());
            setFocusable(true);
            setOutsideTouchable(true);
            setContentView(view);
            showAtLocation(parent, Gravity.BOTTOM, 0, 0);
            update();

            Button bt1 = (Button) view
                    .findViewById(R.id.item_popupwindows_camera);
            Button bt2 = (Button) view
                    .findViewById(R.id.item_popupwindows_Photo);
            Button bt3 = (Button) view
                    .findViewById(R.id.item_popupwindows_cancel);
            bt1.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    photo();
                    dismiss();
                }
            });
            bt2.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    Intent intent=new Intent();
                    intent.setClass(MyShareActivity.this,TestPicActivity.class);
                    intent.putExtra("share_mode", share_mode);
                    intent.putExtra("tofriends", tofriends);
                    startActivity(intent);
                    dismiss();
//				finish();
                }
            });
            bt3.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    dismiss();
                }
            });

        }
    }

    public void photo() {
        Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file = new File(Environment.getExternalStorageDirectory()
                + "/myimage/", String.valueOf(System.currentTimeMillis())
                + ".jpg");
        path = file.getPath();
        Uri imageUri = Uri.fromFile(file);
        openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(openCameraIntent, TAKE_PICTURE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case TAKE_PICTURE:
                if (Bimp.drr.size() < 1 && resultCode == -1) {
                    Bimp.drr.add(path);
                }
                break;
        }
    }
    //上传图片到云端
    public void uploadPic(byte[] i){
        final byte[] img = i;

//	uploadParams.put("x:scope", "youting:"+key);
//	uploadParams.put("x:deadline", ""+3600);
//	uploadParams.put("x:mimeLimit", "image/jpeg");


        //获取token
        StringRequest tokenRequest = new StringRequest(Method.POST,token_url,null,new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                // TODO Auto-generated method stub
                String token = response;
                Log.v(TAG,token);
                //upload
                UploadManager uploadManager = new UploadManager();
                uploadManager.put(img, null, token,
                        new UpCompletionHandler() {
                            @Override
                            public void complete(String arg0, ResponseInfo arg1,
                                                 JSONObject arg2) {
                                // 更新数据库
                                Log.v(TAG,"upload succ"+arg0+"response:"+arg1);
                                Log.v(TAG,"jsonObject:"+arg2.toString());
                                try {
                                    share_pic_url = qiniu_url+arg2.getString("key");
                                } catch (JSONException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                                if(share_mode == share_one_mode){
                                    share_one();
                                }else{
                                    share_all();
                                }


                            }
                        }, new UploadOptions(null, null, false,
                                new UpProgressHandler(){
                                    public void progress(String key, double percent){
                                        int progress = (int)percent*100;

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
    private byte[] Bitmap2Bytes(Bitmap bm){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }
}

package com.example.testvolley;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RemoteControlClient;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.example.activity.PlayerActivity;
import com.example.cacheplayer.HttpGetProxy;
import com.example.cacheplayer.MyPlayer;
import com.example.myview.Bimp;
import com.example.myview.CustomImageView;
import com.example.myview.DragLayout;
import com.example.myview.DragLayout.DragListener;
import com.example.myview.RedPointView;
import com.example.myview.RoundProgressBar;
import com.example.myview.Util;
import com.example.request.JsonArrayRequest;
import com.example.request.JsonObjectRequest;
import com.example.request.StringRequest;
import com.example.service.PlayerService;
import com.example.service.PlayerService.LocalBinder;
import com.example.util.ExitApplication;
import com.fima.glowpadview.GlowPadView;
import com.nineoldandroids.view.ViewHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import cn.jpush.android.api.InstrumentedActivity;
import de.greenrobot.dao.query.QueryBuilder;
import de.greenrobot.daoexample.DaoSession;
import de.greenrobot.daoexample.Music;
import de.greenrobot.daoexample.MusicDao;
import de.greenrobot.daoexample.ShareMessage;
import de.greenrobot.daoexample.ShareMessageDao;
import de.greenrobot.daoexample.UserDao.Properties;

/**
 * Created by Administrator on 2016/5/13.
 */
public class MainActivity extends InstrumentedActivity implements MainActivityCallBack, GlowPadView.OnTriggerListener,
        ViewPager.OnPageChangeListener{

    //侧滑相关
    private DragLayout dl;
    private TextView my_name;
    public ImageView iv_icon;
    public ImageView iv_lover;
    public static boolean LoverMode=false;
    private ImageView search_icon;
    private ImageView message_icon;
    private ImageView share_icon;
    public CustomImageView iv_photo;
    public TextView my_mood;
    private LinearLayout ll_message;
    private LinearLayout ll_friends;
    private LinearLayout ll_lover;
    private  LinearLayout ll_friendShare;
    private RedPointView redPoint_icon;
    private RedPointView redPoint_message;
    private RedPointView redPoint_share;
    private SharedPreferences sp;
    private String[] shareitems = new String[] { "分享给大家", "私信分享" };
    private int[] shareimgIds = {R.mipmap.sharetoall,
            R.mipmap.sharetoone};

    private PlayerService playerservice;
    private ServiceConnection serviceConnection;
    public static boolean isForeground=true;
    private static String TAG = "main";
    private static boolean playFlag = false;
    private int index;
    private int duration;
    private Context context;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private ArrayList<Music> myMusicList,friendMusicList,localMusicList,loverMusicList,loverFriendMusicList;
    private ArrayList<Music> playList;
    private MyApplication application;
    private DaoSession daoSession;
    private MusicDao musicDao;
    private ShareMessageDao shareMessageDao;
    private QueryBuilder qb_music;
    private RemoteControlClient remoteControlClient;
    private RequestQueue mQueue;

    private static final String SESSION_COOKIE = "PHPSESSID";
    private static String CACHE="cache:/sdcard/youting/";
    private static MainActivityCallBack mainActivityCallBack;
    static private final int BUFFER_SIZE= 1024;//Mb cache dir
    static private final int NUM_FILES= 200;//Count files in cache dir
    private HttpGetProxy proxy;
    private String ftplogin = "user", ftppass = "123";
    private String BuffPath = "/youting";
    private MyPlayer myPlayer;

    private View view1, view2, view3;
    private List<View> viewList;// view数组
    private ViewPager viewPager; // 对应的viewPager
    private GlowPadView mGlowPadView;
    private RoundProgressBar mRoundProgressBar1;
    private int progress = 0;
    private int page_temp = 300;

    private TextView song1;
    private TextView singer1;
    private ImageView cover_music1;
    private TextView song2;
    private TextView singer2;
    private ImageView cover_music2;
    private TextView song3;
    private TextView singer3;
    private ImageView cover_music3;
    private Runnable runnable;
    private Thread progressThread;


    private static final String add_my_music = "http://121.42.164.7/index.php/Home/Index/add_my_music";
    private static final String share_all = "http://121.42.164.7/index.php/Home/Index/share_all";
    private static final String get_lover_music = "http://121.42.164.7/index.php/Home/Index/get_lover_music";
    private static final String get_lover_friend_music = "http://121.42.164.7/index.php/Home/Index/get_lover_friend_music";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setContentView(R.layout.activity_main);
        ExitApplication.getInstance().addActivity(this);
        context = this.getApplicationContext();
        mainActivityCallBack = this;
        application = (MyApplication)this.getApplicationContext();
        daoSession = application.getDaoSession(getApplicationContext());
        musicDao = daoSession.getMusicDao();
        shareMessageDao = daoSession.getShareMessageDao();
        remoteControlClient = application.getRemoteControlClient();
        mQueue = application.getRequestQueue();

        myMusicList = application.getMyMusicList();
        friendMusicList = application.getFriendMusicList();
        localMusicList = application.getLocalMusicList();
        playList = application.getPlayingList();

        preferences = getSharedPreferences("youting",MODE_PRIVATE);
        editor = preferences.edit();
        index = preferences.getInt("INDEX", 0);
        // 这里要判断index,如果index>playList.size(),会crash

        Log.v(TAG,"playList"+playList.size()+"myMusicList:"+myMusicList.size());

        viewPager = (ViewPager) findViewById(R.id.mini_music);
        miniControlViewInit();	//mini播放界面初始化
        //New Adding
        Util.initImageLoader(this);
        initDragLayout();

        //绑定service
        serviceConnection=new ServiceConnection() {
            @Override
            public void onServiceDisconnected(ComponentName name) {
            }
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.d("onServiceConnected", "connected");
                playerservice=((LocalBinder)service).getService();
                MyApplication.get().setService(playerservice);
            }
        };

        Intent i=new Intent(this,PlayerService.class);
        bindService(i, serviceConnection, Context.BIND_AUTO_CREATE);

        //更新进度条
        runnable = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        progress = playerservice.getProgress();
                        mRoundProgressBar1.setProgress(progress);
                        Thread.sleep(1000);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        };
        progressThread = new Thread(runnable);

        //圆形进度环
        mRoundProgressBar1 = (RoundProgressBar) findViewById(R.id.roundProgressBar1);

        mRoundProgressBar1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                // if(playerservice.isFirstFlag()) playerservice.setFirstFlag(false);

                if(playList.isEmpty()){
                    Toast.makeText(getApplicationContext(), "未选中任何歌曲", Toast.LENGTH_SHORT).show();
                }else{
                    playerservice.setPlayFlag(!playerservice.isPlayFlag());
                    if(playerservice.isPlayFlag()) {
                        if(playerservice.isFirstFlag()){
                            playerservice.playItems(playerservice.getIndex());
                        }else{
                            playerservice.play();
                        }
                    }
                    else {
                        playerservice.pause();
                    }
                }
            }
        });

        //跳到播放界面
        //	switchToPlayer();
        //大圆环
        mGlowPadView = (GlowPadView) findViewById(R.id.glow_pad_view);
        mGlowPadView.setOnTriggerListener(this);
        mGlowPadView.setShowTargetsOnIdle(true); //空闲状态是否显示target
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        Log.v(TAG,"onResume");
        Log.v(TAG,"reddot:"+application.hasMessage());
        initView();
        if(application.getService()!=null ){
            setPage();

        }
    }

    @Override
    public void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        // 解除playerservice的绑定
        unbindService(serviceConnection);

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        moveTaskToBack(true);
        //	super.onBackPressed();
    }

    //New Adding
    private void initDragLayout() {
        dl = (DragLayout) findViewById(R.id.dl);
        dl.setDragListener(new DragListener() {
            @Override
            public void onOpen() {
                //lv.smoothScrollToPosition(new Random().nextInt(30));
            }

            @Override
            public void onClose() {
                shake();
            }

            @Override
            public void onDrag(float percent) {
                ViewHelper.setAlpha(iv_icon, 1 - percent);
            }
        });
    }

    private void initView() {
        ll_message = (LinearLayout) findViewById(R.id.ll2);
        ll_friendShare = (LinearLayout) findViewById(R.id.ll3);
        ll_friends = (LinearLayout) findViewById(R.id.ll4);
        ll_lover = (LinearLayout) findViewById(R.id.ll5);
        my_name=(TextView) findViewById(R.id.my_name);
        my_mood=(TextView) findViewById(R.id.my_mood);
        iv_icon = (ImageView) findViewById(R.id.iv_icon);
        iv_lover = (ImageView) findViewById(R.id.iv_lover);
        iv_photo = (CustomImageView) findViewById(R.id.iv_photo);
        search_icon = (ImageView) findViewById(R.id.search_icon);
        message_icon = (ImageView) findViewById(R.id.iv_message);
        share_icon = (ImageView) findViewById(R.id.iv_share);


        //给iv_icon绑定消息提示控件
        if(application.hasMessage()){
            Log.v(TAG,"has message");
            redPoint_icon = new RedPointView(this, iv_icon);
            redPoint_icon.setContent(2);
            redPoint_icon.setSizeContent(8);
            redPoint_icon.setColorContent(Color.RED);
            redPoint_icon.setColorBg(Color.RED);
            redPoint_icon.setPosition(Gravity.RIGHT, Gravity.TOP);

            redPoint_icon.show();
        }else{
            if(redPoint_icon != null){
                redPoint_icon.hide();
            }
        }
        if(application.hasSystemMessage()){
            redPoint_message = new RedPointView(this, message_icon);
            redPoint_message.setContent(2);
            redPoint_message.setSizeContent(8);
            redPoint_message.setColorContent(Color.RED);
            redPoint_message.setColorBg(Color.RED);
            redPoint_message.setPosition(Gravity.RIGHT, Gravity.TOP);
            redPoint_message.show();
        }else{
            if(redPoint_message != null){
                redPoint_message.hide();
            }
        }
        if(application.hasMusicMessage()){
            redPoint_share = new RedPointView(this,share_icon);
            redPoint_share.setContent(2);
            redPoint_share.setSizeContent(8);
            redPoint_share.setColorContent(Color.RED);
            redPoint_share.setColorBg(Color.RED);
            redPoint_share.setPosition(Gravity.RIGHT, Gravity.TOP);
            redPoint_share.show();
        }else{
            if(redPoint_share != null){
                redPoint_share.hide();
            }
        }
//		Log.v(TAG,"avatar:"+application.getLoginUser().getAvatar());

        //显示头像
        if(preferences.getBoolean("login", false) && application.getLoginUser()!= null){
            if(!application.getLoginUser().getMood().equals(MyApplication.DEFAULT_MOOD)){

                my_mood.setText(application.getLoginUser().getMood());
            }else{
                my_mood.setText("~暂无心情~");
            }
            my_name.setText(application.getLoginUser().getName());
            if(application.getLoginUser().getAvatar()!= "null"){
                ImageRequest avatarRequest=new ImageRequest(application.getLoginUser().getAvatar(), new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap arg0) {
                        // TODO Auto-generated method stub
                        Log.v("succ","111");
                        iv_icon.setImageBitmap(arg0);
                        iv_photo.setImageBitmap(arg0);
                    }
                }, 150, 150, Config.RGB_565, new ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError arg0) {
                        // TODO Auto-generated method stub
                    }
                });
                mQueue.add(avatarRequest);
            }
        }else{
            Log.v(TAG,"no user bitmap");
            //iv_icon.setImageBitmap(null);
            iv_icon.setImageResource(R.mipmap.img_avatar_default);
            //iv_photo.setImageBitmap(null);
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.img_avatar_default, null);
            iv_photo.setImageBitmap(bitmap);
            //	iv_photo.setImageResource(R.drawable.img_avatar_default);
            my_name.setText("USER");
            my_mood.setText("~暂无心情~");
        }

        //主界面上侧滑图标点击响应
        iv_icon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dl.open();
            }
        });

        //侧滑界面上头像点击响应
        iv_photo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                boolean regstate =preferences.getBoolean("login", false);
                if(regstate==false){
                    //跳转到register界面
                    Intent intent=new Intent(MainActivity.this,MyLoginActivity.class);
                    startActivity(intent);
                    //MainActivity.this.finish();
                }

                if(regstate==true){
                    //跳转到个人资料设置界面
                    Intent intent=new Intent(MainActivity.this,UserInfoSettingActivity.class);
                    startActivity(intent);
                }
            }
        });

        //主界面上搜索图标点击响应
        search_icon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent=new Intent(MainActivity.this,MySearchActivity.class);
                startActivity(intent);
            }
        });

        //心情设置点击响应
        my_mood.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent=new Intent(MainActivity.this,MyMoodActivity.class);
                startActivity(intent);
            }
        });

        //我的消息点击响应
        ll_message.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if(application.isLogin()){
                    Intent intent=new Intent(MainActivity.this,MyMessageActivity.class);
                    startActivity(intent);
                    if(redPoint_message != null){
                        redPoint_message.hide();
                    }
                    if(redPoint_icon != null){
                        redPoint_icon.hide();
                    }
                    application.setMessage(false);
                    application.setSystemMessage(false);

                }else{
                    Toast.makeText(getApplicationContext(), "尚未登录，请先登录", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //我的好友点击响应
        ll_friends.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if(application.isLogin()){
                    Intent intent=new Intent(MainActivity.this,MyFriendsActivity.class);
                    startActivity(intent);
                }else{
                    Toast.makeText(getApplicationContext(), "尚未登录，请先登录", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //好友分享点击响应
        ll_friendShare.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if(application.isLogin()){
                    Intent intent=new Intent(MainActivity.this,MusicMessageActivity.class);
                    startActivity(intent);
                    if(redPoint_share != null){
                        redPoint_share.hide();
                    }
                    if(redPoint_icon != null){
                        redPoint_icon.hide();
                    }

                    application.setMessage(false);
                    application.setMusicMessage(false);

                }else{
                    Toast.makeText(getApplicationContext(), "尚未登录，请先登录", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //情侣模式点击响应
        ll_lover.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if(application.isLogin()){
                    //这里要判断是否有情侣，没有的话要提示添加
                    if(application.isExistLover()){
                        if(LoverMode==false){
                            iv_lover.setImageResource(R.mipmap.ic_lover_activated);
                            LoverMode=true;
                            // set mymusic -> lovermusic
                            setLoverMusic();
                        }
                        else{
                            iv_lover.setImageResource(R.mipmap.ic_lover);
                            LoverMode=false;
                            // set lovermusic -> mymusic
                            application.setMyMusicList(myMusicList);
                            application.setFriendMusicList(friendMusicList);
                        }
                    }
                    else  Toast.makeText(getApplicationContext(), "请先设置您的情侣", Toast.LENGTH_SHORT).show();

                }else{
                    Toast.makeText(getApplicationContext(), "尚未登录，请先登录", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //隐藏侧滑界面时的主界面抖动函数
    private void shake() {
        iv_icon.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public static MainActivityCallBack getMainActivityCallBack() {
        // TODO Auto-generated method stub
        return mainActivityCallBack;
    }
    public static void setMainActivityCallBack(MainActivityCallBack mainActivityCallBack){
        MainActivity.mainActivityCallBack = mainActivityCallBack;
    }

    @Override
    public void setProgress(){
        if (!progressThread.isAlive()&&playerservice.isPlayFlag()){
            progressThread.start();
        }
        MainActivity.this.runOnUiThread(new Runnable(){
            public void run() {
                mRoundProgressBar1.setbackground(playerservice.isPlayFlag());
            }
        });
    }

    //跳到播放界面
    public void switchToPlayer(){
        cover_music1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(playerservice.getMusic() != null){
                    Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
                    startActivity(intent);
                }
            }
        });
        cover_music2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(playerservice.getMusic() != null){
                    Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
                    startActivity(intent);
                }
            }
        });
        cover_music3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(playerservice.getMusic() != null){
                    Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    public void setPage(){
        Log.v(TAG,"setPage");
        int page = viewPager.getCurrentItem();
        if (!playList.isEmpty()){
            switch(page%3){
                case 0:
                    MainActivity.this.runOnUiThread(new Runnable(){
                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            song1.setText(playerservice.getMusic().getName()==""?"未知歌名":playerservice.getMusic().getName());
                            singer1.setText(playerservice.getMusic().getArtist()==""?"未知艺术家":playerservice.getMusic().getArtist());
                            song3.setText(playerservice.getMusic_pre().getName()==""?"未知歌名":playerservice.getMusic_pre().getName());
                            singer3.setText(playerservice.getMusic_pre().getArtist()==""?"未知艺术家":playerservice.getMusic_pre().getArtist());
                            song2.setText(playerservice.getMusic_next().getName()==""?"未知歌名":playerservice.getMusic_next().getName());
                            singer2.setText(playerservice.getMusic_next().getArtist()==""?"未知艺术家":playerservice.getMusic_next().getArtist());
                            if(playerservice.getMusic().getSource() == 1){

                                long id = playerservice.getMusic().getParameter();
                                Bitmap bm = getArtAlbum(id);
                                if(bm!= null){
                                    cover_music1.setImageBitmap(bm);
                                }else{
                                    cover_music1.setImageResource(R.mipmap.music_cover);
                                }
                            }else{
                                ImageRequest imgRequest1=new ImageRequest(playerservice.getMusic().getPic_url(), new Response.Listener<Bitmap>() {
                                    @Override
                                    public void onResponse(Bitmap arg0) {
                                        // TODO Auto-generated method stub
                                        Log.v(TAG,"cover_music1");
                                        cover_music1.setImageBitmap(arg0);

                                    }
                                }, 50, 50, Config.RGB_565, new ErrorListener(){
                                    @Override
                                    public void onErrorResponse(VolleyError arg0) {
                                        // TODO Auto-generated method stub
                                        cover_music1.setImageResource(R.mipmap.music_cover);
                                    }
                                });
                                mQueue.add(imgRequest1);
                            }
                            if(playerservice.getMusic_next().getSource() == 1){
                                long id = playerservice.getMusic_next().getParameter();
                                Bitmap bm = getArtAlbum(id);
                                if(bm!= null){
                                    cover_music2.setImageBitmap(bm);
                                }else{
                                    cover_music2.setImageResource(R.mipmap.music_cover);
                                }
                            }else{
                                ImageRequest imgRequest2=new ImageRequest(playerservice.getMusic_next().getPic_url(), new Response.Listener<Bitmap>() {
                                    @Override
                                    public void onResponse(Bitmap arg0) {
                                        // TODO Auto-generated method stub
                                        Log.v(TAG,"cover_music2");
                                        cover_music2.setImageBitmap(arg0);

                                    }
                                }, 50, 50, Config.RGB_565, new ErrorListener(){
                                    @Override
                                    public void onErrorResponse(VolleyError arg0) {
                                        // TODO Auto-generated method stub
                                        cover_music2.setImageResource(R.mipmap.music_cover);
                                    }
                                });
                                mQueue.add(imgRequest2);
                            }
                            if(playerservice.getMusic_pre().getSource() == 1){
                                long id = playerservice.getMusic_pre().getParameter();
                                Bitmap bm = getArtAlbum(id);
                                if(bm!= null){
                                    cover_music3.setImageBitmap(bm);
                                }else{
                                    cover_music3.setImageResource(R.mipmap.music_cover);
                                }
                            }else{
                                ImageRequest imgRequest3=new ImageRequest(playerservice.getMusic_pre().getPic_url(), new Response.Listener<Bitmap>() {
                                    @Override
                                    public void onResponse(Bitmap arg0) {
                                        // TODO Auto-generated method stub
                                        Log.v(TAG,"cover_music3");
                                        cover_music3.setImageBitmap(arg0);

                                    }
                                }, 50, 50, Config.RGB_565, new ErrorListener(){
                                    @Override
                                    public void onErrorResponse(VolleyError arg0) {
                                        // TODO Auto-generated method stub
                                        cover_music3.setImageResource(R.mipmap.music_cover);
                                    }
                                });
                                mQueue.add(imgRequest3);
                            }
                        }
                    });
                    break;
                case 1:
                    MainActivity.this.runOnUiThread(new Runnable(){
                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            song2.setText(playerservice.getMusic().getName()==""?"未知歌名":playerservice.getMusic().getName());
                            singer2.setText(playerservice.getMusic().getArtist()==""?"未知艺术家":playerservice.getMusic().getArtist());
                            song1.setText(playerservice.getMusic_pre().getName()==""?"未知歌名":playerservice.getMusic_pre().getName());
                            singer1.setText(playerservice.getMusic_pre().getArtist()==""?"未知艺术家":playerservice.getMusic_pre().getArtist());
                            song3.setText(playerservice.getMusic_next().getName()==""?"未知歌名":playerservice.getMusic_next().getName());
                            singer3.setText(playerservice.getMusic_next().getArtist()==""?"未知艺术家":playerservice.getMusic_next().getArtist());
                            if(playerservice.getMusic().getSource() ==1){
                                long id = playerservice.getMusic().getParameter();
                                Bitmap bm = getArtAlbum(id);
                                if(bm!= null){
                                    cover_music2.setImageBitmap(bm);
                                }else{
                                    cover_music2.setImageResource(R.mipmap.music_cover);
                                }

                            }else{
                                ImageRequest imgRequest1=new ImageRequest(playerservice.getMusic().getPic_url(), new Response.Listener<Bitmap>() {
                                    @Override
                                    public void onResponse(Bitmap arg0) {
                                        // TODO Auto-generated method stub
                                        Log.v(TAG,"cover_music1");
                                        cover_music2.setImageBitmap(arg0);

                                    }
                                }, 50, 50, Config.RGB_565, new ErrorListener(){
                                    @Override
                                    public void onErrorResponse(VolleyError arg0) {
                                        // TODO Auto-generated method stub
                                        cover_music2.setImageResource(R.mipmap.music_cover);
                                    }
                                });
                                mQueue.add(imgRequest1);
                            }
                            if(playerservice.getMusic_next().getSource() == 1){
                                long id = playerservice.getMusic_next().getParameter();
                                Bitmap bm = getArtAlbum(id);
                                if(bm!= null){
                                    cover_music3.setImageBitmap(bm);
                                }else{
                                    cover_music3.setImageResource(R.mipmap.music_cover);
                                }
                            }else{
                                ImageRequest imgRequest2=new ImageRequest(playerservice.getMusic_next().getPic_url(), new Response.Listener<Bitmap>() {
                                    @Override
                                    public void onResponse(Bitmap arg0) {
                                        // TODO Auto-generated method stub
                                        Log.v(TAG,"cover_music2");
                                        cover_music3.setImageBitmap(arg0);

                                    }
                                }, 50, 50, Config.RGB_565, new ErrorListener(){
                                    @Override
                                    public void onErrorResponse(VolleyError arg0) {
                                        // TODO Auto-generated method stub
                                        cover_music3.setImageResource(R.mipmap.music_cover);
                                    }
                                });
                                mQueue.add(imgRequest2);
                            }
                            if(playerservice.getMusic_pre().getSource() == 1){
                                long id = playerservice.getMusic_pre().getParameter();
                                Bitmap bm = getArtAlbum(id);
                                if(bm!= null){
                                    cover_music1.setImageBitmap(bm);
                                }else{
                                    cover_music1.setImageResource(R.mipmap.music_cover);
                                }
                            }else{
                                ImageRequest imgRequest3=new ImageRequest(playerservice.getMusic_pre().getPic_url(), new Response.Listener<Bitmap>() {
                                    @Override
                                    public void onResponse(Bitmap arg0) {
                                        // TODO Auto-generated method stub
                                        Log.v(TAG,"cover_music3");
                                        cover_music1.setImageBitmap(arg0);

                                    }
                                }, 50, 50, Config.RGB_565, new ErrorListener(){
                                    @Override
                                    public void onErrorResponse(VolleyError arg0) {
                                        // TODO Auto-generated method stub
                                        cover_music1.setImageResource(R.mipmap.music_cover);
                                    }
                                });
                                mQueue.add(imgRequest3);
                            }
                        }

                    });
                    break;
                case 2:
                    MainActivity.this.runOnUiThread(new Runnable(){

                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            song3.setText(playerservice.getMusic().getName()==""?"未知歌名":playerservice.getMusic().getName());
                            singer3.setText(playerservice.getMusic().getArtist()==""?"未知艺术家":playerservice.getMusic().getArtist());
                            song2.setText(playerservice.getMusic_pre().getName()==""?"未知歌名":playerservice.getMusic_pre().getName());
                            singer2.setText(playerservice.getMusic_pre().getArtist()==""?"未知艺术家":playerservice.getMusic_pre().getArtist());
                            song1.setText(playerservice.getMusic_next().getName()==""?"未知歌名":playerservice.getMusic_next().getName());
                            singer1.setText(playerservice.getMusic_next().getArtist()==""?"未知艺术家":playerservice.getMusic_next().getArtist());
                            if(playerservice.getMusic().getSource() ==1){
                                long id = playerservice.getMusic().getParameter();
                                Bitmap bm = getArtAlbum(id);
                                if(bm!= null){
                                    cover_music3.setImageBitmap(bm);
                                }else{
                                    cover_music3.setImageResource(R.mipmap.music_cover);
                                }

                            }else{
                                ImageRequest imgRequest1=new ImageRequest(playerservice.getMusic().getPic_url(), new Response.Listener<Bitmap>() {
                                    @Override
                                    public void onResponse(Bitmap arg0) {
                                        // TODO Auto-generated method stub
                                        Log.v(TAG,"cover_music1");
                                        cover_music3.setImageBitmap(arg0);

                                    }
                                }, 50, 50, Config.RGB_565, new ErrorListener(){
                                    @Override
                                    public void onErrorResponse(VolleyError arg0) {
                                        // TODO Auto-generated method stub
                                        cover_music3.setImageResource(R.mipmap.music_cover);
                                    }
                                });
                                mQueue.add(imgRequest1);
                            }
                            if(playerservice.getMusic_next().getSource() == 1){
                                long id = playerservice.getMusic_next().getParameter();
                                Bitmap bm = getArtAlbum(id);
                                if(bm!= null){
                                    cover_music1.setImageBitmap(bm);
                                }else{
                                    cover_music1.setImageResource(R.mipmap.music_cover);
                                }
                            }else{
                                ImageRequest imgRequest2=new ImageRequest(playerservice.getMusic_next().getPic_url(), new Response.Listener<Bitmap>() {
                                    @Override
                                    public void onResponse(Bitmap arg0) {
                                        // TODO Auto-generated method stub
                                        Log.v(TAG,"cover_music2");
                                        cover_music1.setImageBitmap(arg0);

                                    }
                                }, 50, 50, Config.RGB_565, new ErrorListener(){
                                    @Override
                                    public void onErrorResponse(VolleyError arg0) {
                                        // TODO Auto-generated method stub
                                        cover_music1.setImageResource(R.mipmap.music_cover);
                                    }
                                });
                                mQueue.add(imgRequest2);
                            }
                            if(playerservice.getMusic_pre().getSource() == 1){
                                long id = playerservice.getMusic_pre().getParameter();
                                Bitmap bm = getArtAlbum(id);
                                if(bm!= null){
                                    cover_music2.setImageBitmap(bm);
                                }else{
                                    cover_music2.setImageResource(R.mipmap.music_cover);
                                }
                            }else{
                                ImageRequest imgRequest3=new ImageRequest(playerservice.getMusic_pre().getPic_url(), new Response.Listener<Bitmap>() {
                                    @Override
                                    public void onResponse(Bitmap arg0) {
                                        // TODO Auto-generated method stub
                                        Log.v(TAG,"cover_music3");
                                        cover_music2.setImageBitmap(arg0);
                                    }
                                }, 50, 50, Config.RGB_565, new ErrorListener(){
                                    @Override
                                    public void onErrorResponse(VolleyError arg0) {
                                        // TODO Auto-generated method stub
                                        cover_music2.setImageResource(R.mipmap.music_cover);
                                    }
                                });
                                mQueue.add(imgRequest3);
                            }
                        }

                    });
                    break;
            }
        } else{
            song1.setText("No Song");
            singer1.setText("No Singer");
            song2.setText("No Song");
            singer2.setText("No Singer");
            song3.setText("No Song");
            singer3.setText("No Singer");
            cover_music1.setImageResource(R.mipmap.music_cover);
            cover_music2.setImageResource(R.mipmap.music_cover);
            cover_music3.setImageResource(R.mipmap.music_cover);
        }
    }

    @Override
    public void initCallBack(){
        myMusicList = application.getMyMusicList();
        friendMusicList = application.getFriendMusicList();
        index = preferences.getInt("INDEX", 0);
        if( preferences.getInt("PLAYLIST", 0)==0){
            playList = (ArrayList<Music>)myMusicList.clone();
        }else if(preferences.getInt("PLAYLIST", 0) == 1){
            playList = (ArrayList<Music>)friendMusicList.clone();
        }else{
            playList = (ArrayList<Music>)localMusicList.clone();;
        }
        playerservice.setPlayList(playList);
        Log.v(TAG,"playList"+playList.size()+"myMusicList:"+myMusicList.size());
        miniControlViewInit();
    }

    /**
     * New Adding 显示选择对话框
     */
    public void showShareDialog() {
        final AlertDialog ald = new AlertDialog.Builder(this).create();
        ald.show();
        ald.getWindow().setContentView(R.layout.sharedialog);
        ald.getWindow()
                .findViewById(R.id.ll_sharetoall)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(playerservice.getMusic().getSource() != 1){
                            Bimp.max=0;
                            Bimp.act_bool=true;
                            Bimp.bmp.clear();
                            Bimp.drr.clear();
                            String artist=playerservice.getMusic().getArtist();
                            String name=playerservice.getMusic().getName();
                            String pic_url=playerservice.getMusic().getPic_url();
                            long music_id = playerservice.getMusic().getUid();
                            Intent intent=new Intent();
                            intent.setClass(MainActivity.this,MyShareActivity.class);
                            intent.putExtra("share_mode", 0);
                            intent.putExtra("tofriends", "所有人");
                            intent.putExtra("artist", artist);
                            intent.putExtra("name", name);
                            intent.putExtra("pic_url",pic_url);
                            intent.putExtra("music_id",music_id);
                            Log.v(TAG,"share_all:"+music_id);
                            startActivity(intent);
                            ald.dismiss();
                        }
                    }
                });

        ald.getWindow()
                .findViewById(R.id.ll_sharetoone)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(playerservice.getMusic().getSource() != 1){
                            Bimp.max=0;
                            Bimp.act_bool=true;
                            Bimp.bmp.clear();
                            Bimp.drr.clear();
                            String artist=playerservice.getMusic().getArtist();
                            String name=playerservice.getMusic().getName();
                            String pic_url=playerservice.getMusic().getPic_url();
                            long music_id = playerservice.getMusic().getUid();
                            Intent intent=new Intent();
                            intent.setClass(MainActivity.this,FriendSelectActivity.class);
                            intent.putExtra("name", name);
                            intent.putExtra("artist", artist);
                            intent.putExtra("pic_url", pic_url);
                            intent.putExtra("music_id",music_id);
                            startActivity(intent);
                            ald.dismiss();
                        }
                    }
                });

    }

    class ListItemAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return shareimgIds.length;
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
        public View getView(int position,
                            View contentView, ViewGroup parent) {
            TextView textView =
                    new TextView(MainActivity.this);
            //获得array.xml中的数组资源getStringArray返回的是一个String数组
            String text = shareitems[position];
            textView.setText(text);
            //设置字体大小
            textView.setTextSize(24);
            AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(
                    LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
            textView.setLayoutParams(layoutParams);
            //设置水平方向上居中
            textView.setGravity(android.view.Gravity.CENTER_VERTICAL);
            textView.setMinHeight(65);
            //设置文字颜色
            textView.setTextColor(Color.BLACK);
            //设置图标在文字的左边
            textView.setCompoundDrawablesWithIntrinsicBounds(shareimgIds[position], 0, 0, 0);
            //设置textView的左上右下的padding大小
            textView.setPadding(15, 0, 15, 0);
            //设置文字和图标之间的padding大小
            textView.setCompoundDrawablePadding(15);
            return textView;
        }
    }

    @Override
    public void setUserInfo(){
        MainActivity.this.runOnUiThread(new Runnable(){
            @Override
            public void run() {
                // TODO Auto-generated method stub
                initView();
            }
        });
    }

    @Override
    //显示Notification
    @SuppressLint("NewApi")
    public NotificationManager showCustomView() {

        playList = playerservice.getPlayList();
        index = playerservice.getIndex();
        final RemoteViews remoteViews = new RemoteViews(getPackageName(),
                R.layout.music_notification);
        final RemoteViews remoteViewsNormal = new RemoteViews(getPackageName(),
                R.layout.music_notification_normal);
        remoteViews.setTextViewText(R.id.notification_song_name, playList.get(index).getName()==""?"未知歌名":playList.get(index).getName()); //设置textview
        remoteViewsNormal.setTextViewText(R.id.notification_song_name, playList.get(index).getName()==""?"未知歌名":playList.get(index).getName());
        remoteViews.setTextViewText(R.id.notification_singer_name,playList.get(index).getArtist()==""?"未知艺术家":playList.get(index).getArtist());
        remoteViewsNormal.setTextViewText(R.id.notification_singer_name,playList.get(index).getArtist()==""?"未知艺术家":playList.get(index).getArtist());

        if(playList.get(index).getSource() == 1){
            long id = playList.get(index).getParameter();

            Bitmap bm = getArtAlbum(id);
            if(bm!= null){
                remoteViews.setImageViewBitmap(R.id.notification_singer_pic, bm);
                remoteViewsNormal.setImageViewBitmap(R.id.notification_singer_pic, bm);
            }else{
                remoteViews.setImageViewResource(R.id.notification_singer_pic,R.mipmap.music_cover);
                remoteViewsNormal.setImageViewResource(R.id.notification_singer_pic,R.mipmap.music_cover);
            }
        }else{
            ImageRequest imgRequest=new ImageRequest(playList.get(index).getPic_url(), new Response.Listener<Bitmap>() {
                @Override
                public void onResponse(Bitmap arg0) {
                    // TODO Auto-generated method stub
                    Log.v(TAG+"notification","onresponse");
                    remoteViews.setImageViewBitmap(R.id.notification_singer_pic, arg0);
                    remoteViewsNormal.setImageViewBitmap(R.id.notification_singer_pic, arg0);
                    application.getNotificationManager().notify(1, application.getNotification());

                }
            }, 50, 50, Config.RGB_565, new ErrorListener(){
                @Override
                public void onErrorResponse(VolleyError arg0) {
                    // TODO Auto-generated method stub
                    Log.v(TAG,"onerror");
                    remoteViews.setImageViewResource(R.id.notification_singer_pic,R.mipmap.music_cover);
                    remoteViewsNormal.setImageViewResource(R.id.notification_singer_pic,R.mipmap.music_cover);
                }
            });
            mQueue.add(imgRequest);
        }

        Builder builder = new Builder(MainActivity.this);
        builder.setContent(remoteViews).setSmallIcon(R.mipmap.ic_launcher)
                .setOngoing(true)
                .setTicker("You Ting");
        Notification notification=builder.build();
        MyApplication.get().setNotification(notification);

        notification.contentView=remoteViewsNormal;
        notification.bigContentView = remoteViews;
        NotificationManager manager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1, notification);
        Log.v(TAG,"notification");
        return manager;
    }

    @Override
    public void onGrabbed(View v, int handle) {

    }

    @Override
    public void onReleased(View v, int handle) {
        Toast.makeText(this, "中间按钮按下", Toast.LENGTH_SHORT).show();
    }

    public void onTrigger(View v, int target) {
        final int resId = mGlowPadView.getResourceIdForTarget(target);
        switch (resId) {
            case R.drawable.ic_item_share:
                //New Adding
                //boolean state=true;
                if(playerservice.getMusic() != null){
                    shareSong(playerservice.getMusic());
                }
                break;

            case R.drawable.ic_item_collect:
                if(application.getMyMusicList().isEmpty()){
                    Toast.makeText(getApplicationContext(), "列表为空", Toast.LENGTH_SHORT).show();

                }else{

                    playerservice.setPlayList(application.getMyMusicList());
                    playerservice.playItems(0);
                    playList = application.getPlayingList();
                    if(playerservice.isFirstFlag()){
                        MyApplication.get().setNotificationManager(showCustomView());
                    }
                    setPage();
                }
                break;
            case R.drawable.ic_item_like:
                Log.v(TAG,"add_my_music");
                if(playerservice.getMusic() != null){
                    add_my_music(playerservice.getMusic());
                }
                break;
            case R.drawable.ic_item_push:
                if(application.getFriendMusicList().isEmpty()){
                    Toast.makeText(getApplicationContext(), "列表为空", Toast.LENGTH_SHORT).show();
                }else{
                    //这几行代码很可疑
                    playerservice.setPlayList(application.getFriendMusicList());
                    playerservice.playItems(0);
                    playList = application.getPlayingList();
                    if(playerservice.isFirstFlag()){
                        MyApplication.get().setNotificationManager(showCustomView());
                    }
                    setPage();
                }
                break;
            default:
                // Code should never reach here.
        }
    }

    @Override
    public void onGrabbedStateChange(View v, int handle) {

    }

    @Override
    public void onFinishFinalAnimation() {

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    //滑动切歌
    @Override
    public void onPageSelected(int page) {
        Log.v(TAG,"page:"+page);
        if(!playList.isEmpty()){
            if(page<page_temp){//向左滑，播放前一首
                playerservice.playPrevious();

                final int viewNum=(page-1)%3+1;
                if(playerservice.getMusic_pre().getSource() == 1){
                    long id = playerservice.getMusic_pre().getParameter();
                    Bitmap bm = getArtAlbum(id);
                    if(bm!= null){
                        switch(viewNum){
                            case 1:
                                cover_music1.setImageBitmap(bm);
                                break;
                            case 2:
                                cover_music2.setImageBitmap(bm);
                                break;
                            case 3:
                                cover_music3.setImageBitmap(bm);
                                break;
                        }
                    } else {
                        switch(viewNum){
                            case 1:
                                cover_music1.setImageResource(R.mipmap.music_cover);
                                break;
                            case 2:
                                cover_music2.setImageResource(R.mipmap.music_cover);
                                break;
                            case 3:
                                cover_music3.setImageResource(R.mipmap.music_cover);
                                break;
                        }
                    }
                }else{
                    ImageRequest imgRequest=new ImageRequest(playerservice.getMusic_pre().getPic_url(), new Response.Listener<Bitmap>() {
                        @Override
                        public void onResponse(Bitmap arg0) {
                            // TODO Auto-generated method stub
                            Log.v("succ","111");
                            switch(viewNum){
                                case 1:
                                    cover_music1.setImageBitmap(arg0);
                                    break;
                                case 2:
                                    cover_music2.setImageBitmap(arg0);
                                    break;
                                case 3:
                                    cover_music3.setImageBitmap(arg0);
                                    break;
                            }
                        }
                    }, 50, 50, Config.RGB_565, new ErrorListener(){
                        @Override
                        public void onErrorResponse(VolleyError arg0) {
                            // TODO Auto-generated method stub
                        }
                    });
                    mQueue.add(imgRequest);
                }

                switch (viewNum){
                    case 1:
                        song1.setText(playerservice.getMusic_pre().getName()==""?"未知歌名":playerservice.getMusic_pre().getName());
                        singer1.setText( playerservice.getMusic_pre().getArtist()==""?"未知艺术家":playerservice.getMusic_pre().getArtist());
                        break;
                    case 2:
                        song2.setText(playerservice.getMusic_pre().getName()==""?"未知歌名":playerservice.getMusic_pre().getName());
                        singer2.setText( playerservice.getMusic_pre().getArtist()==""?"未知艺术家":playerservice.getMusic_pre().getArtist());
                        break;
                    case 3:
                        song3.setText(playerservice.getMusic_pre().getName()==""?"未知歌名":playerservice.getMusic_pre().getName());
                        singer3.setText( playerservice.getMusic_pre().getArtist()==""?"未知艺术家":playerservice.getMusic_pre().getArtist());
                        break;
                    default:
                }

            } else if(page>page_temp) {	//向右滑，播放下一首
                final int viewNum=(page+1)%3+1;
                playerservice.playNext();
                Log.v(TAG,"viewNum"+viewNum);
                Log.v(TAG+"islocal",playerservice.getMusic_pre().getName()+playerservice.getMusic_pre().getSource());
                if(playerservice.getMusic_next().getSource() == 1){
                    long id = playerservice.getMusic_next().getParameter();
                    Bitmap bm = getArtAlbum(id);
                    if(bm!= null){
                        switch(viewNum){
                            case 1:
                                cover_music1.setImageBitmap(bm);
                                break;
                            case 2:
                                cover_music2.setImageBitmap(bm);
                                break;
                            case 3:
                                cover_music3.setImageBitmap(bm);
                                break;
                        }
                    }else{
                        switch(viewNum){
                            case 1:
                                cover_music1.setImageResource(R.mipmap.music_cover);
                                break;
                            case 2:
                                cover_music2.setImageResource(R.mipmap.music_cover);
                                break;
                            case 3:
                                cover_music3.setImageResource(R.mipmap.music_cover);
                                break;
                        }
                    }
                }else{
                    ImageRequest imgRequest=new ImageRequest(playerservice.getMusic_next().getPic_url(), new Response.Listener<Bitmap>() {
                        @Override
                        public void onResponse(Bitmap arg0) {
                            // TODO Auto-generated method stub
                            Log.v("succ","111");
                            switch(viewNum){
                                case 1:
                                    cover_music1.setImageBitmap(arg0);
                                    break;
                                case 2:
                                    cover_music2.setImageBitmap(arg0);
                                    break;
                                case 3:
                                    cover_music3.setImageBitmap(arg0);
                                    break;
                            }

                        }
                    }, 50, 50, Config.RGB_565, new ErrorListener(){
                        @Override
                        public void onErrorResponse(VolleyError arg0) {
                            // TODO Auto-generated method stub
                        }
                    });
                    mQueue.add(imgRequest);
                }
                switch (viewNum){
                    case 1:
                        song1.setText(playerservice.getMusic_next().getName()==""?"未知歌名":playerservice.getMusic_next().getName());
                        singer1.setText(playerservice.getMusic_next().getArtist()==""?"未知艺术家":playerservice.getMusic_next().getArtist());

                        break;
                    case 2:
                        song2.setText(playerservice.getMusic_next().getName()==""?"未知歌名":playerservice.getMusic_next().getName());
                        singer2.setText(playerservice.getMusic_next().getArtist()==""?"未知艺术家":playerservice.getMusic_next().getArtist());
                        break;
                    case 3:
                        song3.setText(playerservice.getMusic_next().getName()==""?"未知歌名":playerservice.getMusic_next().getName());
                        singer3.setText(playerservice.getMusic_next().getArtist()==""?"未知艺术家":playerservice.getMusic_next().getArtist());
                        break;
                    default:
                }
            }
            page_temp=page;
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    //mini播放器初始化
    private void miniControlViewInit(){
        Log.v(TAG,"mini Init");
        LayoutInflater inflater = getLayoutInflater();

        view1 = inflater.inflate(R.layout.layout1, null);
        view2 = inflater.inflate(R.layout.layout2, null);
        view3 = inflater.inflate(R.layout.layout3, null);

        song1 = (TextView) view1.findViewById(R.id.song_name);
        singer1 = (TextView) view1.findViewById(R.id.singer_name);
        cover_music1= (ImageView) view1.findViewById(R.id.cover_music);

        song2 = (TextView) view2.findViewById(R.id.song_name);
        singer2= (TextView) view2.findViewById(R.id.singer_name);
        cover_music2= (ImageView) view2.findViewById(R.id.cover_music);

        song3 = (TextView) view3.findViewById(R.id.song_name);
        singer3= (TextView) view3.findViewById(R.id.singer_name);
        cover_music3= (ImageView) view3.findViewById(R.id.cover_music);
        if (!playList.isEmpty()){
            song1.setText(playList.get(index).getName()==""?"未知歌名":playList.get(index).getName());
            singer1.setText(playList.get(index).getArtist()==""?"未知艺术家":playList.get(index).getArtist());
            int index_next = (index==(playList.size()-1))?0:index+1;
            int index_pre = (index==0)?playList.size()-1:index-1;
            song3.setText(playList.get(index_pre).getName()==""?"未知歌名":playList.get(index_pre).getName());
            singer3.setText(playList.get(index_pre).getArtist()==""?"未知艺术家":playList.get(index_pre).getArtist());
            song2.setText(playList.get(index_next).getName()==""?"未知歌名":playList.get(index_next).getName());
            singer2.setText(playList.get(index_next).getArtist()==""?"未知艺术家":playList.get(index_next).getArtist());
            if(playList.get(index).getSource() == 1){
                long id = playList.get(index).getParameter();
                Bitmap bm = getArtAlbum(id);
                if(bm!= null){
                    cover_music1.setImageBitmap(bm);
                }else{
                    cover_music1.setImageResource(R.mipmap.music_cover);
                }
            }else{
                ImageRequest imgRequest1=new ImageRequest(playList.get(index).getPic_url(), new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap arg0) {
                        // TODO Auto-generated method stub
                        Log.v("succ","111");
                        cover_music1.setImageBitmap(arg0);
                    }
                }, 50, 50, Config.RGB_565, new ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError arg0) {
                        // TODO Auto-generated method stub
                        cover_music1.setImageResource(R.mipmap.music_cover);
                    }
                });
                mQueue.add(imgRequest1);
            }
            if(playList.get(index_next).getSource() == 1){
                long id = playList.get(index_next).getParameter();
                Bitmap bm = getArtAlbum(id);
                if(bm!= null){
                    cover_music2.setImageBitmap(bm);
                }else{
                    cover_music2.setImageResource(R.mipmap.music_cover);
                }
            }else{
                ImageRequest imgRequest2=new ImageRequest(playList.get(index_next).getPic_url(), new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap arg0) {
                        // TODO Auto-generated method stub
                        Log.v("succ","111");
                        cover_music2.setImageBitmap(arg0);
                    }
                }, 50, 50, Config.RGB_565, new ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError arg0) {
                        // TODO Auto-generated method stub
                        cover_music2.setImageResource(R.mipmap.music_cover);
                    }
                });
                mQueue.add(imgRequest2);
            }
            if(playList.get(index_pre).getSource() == 1){
                long id = playList.get(index_pre).getParameter();
                Bitmap bm = getArtAlbum(id);
                if(bm!= null){
                    cover_music3.setImageBitmap(bm);
                }else{
                    cover_music3.setImageResource(R.mipmap.music_cover);
                }
            }else{
                ImageRequest imgRequest3=new ImageRequest(playList.get(index_pre).getPic_url(), new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap arg0) {
                        // TODO Auto-generated method stub
                        Log.v("succ","111");
                        cover_music3.setImageBitmap(arg0);
                    }
                }, 50, 50, Config.RGB_565, new ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError arg0) {
                        // TODO Auto-generated method stub
                        cover_music3.setImageResource(R.mipmap.music_cover);
                    }
                });
                mQueue.add(imgRequest3);
            }
        }
        //播放列表为空
        else{
            song1.setText("No Song");
            singer1.setText("No Singer");
            song2.setText("No Song");
            singer2.setText("No Singer");
            song3.setText("No Song");
            singer3.setText("No Singer");
            cover_music1.setImageResource(R.mipmap.music_cover);
            cover_music2.setImageResource(R.mipmap.music_cover);
            cover_music3.setImageResource(R.mipmap.music_cover);
        }

        viewList = new ArrayList<View>();// 将要分页显示的View装入数组中
        viewList.add(view1);
        viewList.add(view2);
        viewList.add(view3);

        PagerAdapter pagerAdapter = new PagerAdapter() {
            @Override
            public boolean isViewFromObject(View arg0, Object arg1) {
                // TODO Auto-generated method stub
                return arg0 == arg1;
            }

            @Override
            public int getCount() {
                // TODO Auto-generated method stub
                //return viewList.size();
                return Integer.MAX_VALUE;
            }

            @Override
            public void destroyItem(ViewGroup container, int position,
                                    Object object) {
                // TODO Auto-generated method stub

            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                // TODO Auto-generated method stub
                try {
                    container.addView(viewList.get(position%3),0);
                }catch(Exception e){
                    //handler something
                }
                return viewList.get(position%3);
            }
        };

        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(300);
        page_temp=300;
        viewPager.setOnPageChangeListener(this);
        switchToPlayer();
    }

    public Bitmap getArtAlbum(long audioId) {
        String str = "content://media/external/audio/media/" + audioId
                + "/albumart";
        Uri uri = Uri.parse(str);

        ParcelFileDescriptor pfd = null;
        try {
            pfd = this.getContentResolver().openFileDescriptor(uri, "r");
        } catch (FileNotFoundException e) {
            return null;
        }
        Bitmap bm;
        if (pfd != null) {
            FileDescriptor fd = pfd.getFileDescriptor();
            bm = BitmapFactory.decodeFileDescriptor(fd);
            return bm;
        }
        return null;
    }

    public void shareSong(Music m){
        long uid = m.getUid();
        if(uid == 0){
            Toast.makeText(getApplicationContext(),"本地音乐无法分享", Toast.LENGTH_SHORT).show();
        }else{
            JSONObject jObject = new JSONObject();
            try {
                jObject.put("music_id", uid);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            JsonObjectRequest shareRequest = new JsonObjectRequest(Method.POST,share_all,jObject,null,new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    // TODO Auto-generated method stub
                    Toast.makeText(getApplicationContext(),"分享成功", Toast.LENGTH_SHORT).show();
                }
            },new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    // TODO Auto-generated method stub

                }
            });
            mQueue.add(shareRequest);
        }
    }

    public void add_my_music(final Music m){
        long uid = m.getUid();
        if(uid == 0){
            Toast.makeText(getApplicationContext(),"本地音乐无法分享", Toast.LENGTH_SHORT).show();
        }else{
            if(!isExistInMyfavoriteList(m)){
                String add_music_url = add_my_music+"?music_id="+uid;
                StringRequest addRequest= new StringRequest(Method.GET,add_music_url,null,new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        // TODO Auto-generated method stub
                        Music music = new Music(m);
                        music.setSource(0);
                        music.setParameter((long)0);
                        application.addToMyMusicList(music);
                        Toast.makeText(getApplicationContext(),"添加成功", Toast.LENGTH_SHORT).show();
                    }
                },new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub

                    }
                });
                mQueue.add(addRequest);
            }else{
                Toast.makeText(getApplicationContext(),"歌曲已存在在列表中", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void setLoverMusic(){

        Log.v(TAG+"setLoverMusic","lovermusic");
        JsonArrayRequest lovermusicRequest = new JsonArrayRequest(get_lover_music, null,new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                // TODO Auto-generated method stub
                Log.v(TAG+"setLoverMusic", response.toString());
                if(response.length() == 0){

                }
                loverMusicList = new ArrayList<Music>();
                for(int i=0;i<response.length();i++){
                    String s = null;
                    try {
                        s = ( response.getJSONObject(i)).getString("uid");
                        long uid = Long.parseLong(s);
                        String  name = (response.getJSONObject(i)).getString("name");
                        String artist = (response.getJSONObject(i)).getString("artist");
                        String url = (response.getJSONObject(i)).getString("url");
                        String lrc_url = (response.getJSONObject(i)).getString("lrc_url");
                        String pic_url = (response.getJSONObject(i)).getString("pic_url");
                        // 取出数据保存在手机数据库中
                        musicDao = daoSession.getMusicDao();
                        //userDao.deleteAll();
                        Music music = new Music(uid,name,artist,null,url,lrc_url,null,pic_url,0,(long)0);
                        qb_music = musicDao.queryBuilder();
                        qb_music.where(Properties.Uid.eq(uid));
                        long count = qb_music.buildCount().count();
                        Log.v(TAG,url);
                        if (count > 0){
                            Music music_tmp = (Music) qb_music.unique();
                            String lrc_cache_url = music_tmp.getLrc_cache_url();
                            music = new Music(uid,name,artist,null,url,lrc_url,lrc_cache_url,pic_url,0,(long)0);
                            musicDao.update(music);
                            Log.v(TAG,"update");
                        }else{

                            musicDao.insert(music);
                        }
                        loverMusicList.add(music);
                        Log.v(TAG+"dao","inset new"+music.getUid());
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }


                }
                editor.putString("LOVER_MUSIC", response.toString());
                editor.commit();
                Log.v(TAG+"lovermusic","lovermusic");
                application.setMyMusicList(loverMusicList);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                String sb = volleyError.toString();
                Log.v(TAG+"setLoverMusic",sb);
                loverMusicList = new ArrayList<Music>();
                String tmp = preferences.getString("LOVER_MUSIC", null);
                if (tmp != null){
                    JSONArray response = new JSONArray();
                    try {
                        response = new JSONArray(tmp);
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    for(int i=0;i<response.length();i++){
                        String s = null;
                        try {
                            s = ( response.getJSONObject(i)).getString("uid");
                            long uid = Long.parseLong(s);
                            String  name = (response.getJSONObject(i)).getString("name");
                            String artist = (response.getJSONObject(i)).getString("artist");
                            String url = (response.getJSONObject(i)).getString("url");
                            String lrc_url = (response.getJSONObject(i)).getString("lrc_url");
                            String pic_url = (response.getJSONObject(i)).getString("pic_url");
                            Music music = new Music(uid,name,artist,null,url,lrc_url,null,pic_url,0,(long)0);
                            loverMusicList.add(music);
                            Log.v(TAG+"dao","inset new"+music.getUid());
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
                application.setMyMusicList(loverMusicList);
            }
        });
        JsonArrayRequest loverfriendmusicRequest = new JsonArrayRequest(get_lover_friend_music,null,new Response.Listener<JSONArray>(){

            @Override
            public void onResponse(JSONArray response) {
                // TODO Auto-generated method stub
                Log.v(TAG+"friendMusic",response.toString());
                loverFriendMusicList = new ArrayList<Music>();
                // TODO Auto-generated method stub
                if(response.length() == 0){

                }
                for(int i=0;i<response.length();i++){
                    String s = null;
                    try {
                        s = (response.getJSONObject(i)).getString("music_id");
                        long uid = Long.parseLong(s);
                        String  name = (response.getJSONObject(i)).getString("name");
                        String artist = (response.getJSONObject(i)).getString("artist");
                        String url = (response.getJSONObject(i)).getString("url");
                        String lrc_url = (response.getJSONObject(i)).getString("lrc_url");
                        String pic_url = (response.getJSONObject(i)).getString("pic_url");
//						long friend_music_id = Long.parseLong((response.getJSONObject(i)).getString("uid"));
                        long sender_id = Long.parseLong((response.getJSONObject(i)).getString("sender_id"));
                        String message = (response.getJSONObject(i)).getString("message");
                        String share_pic = (response.getJSONObject(i)).getString("share_pic");
                        //将分享消息保存到shareMessage
                        long user_id = application.getLoginUser().getUid();
                        ShareMessage shareMessage = new ShareMessage(null,user_id,sender_id,message,share_pic);
                        long shareMessage_id = shareMessageDao.insert(shareMessage);
                        // 取出数据保存在手机数据库中
                        musicDao = daoSession.getMusicDao();
                        //userDao.deleteAll();
                        Music music = new Music(uid,name,artist,null,url,lrc_url,null,pic_url,2,shareMessage_id);
                        qb_music = musicDao.queryBuilder();
                        qb_music.where(Properties.Uid.eq(uid));
                        long count = qb_music.buildCount().count();
                        Log.v(TAG,url);
                        if (count > 0){
                            Music music_tmp = (Music) qb_music.unique();
                            String lrc_cache_url = music_tmp.getLrc_cache_url();
                            music = new Music(uid,name,artist,null,url,lrc_url,lrc_cache_url,pic_url,2,shareMessage_id);
                            musicDao.update(music);
                            Log.v(TAG+"friendMusic","update");
                        }else{

                            musicDao.insert(music);
                        }
                        loverFriendMusicList.add(music);
                        Log.v(TAG+"dao","inset new"+music.getUid());
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }


                }
                editor.putString("LOVER_FRIEND_MUSIC", response.toString());
                editor.commit();
                Log.v(TAG+"friendmusic","friendmusic");
                application.setFriendMusicList(loverFriendMusicList);
            }

        },new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError volleyError) {
                // TODO Auto-generated method stub
                String sb = volleyError.toString();
                Log.v(TAG+"friendmusic",sb);
                loverFriendMusicList = new ArrayList<Music>();
                String tmp = preferences.getString("LOVER_FRIEND_MUSIC", null);
                if (tmp != null){
                    JSONArray response = new JSONArray();
                    try {
                        response = new JSONArray(tmp);
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    for(int i=0;i<response.length();i++){
                        String s = null;
                        try {
                            s = (response.getJSONObject(i)).getString("music_id");
                            long uid = Long.parseLong(s);
                            String  name = (response.getJSONObject(i)).getString("name");
                            String artist = (response.getJSONObject(i)).getString("artist");
                            String url = (response.getJSONObject(i)).getString("url");
                            String lrc_url = (response.getJSONObject(i)).getString("lrc_url");
                            String pic_url = (response.getJSONObject(i)).getString("pic_url");
                            long friend_music_id = Long.parseLong((response.getJSONObject(i)).getString("uid"));
                            long sender_id = Long.parseLong((response.getJSONObject(i)).getString("sender_id"));
                            String message = (response.getJSONObject(i)).getString("message");
                            String share_pic = (response.getJSONObject(i)).getString("share_pic");
                            //将分享消息保存到shareMessage
                            long user_id = application.getLoginUser().getUid();


                            ShareMessage shareMessage = new ShareMessage(null,user_id,sender_id,message,share_pic);
                            long shareMessage_id = shareMessageDao.insert(shareMessage);
                            // 取出数据保存在手机数据库中
                            musicDao = daoSession.getMusicDao();
                            //userDao.deleteAll();
                            Music music = new Music(uid,name,artist,null,url,lrc_url,null,pic_url,2,shareMessage_id);
                            qb_music = musicDao.queryBuilder();
                            qb_music.where(Properties.Uid.eq(uid));
                            long count = qb_music.buildCount().count();
                            Log.v(TAG,url);
                            if (count > 0){
                                Music music_tmp = (Music) qb_music.unique();
                                String lrc_cache_url = music_tmp.getLrc_cache_url();
                                music = new Music(uid,name,artist,null,url,lrc_url,lrc_cache_url,pic_url,2,shareMessage_id);
                                musicDao.update(music);
                                Log.v(TAG,"update");
                            }else{

                                musicDao.insert(music);
                            }
                            loverFriendMusicList.add(music);
                            Log.v(TAG+"dao","inset new"+music.getUid());
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }


                    }
                }
                application.setFriendMusicList(loverFriendMusicList);
            }
        });
        mQueue.add(lovermusicRequest);
        mQueue.add(loverfriendmusicRequest);
    }

    public boolean isExistInMyfavoriteList(Music m){

        for(int i=0;i<application.getMyMusicList().size();i++){

            if(m.getUid() == application.getMyMusicList().get(i).getUid()){
                return true;
            }
        }
        return false;
    }
}

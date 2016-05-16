package com.example.playlistactivity;

import android.app.LocalActivityManager;
import android.app.NotificationManager;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.example.activity.PlayerActivity;
import com.example.myview.RoundProgressBar;
import com.example.service.PlayerService;
import com.example.testvolley.MyApplication;
import com.example.testvolley.R;
import com.example.util.ExitApplication;
import com.fima.glowpadview.GlowPadView;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.daoexample.Music;

/**
 * Created by Administrator on 2016/5/13.
 */
public class MainActivity extends TabActivity implements OnPageChangeListener, MainActivityCallBack{

    public static Context context;
    private LocalActivityManager manager;
    private TabHost tabHost;
    private ViewPager viewPager;
    public static TextView footer;

    //	mini_music_player
    private static String TAG = "Music List Activity";
    private View view1, view2, view3;
    private List<View> viewList;// view数组
    private ViewPager viewPagerMiniPlayer; // 对应的viewPager
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
    private MyApplication application;
    private PlayerService playerservice;
    private ArrayList<Music> playList;
    private RequestQueue mQueue;
    private int index;
    private SharedPreferences preferences;
    private static MainActivityCallBack mainActivityCallBack;
    //  LinearLayout bt_play_all;
///////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.musiclist_main);

        ExitApplication.getInstance().addActivity(this);
        context = MainActivity.this;
        application = (MyApplication)this.getApplicationContext();
        playList = application.getPlayingList();
        mQueue = application.getRequestQueue();
        playerservice = application.getService();
        preferences = getSharedPreferences("youting",MODE_PRIVATE);
        index = preferences.getInt("INDEX", 0);
        mainActivityCallBack = this;
        //footer = (TextView) findViewById(R.id.text_footet_playingMusic);

        int gitb;
        manager = new LocalActivityManager(this, true);
        manager.dispatchCreate(savedInstanceState);

        this.loadTabHost();
        this.loadViewPager();

        //		bt_play_all = (LinearLayout) findViewById(R.id.bt_playall);
//		bt_play_all.setOnClickListener(new OnClickListener() {
//			int ntab = tabHost.getCurrentTab();
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				switch(ntab){
//					case 0:
//						playerservice.setPlayList(application.getMyMusicList());
//						playerservice.playSong(application.getMyMusicList().get(0));
//						com.example.playlistactivity.MainActivity.getMainActivityCallBack().setPage();
//						break;
//					case 1:
//						playerservice.setPlayList(application.getFriendMusicList());
//						playerservice.playSong(application.getFriendMusicList().get(0));
//						com.example.playlistactivity.MainActivity.getMainActivityCallBack().setPage();
//						break;
//					case 2:
//						playerservice.setPlayList(application.getLocalMusicList());
//						playerservice.playSong(application.getLocalMusicList().get(0));
//						com.example.playlistactivity.MainActivity.getMainActivityCallBack().setPage();
//						break;
//					default:
//						break;
//				}
//			}
//		});

        //选择要进入哪个链表
        Intent intent = getIntent();
        int listNum = (int)intent.getExtras().getInt("listNum");
        //Log.v("listNum","ok "+listNum);


        tabHost.setCurrentTab(listNum);

        miniControlViewInit();
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
        mRoundProgressBar1.setbackground(playerservice.isPlayFlag());
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
        switchToPlayer();
    }

    // 下面的播放小列表显示歌名
    @Override
    protected void onResume() {
        super.onResume();
        if(application.getService()!=null && playList.size()!=0){
            setPage();
        }
        setProgress();
    }

//	@Override
//	protected void onRestart() {
//		// TODO Auto-generated method stub
//		super.onRestart();
//		mRoundProgressBar1.setbackground(playerservice.isPlayFlag());
//	}

    public View getView(String id, Intent intent) {
        return manager.startActivity(id, intent).getDecorView();
    }

    public void loadTabHost() {
        tabHost = getTabHost();
        tabHost.addTab(tabHost.newTabSpec("myFavoriteList").setIndicator("我的最爱")
                .setContent(new Intent(context, MyFavoriteListActivity.class)));
        tabHost.addTab(tabHost.newTabSpec("friendsMusicList").setIndicator("好友分享").setContent(new Intent(context, FriendsMusicListActivity.class)));
        tabHost.addTab(tabHost.newTabSpec("localMusicList").setIndicator("本地音乐").setContent(new Intent(context, LocalMusicListActivity.class)));

        Resources resource=(Resources)getBaseContext().getResources();
        //	ColorStateList csl=(ColorStateList)resource.getColorStateList(R.color.tabwidget_color);
        for(int i=0; i<tabHost.getTabWidget().getChildCount();++i){
            TextView textView = (TextView)tabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
            textView.setTextSize(19);
            textView.setTextColor(0xffffffff);

        }
        tabHost.setOnTabChangedListener(new OnTabChangeListener() {

            public void onTabChanged(String tabId) {
                viewPager.getAdapter().notifyDataSetChanged();
                if (tabId.equals("myFavoriteList")) {
                    viewPager.setCurrentItem(0);
                }
                if (tabId.equals("friendsMusicList")) {
                    viewPager.setCurrentItem(1);
                }

                if (tabId.equals("localMusicList")) {
                    viewPager.setCurrentItem(2);
                }
            }
        });
    }

    public void loadViewPager() {

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        final ArrayList<View> list = new ArrayList<View>();
        list.add(getView("myFavoriteList", new Intent(context, MyFavoriteListActivity.class)));
        list.add(getView("friendsMusicList", new Intent(context, FriendsMusicListActivity.class)));
        list.add(getView("localMusicList", new Intent(context, LocalMusicListActivity.class)));


        viewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getItemPosition(Object object) {
                // TODO Auto-generated method stub
                return POSITION_NONE;
            }

            @Override
            public void destroyItem(View arg0, int arg1, Object arg2) {
                viewPager.removeView(list.get(arg1));
            }

            @Override
            public Object instantiateItem(View arg0, int arg1) {
                ((ViewPager) arg0).addView(list.get(arg1));
                return list.get(arg1);
            }

            @Override
            public boolean isViewFromObject(View arg0, Object arg1) {
                return arg0 == arg1;
            }

            @Override
            public int getCount() {
                return list.size();
            }

            @Override
            public void finishUpdate(View arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void restoreState(Parcelable arg0, ClassLoader arg1) {
                // TODO Auto-generated method stub
            }

            @Override
            public Parcelable saveState() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public void startUpdate(View arg0) {
                // TODO Auto-generated method stub

            }
        });

        viewPager.setOnPageChangeListener(new OnPageChangeListener() {
            public void onPageSelected(int arg0) {
                tabHost.setCurrentTab(arg0);
            }

            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            public void onPageScrollStateChanged(int arg0) {

            }
        });
    }

    //mini播放器初始化
    private void miniControlViewInit(){
        viewPagerMiniPlayer = (ViewPager)findViewById(R.id.mini_music);
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
//        Log.v(TAG,"name:"+playerservice.getMusic_pre().getName());

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

        viewPagerMiniPlayer.setAdapter(pagerAdapter);
        viewPagerMiniPlayer.setCurrentItem(300);
        page_temp=300;
        viewPagerMiniPlayer.setOnPageChangeListener(this);
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

    @Override
    public void setProgress(){
        Log.v(TAG,"setProgress");
        if (!progressThread.isAlive()&&playerservice.isPlayFlag()){
            progressThread.start();
        }
        MainActivity.this.runOnUiThread(new Runnable(){
            public void run() {
                mRoundProgressBar1.setbackground(playerservice.isPlayFlag());
            }
        });
    }

    @Override
    public void setPage(){
        Log.v(TAG,"setPage");
        int page = viewPagerMiniPlayer.getCurrentItem();

        if (!playList.isEmpty()){
            Log.v(TAG,"name"+playerservice.getMusic().getName());
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
                            if(playerservice.getMusic().getSource() == 1){
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
                            if(playerservice.getMusic().getSource() == 1){
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
    public void initCallBack() {

    }

    @Override
    public void setUserInfo() {

    }

    @Override
    public NotificationManager showCustomView() {
        return null;
    }

    public static MainActivityCallBack getMainActivityCallBack() {
        return mainActivityCallBack;
    }
    public static void setMainActivityCallBack(MainActivityCallBack mainActivityCallBack){
        MainActivity.mainActivityCallBack = mainActivityCallBack;
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
    public void onPageScrollStateChanged(int state) {

    }
}

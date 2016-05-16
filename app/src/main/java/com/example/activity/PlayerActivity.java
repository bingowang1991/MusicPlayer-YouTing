package com.example.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.example.myview.CustomImageView;
import com.example.myview.DefaultLrcBuilder;
import com.example.myview.ILrcBuilder;
import com.example.myview.ILrcView.LrcViewListener;
import com.example.myview.LrcRow;
import com.example.myview.LrcView;
import com.example.playlistactivity.LocalMusicListActivity;
import com.example.request.StringRequest;
import com.example.service.PlayerService;
import com.example.service.PlayerService.PlayMode;
import com.example.testvolley.FriendSelectActivity;
import com.example.testvolley.MyApplication;
import com.example.testvolley.MyShareActivity;
import com.example.testvolley.PlayerActivityCallBack;
import com.example.testvolley.R;
import com.example.util.ExitApplication;
import com.example.util.TimeHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.dao.query.QueryBuilder;
import de.greenrobot.daoexample.DaoSession;
import de.greenrobot.daoexample.Music;
import de.greenrobot.daoexample.MusicMessage;
import de.greenrobot.daoexample.MusicMessageDao;
import de.greenrobot.daoexample.ShareMessage;
import de.greenrobot.daoexample.ShareMessageDao;
import de.greenrobot.daoexample.ShareMessageDao.Properties;
import de.greenrobot.daoexample.User;
import de.greenrobot.daoexample.UserDao;


/**
 * Created by Administrator on 2016/5/13.
 */
public class PlayerActivity extends FragmentActivity  implements PlayerActivityCallBack,
        OnPageChangeListener{

    public static final String TAG = PlayerActivity.class.getSimpleName();

    public static final int MSG_SET_LYRIC_INDEX = 1;

    private ImageButton mView_ib_back = null;
    private ImageButton mView_ib_more_functions = null;
    private TextView mView_tv_songtitle = null;
    private TextView mView_tv_current_time = null;
    private TextView mView_tv_total_time = null;
    private TextView mView_message = null;
    private TextView mView_name = null;
    private SeekBar mView_sb_song_progress = null;
    private ImageButton mView_ib_play_mode = null;
    private ImageButton mView_ib_play_previous = null;
    private ImageButton mView_ib_play_or_pause = null;
    private ImageButton mView_ib_play_next = null;
    private ImageButton mView_ib_playqueue = null;
    private CustomImageView music_cover = null;
    private ImageView music_cover_bg;
    private LrcView mLrcView;
    private boolean showLrc = false;
    private boolean showPic = false;
    private PopupMenu mOverflowPopupMenu = null;
    private PlayerService playerService;
    private boolean threadFlag;
    private Runnable runnable;
    private int progress = 0;
    private int duration = 0;
    private Thread progressThread;
    private static PlayerActivityCallBack playerActivityCallBack;
    private ILrcBuilder builder;
    private ViewPager viewPager;
    //装点点的ImageView数组
    private ImageView[] tips;
    private ViewGroup group;
    private View viewDetail, viewLyric;
    private MyApplication application;
    private ArrayList<Music> playList;
    private RequestQueue mQueue;
    private RelativeLayout my_player_container;
    static Activity playerActivity;
    IntentFilter filter;

    private MusicMessageDao musicMessageDao;
    private ShareMessageDao shareMessageDao;
    private UserDao userDao;
    private DaoSession daoSession;
    private QueryBuilder qb;

    private PopupWindow pwMyPopWindow;// popupwindow
    private ListView lvPopupList;// popupwindow中的ListView
    private int NUM_OF_VISIBLE_LIST_ROWS = 2;// 指定popupwindow中Item的数量
    private static SimpleAdapter simpleAdapter;
    List<Map<String, String>> moreList;
    private boolean music_status = false;
    private String[] shareitems = new String[] { "分享给大家", "私信分享" };
    private final static String add_my_music = "http://121.42.164.7/index.php/Home/Index/add_my_music";
    private final static String delete_my_music = "http://121.42.164.7/index.php/Home/Index/delete_my_music";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        application = (MyApplication)getApplicationContext();
        ExitApplication.getInstance().addActivity(this);
        daoSession = application.getDaoSession(this);
        shareMessageDao = daoSession.getShareMessageDao();
        musicMessageDao = daoSession.getMusicMessageDao();
        userDao = daoSession.getUserDao();
        mQueue = application.getRequestQueue();
        playerService = ((MyApplication) application).getService();
        findViews();
        initViewPager();
        initViewsSetting();
        playerActivityCallBack = this;
        playerActivity = this;
        playList = application.getPlayingList();
        initCurrentPlayInfo();

        //更新进度条
        runnable = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        progress = playerService.myPlayer.getCurrentPosition();
                        duration = playerService.myPlayer.getDuration();
                        // 更新当前播放进度
                        PlayerActivity.this.runOnUiThread(new Runnable(){

                            @Override
                            public void run() {
                                // TODO Auto-generated method stub
                                mView_tv_total_time.setText(TimeHelper
                                        .milliSecondsToFormatTimeString(duration));
                                mView_sb_song_progress.setProgress(progress
                                        * mView_sb_song_progress.getMax()
                                        / duration );
                                mView_tv_current_time.setText(TimeHelper
                                        .milliSecondsToFormatTimeString(progress));
                                mLrcView.seekLrcToTime(progress);
                            }
                        });
                        Thread.sleep(1000);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        };

        progressThread = new Thread(runnable);
        if(!progressThread.isAlive() && playerService.isPlayFlag())
            progressThread.start();
        // 监听界面更新
    }

    @Override
    public void onStart() {
        Log.i(TAG, "onStart");
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshview();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.i(TAG, "onStop");
        super.onStop();

    }

    private void findViews() {
        setContentView(R.layout.layout_musicplay);
        LayoutInflater inflater = getLayoutInflater();
        mView_ib_back = (ImageButton) findViewById(R.id.play_button_back);
        mView_ib_more_functions = (ImageButton) findViewById(R.id.play_more_functions);
        mView_ib_playqueue = (ImageButton) findViewById(R.id.play_list);
        mView_ib_play_mode = (ImageButton) findViewById(R.id.play_mode);
        mView_ib_play_next = (ImageButton) findViewById(R.id.play_playnext);
        mView_ib_play_previous = (ImageButton) findViewById(R.id.play_playprevious);
        mView_ib_play_or_pause = (ImageButton) findViewById(R.id.play_playbutton);
        mView_sb_song_progress = (SeekBar) findViewById(R.id.play_progress);
        mView_tv_current_time = (TextView) findViewById(R.id.play_current_time);
        mView_tv_total_time = (TextView) findViewById(R.id.play_song_total_time);
        mView_tv_songtitle = (TextView) findViewById(R.id.play_song_title);
        my_player_container = (RelativeLayout)findViewById(R.id.player_container);
        group = (ViewGroup)findViewById(R.id.viewGroup);
        viewPager = (ViewPager) findViewById(R.id.lyric_or_detail);
        viewDetail = inflater.inflate(R.layout.music_detail, null);
        viewLyric = inflater.inflate(R.layout.music_lyric, null);
        mLrcView=(LrcView) viewLyric.findViewById(R.id.lyricshow);
        music_cover=(CustomImageView) viewDetail.findViewById(R.id.cover_music);
        music_cover_bg = (ImageView) viewDetail.findViewById(R.id.detail_bg);
        mView_message = (TextView) viewDetail.findViewById(R.id.message);
        mView_name = (TextView) viewDetail.findViewById(R.id.name);
        int w = this.getBaseContext().getResources().getDisplayMetrics().widthPixels;

        RelativeLayout.LayoutParams linearParams =(RelativeLayout.LayoutParams) music_cover.getLayoutParams(); //取控件textView当前的布局参数
        linearParams.height = w-20;// 控件的高
        linearParams.width = w-30;// 控件的宽
        music_cover.setLayoutParams(linearParams);

        RelativeLayout.LayoutParams RelativeParams =(RelativeLayout.LayoutParams) music_cover_bg.getLayoutParams(); //取控件textView当前的布局参数
        RelativeParams.height = w-10;// 控件的高
        RelativeParams.width = w-20;// 控件的宽
        music_cover_bg.setLayoutParams(RelativeParams);

/*		Resources src= getResources();
		Bitmap bp=BitmapFactory.decodeResource(src, R.drawable.ic_launcher);*/
        //music_cover.setImageBitmap(bp);
        //Bitmap bp2=blurBitmap(bp);
		/*wuman 在这里加读取专辑图片的代码，直接从MainActivity里面copy就好啦*/
        setPlayerCoverAndBackground();
		/*wuman*/
    }

    /** 对各个控件设置相关参数、监听器等 */
    private void initViewsSetting() {
        //初始化弹窗
        initPopupWindow();

        // 当前播放信息-----------------------------------------------------
        mView_tv_current_time.setText(TimeHelper
                .milliSecondsToFormatTimeString(0));
        mView_tv_total_time.setText(TimeHelper
                .milliSecondsToFormatTimeString(0));

        // 回退按键----------------------------------------------------------
        mView_ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToMain();
            }
        });

        // 播放控制-----------------------------------------------------------------

        // 播放模式--
        mView_ib_play_mode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playerService != null) {
                    playerService.changePlayMode();
                }
            }
        });

        // 上一首--
        mView_ib_play_previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playerService.playPrevious();
                setPlayerCoverAndBackground();
                mView_ib_play_or_pause.setImageResource(R.mipmap.pause_normal);
                refreshview();
            }
        });

        // 播放、暂停
        mView_ib_play_or_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(playList.isEmpty()){

                }else{
                    playerService.setPlayFlag(!playerService.isPlayFlag());
                    if(playerService.isPlayFlag()) {
                        if(playerService.isFirstFlag()){
                            playerService.playItems(playerService.getIndex());
                        }else{
                            playerService.play();
                            //playerService.playSong(playerService.getMusic());
                        }
                        mView_ib_play_or_pause.setImageResource(R.mipmap.pause_normal);
                    }
                    else {
                        playerService.pause();
                        mView_ib_play_or_pause.setImageResource(R.mipmap.play_normal);
                    }
                }
            }
        });

        // 下一首--
        mView_ib_play_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playerService.playNext();
                setPlayerCoverAndBackground();
                mView_ib_play_or_pause.setImageResource(R.mipmap.pause_normal);
                refreshview();
            }
        });

        // 可拖动的进度条
        mView_sb_song_progress
                .setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        // 拖动播放进度条后发送消息给服务端，指示从指定进度开始播放
                        if (playerService != null ) {
                            playerService.seekToSpecifiedPosition(seekBar.getProgress()*(int) playerService.myPlayer.getDuration()/seekBar.getMax());
                            Log.v(TAG, ""+seekBar.getProgress());
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onProgressChanged(SeekBar seekBar,
                                                  int progress, boolean fromUser) {
                        // 根据滑动的进度计算出对应的播放时刻
                        mView_tv_current_time.setText(TimeHelper
                                .milliSecondsToFormatTimeString(progress
                                        * playerService.myPlayer.getDuration()
                                        / seekBar.getMax()));
                        mLrcView.seekLrcToTime(progress);

                    }
                });

        // 播放列表
        mView_ib_playqueue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PlayerActivity.this,PlayQueueActivity.class);
                startActivity(intent);
            }
        });

        //设置歌词监听
        mLrcView.setListener(new LrcViewListener() {

            public void onLrcSeeked(int newPosition, LrcRow row) {
                if (playerService.myPlayer != null) {
                    Log.d(TAG, "onLrcSeeked:" + row.time);
                    playerService.seekToSpecifiedPosition((int)row.time);
                }
            }
        });

        //显示完整图片
        music_cover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(playerService.getMusic().getSource()==2 || playerService.getMusic().getSource()==3){
                    showPic=!showPic;

                    if(showPic){
                        music_cover_bg.setVisibility(View.INVISIBLE);
                        mView_message.setVisibility(View.INVISIBLE);
                        mView_name.setVisibility(View.INVISIBLE);
                    }
                    else {
                        music_cover_bg.setVisibility(View.VISIBLE);
                        mView_message.setVisibility(View.VISIBLE);
                        mView_name.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        //更多设置,弹窗
        mView_ib_more_functions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 判断当前这首歌的是否属于myMusicList
                if(isExistInMyfavoriteList(playerService.getMusic())){
                    Map<String,String> map = moreList.get(0);
                    map.put("share_key", "取消收藏");
                    simpleAdapter.notifyDataSetChanged();
                    music_status = true;
                }
                if (pwMyPopWindow.isShowing()) {

                    pwMyPopWindow.dismiss();// 关闭
                } else {
                    pwMyPopWindow.showAsDropDown(mView_ib_more_functions);// 显示
                }

            }
        });

        final ArrayList<View> viewList = new ArrayList<View>();// 将要分页显示的View装入数组中
        viewList.add(viewDetail);
        viewList.add(viewLyric);

        PagerAdapter pagerAdapter = new PagerAdapter() {

            @Override
            public boolean isViewFromObject(View arg0, Object arg1) {
                // TODO Auto-generated method stub
                return arg0 == arg1;
            }

            @Override
            public int getCount() {
                // TODO Auto-generated method stub
                return viewList.size();

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
                    container.addView(viewList.get(position),0);
                }catch(Exception e){
                    //handler something
                }
                return viewList.get(position);
            }
        };

        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(0);
        viewPager.setOnPageChangeListener(this);
    }

    /**
     * 根据播放模式设置播放模式按钮的图标
     *
     * @param mode
     *            音乐播放模式
     * */
    private void setPlayModeImage(int mode) {
        switch (mode) {
            case PlayMode.REPEAT_SINGLE:
                mView_ib_play_mode
                        .setImageResource(R.mipmap.button_playmode_repeat_single);
                Toast.makeText(this, "单曲循环", Toast.LENGTH_SHORT).show();
                break;
            case PlayMode.REPEAT:
                mView_ib_play_mode
                        .setImageResource(R.mipmap.button_playmode_repeat);
                Toast.makeText(this, "顺序播放", Toast.LENGTH_SHORT).show();
                break;
            case PlayMode.SHUFFLE:
                mView_ib_play_mode
                        .setImageResource(R.mipmap.button_playmode_shuffle);
                Toast.makeText(this, "随机播放", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

    /** 初始化当前播放信息 */
    private void initCurrentPlayInfo() {

        // 设置歌曲标题、时长、当前播放时间、当前播放进度、歌词
        Music myMusic = playerService.getMusic();

        if (myMusic!=null) {
            mView_tv_total_time.setText(TimeHelper
                    .milliSecondsToFormatTimeString(playerService.myPlayer.getDuration()));
            Log.v(TAG,"songduration"+playerService.myPlayer.getDuration());
            mView_tv_songtitle.setText(playerService.getMusic().getName().equals("")?"未知歌名":playerService.getMusic().getName());
            mView_tv_current_time.setText(TimeHelper
                    .milliSecondsToFormatTimeString(playerService.myPlayer.getCurrentPosition()));
            mView_sb_song_progress.setProgress(playerService.myPlayer.getCurrentPosition()
                    * mView_sb_song_progress.getMax()
                    / (int) playerService.myPlayer.getDuration());
            if(showLrc) playerService.loadLyric(myMusic);
            setMessage();
        }

        if (playerService.isPlayFlag()) {
            mView_ib_play_or_pause.setImageResource(R.mipmap.pause_normal);
        } else {
            mView_ib_play_or_pause.setImageResource(R.mipmap.play_normal);
        }
        // 设置播放模式按钮图片
        setPlayModeImage(playerService.mPlayMode);
    }

    public void setPlayerCoverAndBackground(){
        final GaussionBlur gaussionblur = new GaussionBlur();
        Log.v(TAG,"playerService.getMusic().getSource():"+playerService.getMusic().getSource());
        if(playerService.getMusic().getSource() == 1){
            long id = playerService.getMusic().getParameter();
            Bitmap bm = getArtAlbum(id);
            if(bm!= null){
                music_cover.setImageBitmap(bm);
                gaussionblur.execute(bm);
            }else{
                Resources src= getResources();
                bm = BitmapFactory.decodeResource(src, R.mipmap.music_cover2);
                music_cover.setImageBitmap(bm);
                gaussionblur.execute(bm);
            }
        }else if(playerService.getMusic().getSource() == 2){
            String url = playerService.getMusic().getPic_url();
            long shareMessage_id = playerService.getMusic().getParameter();
            qb = shareMessageDao.queryBuilder();
            qb.where(Properties.Id.eq(shareMessage_id));
            long count = qb.buildCount().count();
            if(count > 0){
                ShareMessage shareMessage = (ShareMessage)qb.unique();
                String share_pic = shareMessage.getShare_pic();
                Log.v(TAG,"share_pic"+share_pic);
                if(!share_pic.equals("null")){
                    url = share_pic;
                }
            }
            ImageRequest imgRequest=new ImageRequest(url, new Response.Listener<Bitmap>() {
                @Override
                public void onResponse(Bitmap arg0) {
                    // TODO Auto-generated method stub
                    Log.v("succ","111");
                    music_cover.setImageBitmap(arg0);
                    gaussionblur.execute(arg0);
                }
            }, 0, 0, Config.RGB_565, new ErrorListener(){
                @Override
                public void onErrorResponse(VolleyError arg0) {
                    // TODO Auto-generated method stub
                    Log.v("VolleyError","Sorry");
                }
            });
            mQueue.add(imgRequest);

        }else if(playerService.getMusic().getSource() == 3){
            String url = playerService.getMusic().getPic_url();
            long musicMessage_id = playerService.getMusic().getParameter();
            qb = musicMessageDao.queryBuilder();
            qb.where(de.greenrobot.daoexample.MusicMessageDao.Properties.Id.eq(musicMessage_id));
            long count = qb.buildCount().count();
            if(count > 0){
                MusicMessage musicMessage = (MusicMessage)qb.unique();
                String share_pic = musicMessage.getPic_url();
                Log.v(TAG,"share_pic"+share_pic);
                if(!share_pic.equals("null")){
                    url = share_pic;
                }
            }
            ImageRequest imgRequest=new ImageRequest(url, new Response.Listener<Bitmap>() {
                @Override
                public void onResponse(Bitmap arg0) {
                    // TODO Auto-generated method stub
                    Log.v("succ","111");
                    music_cover.setImageBitmap(arg0);
                    gaussionblur.execute(arg0);
                }
            }, 0, 0, Config.RGB_565, new ErrorListener(){
                @Override
                public void onErrorResponse(VolleyError arg0) {
                    // TODO Auto-generated method stub
                    Log.v("VolleyError","Sorry");
                }
            });
            mQueue.add(imgRequest);

        } else{
            ImageRequest imgRequest=new ImageRequest(playerService.getMusic().getPic_url(), new Response.Listener<Bitmap>() {
                @Override
                public void onResponse(Bitmap arg0) {
                    // TODO Auto-generated method stub
                    Log.v("succ","111");
                    music_cover.setImageBitmap(arg0);
                    gaussionblur.execute(arg0);
                }
            }, 0, 0, Config.RGB_565, new ErrorListener(){
                @Override
                public void onErrorResponse(VolleyError arg0) {
                    // TODO Auto-generated method stub
                    Log.v("VolleyError","Sorry");
                }
            });
            mQueue.add(imgRequest);
        }
    }

    class GaussionBlur extends AsyncTask<Bitmap,Void,Bitmap> {

        Bitmap bmp = null;
        @Override
        protected Bitmap doInBackground(Bitmap... oldpic) {
            // TODO Auto-generated method stub

            bmp = Bitmap.createBitmap(oldpic[0]);
            return Blur.doBlur(PlayerActivity.this,bmp,1);
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            // TODO Auto-generated method stub
            BitmapDrawable bd= new BitmapDrawable(getResources(), result);
            my_player_container.setBackgroundDrawable(bd);
        }
    };

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

    private void switchToMain() {
        //Intent intent = new Intent(PlayerActivity.this, MainActivity.class);
        //startActivity(intent);
        this.finish();
    }

    public static PlayerActivityCallBack getPlayerActivityCallBack() {
        return playerActivityCallBack;
    }

    public void setPlayerActivityCallBack(
            PlayerActivityCallBack playerActivityCallBack) {
        this.playerActivityCallBack = playerActivityCallBack;
    }



    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int arg0) {
        // TODO Auto-generated method stub
        setImageBackground(arg0);
        if(arg0==1) {
            showLrc=true;
            playerService.loadLyric(playerService.getMusic());
        }
        else showLrc=false;

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void setProgress() {
        // TODO Auto-generated method stub
        if (playerService.isPlayFlag()){
            Log.v(TAG,"playerthread");
            progressThread = new Thread(runnable);
            progressThread.start();
        }
    }

    //切歌时刷新歌曲信息
    @Override
    public void refreshview() {
        final Music myMusic = playerService.getMusic();

        PlayerActivity.this.runOnUiThread(new Runnable(){
            @Override
            public void run() {
                if (myMusic != null) {
                    mView_tv_songtitle.setText(playerService.getMusic().getName().equals("")?"未知歌名":playerService.getMusic().getName());
                    Log.v(TAG,"refresh");
                    setPlayerCoverAndBackground();
                    if(showLrc) playerService.loadLyric(myMusic);
                    if(PlayerService.isPlayFlag()) mView_ib_play_or_pause.setImageResource(R.mipmap.pause_normal);
                    else mView_ib_play_or_pause.setImageResource(R.mipmap.play_normal);
                    setMessage();
                }
            }
        });
    }

    @Override
    public void onPlayModeChanged(int playMode) {
        setPlayModeImage(playMode);
    }

    @Override
    //显示歌词
    public void showLrc(){
        if(playerService.mHasLyric){
            Log.v(TAG,"haslrc");
            String lrc = transFileToString(playerService.getMusic().getLrc_cache_url());

            builder = new DefaultLrcBuilder();
            List<LrcRow> rows = builder.getLrcRows(lrc);
            mLrcView.mHignlightRow=0;
            mLrcView.setLrc(rows);

        } else  {
            mLrcView.setLoadingTipText("没有歌词");
            mLrcView.setLrc(null);
        }
    }

    //设置歌词切换的点点
    private void initViewPager(){

        tips = new ImageView[2];
        for(int i=0; i<tips.length; i++){
            ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(new LayoutParams(10,10));
            tips[i] = imageView;
            if(i == 0){
                tips[i].setBackgroundResource(R.mipmap.page_indicator_focused);
            }else{
                tips[i].setBackgroundResource(R.mipmap.page_indicator_unfocused);
            }

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT));
            layoutParams.leftMargin = 5;
            layoutParams.rightMargin = 5;
            group.addView(imageView, layoutParams);
        }
    }

    //将歌词文件转换为字符串
    public String transFileToString(String fileName){
        try {
            File file = new File(fileName);
            Log.v(TAG,"succ");
            BufferedReader bufReader = new BufferedReader(new FileReader(file) );
            String line="";
            String Result="";
            while((line = bufReader.readLine()) != null){
                if(line.trim().equals(""))
                    continue;
                Result += line + "\r\n";
            }
            return Result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private void setImageBackground(int selectItems){
        for(int i=0; i<tips.length; i++){
            if(i == selectItems){
                tips[i].setBackgroundResource(R.mipmap.page_indicator_focused);
            }else{
                tips[i].setBackgroundResource(R.mipmap.page_indicator_unfocused);
            }
        }
    }
    //高斯模糊的代码，AsynTask
    class GaussianBlurTask extends AsyncTask<Bitmap, Void, Bitmap>{

        @Override
        protected Bitmap doInBackground(Bitmap... params) {
            // TODO Auto-generated method stub
            return null;
        }
    };

    //弹窗初始化
    private void initPopupWindow() {
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.task_detail_popupwindow, null);
        lvPopupList = (ListView) layout.findViewById(R.id.lv_popup_list);
        pwMyPopWindow = new PopupWindow(layout);
        pwMyPopWindow.setFocusable(true);// 加上这个popupwindow中的ListView才可以接收点击事件

        moreList = new ArrayList<Map<String, String>>();
        Map<String, String> map;
        map = new HashMap<String, String>();
        map.put("share_key", "收藏歌曲");
        moreList.add(map);
        map = new HashMap<String, String>();
        map.put("share_key","分享歌曲");
        moreList.add(map);

        simpleAdapter = new SimpleAdapter(PlayerActivity.this, moreList,
                R.layout.item_list_popupwindow2, new String[] { "share_key" },
                new int[] { R.id.tv_list_item });
        lvPopupList.setAdapter(simpleAdapter);
        lvPopupList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch(position){
                    case 0:
                        if(music_status == true){
                            removeMusicFromMyMusic(playerService.getMusic());

                        }else{
                            addMusicToMyMusic(playerService.getMusic());

                        }

                        pwMyPopWindow.dismiss();// 关闭
                        break;
                    case 1:
                        if(playerService.getMusic().getSource() == 1){
                            Toast.makeText(getApplicationContext(),"本地音乐无法分享", Toast.LENGTH_SHORT).show();
                        }else{
                            showShareDialog();
                        }

                        pwMyPopWindow.dismiss();// 关闭
                        break;
                }
            }
        });

        // 控制popupwindow的宽度和高度自适应
        lvPopupList.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        pwMyPopWindow.setWidth(lvPopupList.getMeasuredWidth()+10);
        pwMyPopWindow.setHeight((lvPopupList.getMeasuredHeight() + 5) * NUM_OF_VISIBLE_LIST_ROWS);

        // 控制popupwindow点击屏幕其他地方消失
        pwMyPopWindow.setBackgroundDrawable(this.getResources().getDrawable(
                R.mipmap.black_bg));// 设置背景图片，不能在布局中设置，要通过代码来设置
        pwMyPopWindow.setOutsideTouchable(true);// 触摸popupwindow外部，popupwindow消失。这个要求你的popupwindow要有背景图片才可以成功，如上
    }

    private void setMessage(){
        if(playerService.getMusic() != null){
            Music music = playerService.getMusic();
            if (music.getSource() == 2 ){
                // 分享音乐显示分享信息
                long shareMessage_id = music.getParameter();
                qb = shareMessageDao.queryBuilder();
                qb.where(Properties.Id.eq(shareMessage_id));
                long count = qb.buildCount().count();
                if(count > 0){
                    ShareMessage shareMessage = (ShareMessage)qb.unique();
                    long sender_id = shareMessage.getSender_id();
                    String message = shareMessage.getMessage();
                    String share_pic = shareMessage.getShare_pic();
                    qb = userDao.queryBuilder();
                    qb.where(de.greenrobot.daoexample.UserDao.Properties.Uid.eq(sender_id));
                    User user = (User)qb.unique();
                    Log.v(TAG,"Message"+message+"sende_id"+sender_id);
                    String name = user.getName();
                    Log.v(TAG,MyApplication.DEFAULT_MOOD);
                    if(message.equals(MyApplication.DEFAULT_MOOD)){
                        // 无消息
                        mView_name.setText("来自"+name+"的分享");
                        mView_message.setText("");

                    }else{
                        mView_name.setText(name+":");
                        mView_message.setText(message);
                    }

                }
            }else if(music.getSource() == 3){
                long musicMessage_id = music.getParameter();
                qb = musicMessageDao.queryBuilder();
                qb.where(de.greenrobot.daoexample.MusicMessageDao.Properties.Id.eq(musicMessage_id));
                long count = qb.buildCount().count();
                if(count > 0){
                    MusicMessage musicMessage = (MusicMessage)qb.unique();
                    long sender_id = musicMessage.getSender_id();
                    String message = musicMessage.getMessage();
                    qb = userDao.queryBuilder();
                    qb.where(de.greenrobot.daoexample.UserDao.Properties.Uid.eq(sender_id));
                    User user = (User)qb.unique();
                    String name = user.getName();
                    if(message.equals(application.DEFAULT_MOOD)){
                        // 无消息
                        mView_name.setText("来自"+name+"的分享");
                        mView_message.setText("");
                    }else{
                        mView_name.setText(name+":");
                        mView_message.setText(message);
                    }

                }
            }else{
                music_cover_bg.setVisibility(View.INVISIBLE);
                mView_message.setVisibility(View.INVISIBLE);
                mView_name.setVisibility(View.INVISIBLE);
                // 设置下半部分不可见
            }
        }
    }

    public void addMusicToMyMusic(final Music m){
        if(m.getSource() == 1){
            try {
                LocalMusicListActivity.getLocalMusicListActivityCallBack().addLocalToMyMusic(m);
                Map<String,String> map = moreList.get(0);
                map.put("share_key", "取消收藏");
                simpleAdapter.notifyDataSetChanged();
                music_status = true;
                Toast.makeText(getApplicationContext(),"收藏歌曲成功", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }else{
            long uid = m.getUid();
            String add_music_url = add_my_music+"?music_id="+uid;
            StringRequest addRequest= new StringRequest(Method.GET,add_music_url,null,new Response.Listener<String>() {

                @Override
                public void onResponse(String response) {
                    // TODO Auto-generated method stub
                    Music music = new Music(m);
                    music.setSource(0);
                    music.setParameter((long)0);
                    application.addToMyMusicList(music);
                    Map<String,String> map = moreList.get(0);
                    map.put("share_key", "取消收藏");
                    simpleAdapter.notifyDataSetChanged();
                    music_status = true;
                    Toast.makeText(getApplicationContext(),"收藏歌曲成功", Toast.LENGTH_SHORT).show();
                }
            },new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    // TODO Auto-generated method stub

                }
            });
            mQueue.add(addRequest);
        }
    }
    public void removeMusicFromMyMusic(final Music m){
        if(m.getSource() == 1){
            try {
                LocalMusicListActivity.getLocalMusicListActivityCallBack().removLocalFromMyMusic(m);
                Map<String,String> map = moreList.get(0);
                map.put("share_key", "收藏歌曲");
                simpleAdapter.notifyDataSetChanged();
                music_status = false;
                Toast.makeText(getApplicationContext(),"移除歌曲成功", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }else{
            final long uid = m.getUid();
            String delete_url = delete_my_music+"?music_id="+uid;
            StringRequest deleteRequest = new StringRequest(Method.GET,delete_url,null,new Response.Listener<String>() {

                @Override
                public void onResponse(String response) {
                    // TODO Auto-generated method stub
                    Log.v(TAG,"delete_my_music");
                    Map<String,String> map = moreList.get(0);
                    map.put("share_key", "收藏歌曲");
                    simpleAdapter.notifyDataSetChanged();
                    music_status = false;
                    for(int i=0;i<application.getMyMusicList().size();i++){

                        if(uid == application.getMyMusicList().get(i).getUid()){
                            application.RemoveFromMyMusicList(application.getMyMusicList().get(i));
                            break;
                        }
                    }
                    Toast.makeText(getApplicationContext(),"移除歌曲成功", Toast.LENGTH_SHORT).show();

                }
            },new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    // TODO Auto-generated method stub
                    Log.v(TAG,error.toString());
                }
            });
            mQueue.add(deleteRequest);

        }
    }

    public boolean isExistInMyfavoriteList(Music m){
        for(int i=0;i<application.getMyMusicList().size();i++){
            Music music = application.getMyMusicList().get(i);
            if(m.getSource()==1){
                if(m.getParameter() == music.getParameter()){
                    return true;
                }
            }else if(m.getUid() == music.getUid()){
                return true;
            }
        }
        return false;
    }

    /**
     * New Adding 显示选择对话框
     */
    public void showShareDialog() {
        final AlertDialog ald=new AlertDialog.Builder(this).create();
        ald.show();
        ald.getWindow().setContentView(R.layout.sharedialog);
        ald.getWindow()
                .findViewById(R.id.ll_sharetoall)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(playerService.getMusic().getSource() != 1){
                            String artist=playerService.getMusic().getArtist();
                            String name=playerService.getMusic().getName();
                            String pic_url=playerService.getMusic().getPic_url();
                            long uid = playerService.getMusic().getUid();
                            Intent intent=new Intent();
                            intent.setClass(PlayerActivity.this,MyShareActivity.class);
                            intent.putExtra("share_mode", 0);
                            intent.putExtra("tofriends", "所有人");
                            intent.putExtra("name", name);
                            intent.putExtra("artist", artist);
                            intent.putExtra("pic_url", pic_url);
                            intent.putExtra("music_id", uid);
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
                        if(playerService.getMusic().getSource() != 1){
                            String artist=playerService.getMusic().getArtist();
                            String name=playerService.getMusic().getName();
                            String pic_url=playerService.getMusic().getPic_url();
                            long uid = playerService.getMusic().getUid();
                            Intent intent=new Intent();
                            intent.setClass(PlayerActivity.this,FriendSelectActivity.class);
                            intent.putExtra("name", name);
                            intent.putExtra("artist", artist);
                            intent.putExtra("pic_url", pic_url);
                            intent.putExtra("music_id", uid);
                            startActivity(intent);
                            ald.dismiss();
                        }
                    }
                });
    }

}

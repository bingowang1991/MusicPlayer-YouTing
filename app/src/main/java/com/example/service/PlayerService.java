package com.example.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.RemoteControlClient;
import android.media.RemoteControlClient.MetadataEditor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.example.activity.PlayerActivity;
import com.example.cacheplayer.HttpGetProxy;
import com.example.cacheplayer.MyPlayer;
import com.example.receiver.PlayerReceiver;
import com.example.request.StringRequest;
import com.example.testvolley.MainActivity;
import com.example.testvolley.MyApplication;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import de.greenrobot.dao.query.QueryBuilder;
import de.greenrobot.daoexample.DaoSession;
import de.greenrobot.daoexample.Music;
import de.greenrobot.daoexample.MusicDao;
import de.greenrobot.daoexample.MusicDao.Properties;
import de.greenrobot.daoexample.ShareMessage;
import de.greenrobot.daoexample.ShareMessageDao;

/**
 * Created by Administrator on 2016/5/13.
 */
public class PlayerService extends IntentService {

    private static final String TAG = "service";

    private static boolean playFlag = false;
    private  boolean firstFlag = true;
    private  int index;
    private int err_num = 0;
    private  int duration;     //歌长
    private  ArrayList<Music> playList;  //歌曲列表
    private  int listSize;  //歌单长度
    private HttpGetProxy proxy;
    private String BuffPath = "/youting";
    private String ftplogin = "user", ftppass = "123";
    private Context context;
    static private int BUFFER_SIZE= 1024;//Mb cache dir
    static private int NUM_FILES= 200;//Count files in cache dir
    private RemoteControlClient remoteControlClient;
    private MyApplication application;
    private RequestQueue mQueue;
    private DaoSession daoSession;
    private ShareMessageDao shareMessageDao;
    private MusicDao musicDao;
    private QueryBuilder qb_music;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private ArrayList<Music> myMusicList,friendMusicList,localMusicList;
    private ArrayList<Music> playListNormal;
    public MyPlayer myPlayer;
    private Music music_pre;
    private Music music_next;
    private static Music music;
    private int progress;
    public int mPlayMode = 1;
    private MyApplication myApplication=MyApplication.get();
    public boolean mHasLyric = false;

    private static final String count_music_url = "http://121.42.164.7/index.php/Home/Index/count_music";
    private static final String set_friend_music_activated = "http://121.42.164.7/index.php/Home/Index/set_friend_music_activated";

    public PlayerService(String name) {
        super(name);
    }

    public PlayerService() {
        super("player!");
    }

    private final IBinder mBinder = new LocalBinder();

    private Handler handler = new Handler();

    /**
     * 播放模式<br>
     * 0代表单曲循环，1代表列表循环，2代表顺序播放，3代表随机播放
     */
    /**
     * 播放模式<br>
     * 0代表单曲循环，1代表列表循环，2代表顺序播放，3代表随机播放
     */
    public class PlayMode {
        public static final int REPEAT_SINGLE = 0;
        public static final int REPEAT = 1;
        public static final int SHUFFLE = 2;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this.getApplicationContext();
        myPlayer = new MyPlayer(context);
        application = (MyApplication)this.getApplicationContext();
        mQueue = application.getRequestQueue();
        daoSession = application.getDaoSession(context);
        musicDao = daoSession.getMusicDao();
        shareMessageDao = daoSession.getShareMessageDao();
        remoteControlClient = application.getRemoteControlClient();
        myMusicList = application.getMyMusicList();
//		friendMusicList = application.getFriendMusicList();
        localMusicList = application.getLocalMusicList();

        playList = application.getPlayingList();

        preferences = getSharedPreferences("youting",MODE_PRIVATE);
        editor = preferences.edit();
        setIndex(preferences.getInt("INDEX", 0));
        listSize=playList.size();
        if(listSize !=0){
            music_pre = (this.index==0)?playList.get(listSize-1):playList.get(index-1);
            music_next =(index==(listSize-1))?playList.get(0):playList.get(index+1);
            music = playList.get(index);
        }
//		IntentFilter filter = new IntentFilter();
//		filter.addAction("Android.intent.action.New_OutGGOING_CALL");
//		registerReceiver(new PhoneListener(),filter);
//		TelephonyManager manager = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
//		manager.listen(new MyPhoneStateListener(), PhoneStateListener.LISTEN_CALL_STATE);
    }

//	private final class MyPhoneStateListener extends PhoneStateListener {
//        public void onCallStateChanged(int state, String incomingNumber) {
//            pause();
//        }
//    }
//	private final class PhoneListener extends BroadcastReceiver {
//        public void onReceive(Context context, Intent intent) {
//        	Log.v(TAG,"outgoing call");
//            pause();
//        }
//    }

    public void play(){
        Log.v(TAG,"play"+firstFlag);
        myPlayer.start();
        playFlag = true;
        com.example.testvolley.MainActivity.getMainActivityCallBack().setProgress();
        if(com.example.playlistactivity.MainActivity.getMainActivityCallBack()!=null){
            com.example.playlistactivity.MainActivity.getMainActivityCallBack().setProgress();
        }
        if(PlayerActivity.getPlayerActivityCallBack()!=null){
            PlayerActivity.getPlayerActivityCallBack().setProgress();

        }
        remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
        if(firstFlag){
            MyApplication.get().setNotificationManager(com.example.testvolley.MainActivity.getMainActivityCallBack().showCustomView());
            firstFlag=false;
        }
        Intent i = new Intent(this, PlayerReceiver.class);
        i.putExtra("action", "play");
        sendBroadcast(i);
    }

    public void pause() {
        Log.v(TAG,"pause");
        myPlayer.pause();
        playFlag = false;
        remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);
        com.example.testvolley.MainActivity.getMainActivityCallBack().setProgress();
        if(com.example.playlistactivity.MainActivity.getMainActivityCallBack()!=null){
            com.example.playlistactivity.MainActivity.getMainActivityCallBack().setProgress();
        }
        if(PlayerActivity.getPlayerActivityCallBack()!=null){
            PlayerActivity.getPlayerActivityCallBack().setProgress();
        }
        Intent i=new Intent(this,PlayerReceiver.class);
        i.putExtra("action", "pause");
        sendBroadcast(i);
    }

    public void stop() {
        Log.v(TAG,"stop");
        myPlayer.stop();
        music = null;
        playFlag = false;
    }

    public void play_pause() {
        Log.v(TAG,"play_pause");
        if(playFlag){
            pause();
        }else{
            play();
        }
    }

    public void playPrevious() {
        // TODO Auto-generated method stub
        if(index == 0){
            index = playList.size()-1;

        }else{
            index--;
        }
//	    myPlayer.release();
        Log.v(TAG,"playPrevious success");
        Music m = playList.get(index);
        playSong(m);

    }

    public void playNext() {
        // TODO Auto-generated method stub

        if (index+1 < playList.size()){
            index++;

        }else{
            index=0;
        }
//	    myPlayer.release();
        Log.v(TAG,"playnext success"+index);
        Music m = playList.get(index);
        playSong(m);
    }

    public void jumptoNext() {
        // TODO Auto-generated method stub
        if (index+1 < playList.size()){
            index++;
        }else{
            index=0;
        }
        Log.v(TAG,"playnext success"+index);
        myPlayer.release();
        music = playList.get(index);

    }

    public void playItems(int index){

//	    myPlayer.release();
        Log.v(TAG,"play items"+playList.size()+"this.index:"+this.index);
        music = playList.get(index);
        Log.v(TAG,"service music.getSOurce()"+music.getSource());
        listSize = playList.size();
        music_pre = (index==0)?playList.get(listSize-1):playList.get(index-1);
        music_next =(index==(listSize-1))?playList.get(0):playList.get(index+1);
//	    MainActivity.getMainActivityCallBack().setPage();
        playSong(music);
    }

    public void playSong(Music m){
        Log.v(TAG,"playSong index:"+index);
        myPlayer.release();
        if (isPlaylistContain(m)){
//			index = playList.indexOf(m);

            editor.putInt("INDEX", index);
            editor.commit();
            Log.v(TAG,"already exist index:"+index);
        }else{

            playList.add(m);
            Log.v(TAG,"add into playlist index:"+playList.indexOf(m));
            //搜索的歌曲，把音乐添加进greenDao数据库
            long id = m.getUid();
            if(id != 0){
                qb_music = musicDao.queryBuilder();
                qb_music.where(Properties.Uid.eq(id));
                long count = qb_music.buildCount().count();
                String  name = m.getName();
                String artist = m.getArtist();
                String url = m.getUrl();
                String lrc_url = m.getLrc_url();
                String pic_url = m.getPic_url();
                if (count > 0){
                    Music music_tmp = (Music) qb_music.unique();
                    String lrc_cache_url = music_tmp.getLrc_cache_url();
                    music_tmp.setLrc_cache_url(lrc_cache_url);
                    musicDao.update(music_tmp);
                    Log.v(TAG,"update");
                }else{
                    musicDao.insert(m);
                    Log.v(TAG,"insert");
                }
            }

            //playList发生改变，保存playList到sharePreference
            JSONArray jArray = new JSONArray();
            for (int i=0;i<playList.size();i++){
                Music music = playList.get(i);
                JSONObject jObject = new JSONObject();
                Log.v(TAG,music.getName());
                long uid = music.getUid();
                long local_id = 0;
                if(music.getSource() == 1){
                    local_id = music.getParameter();
                }
                try {
                    jObject.put("uid", uid);
                    jObject.put("source", music.getSource());
                    jObject.put("parameter",music.getParameter());
                    jObject.put("local_id", local_id);

                    jArray.put(i, jObject);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            listSize = playList.size();
            index = playList.indexOf(m);
            Log.v(TAG,jArray.toString());
            editor.putString("playingList",jArray.toString());
            editor.putInt("INDEX", index);
            editor.commit();
        }
        String url = m.getUrl();
        setMusicInfo(m);
        listSize = playList.size();
        music = m;

        music_pre = (index==0)?playList.get(listSize-1):playList.get(index-1);
        music_next =(index==(listSize-1))?playList.get(0):playList.get(index+1);
//	    MainActivity.getMainActivityCallBack().setPage();
        Log.v(TAG,"pre:"+music_pre.getName()+"next:"+music_next.getName());
        if(proxy != null){
            proxy.stopProxy();
        }
        proxy = new HttpGetProxy();
        String buffPath;
        if(m.getSource() == 1){
            buffPath = m.getCache_url();
            BUFFER_SIZE = 0;
            NUM_FILES = 0;
        }else{
            buffPath = BuffPath;
        }
        Log.v(TAG,BuffPath);
        proxy.setPaths(buffPath, url, BUFFER_SIZE, NUM_FILES, context, false, ftplogin, ftppass, false, false, "5FQTRE5AIPHN7K5Z4D3HTN653FXLCPH3VDVBU5A");
        //start player
        String proxyUrl = proxy.getLocalURL();
        myPlayer.setPath(proxyUrl);
        myPlayer.setOnErrorListener(new OnErrorListener(){

            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {

                // TODO Auto-generated method stub
                err_num ++;
                if(err_num == playList.size()){
                    myPlayer.release();
                }else{
                    playNext();
                    com.example.testvolley.MainActivity.getMainActivityCallBack().setPage();
                    if(com.example.playlistactivity.MainActivity.getMainActivityCallBack()!=null){
                        com.example.playlistactivity.MainActivity.getMainActivityCallBack().setPage();
                    }
                }
                return false;
            }

        });
        myPlayer.setSeekListener(new MyPlayer.SeekListener() {
            @Override
            public void onSeek(int msec) {
                if (proxy!=null) {proxy.seek=true;} //more speed for seeking
            }

            @Override
            public void onSeekComplete(MediaPlayer mp) {

            }
        });
        myPlayer.setOnPreparedListener(new OnPreparedListener(){

            @Override
            public void onPrepared(MediaPlayer mp) {
                // TODO Auto-generated method stub
                duration = myPlayer.getDuration();
                Log.v(TAG,"duration:"+duration);
                play();
//				if(PlayerActivity.getPlayerActivityCallBack()!=null){
//					PlayerActivity.getPlayerActivityCallBack().refreshview();
//				}
            }

        });
        myPlayer.setOnCompletionListener(new OnCompletionListener(){

            @Override
            public void onCompletion(MediaPlayer mp) {
                // TODO Auto-generated method stub
                // 与服务器交互，播放这首歌的次数+1
                if(music.getSource() == 0){
                    addMusicCount();
                }else if(music.getSource() == 2){
                    // 将分享的音乐设成已听过
                    setFriendMusicActivated(music);
                }
                playNext();
                Log.v(TAG,music.getName());
                if(mPlayMode==PlayMode.REPEAT_SINGLE){
                    myPlayer.release();
                    playSong(music);
                }
                com.example.testvolley.MainActivity.getMainActivityCallBack().setPage();
                if(com.example.playlistactivity.MainActivity.getMainActivityCallBack()!=null){
                    com.example.playlistactivity.MainActivity.getMainActivityCallBack().setPage();
                }
            }

        });
    }

    public void setMusicInfo(Music m){
        Music music = m;
        final RemoteControlClient.MetadataEditor ed = remoteControlClient.editMetadata(true);
        ed.putString(MediaMetadataRetriever.METADATA_KEY_TITLE, m.getName());
        ed.putString(MediaMetadataRetriever.METADATA_KEY_ALBUM,m.getArtist());
        // 获得封面
        if(m.getSource() == 1){
            long audio_id = m.getParameter();
            Bitmap bm = getArtAlbum(audio_id);
            if(bm != null){
                ed.putBitmap(MetadataEditor.BITMAP_KEY_ARTWORK, bm);
                ed.apply();
            }else{
                ed.apply();
            }
        }else{
            ImageRequest imgRequest = new ImageRequest(m.getPic_url(),new Response.Listener<Bitmap>() {

                @Override
                public void onResponse(Bitmap response) {
                    // TODO Auto-generated method stub
                    ed.putBitmap(MetadataEditor.BITMAP_KEY_ARTWORK, response);
                    ed.apply();
                }
            }, 0, 0, Config.ARGB_8888,new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // TODO Auto-generated method stub
                    ed.apply();
                }
            });
            mQueue.add(imgRequest);
        }
//       ed.putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, m.getArtist());
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String type = intent.getStringExtra("action");
        Log.v("intent", type);
        if (type.equals("next")) {
            int index = this.index;
            if (index+1 < playList.size()){
                index++;

            }else{
                index=0;
            }
            music = playList.get(index);
            listSize = playList.size();
            music_pre = (index==0)?playList.get(listSize-1):playList.get(index-1);
            music_next =(index==(listSize-1))?playList.get(0):playList.get(index+1);
            // 暂时措施，实际应该用appManager来处理
            MainActivity.getMainActivityCallBack().setPage();
            if(com.example.playlistactivity.MainActivity.getMainActivityCallBack() != null){
                com.example.playlistactivity.MainActivity.getMainActivityCallBack().setPage();
            }

            Log.v(TAG,"playnextsetpage");
            playNext();

        } else if (type.equals("pause") || type.equals("play")) {
//			Intent i=new Intent(this,PlayerReceiver.class);
//			if(isPlayFlag())
//				i.putExtra("action", "play");
//			else
//				i.putExtra("action", "pause");
//			sendBroadcast(i);
            play_pause();
        }

    }

    /**
     * 自定义绑定Service类，通过这里的getService得到Service，之后就可调用Service这里的方法了
     */
    public class LocalBinder extends Binder {
        public PlayerService getService() {
            Log.d("playerService", "getService");
            return PlayerService.this;
        }
    }

    /** 改变播放模式 */
    public void changePlayMode() {

        mPlayMode = (mPlayMode + 1) % 3;
        if (myPlayer != null) {
            // 如果正在播放歌曲
            switch (mPlayMode) {
                case PlayMode.SHUFFLE:
                    playListNormal = (ArrayList<Music>) playList.clone();
                    Collections.shuffle(playList);
                    Log.v(TAG,"shuffle");
                    break;
                case PlayMode.REPEAT:
                    playList=(ArrayList<Music>) playListNormal.clone();
                    Log.v(TAG,"repeat");
                    break;
                case PlayMode.REPEAT_SINGLE:
                    playList=(ArrayList<Music>) playListNormal.clone();
                    Log.v(TAG,"repeatonce");
                    break;
                default:
                    break;
            }
        }
        if(PlayerActivity.getPlayerActivityCallBack()!=null)
            PlayerActivity.getPlayerActivityCallBack().onPlayModeChanged(mPlayMode);
    }

    //跳到指定时间点
    public void seekToSpecifiedPosition(int milliSeconds){
        myPlayer.seekTo(milliSeconds);
    }

    @Override
    public void onDestroy() {

    }

    /**
     * 读取歌词文件
     * @param myMusic
     *              歌曲文件的路径
     */
    public void loadLyric(Music myMusic) {
        // 取得歌曲同目录下的歌词文件绝对路径
        File lyricfile = null;
        String lyricLocalPath = myMusic.getLrc_cache_url();
        if(myMusic.getSource() == 1){
            mHasLyric = false;
            Log.i(TAG, "loadLyric()--->都木有歌词");
            PlayerActivity.getPlayerActivityCallBack().showLrc();
        }else{
            if (lyricLocalPath!=null) {
                // 本地有歌词，直接读取
                Log.i(TAG, "loadLyric()--->本地有歌词，直接读取");

                mHasLyric=true;
                PlayerActivity.getPlayerActivityCallBack().showLrc();
            } else if(myMusic.getLrc_url() == null || myMusic.getLrc_url().equals("null")){
                // 设置歌词为空
                mHasLyric = false;
                Log.i(TAG, "loadLyric()--->都木有歌词");
                PlayerActivity.getPlayerActivityCallBack().showLrc();
            } else {
                // 尝试网络获取歌词
                Log.i(TAG, "loadLyric()--->本地无歌词，尝试从网络获取");
                getLrcFormNet(myMusic);
            }
        }

    }


    //从网络上获得LRC
    public void getLrcFormNet(final Music myMusic){

        if (myMusic.getLrc_url().equals(null))   return;
        String lyricFilePath = null;

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(myMusic.getLrc_url(), new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                // called before request is started
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                // called when response HTTP status is "200 OK"
                try {
                    FileOutputStream fout = new FileOutputStream("sdcard/youting/"+myMusic.getName()+".lrc");
                    String lyricFilePath="sdcard/youting/"+myMusic.getName()+".lrc";
                    myMusic.setLrc_cache_url(lyricFilePath);
                    Long uid = myMusic.getUid();
                    //更改数据库
//					qb_music.where(Properties.Uid.eq(uid));
                    musicDao.update(myMusic);
                    fout.write(response);
                    fout.close();
                    mHasLyric=true;
                    PlayerActivity.getPlayerActivityCallBack().showLrc();
                    Log.i(TAG, "loadLyric()--->从网络获取成功");
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {

            }

            @Override
            public void onRetry(int retryNo) {
            }

        });
    }

    public static boolean isPlayFlag() {
        return playFlag;
    }

    public static void setPlayFlag(boolean playFlag) {
        PlayerService.playFlag = playFlag;
    }

    public boolean isFirstFlag() {
        return firstFlag;
    }

    public void setFirstFlag(boolean firstFlag) {
        this.firstFlag = firstFlag;
    }

    public int getListSize() {
        return listSize;
    }

    public void setListSize(int listSize) {
        this.listSize = listSize;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public ArrayList<Music> getPlayList() {
        return playList;
    }

    public void setPlayList(ArrayList<Music> playList) {

        index = 0;
        err_num =0;
        this.playList=(ArrayList<Music>)playList.clone();
        application.setPlayingList(this.playList);
        JSONArray jArray = new JSONArray();

        for (int i=0;i<playList.size();i++){
            Music music = playList.get(i);
            JSONObject jObject = new JSONObject();
            Log.v(TAG,"service set playList"+music.getSource());
            long uid = music.getUid();
            long local_id = 0;
            if(music.getSource() == 1){
                local_id = music.getParameter();
            }
            try {
                jObject.put("uid", uid);
                jObject.put("local_id", local_id);
                jObject.put("source", music.getSource());
                jObject.put("parameter",music.getParameter());
                jArray.put(i, jObject);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        listSize = playList.size();
        Log.v(TAG,jArray.toString());
        editor.putString("playingList",jArray.toString());
        editor.putInt("INDEX", 0);
        editor.commit();
    }

    public Music getMusic_pre() {
        return music_pre;
    }

    public void setMusic_pre(Music music_pre) {
        this.music_pre = music_pre;
    }

    public Music getMusic_next() {
        return music_next;
    }

    public void setMusic_next(Music music_next) {
        this.music_next = music_next;
    }

    public static Music getMusic() {
        return music;
    }

    public void setMusic(Music music) {
        this.music = music;
    }
    public int getProgress(){
        return myPlayer.getCurrentPosition()*100/myPlayer.getDuration();
    }

    public boolean checkNetworkState(){
        ConnectivityManager manager = (ConnectivityManager)getSystemService(
                Context.CONNECTIVITY_SERVICE);
        State mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
        State wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
        //如果3G、wifi、2G等网络状态是连接的，则退出，否则显示提示信息进入网络设置界面
        if(mobile == State.CONNECTED||mobile==State.CONNECTING)
            return true;
        if(wifi == State.CONNECTED||wifi==State.CONNECTING)
            return false;
        return false;

    }
    public boolean isPlaylistContain(Music m){
        for(int i=0;i<playList.size();i++){
            if(m.getSource() == 1 && m.getParameter() == playList.get(i).getParameter()){
                index = i;
                return true;
            }
            if(m.getSource() == 0 && m.getUid() == playList.get(i).getUid()){
                index = i;
                return true;
            }
            if(m.getSource() == 2 && m.getParameter() == playList.get(i).getParameter()){
                index = i;
                return true;
            }
            if(m.getSource() == 3 && m.getParameter() == playList.get(i).getParameter()){
                index = i;
                return true;
            }

        }
        return false;

    }
    public void addMusicCount(){
        String url = count_music_url +"?music_id="+ music.getUid();
        StringRequest countRequest = new StringRequest(Method.GET,url,null,new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                // TODO Auto-generated method stub

            }
        },new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub

            }
        });
        mQueue.add(countRequest);
    }
    public void setFriendMusicActivated(Music m){
        long friend_music_id = m.getUid();
        long share_message_id = m.getParameter();
        qb_music = shareMessageDao.queryBuilder();
        qb_music.where(de.greenrobot.daoexample.ShareMessageDao.Properties.Id.eq(share_message_id));
        if(qb_music.buildCount().count() > 0){
            ShareMessage shareMessage = (ShareMessage) qb_music.unique();
            long sender_id = shareMessage.getSender_id();
            String url = set_friend_music_activated+"?friend_music_id="+friend_music_id+"&sender_id="+sender_id;
            StringRequest request = new StringRequest(Method.GET,url,null,new Response.Listener() {

                @Override
                public void onResponse(Object response) {
                    // TODO Auto-generated method stub

                }
            },new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    // TODO Auto-generated method stub

                }
            });
            mQueue.add(request);
        }

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

}

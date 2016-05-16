package com.example.testvolley;


import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.RemoteControlClient;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.receiver.MessageEventReceiver;
import com.example.receiver.PlayerReceiver;
import com.example.service.PlayerService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cn.jpush.im.android.api.JMessageClient;
import de.greenrobot.daoexample.DaoMaster;
import de.greenrobot.daoexample.DaoMaster.OpenHelper;
import de.greenrobot.daoexample.DaoSession;
import de.greenrobot.daoexample.Music;
import de.greenrobot.daoexample.User;

/**
 * Created by Administrator on 2016/5/15.
 */
public class MyApplication extends Application{

    private static final String SET_COOKIE_KEY = "Set-Cookie";
    private static final String COOKIE_KEY = "Cookie";
    private static final String SESSION_COOKIE = "PHPSESSID";
    private static final String TAG = "application";

    private static MyApplication mInstance;

    private static DaoMaster daoMaster;
    private static DaoSession daoSession;

    private RequestQueue mQueue;

    private RemoteControlClient myRemoteControlClient;

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    private ArrayList<Music> myMusicList = new ArrayList<Music>();
    private ArrayList<Music> friendMusicList = new ArrayList<Music>();
    private ArrayList<Music> localMusicList = new ArrayList<Music>();
    private ArrayList<User> friendList = new ArrayList<User>();
    private ArrayList<Music> playingList = new ArrayList<Music>();
    private Map<String,String> header = new HashMap<String,String>();
    private User loginUser;
    private User lover;
    private boolean isExistLover = false;
    private boolean isLogin = false;
    private boolean hasMessage = false;
    private boolean hasMusicMessage = false;
    private boolean hasSystemMessage = false;
    private NotificationManager notificationManager;
    private Notification notification;

    private PlayerReceiver receiver;
    private PlayerService service;
    /**
     * JMessage代码
     */
    public static final int REQUESTCODE_CONV_LIST = 0;
    public static final int RESULTCODE_CONV_LIST = 2;
    public static final int REQUESTCODE_TAKE_PHOTO = 4;
    public static final int REQUESTCODE_SELECT_PICTURE = 6;
    public static final int RESULTCODE_SELECT_PICTURE = 8;
    public static final int REFRESH_GROUP_NAME = 3000;
    public static final int ADD_GROUP_MEMBER_EVENT = 3001;
    public static final int REMOVE_GROUP_MEMBER_EVENT = 3002;
    public static final int ON_GROUP_EXIT_EVENT = 3003;
    //从服务器收到广播更新消息
    public static String RECEIVE_ACTION = JMessageClient.ACTION_RECEIVE_IM_MESSAGE;
    //从本地收到广播更新聊天界面
    public static String REFRESH_CHATTING_ACTION = "cn.jpush.im.demo.activity.ACTION_RECEIVER_CHATTING_MESSAGE";
    //从本地收到广播更新会话列表界面
    public static String REFRESH_CONVLIST_ACTION = "cn.jpush.im.demo.activity.ACTION_RECEIVE_CONVERSATION_MESSAGE";
    //从本地收到广播更新群成员变动
    public static String ADD_GROUP_MEMBER_ACTION = "cn.jpush.im.demo.activity.ACTION_ADD_GROUP_MEMBER";
    public static String REMOVE_GROUP_MEMBER_ACTION = "cn.jpush.im.demo.activity.ACTION_REMOVE_GROUP_MEMBER";
    public static String UPDATE_GROUP_NAME_ACTION = "cn.jpush.im.demo.activity.ACTION_UPDATE_GROUP_NAME";
    //从本地收到广播（图片）更新会话界面
    public static String REFRESH_CHATTING_ACTION_IMAGE = "refresh_image";
    public static String DEFAULT_MOOD = "NULL1234567890";

    @Override
    public void onCreate() {
        Log.d(TAG, "[MyApplication] onCreate");
        super.onCreate();
        if(mInstance == null){
            mInstance = this;
        }
        preferences = getSharedPreferences("youting",MODE_PRIVATE);
        mQueue = Volley.newRequestQueue(getApplicationContext());
        initList();
        Log.v(TAG,"mqueue");
        JMessageClient.init(this);             //初始化JMessage
        JMessageClient.setNotificationMode(JMessageClient.NOTI_MODE_NO_NOTIFICATION);
        new MessageEventReceiver(getApplicationContext());
//	         JPushInterface.setDebugMode(true); 	// 设置开启日志,发布时请关闭日志
//	         JPushInterface.init(this);     		// 初始化 JPush
        // 锁屏测试！！！
        ComponentName myEventReceiver = new ComponentName(getPackageName(), MediaButtonReceiver.class.getName());
        // MyRemoteControlEventReceiver是一个接收系统控件请求的BoardCastReceiver，需要在manifest中进行注册
	              /* <receiver android:name=".receiver.MediaButtonIntentReceiver">
	                 <intent-filter >
	                    <action android:name="android.intent.action.MEDIA_BUTTON" />
	                 </intent-filter>
	                 </receiver>*/
        AudioManager myAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        // 需要向AudioManager注册
        myAudioManager.registerMediaButtonEventReceiver(myEventReceiver);
        // build the PendingIntent for the remote control client
        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        mediaButtonIntent.setComponent(myEventReceiver);
        PendingIntent mediaPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, mediaButtonIntent, 0);
        // create and register the remote control client
        myRemoteControlClient = new RemoteControlClient(mediaPendingIntent);
        // 在AudioManager中注册RemoteControlClient
        myAudioManager.registerRemoteControlClient(myRemoteControlClient);
//	    myRemoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);

        int flags = RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS
                | RemoteControlClient.FLAG_KEY_MEDIA_NEXT
                | RemoteControlClient.FLAG_KEY_MEDIA_PLAY
                | RemoteControlClient.FLAG_KEY_MEDIA_PAUSE
                | RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE;
        myRemoteControlClient.setTransportControlFlags(flags);
        myAudioManager.requestAudioFocus(new OnAudioFocusChangeListener() {

            @Override
            public void onAudioFocusChange(int focusChange) {
                System.out.println("focusChange = " + focusChange);
            }
        }, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
//     myRemoteControlClient.setOnGetPlaybackPositionListener(new OnGetPlaybackPositionListener() {
//	         @Override
//	         public long onGetPlaybackPosition() {
//	               return 5;
//	         }
//	    });
    }

    public static MyApplication get(){
        return mInstance;
    }

    public void initList(){

        friendList.clear();
        myMusicList.clear();
        friendMusicList.clear();
    }

    public static DaoMaster getDaoMaster(Context context){
        if (daoMaster == null){
            OpenHelper helper = new DaoMaster.DevOpenHelper(context, "my_db", null);
            daoMaster = new DaoMaster(helper.getWritableDatabase());
        }
        return daoMaster;
    }

    public static DaoSession getDaoSession(Context context) {
        if (daoSession == null) {
            if (daoMaster == null) {
                daoMaster = getDaoMaster(context);
            }
            daoSession = daoMaster.newSession();
        }
        return daoSession;
    }

    public RequestQueue getRequestQueue(){

        return mQueue;
    }
    public RemoteControlClient getRemoteControlClient(){
        return myRemoteControlClient;
    }
    public ArrayList<Music> getMyMusicList(){
        return this.myMusicList;

    }
    public void setMyMusicList(ArrayList<Music> list){
        this.myMusicList = list ;
    }
    public void addToMyMusicList(Music music){
        System.out.println("TAG!!!!!!!!!!!"+"add to MyMusicList!");
        this.myMusicList.add(0,music);
    }
    public void RemoveFromMyMusicList(Music music){
        this.myMusicList.remove(music);
    }
    public void setFriendList(ArrayList<User> list){
        this.friendList = list;
    }
    public void addToFriendList(User user){
        this.friendList.add(0,user);
    }
    public void removeFromFriendList(User user){
        this.friendList.remove(user);
    }
    public ArrayList<User> getFriendList(){
        return this.friendList;
    }
    public void setFriendMusicList(ArrayList<Music> list){
        this.friendMusicList = list;
    }

    public ArrayList<Music> getFriendMusicList(){
        return friendMusicList;
    }
    public ArrayList<Music>  getLocalMusicList(){
        return this.localMusicList;
    }
    public void setLocalMusicList(ArrayList<Music> list){
        this.localMusicList = list;
    }
    public ArrayList<Music> getPlayingList(){
        return playingList;
    }
    public void setPlayingList(ArrayList<Music> list){
        this.playingList = list;
    }
    public User getLoginUser(){

        return this.loginUser;
    }
    public void setLoginUser(User user){
        this.loginUser = user;
    }
    /**
     * Checks the response headers for session cookie and saves it
     * if it finds it.
     * @param headers Response Headers.
     */
    public final void checkSessionCookie(Map<String, String> headers) {
        Log.v(TAG,"check begin");
        Log.v(TAG,"headers:"+headers.toString());
        if (headers.containsKey(SET_COOKIE_KEY)
                && headers.get(SET_COOKIE_KEY).startsWith(SESSION_COOKIE)) {
            Log.v(TAG,"check succ");
            String cookie = headers.get(SET_COOKIE_KEY);
            if (cookie.length() > 0) {
                String[] splitCookie = cookie.split(";");
                String[] splitSessionId = splitCookie[0].split("=");
                cookie = splitSessionId[1];
                editor = preferences.edit();
                editor.putString(SESSION_COOKIE, cookie);
                editor.commit();

            }
        }
    }

    /**
     * Adds session cookie to headers if exists.
     * @param headers
     */
    public final void addSessionCookie(Map<String, String> headers) {
        String sessionId = preferences.getString(SESSION_COOKIE, "");
        Log.v(TAG,"add sessionCookie");
        if (sessionId.length() > 0) {
            StringBuilder builder = new StringBuilder();
            builder.append(SESSION_COOKIE);
            builder.append("=");
            builder.append(sessionId);
            if (headers.containsKey(COOKIE_KEY)) {
                builder.append("; ");
                builder.append(headers.get(COOKIE_KEY));
            }
            headers.put(COOKIE_KEY, builder.toString());
        }
    }

    public Notification getNotification() {
        return notification;
    }
    public void setNotification(Notification notification) {
        this.notification = notification;
    }
    public NotificationManager getNotificationManager() {
        return notificationManager;
    }
    public void setNotificationManager(NotificationManager notificationManager) {
        this.notificationManager = notificationManager;
    }
    public PlayerReceiver getReceiver() {
        return receiver;
    }
    public void setReceiver(PlayerReceiver receiver) {
        this.receiver = receiver;
    }
    public PlayerService getService() {
        return service;
    }
    public void setService(PlayerService service) {
        this.service = service;
    }
    public boolean isLogin(){
        return this.isLogin;
    }
    public void setIsLogin(boolean b){
        this.isLogin = b;
    }
    public boolean hasMessage(){
        return this.hasMessage;
    }
    public void setMessage(boolean b){
        this.hasMessage = b;
    }
    public boolean hasMusicMessage(){
        return this.hasMusicMessage;
    }
    public void setMusicMessage(boolean b){
        this.hasMusicMessage = b;
    }
    public boolean hasSystemMessage(){
        return this.hasSystemMessage;
    }
    public void setSystemMessage(boolean b){
        this.hasSystemMessage = b;
    }
    public void setLover(User lover){
        this.lover = lover;
    }
    public User getLover(){
        return this.lover;
    }
    public void setIsExistLover(boolean b){
        this.isExistLover = b;
    }
    public boolean isExistLover(){
        return this.isExistLover;
    }

}

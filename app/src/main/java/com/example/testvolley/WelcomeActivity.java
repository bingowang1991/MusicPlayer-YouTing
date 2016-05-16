package com.example.testvolley;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.request.JsonArrayRequest;
import com.example.request.JsonObjectRequest;
import com.example.util.ExitApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.api.BasicCallback;
import de.greenrobot.dao.query.QueryBuilder;
import de.greenrobot.daoexample.DaoMaster;
import de.greenrobot.daoexample.DaoSession;
import de.greenrobot.daoexample.Music;
import de.greenrobot.daoexample.MusicDao;
import de.greenrobot.daoexample.ShareMessage;
import de.greenrobot.daoexample.ShareMessageDao;
import de.greenrobot.daoexample.User;
import de.greenrobot.daoexample.UserDao;
import de.greenrobot.daoexample.UserDao.Properties;

public class WelcomeActivity extends AppCompatActivity implements WelcomeActivityCallBack {

    private MyApplication application;
    private RequestQueue mQueue;
    private static WelcomeActivityCallBack welcomeActivityCallBack;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private DaoSession daoSession;
    private DaoMaster daoMaster;
    private UserDao userDao;
    private MusicDao musicDao;
    private ShareMessageDao shareMessageDao;
    private QueryBuilder qb,qb_music,qb_user;
    private JSONObject jObject;
    private String name,password,uid,status,alias;
    private ArrayList<Music> myMusicList = new ArrayList<Music>();
    private ArrayList<Music> friendMusicList = new ArrayList<Music>();
    private ArrayList<Music> localMusicList = new ArrayList<Music>();
    private ArrayList<Music> playingList = new ArrayList<Music>();
    private ArrayList<User> friendList = new ArrayList<User>();
    private Map<String,String> headers;
    private JsonObjectRequest jRequest;
    private JsonArrayRequest friendlistRequest,mymusicRequest,friendmusicRequest;
    private String BuffPath = "/youting";
    private final static String login_url = "http://121.42.164.7/index.php/Home/Index/login";
    private final static String get_lover_url = "http://121.42.164.7/index.php/Home/Index/get_lover";
    private final static String get_friend_url = "http://121.42.164.7/index.php/Home/Index/get_friend_list";
    private final static String get_mymusic_url = "http://121.42.164.7/index.php/Home/Index/get_my_music";
    private final static String get_friendmusic_url = "http://121.42.164.7/index.php/Home/Index/get_friend_music";
    private final static String get_playmusic_url = "http://121.42.164.7/index.php/Home/Index/get_play_music";
    private final String qiniu_url = "http://7xi2lw.com1.z0.glb.clouddn.com/";
    private final static String TAG = "welcome";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        ExitApplication.getInstance().addActivity(this);
        welcomeActivityCallBack = this;
        application = (MyApplication)this.getApplicationContext();
        Log.v(TAG,"onCreate");
        mQueue = application.getRequestQueue();
        daoSession = application.getDaoSession(getApplicationContext());
        musicDao = daoSession.getMusicDao();
        userDao = daoSession.getUserDao();
        shareMessageDao = daoSession.getShareMessageDao();
        qb_music = musicDao.queryBuilder();

        // 读取本地音乐
        localMusic();
        preferences = getSharedPreferences("youting",MODE_PRIVATE);
        editor = preferences.edit();
        // 获得上次的播放列表
        String playingList_string = preferences.getString("playingList", null);
        Log.v(TAG,"playingList"+playingList_string);
        if(playingList_string != null){
            try {
                JSONArray jArray = new JSONArray(playingList_string);
                for(int i=0;i<jArray.length();i++){
                    JSONObject jObject = jArray.getJSONObject(i);
                    if(jObject.getLong("uid") == 0){
                        //本地音乐
                        long local_id = Long.parseLong(jObject.get("local_id").toString()) ;
                        Uri uri = Uri.parse("content://media/external/audio/media/" + local_id);
                        ContentResolver mContentResolver = this.getContentResolver();
                        Cursor cursor = mContentResolver.query(uri, new String[]{MediaStore.Audio.Media._ID,
                                MediaStore.Audio.Media.TITLE,MediaStore.Audio.Media.ARTIST,
                                MediaStore.Audio.Media.DATA}, null, null, null);
                        if(cursor.moveToFirst()){
                            String title = cursor.getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                            String creator = cursor.getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                            String filePath = cursor.getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                            int start = Environment.getExternalStorageDirectory().getAbsolutePath().length();
                            int end = filePath.lastIndexOf("/");
                            String name = filePath.substring(end);
                            filePath = filePath.substring(start,end);

                            Log.v(TAG,"filePath:"+filePath);
                            String fake_url = qiniu_url+name;

                            Music music = new Music((long) 0,title,creator,filePath,fake_url,null,null,null,1,local_id);
                            playingList.add(music);

                        }
                        cursor.close();
                    }else{
                        //网络音乐
                        long uid = jObject.getLong("uid");
                        qb_music = musicDao.queryBuilder();
                        qb_music.where(Properties.Uid.eq(uid));
                        if(qb_music.buildCount().count()>0){

                            Music music =  new Music((Music) qb_music.unique());
                            music.setSource(jObject.getInt("source"));
                            music.setParameter(jObject.getLong("parameter"));
                            playingList.add(music);
                        }
                    }
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            application.setPlayingList(playingList);
            Log.v(TAG,"playingList.get(0).getSource()"+playingList.get(0).getSource());
            Log.v(TAG,"playList"+application.getPlayingList().size()+"add playingList"+playingList.size());
        }

        //获得之前储存的myMusicList
        String myMusicList_string = preferences.getString("MY_MUSIC", null);
        Log.v(TAG,"myMusicList_string"+myMusicList_string);
        if(myMusicList_string != null){
            try {
                JSONArray jArray = new JSONArray(myMusicList_string);
                for(int i=0;i<jArray.length();i++){
                    JSONObject jObject = jArray.getJSONObject(i);
                    if(jObject.getLong("uid") == 0){
                        //本地音乐
                        long local_id = Long.parseLong(jObject.get("local_id").toString()) ;
                        Uri uri = Uri.parse("content://media/external/audio/media/" + local_id);
                        ContentResolver mContentResolver = this.getContentResolver();
                        Cursor cursor = mContentResolver.query(uri, new String[]{MediaStore.Audio.Media._ID,
                                MediaStore.Audio.Media.TITLE,MediaStore.Audio.Media.ARTIST,
                                MediaStore.Audio.Media.DATA}, null, null, null);
                        if(cursor.moveToFirst()){
                            String title = cursor.getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                            String creator = cursor.getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                            String filePath = cursor.getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                            int start = Environment.getExternalStorageDirectory().getAbsolutePath().length();
                            int end = filePath.lastIndexOf("/");
                            String name = filePath.substring(end);
                            filePath = filePath.substring(start,end);

                            Log.v(TAG,"filePath:"+filePath);
                            String fake_url = qiniu_url+name;

                            Music music = new Music((long) 0,title,creator,filePath,fake_url,null,null,null,1,local_id);
                            myMusicList.add(0,music);

                        }
                        cursor.close();

                    }else{
                        //网络音乐

                        long uid = jObject.getLong("uid");
                        qb_music = musicDao.queryBuilder();
                        qb_music.where(Properties.Uid.eq(uid));
                        if(qb_music.buildCount().count()>0){
                            Music music = (Music) qb_music.unique();
                            music.setSource(0);
                            music.setParameter((long)0);
                            myMusicList.add(music);
                        }else{
                            Log.v(TAG,"musiclist generate ");
                        }
                    }
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            application.setMyMusicList(myMusicList);
            Log.v(TAG,"playList"+application.getPlayingList().size()+"add playingList"+playingList.size());
        }

        //login
        name = preferences.getString("USER_NAME", "USER_NAME");
        password = preferences.getString("PASSWORD", "PASSWORD");
        jObject = new JSONObject();
        try {
            jObject.put("name", name);
            jObject.put("password", password);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        JsonObjectRequest jRequest = new JsonObjectRequest(Request.Method.POST, login_url, jObject,null,new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject j) {
                Log.v("json",j.toString());
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
                    editor.putString("user", j.toString());
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
                        // 登陆用户信息储存到数据库里

                        qb_user = userDao.queryBuilder();
                        qb_user.where(Properties.Uid.eq(uid));
                        long count = qb_user.buildCount().count();
                        Log.v(TAG,"uid:"+uid);
                        if (count > 0){

                            userDao.update(user);
                            Log.v(TAG,"update");
                        }else{

                            userDao.insert(user);
                        }
                        Log.v(TAG+"login",application.getLoginUser().getAvatar());
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    setAlias(uid);
                    setIMLogin(uid);
                    setFriendList(uid);
                    setMyMusic(uid);
                    setFriendMusic(uid);
                    setLover();
                    new Handler().postDelayed(new Runnable(){
                        public void run() {
                            Intent intent=new Intent();
                            intent.setClass(WelcomeActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }, 2000);
                }else{
                    Log.v(TAG,"log fail");
                    editor.putBoolean("login", false);
                    editor.commit();
                    Toast.makeText(getApplicationContext(), "登陆失败，请重新登陆", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent();
                    intent.setClass(WelcomeActivity.this,MyLoginActivity.class);
                    startActivity(intent);
                }
            }
        },new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                String sb = volleyError.toString();
                Log.v(TAG+"login",sb);
                Toast.makeText(getApplicationContext(), "网络错误", Toast.LENGTH_SHORT).show();

                setFriendList(null);
                setMyMusic(null);
                setFriendMusic(null);
                //读取preferences保存的user信息到application里
                String s = preferences.getString("user", null);
                if(s != null){
                    try {
                        JSONObject j = new JSONObject(s);
                        String uid = j.getString("uid");
                        String name = j.getString("name");
                        String mood = j.getString("mood");
                        String sex = j.getString("sex");
                        String avatar = j.getString("avatar");
                        User user = new User(Long.parseLong(uid),name,sex,mood,avatar);
                        application.setLoginUser(user);
                        application.setIsLogin(true);
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                new Handler().postDelayed(new Runnable(){
                    public void run() {
                        Intent intent=new Intent();
                        intent.setClass(WelcomeActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }, 2000);
            }
        });
        if(!preferences.getBoolean("login", false)){
            new Handler().postDelayed(new Runnable(){
                public void run() {
                    Intent intent=new Intent();
                    intent.setClass(WelcomeActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }, 2000);
        }else{
            mQueue.add(jRequest);
        }
    }

    @Override
    public void setAlias(String uid){
        JPushInterface.setAliasAndTags(getApplicationContext(), uid, null, new TagAliasCallback(){
            @Override
            public void gotResult(int code, String arg1, Set<String> arg2) {
                // TODO Auto-generated method stub
                String logs ;
                switch (code) {
                    case 0:
                        logs = "Set tag and alias success";
                        Log.i(TAG, logs);
                        break;
                    case 6002:
                        logs = "Failed to set alias and tags due to timeout. Try again after 60s.";
                        Log.i(TAG, logs);
                        break;
                    default:
                        logs = "Failed with errorCode = " + code;
                        Log.e(TAG, logs);
                }
                //ExampleUtil.showToast(logs, getApplicationContext());
            }
        });
    }

    @Override
    public void setFriendList(String uid){
        String url = get_friend_url+"?uid="+uid;
        Log.v(TAG,url);
        friendlistRequest = new JsonArrayRequest(url,null,new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                // TODO Auto-generated method stub
                HashSet friendset=new HashSet();
                for(int i=0;i<response.length();i++){
                    String s = null;
                    try {
                        s = (response.getJSONObject(i)).getString("uid");
                        long uid = Long.parseLong(s);
                        String  name = (response.getJSONObject(i)).getString("name");
                        String sex = (response.getJSONObject(i)).getString("sex");
                        String mood = (response.getJSONObject(i)).getString("mood");
                        String avatar = (response.getJSONObject(i)).getString("avatar");

                        // 取出数据保存在手机数据库中
                        userDao = daoSession.getUserDao();
                        //userDao.deleteAll();
                        User user = new User(uid,name,sex,mood,avatar);
                        qb_user = userDao.queryBuilder();
                        qb_user.where(Properties.Uid.eq(uid));
                        long count = qb_user.buildCount().count();
                        Log.v(TAG,"uid:"+uid);
                        if (count > 0){
                            User user1 = new User(uid,name,sex,mood,avatar);
                            userDao.update(user1);
                            Log.v(TAG,"update");
                        }else{

                            userDao.insert(user);
                        }
                        friendList.add(user);
                        Log.v(TAG+"dao","inset new"+user.getUid());
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    friendset.add(s);
                }

                editor.putStringSet("FRIEND_SET", friendset);
                editor.commit();
                application.setFriendList(friendList);
                Log.v(TAG,"friendlist");
//				HashSet set= preferences.getStringSet("FRIEND_SET", null);
//				Log.v("TAG",set.toString());
            }

        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        String sb = volleyError.toString();
                        Log.v(TAG+"frienderr",sb);
                        HashSet set= (HashSet) preferences.getStringSet("FRIEND_SET", null);
                        if (set != null){
                            Iterator iterator = set.iterator();
                            while(iterator.hasNext()){
                                long uid = Long.parseLong((String)iterator.next());
                                qb_user = userDao.queryBuilder();
                                qb_user.where(Properties.Uid.eq(uid));
                                User user =(User) qb_user.uniqueOrThrow();
                                friendList.add(user);
                            }
                            application.setFriendList(friendList);
                        }

                    }
                });
        mQueue.add(friendlistRequest);
    }

    @Override
    public void setMyMusic(String uid){
        String url = get_mymusic_url+"?uid="+uid;
        Log.v(TAG+"mymusic",url);
        // 这里将云端音乐和本地preferences里MY_MUSIC进行比对，对出现本地音乐添加进去
        mymusicRequest = new JsonArrayRequest(url, null,new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                // TODO Auto-generated method stub
                if(response.length() == 0){

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
                        // 取出数据保存在手机数据库中
                        musicDao = daoSession.getMusicDao();
                        //userDao.deleteAll();
                        Music music = new Music(uid,name,artist,null,url,lrc_url,null,pic_url,0,(long)0);
                        if(!isExistInMyMusicList(music)){

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
                            myMusicList.add(0,music);
                            Log.v(TAG+"dao","inset new"+music.getUid());
                        }
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }


                }
                String myMusic_string = saveListToPreferences(myMusicList);
                editor.putString("MY_MUSIC", myMusic_string);
                editor.commit();
                Log.v(TAG+"mymusic","mymusic");
                application.setMyMusicList(myMusicList);
//				if(MainActivity.getMainActivityCallBack()!=null){
//					MainActivity.getMainActivityCallBack().initCallBack();
//				}

//				Set set= preferences.getStringSet("MY_MUSIC_SET", null);
//				Log.v(TAG+"mymusic",set.toString());
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        String sb = volleyError.toString();
                        Log.v(TAG+"mymusic",sb);
                    }
                });
        mQueue.add(mymusicRequest);
    }

    @Override
    public void setFriendMusic(String uid){
        String url = get_friendmusic_url+"?uid="+uid;
        Log.v(TAG,url);
        friendmusicRequest = new JsonArrayRequest(url, null,new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Log.v(TAG+"friendMusic",response.toString());
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
                        //将分享消息保存到shareMessage
                        long user_id = application.getLoginUser().getUid();
                        String share_pic = (response.getJSONObject(i)).getString("share_pic");
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
                            // 存储在数据库中的音乐
                            music = new Music(uid,name,artist,null,url,lrc_url,lrc_cache_url,pic_url,2,shareMessage_id);

                            musicDao.update(music);
                            Log.v(TAG+"friendMusic","update");
                        }else{


                            musicDao.insert(music);
                        }
                        friendMusicList.add(0,music);
                        Log.v(TAG+"dao","inset new"+music.getUid());
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }


                }
                String friendMusic_string = response.toString();
                editor.putString("FRIEND_MUSIC", friendMusic_string);
                editor.commit();
                Log.v(TAG+"friendmusic","friendmusic");
                application.setFriendMusicList(friendMusicList);
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        String sb = volleyError.toString();
                        Log.v(TAG+"friendmusic",sb);
                        String tmp = preferences.getString("FRIEND_MUSIC", null);
                        if (tmp != null){
                            Log.v(TAG,"friendmusic:"+tmp);
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
                                    //long friend_music_id = Long.parseLong((response.getJSONObject(i)).getString("uid"));
                                    long sender_id = Long.parseLong((response.getJSONObject(i)).getString("sender_id"));
                                    String message = (response.getJSONObject(i)).getString("message");

                                    //将分享消息保存到shareMessage
                                    long user_id = application.getLoginUser().getUid();
                                    String share_pic = (response.getJSONObject(i)).getString("share_pic");
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
                                    friendMusicList.add(0,music);
                                    Log.v(TAG+"dao","inset new"+music.getUid());
                                } catch (JSONException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }

                            }
                        }
                        application.setFriendMusicList(friendMusicList);
                    }
                });
        mQueue.add(friendmusicRequest);
    }

    @Override
    public void setIMLogin(String uid){
        Log.v(TAG,"IMLogin");
        JMessageClient.login(application.getLoginUser().getName(), password,
                new BasicCallback() {
                    @Override
                    public void gotResult(final int status, final String desc) {
                        WelcomeActivity.this.runOnUiThread(new Runnable() {
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

    @Override
    public void setLover(){
        Log.v(TAG,"get lover");
        JsonObjectRequest getLoverRequest = new JsonObjectRequest(Method.GET,get_lover_url,null,null,new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                // TODO Auto-generated method stub
                Log.v(TAG,"get lover:"+response.toString());
                try {
                    String status =  response.getString("status");
                    if(status.equals("success")){
                        String name = response.getString("name");
                        long uid = Long.parseLong(response.getString("uid"));
                        String avatar = response.getString("avatar");
                        String mood = response.getString("mood");
                        String sex = response.getString("sex");
                        User user = new User(uid,name,sex,mood,avatar);
                        qb_user = userDao.queryBuilder();
                        qb_user.where(Properties.Uid.eq(uid));
                        long count = qb_user.buildCount().count();
                        Log.v(TAG,"uid:"+uid);
                        if (count > 0){

                            userDao.update(user);
                            Log.v(TAG,"update");
                        }else{

                            userDao.insert(user);
                        }
                        application.setLover(user);
                        application.setIsExistLover(true);

                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        },new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub

            }
        });
        mQueue.add(getLoverRequest);
    }

    //获取未处理的notification
    public void getNotification(){

    }
    //获取本机音乐并储存在localMusiclist中,屏蔽友听已缓存的音乐
    public void localMusic(){
        Cursor cursor;
        Music music;
        String[] audioColumns = { MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA,MediaStore.Audio.Media.ALBUM};
        cursor = managedQuery(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                audioColumns, null, null,MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if (cursor.moveToFirst()){
            do{
                String id = ""+cursor.getInt(cursor
                        .getColumnIndex(MediaStore.Audio.Media._ID));
                String title = cursor.getString(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                String creator = cursor.getString(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                String filePath = cursor.getString(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                int start = Environment.getExternalStorageDirectory().getAbsolutePath().length();
                int end = filePath.lastIndexOf("/");
                String name = filePath.substring(end);
                filePath = filePath.substring(start,end);
                if(!filePath.equals(BuffPath)){
                    Log.v(TAG,"filePath:"+filePath);
                    String fake_url = qiniu_url+name;
                    Log.v(TAG,"id:"+id);
                    long local_id = Long.parseLong(id);
                    music = new Music((long) 0,title,creator,filePath,fake_url,null,null,null,1,local_id);
                    localMusicList.add(music);
                }

            }while(cursor.moveToNext());
            application.setLocalMusicList(localMusicList);
        }
    }
    public static void setWelcomeActivityCallBack(WelcomeActivityCallBack welcomeActivityCallBack){
        WelcomeActivity.welcomeActivityCallBack = welcomeActivityCallBack;
    }
    public static WelcomeActivityCallBack getWelcomeActivityCallBack(){
        return welcomeActivityCallBack;
    }
    public boolean isExistInMyMusicList(Music m){

        for(int i=0;i<myMusicList.size();i++){

            if(m.getUid() == myMusicList.get(i).getUid()){
                return true;
            }
        }
        return false;
    }
    public String saveListToPreferences(ArrayList<Music> list){
        JSONArray jArray = new JSONArray();
        for (int i=list.size()-1;i >=0;i--){
            Music music = list.get(i);
            JSONObject jObject = new JSONObject();

            Log.v(TAG,music.getName());
            long uid = music.getUid();
            long local_id = 0;
            if(music.getSource() == 1){
                local_id = music.getParameter();
            }
            try {
                jObject.put("uid", uid);
                jObject.put("local_id", local_id);

                jArray.put(i, jObject);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        return jArray.toString();
    }

}

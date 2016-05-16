package com.example.testvolley;



import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;

import cn.jpush.android.api.JPushInterface;
import de.greenrobot.dao.query.QueryBuilder;
import de.greenrobot.daoexample.DaoMaster;
import de.greenrobot.daoexample.DaoSession;
import de.greenrobot.daoexample.Music;
import de.greenrobot.daoexample.MusicDao;
import de.greenrobot.daoexample.MusicMessage;
import de.greenrobot.daoexample.MusicMessageDao;
import de.greenrobot.daoexample.ShareMessageDao;
import de.greenrobot.daoexample.SystemMessage;
import de.greenrobot.daoexample.SystemMessageDao;
import de.greenrobot.daoexample.User;
import de.greenrobot.daoexample.UserDao;
import de.greenrobot.daoexample.UserDao.Properties;

/**
 * 自定义接收器
 *
 * 如果不定义这个 Receiver，则：
 * 1) 默认用户会打开主界面
 * 2) 接收不到自定义消息
 */
public class JpushReceiver extends BroadcastReceiver {
    private static final String TAG = "JpushReceiver";
    private String category;
    private MyApplication application = MyApplication.get();
    private DaoSession daoSession;
    private DaoMaster daoMaster;
    private UserDao userDao;
    private MusicDao musicDao;
    private SystemMessageDao systemMessageDao;
    private MusicMessageDao musicMessageDao;
    private ShareMessageDao shareMessageDao;
    private QueryBuilder qb_user,qb_music,qb_systemMessage,qb_musicMessage,qb;

    private static final String MainActivityName = "ComponentInfo{com.example.testvolley/com.example.testvolley.MainActivity}";

    public JpushReceiver(){
        daoSession = application.getDaoSession(application.getApplicationContext());
        userDao = daoSession.getUserDao();
        musicDao = daoSession.getMusicDao();
        systemMessageDao = daoSession.getSystemMessageDao();
        musicMessageDao = daoSession.getMusicMessageDao();
        shareMessageDao = daoSession.getShareMessageDao();
        qb_user = userDao.queryBuilder();
        qb_music = musicDao.queryBuilder();
        qb_systemMessage = systemMessageDao.queryBuilder();
        qb_musicMessage = musicMessageDao.queryBuilder();
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        Log.d(TAG, "[JpushReceiver] onReceive - " + intent.getAction() + ", extras: " + printBundle(bundle));


        if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
            String regId = bundle.getString(JPushInterface.EXTRA_REGISTRATION_ID);
            Log.d(TAG, "[JpushReceiver] 接收Registration Id : " + regId);
            //send the Registration Id to your server...

        } else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
            Log.d(TAG, "[JpushReceiver] 接收到推送下来的自定义消息: " + bundle.getString(JPushInterface.EXTRA_MESSAGE));
            Log.v(TAG,"process");
            processCustomMessage(context, bundle);
            application.setMessage(true);
            // 刷新主页面上的红点
            Log.v(TAG,"topActivity:"+getTopActivity(context));
            if(getTopActivity(context).equals(MainActivityName)){
                if(MainActivity.getMainActivityCallBack()!=null){
                    MainActivity.getMainActivityCallBack().setUserInfo();
                }
            }

        } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
            Log.d(TAG, "[JpushReceiver] 接收到推送下来的通知");
            int notifactionId = bundle.getInt(JPushInterface.EXTRA_NOTIFICATION_ID);
            Log.d(TAG, "[JPushReceiver] 接收到推送下来的通知的ID: " + notifactionId);

        } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
            Log.d(TAG, "[JpushReceiver] 用户点击打开了通知");
            String j = bundle.getString("cn.jpush.android.EXTRA");
            try {
                JSONObject jObject = new JSONObject(j);
                category = jObject.getString("category");
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            Log.v(TAG,category);
            //打开自定义的Activity
            if(category.equals("add_friend")){
                Intent i = new Intent(context,MyMessageActivity.class);
                if(context == null){
                    Log.v(TAG,"context null");
                }
                i.putExtras(bundle);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP );
                context.startActivity(i);

                application.setSystemMessage(false);
            }
            else if (category.equals("confirm_friend")){
                Intent i = new Intent(context,MyMessageActivity.class);
                if(context == null){
                    Log.v(TAG,"context null");
                }
                i.putExtras(bundle);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP );
                context.startActivity(i);

                application.setSystemMessage(false);
            }else if(category.equals("add_lover")){
                Intent i = new Intent(context,MyMessageActivity.class);
                if(context == null){
                    Log.v(TAG,"context null");
                }
                i.putExtras(bundle);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP );
                context.startActivity(i);
                application.setSystemMessage(false);
            }else if(category.equals("confirm_lover")){
                Intent i = new Intent(context,MyMessageActivity.class);
                if(context == null){
                    Log.v(TAG,"context null");
                }
                i.putExtras(bundle);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP );
                context.startActivity(i);
                application.setSystemMessage(false);
            }
            else if(category.equals("share_one")){
                Intent i = new Intent(context,MusicMessageActivity.class);
                i.putExtras(bundle);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP );
                context.startActivity(i);
                application.setMusicMessage(false);
            }

//        	   switch(category){
//            	case "add_friend":
//            		Intent i = new Intent(context,MyMessageActivity.class);
//            		i.putExtras(bundle);
//                	i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                	i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP );
//            		context.startActivity(i);
//            		break;
//            	case "confirm_friend":
//            		break;
//            	case "share_one":
//            		break;
//
//            }


        } else if (JPushInterface.ACTION_RICHPUSH_CALLBACK.equals(intent.getAction())) {
            Log.d(TAG, "[MyReceiver] 用户收到到RICH PUSH CALLBACK: " + bundle.getString(JPushInterface.EXTRA_EXTRA));
            //在这里根据 JPushInterface.EXTRA_EXTRA 的内容处理代码，比如打开新的Activity， 打开一个网页等..

        } else if(JPushInterface.ACTION_CONNECTION_CHANGE.equals(intent.getAction())) {
            boolean connected = intent.getBooleanExtra(JPushInterface.EXTRA_CONNECTION_CHANGE, false);
            Log.w(TAG, "[MyReceiver]" + intent.getAction() +" connected state change to "+connected);
        } else {
            Log.d(TAG, "[MyReceiver] Unhandled intent - " + intent.getAction());
        }
    }

    // 打印所有的 intent extra 数据
    private static String printBundle(Bundle bundle) {
        StringBuilder sb = new StringBuilder();
        for (String key : bundle.keySet()) {
            if (key.equals(JPushInterface.EXTRA_NOTIFICATION_ID)) {
                sb.append("\nkey:" + key + ", value:" + bundle.getInt(key));
            }else if(key.equals(JPushInterface.EXTRA_CONNECTION_CHANGE)){
                sb.append("\nkey:" + key + ", value:" + bundle.getBoolean(key));
            }
            else {
                sb.append("\nkey:" + key + ", value:" + bundle.getString(key));
            }
        }
        return sb.toString();
    }

    //process the message
    private void processCustomMessage(Context context, Bundle bundle) {
        Log.v(TAG,"processCustomMessage");
        //处理数据，并加入greedDao数据库
        category = bundle.getString(JPushInterface.EXTRA_MESSAGE);
        String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
        if(!ExampleUtil.isEmpty(extras)){
            try {
                JSONObject extraJson = new JSONObject(extras);

                if (null != extraJson && extraJson.length() > 0) {
                    if(category.equals("add_friend")){
                        // 添加用户进数据库
                        application.setSystemMessage(true);
                        Log.v(TAG,extraJson.toString());
                        long uid = Long.parseLong(extraJson.getString("uid"));
                        long user_id = Long.parseLong(extraJson.getString("user_id"));
                        String name = extraJson.getString("name");
                        String sex = extraJson.getString("sex");
                        String avatar = extraJson.getString("avatar");
                        String mood = extraJson.getString("mood");
                        String message = extraJson.getString("message");
                        int act = 0;
                        User user = new User(uid,name,sex,mood,avatar);
                        qb_user.where(Properties.Uid.eq(uid));
                        long count = qb_user.buildCount().count();
                        if (count > 0){

                            userDao.update(user);
                            Log.v(TAG,"update");
                        }else{

                            userDao.insert(user);
                        }

                        //添加systemMessage进数据库
                        long milliSecond = Long.parseLong(extraJson.getString("create_time"))*1000;
                        Date date = new Date(milliSecond);

                        SystemMessage systemMessage = new SystemMessage(null,user_id,uid,category,message,null,date,act);
                        systemMessageDao.insert(systemMessage);
                    }else if(category.equals("confirm_friend")){
                        // 添加用户进数据库
                        application.setSystemMessage(true);
                        long uid = Long.parseLong(extraJson.getString("uid"));
                        long user_id = Long.parseLong(extraJson.getString("user_id"));
                        String name = extraJson.getString("name");
                        String sex = extraJson.getString("sex");
                        String avatar = extraJson.getString("avatar");
                        String mood = extraJson.getString("mood");
                        String message = extraJson.getString("message");
                        int act = 0;
                        User user = new User(uid,name,sex,mood,avatar);
                        qb_user.where(Properties.Uid.eq(uid));
                        long count = qb_user.buildCount().count();
                        if (count > 0){

                            userDao.update(user);
                            Log.v(TAG,"update");
                        }else{

                            userDao.insert(user);
                        }
                        //添加进好友列表，判断此人是否已在friendlist里
                        if(!isExistInFriendList(uid)){
                            application.addToFriendList(user);
                        }

                        //添加systemMessage进数据库
                        long milliSecond = Long.parseLong(extraJson.getString("create_time"))*1000;
                        Date date = new Date(milliSecond);
                        SystemMessage systemMessage = new SystemMessage(null,user_id,uid,category,message,null,date,act);
                        systemMessageDao.insert(systemMessage);

                    }else if(category.equals("add_lover")){
                        // 添加用户进数据库
                        application.setSystemMessage(true);
                        Log.v(TAG+"process",extraJson.toString());
                        long uid = Long.parseLong(extraJson.getString("uid"));
                        long user_id = Long.parseLong(extraJson.getString("user_id"));
                        String name = extraJson.getString("name");
                        String sex = extraJson.getString("sex");
                        String avatar = extraJson.getString("avatar");
                        String mood = extraJson.getString("mood");
                        String message = extraJson.getString("message");
                        int act = 0;
                        User user = new User(uid,name,sex,mood,avatar);
                        qb_user.where(Properties.Uid.eq(uid));
                        long count = qb_user.buildCount().count();
                        if (count > 0){

                            userDao.update(user);
                            Log.v(TAG,"update");
                        }else{

                            userDao.insert(user);
                            Log.v(TAG, "insert");
                        }
                        //添加systemMessage进数据库
                        long milliSecond = Long.parseLong(extraJson.getString("create_time"))*1000;
                        Date date = new Date(milliSecond);
                        Log.v(TAG,"create_time:"+date);
                        SystemMessage systemMessage = new SystemMessage(null,user_id,uid,category,message,null,date,act);
                        systemMessageDao.insert(systemMessage);
                    }else if(category.equals("confirm_lover")){
                        // 添加用户进数据库
                        application.setSystemMessage(true);
                        long uid = Long.parseLong(extraJson.getString("uid"));
                        long user_id = Long.parseLong(extraJson.getString("user_id"));
                        String name = extraJson.getString("name");
                        String sex = extraJson.getString("sex");
                        String avatar = extraJson.getString("avatar");
                        String mood = extraJson.getString("mood");
                        String message = extraJson.getString("message");
                        int act = 0;
                        User user = new User(uid,name,sex,mood,avatar);
                        qb_user.where(Properties.Uid.eq(uid));
                        long count = qb_user.buildCount().count();
                        if (count > 0){

                            userDao.update(user);
                            Log.v(TAG,"update");
                        }else{

                            userDao.insert(user);
                        }
                        // 设置lover
                        application.setLover(user);
                        application.setIsExistLover(true);
                        //添加systemMessage进数据库
                        long milliSecond = Long.parseLong(extraJson.getString("create_time"))*1000;
                        Date date = new Date(milliSecond);
                        SystemMessage systemMessage = new SystemMessage(null,user_id,uid,category,message,null,date,act);
                        long id = systemMessageDao.insert(systemMessage);
                        Log.v(TAG,"smDaoinsert:"+id);
                    }
                    else if(category.equals("share_one")){
                        // 添加用户进数据库
                        application.setMusicMessage(true);
                        long uid = Long.parseLong(extraJson.getString("uid"));
                        long user_id = Long.parseLong(extraJson.getString("user_id"));

                        String name = extraJson.getString("name");

                        String sex = extraJson.getString("sex");
                        String avatar = extraJson.getString("avatar");
                        String mood = extraJson.getString("mood");
                        String message = extraJson.getString("message");
                        User user = new User(uid,name,sex,mood,avatar);
                        qb_user.where(Properties.Uid.eq(uid));
                        long count = qb_user.buildCount().count();
                        if (count > 0){

                            userDao.update(user);
                            Log.v(TAG,"update");
                        }else{

                            userDao.insert(user);
                        }
                        //添加ShareMessage进数据库
//						ShareMessage shareMessage = new ShareMessage(null,user_id,uid,message);
//						long shareMessage_id = shareMessageDao.insert(shareMessage);

                        // 添加新歌曲进数据库
                        long music_id = Long.parseLong(extraJson.getString("music_id"));
                        String  music_name = (extraJson.getString("music_name"));
                        String music_artist = (extraJson.getString("music_artist"));
                        String music_url = (extraJson.getString("music_url"));
                        String music_lrc_url = (extraJson.getString("music_lrc_url"));
                        String music_pic_url = (extraJson.getString("music_pic_url"));
                        Music music = new Music(music_id,music_name,music_artist,null,music_url,music_lrc_url,null,music_pic_url,0,(long)0);
                        qb_music.where(de.greenrobot.daoexample.MusicDao.Properties.Uid.eq(music_id));
                        long music_count = qb_music.buildCount().count();

                        if (music_count > 0){
                            Music music_tmp = (Music)qb_music.unique();
                            if(music_tmp.getLrc_cache_url() != null){
                                String lrc_cache_url = music_tmp.getLrc_cache_url();
                                music = new Music(music_id,music_name,music_artist,null,music_url,music_lrc_url,lrc_cache_url,music_pic_url,0,(long)0);
                                musicDao.update(music);
                                Log.v(TAG,"update");
                            }

                        }else{

                            musicDao.insert(music);
                        }

                        //String pic_url = extraJson.getString("pic_url");
                        long milliSecond = Long.parseLong(extraJson.getString("create_time"))*1000;
                        Date date = new Date(milliSecond);
                        String pic_url = extraJson.getString("pic_url");
                        MusicMessage musicMessage = new MusicMessage(null,user_id,uid,music_id,category,message,date,pic_url);
                        musicMessageDao.insert(musicMessage);
                        Log.v(TAG,"musicMessageDao count"+musicMessageDao.count());

                    }else if(category.equals("share_all")){
                        // 添加用户进数据库
                        application.setMusicMessage(true);
                        long user_id = application.getLoginUser().getUid();
                        long uid = Long.parseLong(extraJson.getString("uid"));
                        String name = extraJson.getString("name");
                        String sex = extraJson.getString("sex");
                        String avatar = extraJson.getString("avatar");
                        String mood = extraJson.getString("mood");
                        String message = extraJson.getString("message");
                        User user = new User(uid,name,sex,mood,avatar);
                        qb_user.where(Properties.Uid.eq(uid));
                        long count = qb_user.buildCount().count();

                        if (count > 0){
                            Log.v(TAG,((User)qb_user.unique()).getName());
                            userDao.update(user);
                            Log.v(TAG,"update");
                        }else{
                            userDao.insert(user);
                        }
                        //添加ShareMessage进数据库
//						ShareMessage shareMessage = new ShareMessage(null,user_id,uid,message);
//						long shareMessage_id = shareMessageDao.insert(shareMessage);
                        // 添加新歌曲进数据库
                        long music_id = Long.parseLong(extraJson.getString("music_id"));
                        String  music_name = (extraJson.getString("music_name"));
                        String music_artist = (extraJson.getString("music_artist"));
                        String music_url = (extraJson.getString("music_url"));
                        String music_lrc_url = (extraJson.getString("music_lrc_url"));
                        String music_pic_url = (extraJson.getString("music_pic_url"));
                        Music music = new Music(music_id,music_name,music_artist,null,music_url,music_lrc_url,null,music_pic_url,0,(long)0);
                        qb_music.where(de.greenrobot.daoexample.MusicDao.Properties.Uid.eq(music_id));
                        long music_count = qb_music.buildCount().count();
                        if (music_count > 0){
                            Music music_tmp = (Music)qb_music.unique();
                            String lrc_cache_url = music_tmp.getLrc_cache_url();
                            music = new Music(music_id,music_name,music_artist,null,music_url,music_lrc_url,lrc_cache_url,music_pic_url,0,(long)0);
                            musicDao.update(music);
                            Log.v(TAG,"update");
                        }else{

                            musicDao.insert(music);
                        }
                        //添加systemMessage进数据库
                        //String pic_url = extraJson.getString("pic_url");
                        long milliSecond = Long.parseLong(extraJson.getString("create_time"))*1000;
                        Date date = new Date(milliSecond);
                        String pic_url = extraJson.getString("pic_url");
                        MusicMessage musicMessage = new MusicMessage(null,user_id,uid,music_id,category,message,date,pic_url);
                        Log.v(TAG,"name"+uid);
                        musicMessageDao.insert(musicMessage);

                    }else{

                    }
                }
            } catch (JSONException e) {

            }
        }
//		if (MainActivity.isForeground) {
//
//			String message = bundle.getString(JPushInterface.EXTRA_MESSAGE);
//			category = message;
//			String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
////			Intent msgIntent = new Intent(MainActivity.MESSAGE_RECEIVED_ACTION);
////			msgIntent.putExtra(MainActivity.KEY_MESSAGE, message);
//			if (!ExampleUtil.isEmpty(extras)) {
//				try {
//					JSONObject extraJson = new JSONObject(extras);
//					if (null != extraJson && extraJson.length() > 0) {
////						msgIntent.putExtra(MainActivity.KEY_EXTRAS, extras);
//					}
//				} catch (JSONException e) {
//
//				}
//
//			}
////			context.sendBroadcast(msgIntent);
//		}
    }
    private boolean isExistInFriendList(long user_id){
        for(int i =0;i<application.getFriendList().size();i++){
            if(user_id == application.getFriendList().get(i).getUid()){
                return true;
            }
        }
        return false;
    }
    public String getTopActivity(Context context)
    {
        ActivityManager manager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE) ;
        List<RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1) ;

        if(runningTaskInfos != null)
            return (runningTaskInfos.get(0).topActivity).toString() ;
        else
            return null ;
    }
}

package com.example.testvolley;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.util.Log;
import android.widget.RemoteViews;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.example.service.PlayerService;

import java.util.ArrayList;

import de.greenrobot.daoexample.Music;

public class PlayerReceiver extends BroadcastReceiver {

    MyApplication myApplication=MyApplication.get();
    private RequestQueue mQueue;
    private String TAG ="playerreceive";
    private PlayerService playerservice ;
    private  ArrayList<Music> playList;
    private  int index;

    public PlayerReceiver(){
        mQueue = myApplication.getRequestQueue();

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action=intent.getStringExtra("action");
        //Log.d("action", action);
        if(action.equals("close")){
            myApplication.getNotificationManager().cancelAll();
            myApplication.getService().pause();
            myApplication.getService().stopSelf();
            System.exit(0);
        }else if(action.equals("playing")){
            myApplication.getNotification().bigContentView.setImageViewResource(R.id.play_pause_music, R.mipmap.notification_play);
            myApplication.getNotification().contentView.setImageViewResource(R.id.play_pause_music, R.mipmap.notification_play);
            myApplication.getNotificationManager().notify(1, myApplication.getNotification());

        }else if(action.equals("pause")){

            myApplication.getNotification().contentView.setImageViewResource(R.id.play_pause_music, R.mipmap.notification_stop);
            myApplication.getNotification().bigContentView.setImageViewResource(R.id.play_pause_music, R.mipmap.notification_stop);
            myApplication.getNotificationManager().notify(1, myApplication.getNotification());
        }
        else if(action.equals("next")){
            Log.v(TAG,"next");
            playerservice =myApplication.getService();
            playList=playerservice.getPlayList();
            index=playerservice.getIndex();
            myApplication.getNotification().contentView = new RemoteViews(myApplication.getPackageName(),
                    R.layout.music_notification);
            myApplication.getNotification().bigContentView = new RemoteViews(myApplication.getPackageName(),
                    R.layout.music_notification_normal);
            myApplication.getNotification().contentView.setTextViewText(R.id.notification_song_name, playList.get(index).getName()==""?"未知歌名":playList.get(index).getName());
            myApplication.getNotification().contentView.setTextViewText(R.id.notification_singer_name,playList.get(index).getArtist()==""?"未知艺术家":playList.get(index).getArtist());
            myApplication.getNotification().bigContentView.setTextViewText(R.id.notification_song_name, playList.get(index).getName()==""?"未知歌名":playList.get(index).getName());
            myApplication.getNotification().bigContentView.setTextViewText(R.id.notification_singer_name,playList.get(index).getArtist()==""?"未知艺术家":playList.get(index).getArtist());
            ImageRequest imgRequest=new ImageRequest(playList.get(index).getPic_url(), new Response.Listener<Bitmap>() {
                @Override
                public void onResponse(Bitmap arg0) {
                    // TODO Auto-generated method stub
                    Log.v(TAG+"notification","onresponse");
                    myApplication.getNotification().contentView.setImageViewBitmap(R.id.notification_singer_pic, arg0);
                    myApplication.getNotification().bigContentView.setImageViewBitmap(R.id.notification_singer_pic, arg0);
                    myApplication.getNotificationManager().notify(1, myApplication.getNotification());
                }
            }, 50, 50, Config.RGB_565, new ErrorListener(){
                @Override
                public void onErrorResponse(VolleyError arg0) {
                    // TODO Auto-generated method stub
                    Log.v(TAG,"onerror");
                    myApplication.getNotification().contentView.setImageViewResource(R.id.notification_singer_pic,R.mipmap.music_cover);
                    myApplication.getNotification().bigContentView.setImageViewResource(R.id.notification_singer_pic,R.mipmap.music_cover);
                }
            });
            mQueue.add(imgRequest);
            myApplication.getNotificationManager().notify(1, myApplication.getNotification());

        }
    }

}

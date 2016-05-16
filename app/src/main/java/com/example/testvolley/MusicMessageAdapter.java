package com.example.testvolley;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.example.service.PlayerService;
import com.example.util.TimeHelper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;

import de.greenrobot.daoexample.Music;
import de.greenrobot.daoexample.MusicMessage;
import de.greenrobot.daoexample.User;

/**
 * Created by Administrator on 2016/5/15.
 */
public class MusicMessageAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<MusicMessage> mmList;
    private ArrayList<User> userList;
    private ArrayList<Music> musicList;
    private MyApplication application;
    private RequestQueue mQueue;
    private ImageLoader imageLoader;
    private MusicMessage mm;
    private PlayerService service;
    private Music music;
    private int play_position = 10000;
    WeakReference<Activity> weak; // 定义弱引用变量

    private static final String confirm_friend = "http://121.42.164.7/index.php/Home/Index/confirm_friend";
    private static final String TAG = "MusicMessageAdapter";

    public MusicMessageAdapter(Context context, ArrayList<MusicMessage> mmList, ArrayList<User> userList, ArrayList<Music> musicList){
        this.context = context;
        this.userList = userList;
        this.mmList = mmList;
        this.musicList = musicList;
        this.weak = new WeakReference<Activity>((Activity)context);

        application = MyApplication.get();
        mQueue = application.getRequestQueue();
        imageLoader = new ImageLoader(mQueue,new BitmapCache());
        service = application.getService();
    }

    @Override
    public int getCount() {
        return mmList.size();
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub

        convertView = LayoutInflater.from(context).inflate(R.layout.item_music_message, null);
        final ImageButton play_button = (ImageButton)convertView.findViewById(R.id.play);
        TextView message_view = (TextView)convertView.findViewById(R.id.message);
        TextView name_view = (TextView)convertView.findViewById(R.id.name);
        TextView artist_view = (TextView)convertView.findViewById(R.id.artist);
        TextView music_name_view = (TextView)convertView.findViewById(R.id.music_name);
        TextView time_view = (TextView)convertView.findViewById(R.id.time);
        NetworkImageView music_pic_view = (NetworkImageView)convertView.findViewById(R.id.music_pic);
        NetworkImageView avatar_view = (NetworkImageView)convertView.findViewById(R.id.avatar);

        music_pic_view.setDefaultImageResId(R.mipmap.music_cover);
        music_pic_view.setErrorImageResId(R.mipmap.music_cover);
        avatar_view.setDefaultImageResId(R.mipmap.ic_launcher);
        avatar_view.setErrorImageResId(R.mipmap.ic_launcher);

        if(position == play_position){
            play_button.setImageResource(R.mipmap.mini_stop);
        }
        mm = mmList.get(position);
        String category = mm.getCategory();
        Date date = mm.getCreate_time();
        time_view.setText(TimeHelper.dateToFormatString(date));
        final User user = userList.get(position);
        if(mm.getMessage().equals(application.DEFAULT_MOOD)){
            message_view.setMaxHeight(0);
            message_view.setVisibility(View.INVISIBLE);
        }else{
            message_view.setText(mm.getMessage());
        }
        name_view.setText(user.getName());
        String avatar = user.getAvatar();
        String share_pic = mm.getPic_url();

        if(!avatar.equals("null")){
            avatar_view.setImageUrl(avatar,imageLoader);
            Log.v(TAG,"avatar:"+avatar);
        }

        music = musicList.get(position);
        music_name_view.setText(music.getName());
        artist_view.setText(music.getArtist());
        String pic_url = music.getPic_url();
        if(!share_pic.equals("null")){
            Log.v(TAG,"share_pic:"+share_pic);
            music_pic_view.setImageUrl(share_pic,imageLoader);
        }else{
            if(!pic_url.equals("null")){
                music_pic_view.setImageUrl(pic_url, imageLoader);
            }
        }

        play_button.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                music = musicList.get(position);
                ImageButton button = (ImageButton)v;
                Log.v(TAG,"position"+music.getName());
                if(service.getMusic() != null && service.getMusic().equals(music)){
                    if(service.isPlayFlag()){
                        Log.v(TAG,"1");
                        service.pause();
                        button.setImageResource(R.mipmap.mini_play);
                    }else{
                        Log.v(TAG,"2");
                        service.play();
                        button.setImageResource(R.mipmap.mini_stop);
                    }

                }else{
                    Log.v(TAG,"3");
                    MusicMessageAdapter.this.notifyDataSetChanged();
                    service.playSong(music);
//					MainActivity.getMainActivityCallBack().setPage();
                    button.setImageResource(R.mipmap.mini_stop);
                    play_position = position;
                }
            }

        });

        name_view.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent=new Intent();
                intent.setClass(weak.get(),FriendInfoActivity.class);
                intent.putExtra("user_id",user.getUid());
                intent.putExtra("name",user.getName());
                intent.putExtra("mood", user.getMood());
                intent.putExtra("avatar", user.getAvatar());
                intent.putExtra("sex", user.getSex());
                context.startActivity(intent);
            }
        });
        return convertView;
    }
}

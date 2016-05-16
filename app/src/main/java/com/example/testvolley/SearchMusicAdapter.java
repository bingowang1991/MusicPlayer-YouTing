package com.example.testvolley;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.example.request.StringRequest;
import com.example.service.PlayerService;

import java.util.ArrayList;

import de.greenrobot.dao.query.QueryBuilder;
import de.greenrobot.daoexample.DaoSession;
import de.greenrobot.daoexample.Music;
import de.greenrobot.daoexample.MusicDao;
import de.greenrobot.daoexample.MusicMessage;
import de.greenrobot.daoexample.MusicDao.Properties;

/**
 * Created by Administrator on 2016/5/15.
 */
public class SearchMusicAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Music> musicList,myMusicList;
    private MyApplication application;
    private RequestQueue mQueue;
    private ImageLoader imageLoader;
    private MusicMessage mm;
    private DaoSession daoSession;
    private MusicDao musicDao;
    private QueryBuilder qb;
    private StringRequest addRequest,delRequest;
    private PlayerService service;
    private Music music;
    private int play_position = 10000;


    private static final String TAG = "SearchMusicAdapter";
    private static final String add_my_music = "http://121.42.164.7/index.php/Home/Index/add_my_music";
    private static final String delete_my_music = "http://121.42.164.7/index.php/Home/Index/delete_my_music";

    public SearchMusicAdapter(Context context, ArrayList<Music> musicList){
        this.context = context;
        this.musicList = musicList;

        application = MyApplication.get();
        myMusicList = application.getMyMusicList();
        mQueue = application.getRequestQueue();
        imageLoader = new ImageLoader(mQueue,new BitmapCache());
        daoSession = application.getDaoSession(context);
        musicDao = daoSession.getMusicDao();
        service = application.getService();
    }

    @Override
    public int getCount() {
        return musicList.size();
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

        convertView = LayoutInflater.from(context).inflate(R.layout.item_search_music, null);
        LinearLayout info_layout = (LinearLayout)convertView.findViewById(R.id.music_info);
        ImageButton like_button = (ImageButton)convertView.findViewById(R.id.like);
        final TextView name_view = (TextView)convertView.findViewById(R.id.name);
        final TextView artist_view = (TextView)convertView.findViewById(R.id.artist);
        final ImageView playing_flag_view = (ImageView)convertView.findViewById(R.id.playing_flag);

        if(position == play_position){
            playing_flag_view.setVisibility(View.VISIBLE);
            name_view.setTextColor(0xff20B2AA);
            artist_view.setTextColor(0xff20B2AA);
        }
        like_button.setTag("0");

        music = musicList.get(position);
        long uid = music.getUid();
        qb =musicDao.queryBuilder();
        qb.where(Properties.Uid.eq(uid));
        if(qb.buildCount().count()>0){
            music = (Music)qb.unique();
            Log.v(TAG,music.getName());
        }
        name_view.setText(music.getName());
        artist_view.setText(music.getArtist());

        if(ismyMusicListContain(music)){
            Log.v(TAG,"contains");
            //	like_button.setImageResource(R.drawable.ic_lover);
            like_button.setTag("1");
        }

        like_button.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(final View v) {
                // TODO Auto-generated method stub
//				final ImageButton button = (ImageButton) v;
                String tag = (String)v.getTag();
                Log.v(TAG,"position:"+position+"tag:"+tag);
                music = musicList.get(position);
                long uid = music.getUid();
                String add_url = add_my_music+"?music_id="+uid;
                String del_url = delete_my_music+"?music_id="+uid;

                addRequest = new StringRequest(Method.GET,add_url,null,new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        // TODO Auto-generated method stub
                        ((ImageButton)v).setImageResource(R.mipmap.search_like_activated);
                        v.setTag("1");
                        music = musicList.get(position);
                        application.addToMyMusicList(music);
                    }
                },new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub

                    }
                });
                delRequest = new StringRequest(Method.GET,del_url,null,new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        // TODO Auto-generated method stub
                        ((ImageButton)v).setImageResource(R.mipmap.search_like);
                        v.setTag("0");
                        music = musicList.get(position);
                        long uid = music.getUid();
                        qb =musicDao.queryBuilder();
                        qb.where(Properties.Uid.eq(uid));
                        if(qb.buildCount().count()>0){
                            music = (Music)qb.unique();
                            Log.v(TAG,music.getName());
                        }
                        application.RemoveFromMyMusicList(music);
                    }
                },new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub

                    }
                });
                if(tag.equals("0")){
                    mQueue.add(addRequest);
                }else{
                    mQueue.add(delRequest);
                }
            }

        });
        info_layout.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                music = musicList.get(position);

                Log.v(TAG,"position"+music.getName());
                if(service.getMusic() != null && service.getMusic().equals(music)){
                    if(service.isPlayFlag()){
                        Log.v(TAG,"1");
                        service.pause();

                    }else{
                        Log.v(TAG,"2");
                        service.play();
                        playing_flag_view.setVisibility(View.VISIBLE);
                        name_view.setTextColor(0xff20B2AA);
                        artist_view.setTextColor(0xff20B2AA);
                    }

                }else{
                    Log.v(TAG,"3");
                    SearchMusicAdapter.this.notifyDataSetChanged();
                    service.playSong(music);
                    MainActivity.getMainActivityCallBack().setPage();
                    play_position = position;
                }
            }
        });
        return convertView;
    }

    public boolean ismyMusicListContain(Music m){
        for(int i=0;i<myMusicList.size();i++){
            if(m.getUid() == myMusicList.get(i).getUid()){
                return true;
            }
        }
        return false;
    }
}

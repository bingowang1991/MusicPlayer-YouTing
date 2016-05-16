package com.example.testvolley;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.example.util.ExitApplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.jpush.im.android.api.Conversation;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.Message;
import cn.jpush.im.android.api.content.CustomContent;
import cn.jpush.im.android.api.enums.ConversationType;
import de.greenrobot.daoexample.Music;

public class MusicSelectActivity extends AppCompatActivity {
    private ImageButton tv_return;
    private TextView tv_name;
    private ImageButton tv_continue;
    private TextView select_music_view;
    private String artist;
    private String name;
    private String userID,targetID;
    private String pic_url;
    private ImageView iv_selected;
    private ListView listView;
    private Music music;
    private int checkedIndex = -1;

    private ArrayList<Music> myMusicList,musicList;
    private ArrayList<String> names;
    private MyApplication application;
    private RequestQueue mQueue;
    private Conversation mConv;

    private MyAdapter myAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_select);
        ExitApplication.getInstance().addActivity(this);
        application = (MyApplication)this.getApplicationContext();
        mQueue = application.getRequestQueue();
        myMusicList=application.getMyMusicList();
        musicList = new ArrayList<Music>();
        names = new ArrayList<String>();
        tv_return = (ImageButton) findViewById(R.id.tv_return);
        tv_continue = (ImageButton) findViewById(R.id.tv_continue);

        listView=(ListView) findViewById(R.id.music_list);

        Intent intent=getIntent();
        targetID=intent.getStringExtra("targetID");

        mConv = JMessageClient.getConversation(ConversationType.single, targetID);
        for(int i=0;i<myMusicList.size();i++){
            if(myMusicList.get(i).getSource() == 0){
                musicList.add(myMusicList.get(i));
                names.add(myMusicList.get(i).getName());
            }
        }

        myAdapter = new MyAdapter(this,musicList);


        //       listView.setAdapter(new ArrayAdapter(this,android.R.layout.simple_list_item_single_choice,names));
        listView.setAdapter(myAdapter);
        listView.setOnItemClickListener(new OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO Auto-generated method stub
                ListView lv = (ListView)parent;
                if(checkedIndex != position){
                    //定位到处于点击状态的item
                    int childId = checkedIndex - parent.getFirstVisiblePosition();
                    if(childId >=0){
                        View item = lv.getChildAt(childId);
                        if(item != null){
                            ImageView selected1 = (ImageView)item.findViewById(R.id.iv_selected);
                            selected1.setVisibility(View.INVISIBLE);
                        }
                    }
                    ImageView selected2 = (ImageView)view.findViewById(R.id.iv_selected);
                    if(selected2 != null)
                        selected2.setVisibility(View.VISIBLE);
                    checkedIndex = position;
                }
            }

        });



        //返回
        tv_return.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                finish();
            }
        });

        //下一步
        tv_continue.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                int position = checkedIndex;
                if(position == -1){
                    Toast.makeText(getApplicationContext(), "未选中音乐", Toast.LENGTH_SHORT).show();
                }else{
                    music = musicList.get(position);
                    Map<String,String> map = new HashMap<String,String>();
                    map.put("name", music.getName());
                    map.put("artist", music.getArtist());
                    map.put("pic_url", music.getPic_url());
                    map.put("url", music.getUrl());
                    map.put("uid", ""+music.getUid());
                    map.put("lrc_url", music.getLrc_url());
                    CustomContent customContent = new CustomContent();
                    customContent.setValue("music", map);

                    Message msg = mConv.createSendMessage(customContent);
                    JMessageClient.sendMessage(msg);
                    Intent intent=new Intent();
                    intent.setClass(MusicSelectActivity.this,ChatActivity.class);
                    intent.putExtra("targetID",targetID);
                    startActivity(intent);

                    finish();
                }
            }
        });

    }

    public class MyAdapter extends BaseAdapter{


        private Context context;
        private List<Music> musicList;

        public MyAdapter(Context context,  List<Music> musicList) {


            // TODO Auto-generated constructor stub
            this.musicList = musicList;
            this.context = context;

        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return musicList.size();
        }

        @Override
        public Music getItem(int position) {
            // TODO Auto-generated method stub
            return musicList.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_musicselect,null);
            final TextView name_view = (TextView)convertView.findViewById(R.id.music_name);
            final TextView artist_view = (TextView)convertView.findViewById(R.id.artist);
            final ImageView cover_view = (ImageView)convertView.findViewById(R.id.music_cover);
            final ImageView select_view = (ImageView)convertView.findViewById(R.id.iv_selected);
            music = musicList.get(position);
            name_view.setText(music.getName());
            artist_view.setText(music.getArtist());
            ImageRequest coverRequest=new ImageRequest(music.getPic_url(), new Response.Listener<Bitmap>() {
                @Override
                public void onResponse(Bitmap arg0) {
                    // TODO Auto-generated method stub
                    Log.v("succ","111");
                    cover_view.setImageBitmap(arg0);
                }
            }, 50, 50, Config.RGB_565, new ErrorListener(){
                @Override
                public void onErrorResponse(VolleyError arg0) {
                    // TODO Auto-generated method stub
                    cover_view.setImageResource(R.mipmap.default_face);
                }
            });
            mQueue.add(coverRequest);
            if(position == checkedIndex){
                select_view.setVisibility(View.VISIBLE);
            }
            return convertView;
        }

    }

}


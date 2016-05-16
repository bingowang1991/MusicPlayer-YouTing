package com.example.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.service.PlayerService;
import com.example.testvolley.MyApplication;
import com.example.testvolley.R;
import com.example.util.ExitApplication;

import java.util.ArrayList;

import de.greenrobot.daoexample.Music;

/**
 * Created by Administrator on 2016/5/13.
 */
public class PlayQueueActivity extends FragmentActivity {

    private static final String TAG = PlayQueueActivity.class.getSimpleName();

    private ListView mListView = null;
    private View mClear = null;
    private TextView mTitle = null;
    private TextView close = null;
    private ArrayList<Music> mDataList = new ArrayList<Music>();
    private MyListAdapter mAdapter = null;
    private MyApplication myApplication;
    private PlayerService playerservice;
    public static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_playqueue);
        ExitApplication.getInstance().addActivity(this);
        mListView = (ListView) findViewById(R.id.listview_play_queue);
        mTitle = (TextView) findViewById(R.id.playqueue_title);
        close = (TextView) findViewById(R.id.close);
        context = this;
        myApplication = (MyApplication)this.getApplicationContext();
        playerservice = myApplication.getService();
        mDataList = playerservice.getPlayList();
        initViewsSetting();
    }

    @Override
    protected void onStart() {
        Log.i(TAG, "onStart");
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy");
        super.onDestroy();
/*		mShownList.clear();
		mShownList = null;*/
    }

    @Override
    public void onAttachedToWindow() {

        super.onAttachedToWindow();
        View view = getWindow().getDecorView();
        WindowManager.LayoutParams lp = (WindowManager.LayoutParams) view
                .getLayoutParams();
        lp.gravity = Gravity.BOTTOM;
        lp.x = getResources().getDimensionPixelSize(
                R.dimen.playqueue_dialog_marginright);
        lp.y = getResources().getDimensionPixelSize(
                R.dimen.playqueue_dialog_marginbottom);
        lp.width = this.getBaseContext().getResources().getDisplayMetrics().widthPixels;
        lp.height = getResources().getDimensionPixelSize(
                R.dimen.playqueue_dialog_height);
        getWindowManager().updateViewLayout(view, lp);
    }


    private void initViewsSetting() {

        if (mDataList == null) {
            mTitle.setText(getResources().getString(R.string.playqueue) + "(0)");
        } else if (mDataList.size() == 0) {
            mTitle.setText(getResources().getString(R.string.playqueue) + "(0)");
        } else {
            mTitle.setText(getResources().getString(R.string.playqueue) + "("
                    + mDataList.size() + ")");
        }


        mAdapter = new MyListAdapter(PlayQueueActivity.this, mDataList);
        mListView.setAdapter(mAdapter);
        mAdapter.setSelectedItem(playerservice.getIndex());
        mListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                Music music = playerservice.getPlayList().get(position);

                if (music.equals(playerservice.getMusic()) && playerservice.isPlayFlag()) {
                    Toast.makeText(context, "锟斤拷锟节诧拷锟斤拷...", 0).show();
                }else {
                    playerservice.playSong(music);
                    // 发送更新界面的广播
//						Intent intent = new Intent();
//						intent.setAction("com.example.PlayQueueActivity");
//						sendBroadcast(intent);
                    PlayerActivity.getPlayerActivityCallBack().refreshview();
                    mAdapter.setSelectedItem(position);
                    mAdapter.notifyDataSetChanged();
                }
            }
        });


        close.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    class MyListAdapter extends BaseAdapter {

        private class textViewHolder {
            TextView musicName;
            TextView musicSinger;
            ImageView playingFlag;
            ImageView delete;
        }
        private ArrayList<Music> musiclist;
        private Context mcontext;
        private LayoutInflater mInflater;
        private textViewHolder holder;
        SparseBooleanArray selected;
        boolean isSingle = true;
        int old = -1;

        public MyListAdapter(Context context,ArrayList<Music>applist){
            this.musiclist = applist;
            this.mcontext = context;
            mInflater = (LayoutInflater) mcontext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            selected = new SparseBooleanArray();
        }
        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return musiclist.size();
        }
        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return musiclist.get(position);
        }
        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        public void setSelectedItem(int selected){
            if(isSingle = true && old != -1){
                this.selected.put(old, false);
            }
            this.selected.put(selected, true);
            old = selected;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            if (convertView != null) {
                holder = (textViewHolder) convertView.getTag();
            } else {
                convertView = mInflater.inflate(R.layout.play_list_item_layout, null);
                holder = new textViewHolder();
                holder.musicName = (TextView) convertView.findViewById(R.id.play_list_item_name);
                holder.musicSinger =(TextView) convertView.findViewById(R.id.play_list_item_singer);
                holder.playingFlag =(ImageView) convertView.findViewById(R.id.playing_flag);
                holder.delete=(ImageView) convertView.findViewById(R.id.delete);
                convertView.setTag(holder);
            }
            if(selected.get(position)){
                holder.musicName.setTextColor(0xff20B2AA);
                holder.musicSinger.setTextColor(0xff20B2AA);
                holder.playingFlag.setVisibility(View.VISIBLE);
            }else{
                holder.musicName.setTextColor(getResources().getColor(R.color.white));
                holder.musicSinger.setTextColor(getResources().getColor(R.color.white));
                holder.playingFlag.setVisibility(View.INVISIBLE);
            }
            Music itemInfo = musiclist.get(position);
            if (itemInfo != null) {
                String aname = (String) itemInfo.getName();
                holder.musicSinger.setText(" - "+itemInfo.getArtist());
                holder.musicName.setText(aname);
            }

            holder.delete.setOnClickListener(new OnClickListener(){

                @Override
                public void onClick(View v) {
                    //只有最后一首歌，删除后跳回主界面
                    if(playerservice.getPlayList().size()==1){
                        playerservice.getPlayList().remove(position);
                        playerservice.stop();
                        PlayerActivity.playerActivity.finish();
                        finish();
                        // 清除notification
                        myApplication.getNotificationManager().cancelAll();
                    }
                    else{
                        if((playerservice.getIndex()>position)){
                            playerservice.setIndex(playerservice.getIndex()-1);
                        }
                        else if((playerservice.getIndex()==position)){
                            if(playerservice.isPlayFlag()){
                                playerservice.playNext();
                                playerservice.setIndex(playerservice.getIndex()-1);
                            }
                            else{
                                playerservice.jumptoNext();
                                playerservice.setIndex(playerservice.getIndex()-1);
                            }
                            if(position==mAdapter.getCount()-1)  playerservice.setIndex(0);
                            PlayerActivity.getPlayerActivityCallBack().refreshview();//更新界面的广播
                        }
                        playerservice.getPlayList().remove(position);
                        mAdapter.setSelectedItem(playerservice.getIndex());
                        mAdapter.notifyDataSetChanged();
                    }
                }
            });
            return convertView;
        }
    }
}

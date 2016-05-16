package com.example.playlistactivity;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.service.PlayerService;
import com.example.testvolley.MyApplication;
import com.example.testvolley.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.daoexample.Music;

/**
 * Created by Administrator on 2016/5/13.
 */
public class LocalMusicListActivity extends ListActivity implements LocalMusicListActivityCallBack {

    private static LocalMusicListActivity localMusicActivityCallBack;
    private ListView listView;
    static MyListAdapter listAdapter;
    public static Context context;
    private MyApplication myApplication;
    private PlayerService playerservice;
    //	private ClearEditText mClearEditText;
    private LinearLayout bt_play_all;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.localmusic_list_layout);
        localMusicActivityCallBack = this;
        getWindow().setBackgroundDrawable(null);
        listView = (ListView) findViewById(android.R.id.list);
        bt_play_all = (LinearLayout)findViewById(R.id.bt_playall);

        context = this;
        myApplication = (MyApplication)this.getApplicationContext();
        sp = getSharedPreferences("youting",MODE_PRIVATE);
        editor = sp.edit();
        playerservice = myApplication.getService();
        listAdapter = new MyListAdapter(this, myApplication.getLocalMusicList());
        listView.setAdapter(listAdapter);
        bt_play_all.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                ArrayList<Music> list = myApplication.getLocalMusicList();
                playerservice.setPlayList(list);
                if(list.size()>0){
                    playerservice.playSong(list.get(0));
                    setMusicNameColor(0);
                    com.example.playlistactivity.MainActivity.getMainActivityCallBack().setPage();
                }
                else{
                    Toast.makeText(context, "列表为空", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "播放全部");
        menu.add(0, 1, 1, "更新");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case 0:
                //	PlayUtils.turnToPlay_List(context, MyApplication.getLocalMusicList());
                break;
            case 1:
                new AllMusic_List_asyncTask(listAdapter).execute();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public class MyListAdapter extends BaseAdapter {
        private class buttonViewHolder {
            TextView musicName;
            //ImageButton share;
            ImageButton love;
        }

        private ArrayList<Music> musicList;
        private LayoutInflater mInflater;
        private Context mContext;
        private buttonViewHolder holder;

        /*		public void updateListView(ArrayList<Music> list){
                    this.musicList =  (ArrayList<Music>)list.clone();
                    System.out.println("updateListView word" + musicList.size() );
                    listAdapter.notifyDataSetChanged();
                }	*/
        public MyListAdapter(Context context, List<Music> appList) {
            this.musicList =  (ArrayList<Music>)appList;
            mContext = context;
            mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        }

        public int getCount() {
            return this.musicList.size();
        }

        public Object getItem(int position) {
            return this.musicList.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public void removeItem(int position) {
            this.musicList.remove(position);
            this.notifyDataSetChanged();
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView != null) {
                holder = (buttonViewHolder) convertView.getTag();
            } else {
                convertView = mInflater.inflate(R.layout.localmusic_list_item_layout, null);
                holder = new buttonViewHolder();
                holder.musicName = (TextView) convertView.findViewById(R.id.text_musicName);
                //	holder.share = (ImageButton) convertView.findViewById(R.id.button_share);
                holder.love = (ImageButton) convertView.findViewById(R.id.button_love);
                convertView.setTag(holder);
            }
            final Music itemInfo = musicList.get(position);
            if(isExistInMyfavoriteList(itemInfo)){
                holder.love.setImageResource(R.mipmap.search_like_activated);
            }
            else{
                holder.love.setImageResource(R.mipmap.search_like);
            }
            // 标识正在播放的歌曲
            if(itemInfo.equals(playerservice.getMusic())){
                holder.musicName.setTextColor(mContext.getResources().getColor(R.color.playlist_music_name_color));
            }
            else{
                holder.musicName.setTextColor(mContext.getResources().getColor(R.color.black));
            }

            if (itemInfo != null) {
                String aname = (String) itemInfo.getName();
                holder.musicName.setText(aname);
                //	holder.share.setOnClickListener(new shareOnClickListener(position));
                holder.love.setOnClickListener(new loveOnClickListener(position));
            }
            convertView.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    // TODO Auto-generated method stub
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage("确定删除?")
                            .setCancelable(false)
                            .setPositiveButton("是", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    removeItem(position);
                                    notifyDataSetChanged();
                                }
                            })
                            .setNegativeButton("否", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            }).show();
                    return true;
                }
            });
            convertView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    if (itemInfo.equals(playerservice.getMusic()) && playerservice.isPlayFlag()) {
                        Toast.makeText(context, "正在播放...", 0).show();
                    }else {
                        playerservice.playSong(itemInfo);
                        com.example.playlistactivity.MainActivity.getMainActivityCallBack().setPage();
                    }
                    setMusicNameColor(position);
                }
            });
            return convertView;
        }
        boolean isExistInMyfavoriteList(Music m){

            for(int i=0;i<myApplication.getMyMusicList().size();i++){

                if(m.getSource()==1 && myApplication.getMyMusicList().get(i).getSource() == 1
                        && m.getParameter().equals( myApplication.getMyMusicList().get(i).getParameter())){
                    return true;
                }

            }
            return false;
        }
        class loveOnClickListener implements OnClickListener {

            int position;

            public loveOnClickListener(int position) {
                this.position = position;
            }


            public void onClick(View v) {
                Music map = myApplication.getLocalMusicList().get(position);
                if(!isExistInMyfavoriteList(map)){
                    try {
                        addLocalToMyMusic(map);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    Toast.makeText(getApplicationContext(), "添加成功",
                            Toast.LENGTH_SHORT).show();
                    View myview = (View)listView.getChildAt(position-listView.getFirstVisiblePosition());
                    ImageButton myButton = (ImageButton)myview.findViewById(R.id.button_love);
                    myButton.setImageResource(R.mipmap.search_like_activated);
                }
                else{
                    try {
                        removLocalFromMyMusic(map);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    View myview = (View)listView.getChildAt(position-listView.getFirstVisiblePosition());
                    ImageButton myButton = (ImageButton)myview.findViewById(R.id.button_love);
                    myButton.setImageResource(R.mipmap.search_like);
                }
                MyFavoriteListActivity.myListAdapter.notifyDataSetChanged();
            }
        }
    }

    public void setMusicNameColor(int position){
        // 标识播放的歌曲
        for(int i=0;i<=listView.getLastVisiblePosition()-listView.getFirstVisiblePosition();++i){
            View myview = (View)listView.getChildAt(i);
            TextView txView = (TextView)myview.findViewById(R.id.text_musicName);
            txView.setTextColor(this.getResources().getColor(R.color.black));
            if(position==i+listView.getFirstVisiblePosition()){
                txView.setTextColor(this.getResources().getColor(R.color.playlist_music_name_color));
            }
        }
    }

    public class AllMusic_List_asyncTask extends AsyncTask<String, Void, Void> {

        private ProgressDialog progressDialog;
        private MyListAdapter listAdapter;

        public AllMusic_List_asyncTask(MyListAdapter listAdapter) {
            this.listAdapter = listAdapter;
            progressDialog = new ProgressDialog(context);
        }

        @Override
        protected void onPreExecute() {
            progressDialog.setMessage("扫描所有音乐...");
            progressDialog.setCancelable(false);
            progressDialog.setIndeterminate(false);
            progressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... params) {
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {

        }

        @Override
        protected void onPostExecute(Void result) {
            listAdapter.notifyDataSetChanged();
            progressDialog.dismiss();
        }
    }

    @Override
    public void addLocalToMyMusic(Music m)throws Exception{
        // 保存到preferences里的MY_MUSIC
        String myMusicString = sp.getString("MY_MUSIC", null);
        long local_id = m.getParameter();
        JSONObject jObject = new JSONObject();
        jObject.put("uid",0);
        jObject.put("local_id", local_id);
        JSONArray jArray = new JSONArray();
        if(myMusicString != null){
            jArray = new JSONArray(myMusicString);
            int length = jArray.length();

            jArray.put(length, jObject);

        }else{

            jArray.put(0,jObject);
        }

        editor.putString("MY_MUSIC",jArray.toString());
        editor.commit();
        myApplication.addToMyMusicList(m);
    }

    @Override
    public void removLocalFromMyMusic(Music m)throws Exception{
        // 保存到preferences里的MY_MUSIC
        String myMusicString = sp.getString("MY_MUSIC", null);
        long local_id = m.getParameter();
        JSONArray jArray = new JSONArray();
        if(myMusicString != null){
            jArray = new JSONArray(myMusicString);
            int length = jArray.length();
            for(int i=0;i<length;i++){
                JSONObject jObject = (JSONObject) jArray.get(i);
                if(jObject.getLong("local_id") == local_id){
                    jArray.remove(i);
                    break;
                }
            }
            editor.putString("MY_MUSIC",jArray.toString());
            editor.commit();
            myApplication.RemoveFromMyMusicList(m);
        }
    }

    public static LocalMusicListActivity getLocalMusicListActivityCallBack(){
        return localMusicActivityCallBack;
    }
}

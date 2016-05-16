package com.example.playlistactivity;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.request.StringRequest;
import com.example.service.PlayerService;
import com.example.testvolley.FriendSelectActivity;
import com.example.testvolley.MyApplication;
import com.example.testvolley.MyShareActivity;
import com.example.testvolley.R;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.daoexample.Music;


public class FriendsMusicListActivity extends ListActivity {

    private  ListView listView;
    static MyListAdapter listAdapter;
    public static Context context;
    private MyApplication myApplication;
    private RequestQueue mQueue;
    private PlayerService playerservice;
    private LinearLayout bt_play_all;
    private final static String TAG = "FriendMusicListActivity";
    private final static String add_my_music = "http://121.42.164.7/index.php/Home/Index/add_my_music";
    private final static String delete_my_music = "http://121.42.164.7/index.php/Home/Index/delete_my_music";
    private String[] shareitems = new String[] { "分享给大家", "私信分享" };
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friendmusic_list_layout);
        getWindow().setBackgroundDrawable(null);
        listView = (ListView) findViewById(android.R.id.list);
        bt_play_all = (LinearLayout)findViewById(R.id.bt_playall);

        context = this;
        myApplication = (MyApplication)this.getApplicationContext();
        mQueue = myApplication.getRequestQueue();
        playerservice = myApplication.getService();
        listAdapter = new MyListAdapter(this, myApplication.getFriendMusicList());
        listView.setAdapter(listAdapter);
        bt_play_all.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                ArrayList<Music> list = myApplication.getFriendMusicList();
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
                //	PlayUtils.turnToPlay_List(context, MyApplication.getFriendMusicList());
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
            ImageButton share;
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
                convertView = mInflater.inflate(R.layout.friendmusic_list_item_layout, null);
                holder = new buttonViewHolder();
                holder.musicName = (TextView) convertView.findViewById(R.id.text_musicName);
                holder.share = (ImageButton) convertView.findViewById(R.id.button_share);
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
                holder.share.setOnClickListener(new shareOnClickListener(position));
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
                    // TTODO Auto-generated method stub
                    if (itemInfo.equals(playerservice.getMusic()) && playerservice.isPlayFlag()) {
                        Toast.makeText(context, "正在播放...", Toast.LENGTH_SHORT).show();
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

                if(m.getUid() == myApplication.getMyMusicList().get(i).getUid()){
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
                Music map = myApplication.getFriendMusicList().get(position);
                if(!isExistInMyfavoriteList(map)){
                    addFriendToMyMusic(map);
                    Toast.makeText(getApplicationContext(), "添加成功",
                            Toast.LENGTH_SHORT).show();
                    View myview = (View)listView.getChildAt(position-listView.getFirstVisiblePosition());
                    ImageButton myButton = (ImageButton)myview.findViewById(R.id.button_love);
                    myButton.setImageResource(R.mipmap.search_like_activated);
                }
                else{
                    removeFriendFromMyMusic(map);
                    View myview = (View)listView.getChildAt(position-listView.getFirstVisiblePosition());
                    ImageButton myButton = (ImageButton)myview.findViewById(R.id.button_love);
                    myButton.setImageResource(R.mipmap.search_like);
                }
                MyFavoriteListActivity.myListAdapter.notifyDataSetChanged();
            }
        }

        class shareOnClickListener implements OnClickListener {

            private int position;

            public shareOnClickListener(int position) {
                this.position = position;
            }

            //这里是要分享的，现在这部分还没做好
            public void onClick(View v) {
//				Music map = myApplication.getFriendMusicList().get(position);
//				FriendsMusicListActivity.myListAdapter.notifyDataSetChanged();
                showShareDialog(position);
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
    public void addFriendToMyMusic(final Music m){
        long uid = m.getUid();


        String add_music_url = add_my_music+"?music_id="+uid;
        StringRequest addRequest= new StringRequest(Method.GET,add_music_url,null,new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                // TODO Auto-generated method stub
                Music music = new Music(m);
                music.setSource(0);
                music.setParameter((long)0);
                myApplication.addToMyMusicList(music);

                Toast.makeText(getApplicationContext(),"添加成功", Toast.LENGTH_SHORT).show();
            }
        },new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub
                Toast.makeText(getApplicationContext(),"网络错误", Toast.LENGTH_SHORT).show();
            }
        });
        mQueue.add(addRequest);
    }
//	/**
//	 * New Adding 显示选择对话框
//	 */
//	public void showShareDialog(final int position) {
//
//		AlertDialog ald=new AlertDialog.Builder(this)
//				.setTitle("音乐分享")
//				.setItems(shareitems, new DialogInterface.OnClickListener() {
//
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//						switch (which) {
//						case 0:
//
//
//                            if(myApplication.getFriendMusicList().get(position).getSource() != 1){
//                            	String artist=myApplication.getFriendMusicList().get(position).getArtist();
//    							String name=myApplication.getFriendMusicList().get(position).getName();
//    							String pic_url=myApplication.getFriendMusicList().get(position).getPic_url();
//    							long uid = myApplication.getFriendMusicList().get(position).getUid();
//    							//String artist="李健";
//    							//String name="什刹海";
//    							//String pic_url=null;
//    							Intent intent=new Intent();
//    							intent.setClass(FriendsMusicListActivity.this,MyShareActivity.class);
//    							intent.putExtra("share_mode", 0);
//    							intent.putExtra("tofriends", "所有人");
//    							intent.putExtra("name", name);
//    							intent.putExtra("artist", artist);
//    							intent.putExtra("pic_url", pic_url);
//    							intent.putExtra("music_id", uid);
//    							startActivity(intent);
//                            }
//							break;
//						case 1:
//
//                            if(myApplication.getFriendMusicList().get(position).getSource() != 1){
//                            	String artist=myApplication.getFriendMusicList().get(position).getArtist();
//    							String name=myApplication.getFriendMusicList().get(position).getName();
//    							String pic_url=myApplication.getFriendMusicList().get(position).getPic_url();
//    							long uid = myApplication.getFriendMusicList().get(position).getUid();
//    							/*String artist="李健";
//    							String name="什刹海";
//    							String pic_url=null;*/
//    							Intent intent=new Intent();
//    							intent.setClass(FriendsMusicListActivity.this,FriendSelectActivity.class);
//    							intent.putExtra("name", name);
//    							intent.putExtra("artist", artist);
//    							intent.putExtra("pic_url", pic_url);
//    							intent.putExtra("music_id", uid);
//    							startActivity(intent);
//                            }
//							break;
//						}
//					}
//				})
//				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
//
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//						dialog.dismiss();
//					}
//				}).show();
//
//        Window window = ald.getWindow();
//        WindowManager.LayoutParams lp = window.getAttributes();
//        lp.alpha = 1.0f;
//        window.setAttributes(lp);
//
//      Context context = ald.getContext();
//        int divierId = context.getResources().getIdentifier("android:id/alertTitle", null, null);
//        TextView divider = (TextView)ald.findViewById(divierId);
//        divider.setTextColor(Color.rgb(0, 0, 0));
//
//	}

    /**
     * New Adding 显示选择对话框
     */
    public void showShareDialog(final int position) {
        final AlertDialog ald=new AlertDialog.Builder(this).create();
        ald.show();
        ald.getWindow().setContentView(R.layout.sharedialog);

        ald.getWindow()
                .findViewById(R.id.ll_sharetoall)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(myApplication.getFriendMusicList().get(position).getSource() != 1){
                            String artist=myApplication.getFriendMusicList().get(position).getArtist();
                            String name=myApplication.getFriendMusicList().get(position).getName();
                            String pic_url=myApplication.getFriendMusicList().get(position).getPic_url();
                            long uid = myApplication.getFriendMusicList().get(position).getUid();
                            Intent intent=new Intent();
                            intent.setClass(FriendsMusicListActivity.this,MyShareActivity.class);
                            intent.putExtra("share_mode", 0);
                            intent.putExtra("tofriends", "所有人");
                            intent.putExtra("name", name);
                            intent.putExtra("artist", artist);
                            intent.putExtra("pic_url", pic_url);
                            intent.putExtra("music_id", uid);
                            startActivity(intent);
                        }
                        ald.dismiss();
                    }
                });


        ald.getWindow()
                .findViewById(R.id.ll_sharetoone)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(myApplication.getFriendMusicList().get(position).getSource() != 1){
                            String artist=myApplication.getFriendMusicList().get(position).getArtist();
                            String name=myApplication.getFriendMusicList().get(position).getName();
                            String pic_url=myApplication.getFriendMusicList().get(position).getPic_url();
                            long uid = myApplication.getFriendMusicList().get(position).getUid();
					/*String artist="李健";
					String name="什刹海";
					String pic_url=null;*/
                            Intent intent=new Intent();
                            intent.setClass(FriendsMusicListActivity.this,FriendSelectActivity.class);
                            intent.putExtra("name", name);
                            intent.putExtra("artist", artist);
                            intent.putExtra("pic_url", pic_url);
                            intent.putExtra("music_id", uid);
                            startActivity(intent);
                        }
                        ald.dismiss();
                    }

                });

    }

    public void removeFriendFromMyMusic(Music music) {
        //playerservice.playNext();
        final long uid = music.getUid();
        String delete_url = delete_my_music+"?music_id="+uid;
        StringRequest deleteRequest = new StringRequest(Method.GET,delete_url,null,new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                // TODO Auto-generated method stub
                Log.v(TAG,"delete_my_music");
                for(int i=0;i<myApplication.getMyMusicList().size();i++){

                    if(uid == myApplication.getMyMusicList().get(i).getUid()){
                        myApplication.RemoveFromMyMusicList(myApplication.getMyMusicList().get(i));
                        break;
                    }
                }

            }
        },new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub
                Log.v(TAG,error.toString());
                Toast.makeText(getApplicationContext(),"网络错误", Toast.LENGTH_SHORT).show();
            }
        });
        mQueue.add(deleteRequest);
    }
}

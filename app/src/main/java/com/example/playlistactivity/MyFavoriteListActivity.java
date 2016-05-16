package com.example.playlistactivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
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

/**
 * Created by Administrator on 2016/5/13.
 */
public class MyFavoriteListActivity extends AppCompatActivity {

    public static ListView listView;
    public static MyListAdapter myListAdapter;
    public static Context context;
    private MyApplication myApplication;
    private PlayerService playerservice;
    private RequestQueue mQueue;
    //private LinearLayout btPlayMyFavoriteList;

    private final static String TAG = "MyFavouriteActivity";
    private final static String delete_my_music = "http://121.42.164.7/index.php/Home/Index/delete_my_music";
    private LinearLayout bt_play_all;
    private String[] shareitems = new String[] { "分享给大家", "私信分享" };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.myfavoritemusic_list_layout);

        context = this;
        myApplication = (MyApplication)this.getApplicationContext();
        mQueue = myApplication.getRequestQueue();
        playerservice = myApplication.getService();
        listView = (ListView) findViewById(R.id.myfavorite_music_list);
        bt_play_all = (LinearLayout)findViewById(R.id.bt_playall);
        bt_play_all.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                ArrayList<Music> list = myApplication.getMyMusicList();
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
        myListAdapter = new MyListAdapter(this, myApplication.getMyMusicList());
        listView.setAdapter(myListAdapter);
    }

    @Override
    protected void onRestart() {
        myListAdapter.notifyDataSetChanged();
        super.onRestart();
    }

    @Override
    protected void onResume() {
        myListAdapter.notifyDataSetChanged();
        myListAdapter.notifyDataSetInvalidated();
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, R.string.text_deletePlayListMusics);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 0) {
            new emptyPlayMusicListTask(myListAdapter).execute();
        }
        return super.onOptionsItemSelected(item);
    }

    public class emptyPlayMusicListTask extends AsyncTask<Void, Void, Void> {
        MyListAdapter myListAdapter;

        public emptyPlayMusicListTask(MyListAdapter myListAdapter) {
            this.myListAdapter = myListAdapter;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            myApplication.getMyMusicList().clear();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            myListAdapter.notifyDataSetChanged();
            playerservice.pause();
//			MainActivity.footer.setText("正在播放的歌曲");
            super.onPostExecute(result);
        }
    }

    public class MyListAdapter extends BaseAdapter {
        private class buttonViewHolder {
            TextView musicName;
            ImageButton delete;
        }

        private ArrayList<Music> musicList;
        private LayoutInflater mInflater;
        private Context mContext;
        private int[] valueViewID;
        private buttonViewHolder holder;

        public MyListAdapter(Context context, List<Music> appList) {
            musicList = (ArrayList<Music>) appList;
            mContext = context;
            mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public int getCount() {
            return musicList.size();
        }

        public Object getItem(int position) {
            return musicList.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public void removeItem(int position) {
            //playerservice.playNext();
            long uid = musicList.get(position).getUid();
            String delete_url = delete_my_music+"?music_id="+uid;
            StringRequest deleteRequest = new StringRequest(Method.GET,delete_url,null,new Response.Listener<String>() {

                @Override
                public void onResponse(String response) {
                    Log.v(TAG,"delete_my_music");
                }
            },new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.v(TAG,error.toString());
                }
            });
            mQueue.add(deleteRequest);
            musicList.remove(position);
            this.notifyDataSetChanged();
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView != null) {
                holder = (buttonViewHolder) convertView.getTag();
            } else {
                convertView = mInflater.inflate(R.layout.myfavoritemusic_list_item_layout, null);
                holder = new buttonViewHolder();
                holder.musicName = (TextView) convertView.findViewById(R.id.text_musicName);
                holder.delete = (ImageButton) convertView.findViewById(R.id.button_share);
                convertView.setTag(holder);
            }

            final Music itemInfo = myApplication.getMyMusicList().get(position);

            if (itemInfo != null) {
                String aname = (String) itemInfo.getName();
                holder.musicName.setText(aname);
                holder.delete.setOnClickListener(new shareOnClickListener(position));
            }
            // 标识正在播放的歌曲
            if(itemInfo.equals(playerservice.getMusic())){
                holder.musicName.setTextColor(mContext.getResources().getColor(R.color.playlist_music_name_color));
            }
            else{
                holder.musicName.setTextColor(mContext.getResources().getColor(R.color.black));
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

        class shareOnClickListener implements OnClickListener {
            private int position;

            public shareOnClickListener(int position) {
                this.position = position;
            }

            public void onClick(View v) {
                if(myApplication.getMyMusicList().get(position).getSource() == 1)
                    Toast.makeText(context, "本地音乐，不能分享", Toast.LENGTH_SHORT).show();
                else
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
                        if(myApplication.getMyMusicList().get(position).getSource() != 1){
                            String artist=myApplication.getMyMusicList().get(position).getArtist();
                            String name=myApplication.getMyMusicList().get(position).getName();
                            String pic_url=myApplication.getMyMusicList().get(position).getPic_url();
                            long uid = myApplication.getMyMusicList().get(position).getUid();
                            Intent intent=new Intent();
                            intent.setClass(MyFavoriteListActivity.this,MyShareActivity.class);
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
                        if(myApplication.getMyMusicList().get(position).getSource() != 1){
                            String artist=myApplication.getMyMusicList().get(position).getArtist();
                            String name=myApplication.getMyMusicList().get(position).getName();
                            String pic_url=myApplication.getMyMusicList().get(position).getPic_url();
                            long uid = myApplication.getMyMusicList().get(position).getUid();
					/*String artist="李健";
					String name="什刹海";
					String pic_url=null;*/
                            Intent intent=new Intent();
                            intent.setClass(MyFavoriteListActivity.this,FriendSelectActivity.class);
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

    /**
     * New Adding 显示选择对话框
     */
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
//                            if(myApplication.getMyMusicList().get(position).getSource() != 1){
//                            	String artist=myApplication.getMyMusicList().get(position).getArtist();
//    							String name=myApplication.getMyMusicList().get(position).getName();
//    							String pic_url=myApplication.getMyMusicList().get(position).getPic_url();
//    							long uid = myApplication.getFriendMusicList().get(position).getUid();
//    							//String artist="李健";
//    							//String name="什刹海";
//    							//String pic_url=null;
//    							Intent intent=new Intent();
//    							intent.setClass(MyFavoriteListActivity.this,MyShareActivity.class);
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
//                            if(myApplication.getMyMusicList().get(position).getSource() != 1){
//                            	String artist=myApplication.getMyMusicList().get(position).getArtist();
//    							String name=myApplication.getMyMusicList().get(position).getName();
//    							String pic_url=myApplication.getMyMusicList().get(position).getPic_url();
//    							long uid = myApplication.getFriendMusicList().get(position).getUid();
//    							/*String artist="李健";
//    							String name="什刹海";
//    							String pic_url=null;*/
//    							Intent intent=new Intent();
//    							intent.setClass(MyFavoriteListActivity.this,FriendSelectActivity.class);
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
//        Context context = ald.getContext();
//        int divierId = context.getResources().getIdentifier("android:id/alertTitle", null, null);
//        TextView divider = (TextView)ald.findViewById(divierId);
//        divider.setTextColor(Color.rgb(0, 0, 0));
//
//	}

}

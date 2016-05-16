package com.example.testvolley;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.example.myview.CustomImageView;
import com.example.request.JsonObjectRequest;
import com.example.request.StringRequest;
import com.example.util.ExitApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import de.greenrobot.dao.query.QueryBuilder;
import de.greenrobot.daoexample.DaoSession;
import de.greenrobot.daoexample.Music;
import de.greenrobot.daoexample.MusicDao;
import de.greenrobot.daoexample.MusicDao.Properties;
import de.greenrobot.daoexample.MusicMessage;
import de.greenrobot.daoexample.MusicMessageDao;
import de.greenrobot.daoexample.ShareMessage;
import de.greenrobot.daoexample.ShareMessageDao;
import de.greenrobot.daoexample.SystemMessage;
import de.greenrobot.daoexample.SystemMessageDao;
import de.greenrobot.daoexample.User;
import de.greenrobot.daoexample.UserDao;

public class FriendInfoActivity extends FragmentActivity {
    private ImageButton OperationMore;
    private CustomImageView faceImage;
    private ImageButton tv_return;
    private TextView friend_name;
    private TextView friend_mood;
    private TextView title;
    private Button add_as_friend;
    private String name;
    private String mood;
    private String avatar;
    private String sex;
    private User user;
    private int list_position=-1;
    private static long uid;
    private long friend_id;
    private MyApplication application;
    private RequestQueue mQueue;
    private MusicMessageDao musicMessageDao;
    public ArrayList<User> friendList = new ArrayList<User>();
    private static ArrayList<Music> myMusicList= new ArrayList<Music>();
    private static ArrayList<Music> shareMusicList = new ArrayList<Music>();

    List<Map<String, String>> moreList;
    private PopupWindow pwMyPopWindow;// popupwindow
    private ListView lvPopupList;// popupwindow中的ListView
    private int NUM_OF_VISIBLE_LIST_ROWS = 2;// 指定popupwindow中Item的数量

    private DaoSession daoSession;
    private MusicDao musicDao;
    private ShareMessageDao shareMessageDao;
    private SystemMessageDao systemMessageDao;
    private static QueryBuilder qb;
    private static UserDao userDao;
    private static  ArrayList<User> userList = new ArrayList<User>();
    private static ArrayList<MusicMessage> mmList = new ArrayList<MusicMessage>();
    private static SimpleAdapter simpleAdapter;
    private static SearchMusicAdapter adapter;
    private static MusicMessageAdapter adapter1;
    private static SystemMessage sm;
    private int lover_status;
    private int friend_status;
    private static final int add_lover = 0;
    private static final int send_add_lover_request = 1;
    private static final int delete_lover = 2;
    private static final int cancel_before_set_lover = 3;
    private static final int add_friend = 4;
    private static final int delete_friend = 5;
    private static final int send_add_friend_request = 6;
    private static final int confirm_friend = 7;
    private static final int confirm_lover = 8;
    private boolean friend_lock = false;
    private boolean lover_lock = false;
    private static final String TAG = "FriendInfoActivity";
    private static final String get_user_info = "http://121.42.164.7/index.php/Home/Index/get_user_info";
    private static final String add_friend_url = "http://121.42.164.7/index.php/Home/Index/add_friend";
    private static final String delete_friend_url = "http://121.42.164.7/index.php/Home/Index/delete_friend";
    private static final String add_lover_url = "http://121.42.164.7/index.php/Home/Index/add_lover";
    private static final String delete_lover_url = "http://121.42.164.7/index.php/Home/Index/delete_lover";
    private static final String confirm_friend_url = "http://121.42.164.7/index.php/Home/Index/confirm_friend";
    private static final String confirm_lover_url = "http://121.42.164.7/index.php/Home/Index/confirm_lover";
    private ViewPager mViewPager;
    private PagerAdapter mPagerAdapter;
    private TabWidget mTabWidget;
    private String[] addresses = { "最近常听", "分享歌曲"};
    private Button[] mBtnTabs = new Button[addresses.length];



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_info);
        ExitApplication.getInstance().addActivity(this);
        Intent intent=getIntent();
        uid=intent.getLongExtra("user_id",-1);
        name=intent.getStringExtra("name");
        mood=intent.getStringExtra("mood");
        avatar=intent.getStringExtra("avatar");
        sex=intent.getStringExtra("sex");
        user=new User(uid,name,sex,mood,avatar);

        application = (MyApplication)this.getApplicationContext();
        mQueue = application.getRequestQueue();
        daoSession = application.getDaoSession(this);
        musicMessageDao = daoSession.getMusicMessageDao();
        musicDao = daoSession.getMusicDao();
        userDao = daoSession.getUserDao();
        systemMessageDao = daoSession.getSystemMessageDao();
        shareMessageDao = daoSession.getShareMessageDao();
        friendList=application.getFriendList();
        friend_status = add_friend;
        iniList();

        add_as_friend=(Button)findViewById(R.id.add_as_friend);
        tv_return=(ImageButton)findViewById(R.id.tv_return_friendinfo);
        friend_name=(TextView)findViewById(R.id.my_name);
        friend_mood=(TextView)findViewById(R.id.my_mood);
        title = (TextView)findViewById(R.id.title);
        faceImage = (CustomImageView) findViewById(R.id.iv_face);
        OperationMore = (ImageButton) findViewById(R.id.tv_operate_more);
        mTabWidget = (TabWidget) findViewById(R.id.tabWidget1);

        //是否显示+好友
        long systemMessageID = intent.getLongExtra("SystemMessageID", -1);
        qb = systemMessageDao.queryBuilder();
        Log.v(TAG,"systemMessageID:"+systemMessageID);
        qb.where(de.greenrobot.daoexample.SystemMessageDao.Properties.Id.eq(systemMessageID));
        if(qb.buildCount().count() > 0){
            SystemMessage sm = (SystemMessage) qb.unique();

            if(sm.getCategory().equals("add_friend")){
                if(sm.getAct() == 0){
                    // 此时按钮应该是确认好友而不是添加好友
                    add_as_friend.setText("确认添加");
                    friend_status = confirm_friend;
                }
            }else if(sm.getCategory().equals("add_lover")){

                if(sm.getAct() == 0){
                    // 确认情侣关系
                    lover_status = confirm_lover;
                    Log.v(TAG,"category:"+sm.getCategory()+sm.getAct());
                }else if(sm.getAct() == 1){
                    // 已经是情侣关系,变成解除好友关系
                    lover_status = delete_lover;
                }
            }
        }
        iniData();
        iniPopupWindow();
        for(int i=0;i<friendList.size();i++){
            if(friendList.get(i).getUid()==uid){
                list_position=i;
                break;
            }
        }
        if(list_position>=0){
            // 已经是好友，建议此时改成删除按钮
            friend_status = delete_friend;
            add_as_friend.setText("删除好友");
        }

        //+好友
        add_as_friend.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if(friend_lock == false){
                    friend_lock = true;
                    if(friend_status == add_friend){

                        add_friend();
                    }else if(friend_status == confirm_friend){
                        confirm_friend();
                    }else if(friend_status == delete_friend){
                        delete_friend();
                    }
                }

            }
        });


        //设置头像、昵称、情绪等
        friend_name.setText(name);
        title.setText(name);
        if(mood.equals(MyApplication.DEFAULT_MOOD)){
            friend_mood.setText("~暂无心情~");
        }else{
            friend_mood.setText(mood);
        }

        if(!avatar.equals("null")){
            ImageRequest avatarRequest = new ImageRequest(avatar,new Response.Listener<Bitmap>() {

                @Override
                public void onResponse(Bitmap response) {
                    // TODO Auto-generated method stub
                    faceImage.setImageBitmap(response);
                }
            },300,200,Config.ARGB_8888,new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    // TODO Auto-generated method stub

                }
            });
            mQueue.add(avatarRequest);}


        mTabWidget.setStripEnabled(false);


        mBtnTabs[0] = new Button(this);
        mBtnTabs[0].setBackgroundColor(Color.parseColor("#50000000"));

        mBtnTabs[0].setFocusable(true);
        mBtnTabs[0].setText(addresses[0]);
        mBtnTabs[0].setTextColor(getResources().getColorStateList(R.color.white));
        mTabWidget.addView(mBtnTabs[0]);

	        /*
	         * Listener必须在mTabWidget.addView()之后再加入，用于覆盖默认的Listener，
	         * mTabWidget.addView()中默认的Listener没有NullPointer检测。
	         */
        mBtnTabs[0].setOnClickListener(mTabClickListener);
        mBtnTabs[1] = new Button(this);
        mBtnTabs[1].setBackgroundColor(Color.parseColor("#50000000"));
        mBtnTabs[1].setAlpha((float) 0.5);
        mBtnTabs[1].setFocusable(true);
        mBtnTabs[1].setText(addresses[1]);
        mBtnTabs[1].setTextColor(getResources().getColorStateList(R.color.white));
        mTabWidget.addView(mBtnTabs[1]);
        mBtnTabs[1].setOnClickListener(mTabClickListener);


        mViewPager = (ViewPager) findViewById(R.id.viewPager1);
        mPagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOnPageChangeListener(mPageChangeListener);
        mTabWidget.setCurrentTab(0);

        //弹出窗设置
        OperationMore.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if (pwMyPopWindow.isShowing()) {

                    pwMyPopWindow.dismiss();// 关闭
                } else {
                    pwMyPopWindow.showAsDropDown(OperationMore);// 显示
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

    }


    private OnClickListener mTabClickListener = new OnClickListener() {
        @Override
        public void onClick(View v)
        {
            if (v == mBtnTabs[0])
            {
                mViewPager.setCurrentItem(0);
                mBtnTabs[0].setAlpha((float) 1);
                mBtnTabs[1].setAlpha((float) 0.5);
            } else if (v == mBtnTabs[1])
            {
                mViewPager.setCurrentItem(1);
                mBtnTabs[1].setAlpha((float) 1);
                mBtnTabs[0].setAlpha((float) 0.5);
            }
        }
    };


    private OnPageChangeListener mPageChangeListener = new OnPageChangeListener() {

        @Override
        public void onPageSelected(int arg0)
        {
            mTabWidget.setCurrentTab(arg0);
            if(arg0==0){
                mBtnTabs[0].setAlpha((float) 1);
                mBtnTabs[1].setAlpha((float) 0.5);
            }
            if(arg0==1){
                mBtnTabs[1].setAlpha((float) 1);
                mBtnTabs[0].setAlpha((float) 0.5);
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2)
        {

        }

        @Override
        public void onPageScrollStateChanged(int arg0)
        {

        }
    };


    private class MyPagerAdapter extends FragmentStatePagerAdapter
    {
        public MyPagerAdapter(FragmentManager fm)
        {
            super(fm);
        }
        @Override
        public Fragment getItem(int position)
        {
            return MyFragment.create(addresses[position]);
        }
        @Override
        public int getCount()
        {
            return addresses.length;
        }
    }


    public static class MyFragment extends Fragment
    {
        public static MyFragment create(String address)
        {
            MyFragment f = new MyFragment();
            Bundle b = new Bundle();
            b.putString("address", address);
            f.setArguments(b);
            return f;
        }
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState)
        {
            Random r = new Random(System.currentTimeMillis());
            Bundle b = getArguments();
            View v = inflater.inflate(R.layout.friendinfo_musiclist, null);
            ListView friendinfo_music_lv = (ListView) v.findViewById(R.id.friendinfo_music_lv);

            if (b.getString("address", "")=="最近常听"){
                adapter = new SearchMusicAdapter(getActivity(),myMusicList);
                friendinfo_music_lv.setAdapter(adapter);
            }

            if (b.getString("address", "")=="分享歌曲"){
                int count=shareMusicList.size();
                if(count>0){
                    for(int i=0;i<shareMusicList.size();i++){
                        long sender_id = mmList.get(i).getSender_id();
                        qb = userDao.queryBuilder();
                        qb.where(de.greenrobot.daoexample.UserDao.Properties.Uid.eq(sender_id));
                        User user = (User) qb.unique();
                        userList.add(user);
                    }
                }
                adapter1 = new MusicMessageAdapter(getActivity(),mmList,userList,shareMusicList);
                friendinfo_music_lv.setAdapter(adapter1);
            }

            return v;
        }
    }



    private void iniData() {
        Log.v(TAG,"iniData"+lover_status+confirm_lover);
        moreList = new ArrayList<Map<String, String>>();
        Map<String, String> map;
        map = new HashMap<String, String>();
        map.put("share_key", "设置备注");
        moreList.add(map);
        map = new HashMap<String, String>();
        // 判断是否为情侣
        if(lover_status == confirm_lover){
            Log.v(TAG,"lover_status:"+confirm_lover);
            map.put("share_key","确认情侣");
        }else{
            if(application.isExistLover()){

                if(application.getLover().getUid() == uid){
                    map.put("share_key", "取消情侣");
                    lover_status = delete_lover;
                }else{

                    map.put("share_key", "设为情侣");
                    lover_status = cancel_before_set_lover;


                }
            }else{
                map.put("share_key", "设为情侣");
                lover_status = add_lover;
            }
        }



        moreList.add(map);
//		map = new HashMap<String, String>();
//		map.put("share_key", "删除");
//		moreList.add(map);
    }

    private void iniPopupWindow() {

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.task_detail_popupwindow, null);
        lvPopupList = (ListView) layout.findViewById(R.id.lv_popup_list);
        pwMyPopWindow = new PopupWindow(layout);
        pwMyPopWindow.setFocusable(true);// 加上这个popupwindow中的ListView才可以接收点击事件
        simpleAdapter = new SimpleAdapter(FriendInfoActivity.this, moreList,
                R.layout.item_list_popupwindow2, new String[] { "share_key" },
                new int[] { R.id.tv_list_item });
        lvPopupList.setAdapter(simpleAdapter);
        lvPopupList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch(position){
                    case 0:
                        Intent intent=new Intent();
                        intent.setClass(FriendInfoActivity.this,RemarkActivity.class);
                        intent.putExtra("user_id",uid);
                        intent.putExtra("name",name);
                        intent.putExtra("mood", mood);
                        intent.putExtra("avatar", avatar);
                        intent.putExtra("sex", sex);
                        startActivity(intent);
                        pwMyPopWindow.dismiss();// 关闭
                        break;
                    case 1:
                        //情侣模式
                        if(lover_lock == false){
                            if(lover_status == add_lover){
                                lover_lock = true;
                                add_lover();
                            }else if(lover_status == delete_lover){
                                lover_lock = true;
                                delete_lover();
                            }else if(lover_status == cancel_before_set_lover){

                                Toast.makeText(getApplicationContext(),"必须取消当前情侣关系",Toast.LENGTH_SHORT).show();
                            }else if(lover_status == confirm_lover){
                                lover_lock = true;
                                confirm_lover();
                            }
                        }
                        break;
//				case 2:
//					delete_friend();
//
//
//					break;
                }
            }
        });

        // 控制popupwindow的宽度和高度自适应
        lvPopupList.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        pwMyPopWindow.setWidth(lvPopupList.getMeasuredWidth());
        pwMyPopWindow.setHeight((lvPopupList.getMeasuredHeight() + 5) * NUM_OF_VISIBLE_LIST_ROWS);

        // 控制popupwindow点击屏幕其他地方消失
        pwMyPopWindow.setBackgroundDrawable(this.getResources().getDrawable(
                R.mipmap.black_bg));// 设置背景图片，不能在布局中设置，要通过代码来设置
        pwMyPopWindow.setOutsideTouchable(true);// 触摸popupwindow外部，popupwindow消失。这个要求你的popupwindow要有背景图片才可以成功，如上
    }

    // 初始化歌曲列表
    public void iniList(){
        mmList.clear();
        userList.clear();
        myMusicList.clear();
        shareMusicList.clear();
        String url = get_user_info +"?uid="+uid;
        JsonObjectRequest infoRequest = new JsonObjectRequest(Method.GET,url,null,null,new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                // TODO Auto-generated method stub

                Log.v(TAG,response.toString());
                if(response.length() == 0){

                }
                try {
                    JSONArray myMusic = response.getJSONArray("my_music");
                    JSONArray shareMusic = response.getJSONArray("share_music");
                    for(int i=0;i<myMusic.length();i++){
                        String s = null;
                        try {
                            s = ( myMusic.getJSONObject(i)).getString("uid");
                            long uid = Long.parseLong(s);
                            String  name = (myMusic.getJSONObject(i)).getString("name");
                            String artist = (myMusic.getJSONObject(i)).getString("artist");
                            String url = (myMusic.getJSONObject(i)).getString("url");
                            String lrc_url = (myMusic.getJSONObject(i)).getString("lrc_url");
                            String pic_url = (myMusic.getJSONObject(i)).getString("pic_url");
                            // 取出数据保存在手机数据库中
                            musicDao = daoSession.getMusicDao();
                            //userDao.deleteAll();
                            Music music = new Music(uid,name,artist,null,url,lrc_url,null,pic_url,0,(long)0);
                            qb = musicDao.queryBuilder();
                            qb.where(Properties.Uid.eq(uid));
                            long count = qb.buildCount().count();
                            Log.v(TAG,"mymusic"+name);
                            if (count > 0){
                                Music music_tmp = (Music) qb.unique();
                                String lrc_cache_url = music_tmp.getLrc_cache_url();
                                music = new Music(uid,name,artist,null,url,lrc_url,lrc_cache_url,pic_url,0,(long)0);
                                musicDao.update(music);
                                Log.v(TAG,"update");
                            }else{

                                musicDao.insert(music);
                            }
                            myMusicList.add(music);
                            Log.v(TAG+"dao","inset new"+music.getUid());
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    for(int i=0;i<shareMusic.length();i++){
                        String s = null;
                        try {
                            s = ( shareMusic.getJSONObject(i)).getString("uid");
                            long uid = Long.parseLong(s);
                            String  name = (shareMusic.getJSONObject(i)).getString("name");
                            String artist = (shareMusic.getJSONObject(i)).getString("artist");
                            String url = (shareMusic.getJSONObject(i)).getString("url");
                            String lrc_url = (shareMusic.getJSONObject(i)).getString("lrc_url");
                            String pic_url = (shareMusic.getJSONObject(i)).getString("pic_url");

                            String message = (shareMusic.getJSONObject(i)).getString("message");
                            String share_pic = (shareMusic.getJSONObject(i)).getString("share_pic");
                            //将分享消息保存到shareMessage
                            long user_id = application.getLoginUser().getUid();
                            long sender_id = FriendInfoActivity.this.uid;
                            ShareMessage shareMessage = new ShareMessage(null,user_id,sender_id,message,share_pic);
                            long shareMessage_id = shareMessageDao.insert(shareMessage);
                            // 取出数据保存在手机数据库中
                            musicDao = daoSession.getMusicDao();
                            //userDao.deleteAll();
                            Music music = new Music(uid,name,artist,null,url,lrc_url,null,pic_url,2,shareMessage_id);
                            shareMusicList.add(music);
                            qb = musicDao.queryBuilder();
                            qb.where(Properties.Uid.eq(uid));
                            long count = qb.buildCount().count();
                            Log.v(TAG,"sharemusic"+name);
                            if (count > 0){
                                Music music_tmp = (Music) qb.unique();
                                String lrc_cache_url = music_tmp.getLrc_cache_url();
                                music_tmp.setLrc_cache_url(lrc_cache_url);

                                musicDao.update(music_tmp);
                                Log.v(TAG,"update");
                            }else{
                                music.setSource(0);
                                music.setParameter((long) 0);
                                musicDao.insert(music);
                            }

                            //New adding
                            java.util.Date date1=new java.util.Date();
                            MusicMessage musicmessage = new MusicMessage(null,user_id,sender_id,uid,null,message,date1,share_pic);
                            mmList.add(musicmessage);

                            Log.v(TAG+"dao","inset new"+music.getUid());
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    adapter.notifyDataSetChanged();
                    int count=shareMusicList.size();
                    if(count>0){
                        for(int i=0;i<shareMusicList.size();i++){
                            long sender_id = mmList.get(i).getSender_id();
                            qb = userDao.queryBuilder();
                            qb.where(de.greenrobot.daoexample.UserDao.Properties.Uid.eq(sender_id));
                            User user = (User) qb.unique();
                            userList.add(user);
                        }
                    }
                    adapter1.notifyDataSetChanged();
                    // 将列表进adapter
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }

        },new Response.ErrorListener(){

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub
                Toast.makeText(getApplicationContext(), "网络错误", Toast.LENGTH_SHORT).show();
            }

        });
        mQueue.add(infoRequest);
    }
    public void delete_friend(){
        String url = delete_friend_url + "?friend_id="+uid;
        StringRequest delFriendRequest = new StringRequest(Method.GET,url,null,new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                // TODO Auto-generated method stub
                friend_lock = false;
                User friend = null;
                for(int i=0;i<friendList.size();i++){
                    if(friendList.get(i).getUid() == uid){
                        friend = friendList.get(i);
                        break;
                    }
                }
                if(friend != null){
                    application.removeFromFriendList(friend);
                    add_as_friend.setText("添加好友");
                    friend_status = add_friend;
                    Toast.makeText(getApplicationContext(), "好友已删除", Toast.LENGTH_SHORT).show();
                }
            }
        },new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub
                friend_lock = false;
            }
        });
        mQueue.add(delFriendRequest);
    }
    public void add_friend(){
        String url = add_friend_url +"?friend_id="+uid;
        StringRequest addFriendRequest = new StringRequest(Method.GET,url,null,new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                // TODO Auto-generated method stub
                friend_lock = false;
                add_as_friend.setText("已发送");
                add_as_friend.setClickable(false);
                Toast.makeText(getApplicationContext(), "请求已发送", Toast.LENGTH_SHORT).show();
                friend_status = send_add_friend_request;
            }
        },new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub
                friend_lock = false;
            }
        });
        mQueue.add(addFriendRequest);
    }
    public void add_lover(){
        String url = add_lover_url +"?friend_id="+uid;
        StringRequest addLoverRequest = new StringRequest(Method.GET,url,null,new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                // TODO Auto-generated method stub
                Log.v(TAG,"add_lover"+response.toString());
                lover_lock = false;
                if(response.equals("not friend")){
                    Toast.makeText(getApplicationContext(), "你和此用户还不是好友", Toast.LENGTH_SHORT).show();
                }else{
                    lover_status =send_add_lover_request;
                    Map<String, String> map = moreList.get(1);
                    map.put("share_key", "请求已发送");
                    simpleAdapter.notifyDataSetChanged();
                    Toast.makeText(getApplicationContext(), "请求已发送", Toast.LENGTH_SHORT).show();
                }
            }
        },new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub
                lover_lock = false;
            }
        });
        mQueue.add(addLoverRequest);
    }
    public void delete_lover(){
        String url = delete_lover_url +"?friend_id="+uid;
        StringRequest delLoverRequest = new StringRequest(Method.GET,url,null,new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                // 改变按钮图标
                Log.v(TAG,response.toString());
                lover_lock = false;
                if(response.equals("success")){
                    Map<String, String> map = moreList.get(1);
                    map.put("share_key", "设为情侣");
                    simpleAdapter.notifyDataSetChanged();
//					iniPopupWindow();
                    lover_status = add_lover;
                    application.setIsExistLover(false);
                    application.setLover(null);
                }

            }
        },new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub
                lover_lock = false;
            }
        });
        mQueue.add(delLoverRequest);
    }
    public void confirm_friend(){
        String url = confirm_friend_url +"?friend_id="+uid+"&confirm="+1;

        StringRequest confirmRequest = new StringRequest(Method.GET,url,null,new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.v(TAG,response);
                //设定此消息为已处理
                friend_lock = false;
                if(sm != null){
                    sm.setAct(1);
                    systemMessageDao.update(sm);
                }
                friend_status = delete_friend;
                add_as_friend.setText("删除好友");
            }
        },new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub
                Log.v(TAG,error.toString());
                friend_lock = false;
            }
        });
        mQueue.add(confirmRequest);
    }
    public void confirm_lover(){
        String url = confirm_lover_url +"?friend_id="+uid+"&confirm="+1;
        Log.v(TAG,"confirm url"+url);
        StringRequest confirmRequest = new StringRequest(Method.GET,url,null,new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.v(TAG,response);
                lover_lock = false;
                //设定此消息为已处理
                if(sm != null){
                    sm.setAct(1);
                    systemMessageDao.update(sm);
                }

                Map<String, String> map = moreList.get(1);
                map.put("share_key", "删除情侣");
                simpleAdapter.notifyDataSetChanged();
                //iniPopupWindow();
                lover_status = delete_lover;

            }
        },new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub
                lover_lock = false;
                Log.v(TAG,error.toString());

            }
        });
        mQueue.add(confirmRequest);
    }
}


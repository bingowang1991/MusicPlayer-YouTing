package com.example.testvolley;

import android.app.TabActivity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.request.JsonArrayRequest;
import com.example.util.ExitApplication;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import de.greenrobot.daoexample.Music;
import de.greenrobot.daoexample.User;

/**
 * Created by Administrator on 2016/5/13.
 */
public class MySearchActivity extends TabActivity implements SearchView.OnQueryTextListener {

    private ImageButton iv_return;
    private MyApplication application;
    private RequestQueue mQueue;
    private TabHost tabHost;
    private TextView tv1;
    private TextView tv2;
    public static ListView music_list_view,user_list_view;

    private InputMethodManager imm;

    private static final String TAG = "MySearchActivity";
    private static final String search_music = "http://121.42.164.7/index.php/Home/Index/search_music";
    private static final String search_user = "http://121.42.164.7/index.php/Home/Index/search_user";

    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_search);
        ExitApplication.getInstance().addActivity(this);
        application = (MyApplication)this.getApplicationContext();
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mQueue = application.getRequestQueue();
        searchView = (SearchView)findViewById(R.id.sv);
        music_list_view = (ListView)findViewById(R.id.music_list_view);
        user_list_view = (ListView)findViewById(R.id.user_list_view);
        searchView.setOnQueryTextListener(this);

        // 获取该Activity里面的TabHost组件
        tabHost = getTabHost();
        // 创建第一个Tab页
        TabSpec tab1 = tabHost.newTabSpec("tab1")
                .setIndicator("歌曲") // 设置标题
                .setContent(R.id.tab01); //设置内容

        // 添加第一个标签页
        tabHost.addTab(tab1);
        TabSpec tab2 = tabHost.newTabSpec("tab2")
                // 在标签标题上放置图标
                .setIndicator("用户")
                .setContent(R.id.tab02);
        // 添加第二个标签页
        tabHost.addTab(tab2);

        //设置标签字体
        View view1 = tabHost.getTabWidget().getChildAt(0);
        View view2 = tabHost.getTabWidget().getChildAt(1);
        tv1 = (TextView) view1.findViewById(android.R.id.title);
        tv1.setTextSize(18);
        tv2 = (TextView) view2.findViewById(android.R.id.title);
        tv2.setTextSize(18);
        tv1.setTypeface(Typeface.SERIF, 2); // 设置字体和风格
        tv2.setTypeface(Typeface.SERIF, 2); // 设置字体和风格

        /*  if (tabHost.getCurrentTab() == 0) {//选中
            view1.setBackgroundColor(android.R.color.white);//选中后的背景
            tv1.setTextColor(android.graphics.Color.BLACK);
        } else {//不选中
        	view1.setBackgroundColor(android.R.color.darker_gray);//选中后的背景
            tv1.setTextColor(android.R.color.black);
        }

        if (tabHost.getCurrentTab() == 1) {//选中
            view2.setBackgroundColor(android.R.color.white);//选中后的背景
            tv2.setTextColor(android.graphics.Color.BLACK);
        } else {//不选中
        	view2.setBackgroundColor(android.R.color.darker_gray);//选中后的背景
            tv2.setTextColor(android.R.color.black);
        } */

        iv_return=(ImageButton)findViewById(R.id.iv_return);
        iv_return.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                finish();

            }
        });
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        String name = query.trim();
        try {
            name = URLEncoder.encode(name,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String search_music_url = search_music+"?name="+name;
        String search_user_url = search_user+"?name="+name;
        Log.v(TAG,search_music_url);
        JsonArrayRequest musicRequest = new JsonArrayRequest(search_music_url,null,new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray response) {
                // 处理返回的音乐
                Log.v(TAG,""+response.toString());
                if(response.length() == 0){
                    Toast.makeText(getApplicationContext(), "搜索结果为空", Toast.LENGTH_LONG).show();
                }
                processMusic(response);
            }
        },new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub
                Log.v(TAG,error.toString());
                Toast.makeText(getApplicationContext(), "网络错误", Toast.LENGTH_LONG).show();
            }
        });
        JsonArrayRequest userRequest = new JsonArrayRequest(search_user_url,null,new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray response) {
                // 处理返回的用户
                if(response.length() == 0){
                    Toast.makeText(getApplicationContext(), "搜索结果为空", Toast.LENGTH_LONG).show();
                }
                processUser(response);
            }
        },new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub
                Toast.makeText(getApplicationContext(), "网络错误", Toast.LENGTH_LONG).show();
            }
        });
        Log.v(TAG,"ctab:"+tabHost.getCurrentTab());
        if(tabHost.getCurrentTab() == 0){
            Log.v(TAG,"sendMusicRequest");
//			imm.hideSoftInputFromWindow(music_list_view.getWindowToken(), 0);
            mQueue.add(musicRequest);
        }else{
//			imm.hideSoftInputFromWindow(MySearchActivity.this.getCurrentFocus().getWindowToken(), 0);
            mQueue.add(userRequest);
        }
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        Log.v(TAG,"textChange");
        return false;
    }

    public void processMusic(JSONArray jArray){
        if(jArray.length() == 0){

        }else{
            ArrayList<Music> musicList = new ArrayList<Music>();

            for(int i=0;i<jArray.length();i++){
                String s = null;
                try {
                    s = ( jArray.getJSONObject(i)).getString("uid");
                    long uid = Long.parseLong(s);
                    String  name = (jArray.getJSONObject(i)).getString("name");
                    String artist = (jArray.getJSONObject(i)).getString("artist");
                    String url = (jArray.getJSONObject(i)).getString("url");
                    String lrc_url = (jArray.getJSONObject(i)).getString("lrc_url");
                    String pic_url = (jArray.getJSONObject(i)).getString("pic_url");
                    // 取出数据保存在手机数据库中
                    Music music = new Music(uid,name,artist,null,url,lrc_url,null,pic_url,0,(long)0);

                    musicList.add(music);
                }catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            SearchMusicAdapter adapter = new SearchMusicAdapter(this,musicList);
            music_list_view.setAdapter(adapter);
        }
    }
    public void processUser(JSONArray jArray){
        if(jArray.length() == 0){

        }else{
            ArrayList<User> userList = new ArrayList<User>();

            for(int i=0;i<jArray.length();i++){
                String s = null;
                try {
                    s = ( jArray.getJSONObject(i)).getString("uid");
                    long uid = Long.parseLong(s);
                    String  name = (jArray.getJSONObject(i)).getString("name");
                    String avatar = (jArray.getJSONObject(i)).getString("avatar");
                    String sex = (jArray.getJSONObject(i)).getString("sex");
                    String mood = (jArray.getJSONObject(i)).getString("mood");
                    // 取出数据保存在手机数据库中
                    User user = new User(uid,name,sex,mood,avatar);

                    userList.add(user);
                }catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            SearchUserAdapter adapter = new SearchUserAdapter(this,userList);
            user_list_view.setAdapter(adapter);
        }
    }

}

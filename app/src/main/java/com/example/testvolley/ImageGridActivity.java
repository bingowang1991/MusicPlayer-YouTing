package com.example.testvolley;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.myview.AlbumHelper;
import com.example.myview.Bimp;
import com.example.myview.ImageItem;
import com.example.testvolley.ImageGridAdapter.TextCallback;
import com.example.util.ExitApplication;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Administrator on 2016/5/13.
 */
public class ImageGridActivity extends AppCompatActivity {

    public static final String EXTRA_IMAGE_LIST = "imagelist";

    // ArrayList<Entity> dataList;
    List<ImageItem> dataList;
    GridView gridView;
    private ImageButton tv_return;
    ImageGridAdapter adapter;
    AlbumHelper helper;
    Button bt;
    private String tofriends;
    private int share_mode = 0;
    private final int share_all_mode = 0;

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    Toast.makeText(ImageGridActivity.this, "最多选择1张图片", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_image_grid);
        ExitApplication.getInstance().addActivity(this);
        helper = AlbumHelper.getHelper();
        helper.init(getApplicationContext());

        Intent intent=getIntent();
        tofriends=intent.getStringExtra("tofriends");
        share_mode=intent.getIntExtra("share_mode", share_all_mode);
        dataList = (List<ImageItem>) getIntent().getSerializableExtra(
                EXTRA_IMAGE_LIST);

        initView();
        tv_return=(ImageButton)findViewById(R.id.tv_return_image_grid);
        bt = (Button) findViewById(R.id.bt);
        bt.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                ArrayList<String> list = new ArrayList<String>();
                Collection<String> c = adapter.map.values();
                Iterator<String> it = c.iterator();
                for (; it.hasNext();) {
                    list.add(it.next());
                }

                if (Bimp.act_bool) {
                    Intent intent=new Intent();
                    intent.setClass(ImageGridActivity.this,MyShareActivity.class);
                    intent.putExtra("share_mode", share_mode);
                    intent.putExtra("tofriends", tofriends);
                    startActivity(intent);
                    Bimp.act_bool = false;
                }
                for (int i = 0; i < list.size(); i++) {
                    if (Bimp.drr.size() < 1) {
                        Bimp.drr.add(list.get(i));
                    }
                }
                finish();
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

    private void initView() {
        gridView = (GridView) findViewById(R.id.gridview);
        gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        adapter = new ImageGridAdapter(ImageGridActivity.this, dataList,
                mHandler);
        gridView.setAdapter(adapter);
        adapter.setTextCallback(new TextCallback() {
            public void onListen(int count) {
                bt.setText("已选择" + "(" + count + ")");
            }
        });

        gridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // if(dataList.get(position).isSelected()){
                // dataList.get(position).setSelected(false);
                // }else{
                // dataList.get(position).setSelected(true);
                // }
                adapter.notifyDataSetChanged();
            }

        });

    }

}

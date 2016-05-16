package com.example.testvolley;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import com.example.util.ExitApplication;

public class MySecondMoodActivity extends AppCompatActivity {
    private EditText content_mood;
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_second_mood);
        ExitApplication.getInstance().addActivity(this);
        content_mood = (EditText)findViewById(R.id.mood_edit2);

        sp = getSharedPreferences("userdata",0);
    }

    public void ReturnToMain(View view){
        //获取用户心情，保存到sp
        String mood = "";
        mood="~"+content_mood.getText().toString()+"~";
        //若心情为空则显示暂无心情
        if(mood.equals("")||mood==null)
        {
            mood="~暂无心情~";
        }
        Editor editor = sp.edit();
        editor.putString("mood", mood);
        editor.commit();
//       MainActivity.my_mood.setText(sp.getString("mood", ""));

        //返回界面
        this.finish();
    }
}



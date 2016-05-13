package com.example.testvolley;

import android.app.NotificationManager;

/**
 * Created by Administrator on 2016/5/13.
 */
public interface MainActivityCallBack {

    public void setProgress();
    public void setPage();
    public void initCallBack();
    public void setUserInfo();
    public NotificationManager showCustomView();
}

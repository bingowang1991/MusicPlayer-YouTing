package com.example.service;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by Administrator on 2016/5/13.
 */
public class PlayerService extends IntentService {

    public PlayerService(String name) {
        super(name);
    }

    public PlayerService() {
        super("player!");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }
}

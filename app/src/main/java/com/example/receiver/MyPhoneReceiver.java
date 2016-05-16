package com.example.receiver;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.example.service.PlayerService;
import com.example.testvolley.MyApplication;

/**
 * Created by Administrator on 2016/5/13.
 */
public class MyPhoneReceiver extends BroadcastReceiver {

    private static final String TAG = "MyPhoneReceiver";
    private MyApplication myApplication=MyApplication.get();
    private PlayerService playerService;
    private Context context;
    private boolean originalPlayState;

    public MyPhoneReceiver(){
        playerService = myApplication.getService();

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        originalPlayState = playerService.isPlayFlag();
        //如果是去电
        Log.v(TAG,intent.getAction());
        if(intent.getAction().equals("android.intent.action.New_OUTGOING_CALL")){
            String phoneNumber = intent
                    .getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            Log.v(TAG, "call OUT:" + phoneNumber);
            if(originalPlayState){
                // 暂停
                playerService.pause();

            }

        }else{
            TelephonyManager tm = (TelephonyManager)context.getSystemService(Service.TELEPHONY_SERVICE);
            tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }

    PhoneStateListener listener=new PhoneStateListener(){

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            //注意，方法必须写在super方法后面，否则incomingNumber无法获取到值。
            super.onCallStateChanged(state, incomingNumber);
            switch(state){
                case TelephonyManager.CALL_STATE_IDLE:
                    Log.v(TAG,"挂断");
                    // 如果之前是播放状态，必须要恢复
                    if(originalPlayState){
                        playerService.play();
                    }
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    Log.v(TAG,"接听");
                    // 暂停
                    if(originalPlayState){
                        playerService.pause();
                    }
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    Log.v(TAG,"响铃:来电号码"+incomingNumber);
                    // 暂停
                    if(originalPlayState){
                        playerService.pause();
                    }

                    break;
            }
        }
    };

    private void setPauseState(){
        Log.v(TAG,"setPauseState");
        // 向PlayerReceiver传递一个
        playerService.pause();

    }

}

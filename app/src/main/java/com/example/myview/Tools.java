package com.example.myview;

import android.os.Environment;

/**
 * Created by Administrator on 2016/5/16.
 */
public class Tools {
    /**
     * 棿查是否存在SDCard
     * @return
     */
    public static boolean hasSdcard(){
        String state = Environment.getExternalStorageState();
        if(state.equals(Environment.MEDIA_MOUNTED)){
            return true;
        }else{
            return false;
        }
    }
}

package com.example.util;

import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Administrator on 2016/5/15.
 */
public class TimeHelper {

    private static final String TAG = TimeHelper.class.getSimpleName();
    //enum date {"","一天前","两天前","一个月前"};
    /**
     * 将给定的毫秒数转换成00:00:00样式的字符串
     *
     * @param milliseconds
     *            待转换的毫秒数
     * */
    public static String milliSecondsToFormatTimeString(long milliseconds) {
        String finalTimerString = "";
        int hours, minutes, seconds;

        hours = (int) (milliseconds / (1000 * 60 * 60));
        minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);

        if (hours > 0) {
            finalTimerString = String.format(Locale.getDefault(),
                    "%02d%02d:%02d", hours, minutes, seconds);
        } else {
            finalTimerString = String.format(Locale.getDefault(), "%02d:%02d",
                    minutes, seconds);
        }
        // Log.d(TAG, "milliseconds=" + milliseconds + "\t finalTimerString=" +
        // finalTimerString);
        return finalTimerString;
    }
    /** 将格林威治时间秒数转换
     */
    public static String dateToFormatString(Date date){

        long currentSeconds = new Date().getTime()/1000;
        long messageSeconds = date.getTime()/1000;
        Log.v(TAG,"currentSeconds:"+currentSeconds);
        Log.v(TAG,"messageSeconds:"+messageSeconds);
        String s = (currentSeconds - messageSeconds)/(60*60*24)+"";
        Log.v(TAG,"minus:"+s);
        int minus = Integer.parseInt(s);
        switch(minus){
            case 0:
                SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
                TimeZone destTimeZone = TimeZone.getTimeZone("GMT+8");
                TimeZone srcTimeZone = TimeZone.getTimeZone("EST");
                long targetTime = messageSeconds;

                return formatter.format(new Date(targetTime*1000));
            //		return  TimeHelper.dateTransformBetweenTimeZone(date, formatter, srcTimeZone, destTimeZone);
            case 1:
                return "一天前";
            case 2:
                return "两天前";
            case 3:
                return "三天前";
            default:
                return "一个月以前";
        }

    }
    public static String dateTransformBetweenTimeZone(Date sourceDate, DateFormat formatter,
                                                      TimeZone sourceTimeZone, TimeZone targetTimeZone) {
        Long targetTime = sourceDate.getTime() - sourceTimeZone.getRawOffset() + targetTimeZone.getRawOffset();
        return TimeHelper.getTime(new Date(targetTime), formatter);
    }

    public static String getTime(Date date, DateFormat formatter){
        return formatter.format(date);
    }

}

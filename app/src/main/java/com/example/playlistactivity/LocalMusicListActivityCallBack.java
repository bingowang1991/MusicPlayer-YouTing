package com.example.playlistactivity;

import de.greenrobot.daoexample.Music;

/**
 * Created by Administrator on 2016/5/16.
 */
public interface LocalMusicListActivityCallBack {
    public void addLocalToMyMusic(Music m) throws Exception;
    public void removLocalFromMyMusic(Music m)throws Exception;
}

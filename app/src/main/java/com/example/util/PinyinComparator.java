package com.example.util;

import java.util.Comparator;

import de.greenrobot.daoexample.User;

/**
 * Created by Administrator on 2016/5/15.
 */
public class PinyinComparator implements Comparator<User>  {

    public int compare(User o1, User o2) {
        if (o1.getName().equals("@")
                || o2.getName().equals("#")) {
            return -1;
        } else if (o1.getName().equals("#")
                || o2.getName().equals("@")) {
            return 1;
        } else {
            return o1.getName().compareTo(o2.getName());
        }
    }
}

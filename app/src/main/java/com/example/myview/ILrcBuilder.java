package com.example.myview;

import java.util.List;

/**
 * Created by Administrator on 2016/5/16.
 */
public interface ILrcBuilder {
    List<LrcRow> getLrcRows(String rawLrc);
}

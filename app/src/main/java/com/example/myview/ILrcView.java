package com.example.myview;

import java.util.List;

/**
 * Created by Administrator on 2016/5/16.
 */
public interface ILrcView {

    /**
     * set the lyric rows to display
     */
    void setLrc(List<LrcRow> lrcRows);

    /**
     * seek lyric row to special time
     * @time time to be seek
     *
     */
    void seekLrcToTime(long time);

    void setListener(LrcViewListener l);

    public static interface LrcViewListener {

        /**
         * when lyric line was seeked by user
         */
        void onLrcSeeked(int newPosition, LrcRow row);
    }
}

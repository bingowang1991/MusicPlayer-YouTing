package com.example.testvolley;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.example.service.PlayerService;
import com.example.util.TimeFormat;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import cn.jpush.im.android.api.Conversation;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.Message;
import cn.jpush.im.android.api.callback.GetGroupMembersCallback;
import cn.jpush.im.android.api.callback.ProgressUpdateCallback;
import cn.jpush.im.android.api.content.CustomContent;
import cn.jpush.im.android.api.content.ImageContent;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.enums.ContentType;
import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.api.enums.MessageDirect;
import cn.jpush.im.api.BasicCallback;

import de.greenrobot.dao.Property;
import de.greenrobot.dao.query.QueryBuilder;
import de.greenrobot.daoexample.DaoSession;
import de.greenrobot.daoexample.Music;
import de.greenrobot.daoexample.MusicDao;
import de.greenrobot.daoexample.User;
import de.greenrobot.daoexample.UserDao;

/**
 * Created by Administrator on 2016/5/15.
 */
public class MsgListAdapter extends BaseAdapter {

    private static final String TAG = "MsgListAdapter";

    private Context mContext;
    private String mTargetID;
    private Conversation mConv;
    private List<Message> mMsgList = new ArrayList<Message>();
    private List<String> mUserIDList = new ArrayList<String>();
    private List<Integer> mIndexList = new ArrayList<Integer>();
    private LayoutInflater mInflater;
    private boolean mSetData = false;
    private boolean mIsGroup = false;
    private long mGroupID;
    private int mPosition = -1;
    private int count = 0;
    private final int UPDATE_IMAGEVIEW = 1999;
    private final int UPDATE_PROGRESS = 1998;

    private final int TYPE_RECEIVE_TXT = 0;
    private final int TYPE_SEND_TXT = 1;

    private final int TYPE_SEND_IMAGE = 2;
    private final int TYPE_RECEIVER_IMAGE = 3;

    private final int TYPE_SEND_LOCATION = 4;
    private final int TYPE_RECEIVER_LOCATION = 5;

    private final int TYPE_SEND_VOICE = 6;
    private final int TYPE_RECEIVER_VOICE = 7;
    private final int TYPE_GROUP_CHANGE = 8;
    private final MediaPlayer mp;
    private AnimationDrawable mVoiceAnimation;
    private FileInputStream mFIS;
    private FileDescriptor mFD;
    private Activity mActivity;

    private boolean autoPlay = false;

    private int nextPlayPosition = 0;
    private double mDensity;
    private ImageLoader imageLoader;
    private MyApplication application;
    private PlayerService playerService;
    private RequestQueue mQueue;
    private DaoSession daoSession;
    private UserDao userDao;
    private MusicDao musicDao;
    private QueryBuilder qb;

    public MsgListAdapter(Context context, boolean isGroup, String targetID, long groupID) {
        this.mContext = context;
        mActivity = (Activity) context;
        application = MyApplication.get();
        playerService = application.getService();
        mQueue = application.getRequestQueue();
        daoSession = application.getDaoSession(context);
        userDao = daoSession.getUserDao();
        musicDao = daoSession.getMusicDao();
        imageLoader = new ImageLoader(mQueue,new BitmapCache());
        this.mIsGroup = isGroup;
        this.mTargetID = targetID;
        this.mGroupID = groupID;
        DisplayMetrics dm = new DisplayMetrics();
        mActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        mDensity = dm.density;
        if (mIsGroup) {
            mTargetID = String.valueOf(groupID);
            this.mConv = JMessageClient.getConversation(ConversationType.group, groupID);
            this.mMsgList = mConv.getAllMessage();

            JMessageClient.getGroupMembers(groupID, new GetGroupMembersCallback() {
                @Override
                public void gotResult(int status, String desc, List<String> members) {
                    android.os.Message msg = handler.obtainMessage();
                    msg.what = 1;
                    Bundle bundle = new Bundle();
                    bundle.putStringArrayList("memberList", (ArrayList) members);
                    msg.setData(bundle);
                    msg.sendToTarget();
                }
            });
        } else {
            this.mConv = JMessageClient.getConversation(ConversationType.single, mTargetID);
            this.mMsgList = mConv.getAllMessage();
            mUserIDList.add(targetID);
            mUserIDList.add(JMessageClient.getMyInfo().getUserName());
            // 设置头像

        }

        mInflater = LayoutInflater.from(mContext);

        AudioManager audioManager = (AudioManager) mContext
                .getSystemService(Context.AUDIO_SERVICE);
        mp = new MediaPlayer();
        audioManager.setMode(AudioManager.MODE_NORMAL);
        audioManager.setSpeakerphoneOn(true);
        mp.setAudioStreamType(AudioManager.STREAM_RING);
        mp.setOnErrorListener(new OnErrorListener() {

            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                return false;
            }
        });
    }

    public void setSendImg(int[] msgIDs) {
        for(int i=0; i < msgIDs.length; i++){
            JMessageClient.sendMessage(mConv.getMessage(msgIDs[i]));
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mMsgList.size();
    }

    @Override
    public int getItemViewType(int position) {
        Message msg = mMsgList.get(position);
        if (msg.getContentType().equals(ContentType.text)) {
            return msg.getDirect().equals(MessageDirect.send) ? TYPE_SEND_TXT
                    : TYPE_RECEIVE_TXT;
        } else if (msg.getContentType().equals(ContentType.image)) {
            return msg.getDirect().equals(MessageDirect.send) ? TYPE_SEND_IMAGE
                    : TYPE_RECEIVER_IMAGE;
        } else if (msg.getContentType().equals(ContentType.voice)) {
            return msg.getDirect().equals(MessageDirect.send) ? TYPE_SEND_VOICE
                    : TYPE_RECEIVER_VOICE;
        } else if (msg.getContentType().equals(ContentType.custom)) {
            return msg.getDirect().equals(MessageDirect.send) ? TYPE_SEND_IMAGE
                    :TYPE_RECEIVER_IMAGE;
        } else {
            return msg.getDirect().equals(MessageDirect.send) ? TYPE_SEND_LOCATION
                    : TYPE_RECEIVER_LOCATION;
        }
    }

    public int getViewTypeCount() {
        return 9;
    }

    private View createViewByType(Message msg, int position) {
        switch (msg.getContentType()) {
            case image:
                return getItemViewType(position) == TYPE_SEND_IMAGE ? mInflater
                        .inflate(R.layout.chat_item_send_image, null) : mInflater
                        .inflate(R.layout.chat_item_receive_image, null);
            case custom:
                return getItemViewType(position) == TYPE_SEND_IMAGE ? mInflater
                        .inflate(R.layout.chat_item_send_image, null) : mInflater
                        .inflate(R.layout.chat_item_receive_image, null);
            default:
                return getItemViewType(position) == TYPE_SEND_TXT ? mInflater
                        .inflate(R.layout.chat_item_send_text, null) : mInflater
                        .inflate(R.layout.chat_item_receive_text, null);
        }
    }

    @Override
    public Message getItem(int position) {
        return mMsgList.get(position);
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    ArrayList<String> memberList = msg.getData().getStringArrayList("memberList");
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "init avatar succeed");
                            notifyDataSetChanged();
                        }
                    });
                    break;
                case UPDATE_IMAGEVIEW:
                    Bundle bundle = msg.getData();
                    ViewHolder holder = (ViewHolder) msg.obj;
                    String path = bundle.getString("path");

                    refresh();
                    Log.i(TAG, "Refresh Received picture");
                    break;
            }
        }
    };

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void refresh() {
        mMsgList.clear();
        Log.v(TAG, "mConv.toString() " + mConv.toString());
        if(mIsGroup) {
            mConv = JMessageClient.getConversation(ConversationType.group,mGroupID);
        }else{
            mConv = JMessageClient.getConversation(ConversationType.single,mTargetID);
        }
        if(null != mConv) {
            mMsgList = mConv.getAllMessage();
            notifyDataSetChanged();
        }
    }

    public void releaseMediaPlayer() {
        if (mp != null)
            mp.release();
    }

    public void addMsgToList(Message msg) {
        mMsgList.add(msg);
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Message msg = mMsgList.get(position);
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            ContentType contentType = msg.getContentType();
            convertView = createViewByType(msg, position);
            if (contentType.equals(ContentType.text)) {
                try {
                    holder.headIcon = (NetworkImageView) convertView
                            .findViewById(R.id.avatar_iv);
                    holder.displayName = (TextView) convertView
                            .findViewById(R.id.display_name_tv);
                    holder.txtContent = (TextView) convertView
                            .findViewById(R.id.msg_content);
                    holder.sendingIv = (ImageView) convertView
                            .findViewById(R.id.sending_iv);
                    holder.resend = (ImageButton) convertView
                            .findViewById(R.id.fail_resend_ib);

                } catch (Exception e) {
                }
            } else if(contentType.equals(ContentType.custom)){
                try {
                    holder.headIcon = (NetworkImageView) convertView
                            .findViewById(R.id.avatar_iv);
                    holder.displayName = (TextView) convertView
                            .findViewById(R.id.display_name_tv);
                    holder.picture = (NetworkImageView) convertView
                            .findViewById(R.id.picture_iv);
                    holder.musicName = (TextView)convertView
                            .findViewById(R.id.music_name);
                    holder.artist = (TextView)convertView
                            .findViewById(R.id.artist);
                    holder.play_pause = (ImageView)convertView
                            .findViewById(R.id.play_pause);
                    holder.sendingIv = (ImageView) convertView
                            .findViewById(R.id.sending_iv);
                    holder.progressTv = (TextView) convertView
                            .findViewById((R.id.progress_tv));
                    holder.resend = (ImageButton) convertView
                            .findViewById(R.id.fail_resend_ib);
                } catch (Exception e) {
                }
            }else {
                try {
                    holder.headIcon = (NetworkImageView) convertView
                            .findViewById(R.id.avatar_iv);
                    holder.displayName = (TextView) convertView
                            .findViewById(R.id.display_name_tv);
                    holder.txtContent = (TextView) convertView
                            .findViewById(R.id.msg_content);
                    holder.sendingIv = (ImageView) convertView
                            .findViewById(R.id.sending_iv);
                    holder.resend = (ImageButton) convertView
                            .findViewById(R.id.fail_resend_ib);
                } catch (Exception e) {
                }
            }
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        switch (msg.getContentType()) {
            case text:
                handleTextMsg(msg, holder, position);
                break;
            case image:
                //               handleImgMsg(msg, holder, position);
            case custom:
                handleCustomMsg(msg,holder,position);
                break;


            default:
                handleGroupChangeMsg(msg, holder);
        }
        //显示时间
        TextView msgTime = (TextView) convertView
                .findViewById(R.id.send_time_txt);
        long nowDate = msg.getCreateTime();
        if (position != 0) {
            long lastDate = mMsgList.get(position - 1).getCreateTime();
            // 如果两条消息之间的间隔超过十分钟则显示时间
            if (nowDate - lastDate > 600000) {
                TimeFormat timeFormat = new TimeFormat(mContext, nowDate);
                msgTime.setText(timeFormat.getDetailTime());
                msgTime.setVisibility(View.VISIBLE);
            } else {
                msgTime.setVisibility(View.GONE);
            }
        } else {
            TimeFormat timeFormat = new TimeFormat(mContext, nowDate);
            msgTime.setText(timeFormat.getDetailTime());
        }
        //显示头像
        if (holder.headIcon != null) {

            Log.v(TAG,"show avatar&&holder.headIcon!=null");
//            holder.headIcon.setImageResource(R.drawable.head_icon);
            String avatar = getAvatarFromName(msg.getFromID());
            holder.headIcon.setImageUrl(avatar, imageLoader);
            // 点击头像跳转到个人信息界面
            holder.headIcon.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    Intent intent = new Intent();
                    if (msg.getDirect().equals(MessageDirect.send)) {
                        intent.putExtra("targetID", msg.getFromName());
                        Log.i(TAG, "msg.getFromName() " + msg.getFromName());
                        intent.setClass(mContext, UserInfoSettingActivity.class);
                        mContext.startActivity(intent);
                    } else {
                        String targetID = msg.getFromID();
                        qb = userDao.queryBuilder();
                        qb.where(de.greenrobot.daoexample.UserDao.Properties.Name.eq(targetID));
                        User user = (User)qb.unique();
                        intent.putExtra("name", targetID);

                        intent.putExtra("user_id", user.getUid());
                        intent.putExtra("mood", user.getMood());
                        intent.putExtra("sex", user.getSex());
                        intent.putExtra("avatar",user.getAvatar());


                        intent.setClass(mContext, FriendInfoActivity.class);
                        mContext.startActivity(intent);
                    }
                }
            });
        }

        return convertView;
    }

    private void handleGroupChangeMsg(Message msg, ViewHolder holder) {
        CustomContent customContent = (CustomContent) msg.getContent();
        String content = String.valueOf(customContent.getValue("content"));
        holder.groupChange.setText(content);
        holder.groupChange.setVisibility(View.VISIBLE);
    }

    private void handleTextMsg(final Message msg, final ViewHolder holder,
                               final int position) {
        final String content = ((TextContent) msg.getContent()).getText();
        holder.txtContent.setText(content);

        // 妫�煡鍙戦�鐘舵�锛屽彂閫佹柟鏈夐噸鍙戞満鍒�
        if (msg.getDirect().equals(MessageDirect.send)) {
            final Animation sendingAnim = AnimationUtils.loadAnimation(mContext, R.anim.rotate);
            LinearInterpolator lin = new LinearInterpolator();
            sendingAnim.setInterpolator(lin);
            switch (msg.getStatus()) {
                case send_success:
                    if (sendingAnim != null) {
                        holder.sendingIv.clearAnimation();
                        holder.sendingIv.setVisibility(View.GONE);
                    }
                    holder.resend.setVisibility(View.GONE);
                    break;
                case send_fail:
                    if (sendingAnim != null) {
                        holder.sendingIv.clearAnimation();
                        holder.sendingIv.setVisibility(View.GONE);
                    }
                    holder.resend.setVisibility(View.VISIBLE);
                    break;
                case send_going:
                    sendingTextOrVoice(holder, sendingAnim, msg);
                    break;
                default:
            }
            // 鐐瑰嚮閲嶅彂鎸夐挳锛岄噸鍙戞秷鎭�
            holder.resend.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    showResendDialog(holder, sendingAnim, msg);
                }
            });

        } else {
            if (mIsGroup) {
                holder.displayName.setVisibility(View.VISIBLE);
                holder.displayName.setText(msg.getFromName());
            }
        }
    }
    private void handleCustomMsg(final Message msg, final ViewHolder holder,
                                 final int position) {
        CustomContent customContent = (CustomContent) msg.getContent();
        Map<String,String> map = (Map<String, String>) customContent.getValue("music");
        Log.v(TAG,map.toString());
        String name = (String)map.get("name");
        String artist = (String)map.get("artist");
        String url = map.get("url");
        String pic_url = (String)map.get("pic_url");
        String lrc_url = (String)map.get("lrc_url");
        long uid = Long.parseLong(map.get("uid"));
        final Music music = new Music(uid,name,artist,null,url,lrc_url,null,pic_url,0,(long) 0);
        //   	if(msg.getDirect().equals(MessageDirect.receive)){
        holder.picture.setImageUrl(music.getPic_url(),imageLoader);
        holder.artist.setText(music.getArtist());
        holder.musicName.setText(music.getName());
        if(holder.play_pause != null){
            holder.play_pause.setOnClickListener(new OnClickListener(){

                @Override
                public void onClick(View v) {
                    // 点击专辑封面播放歌曲

                    playerService.playSong(music);



                }

            });
        }
        if (msg.getDirect().equals(MessageDirect.send)) {
            final Animation sendingAnim = AnimationUtils.loadAnimation(mContext, R.anim.rotate);
            LinearInterpolator lin = new LinearInterpolator();
            sendingAnim.setInterpolator(lin);
            switch (msg.getStatus()) {
                case send_success:
                    if (sendingAnim != null) {
                        holder.sendingIv.clearAnimation();
                        holder.sendingIv.setVisibility(View.GONE);
                    }
                    holder.resend.setVisibility(View.GONE);
                    break;
                case send_fail:
                    if (sendingAnim != null) {
                        holder.sendingIv.clearAnimation();
                        holder.sendingIv.setVisibility(View.GONE);
                    }
                    holder.resend.setVisibility(View.VISIBLE);
                    break;
                case send_going:
                    sendingTextOrVoice(holder, sendingAnim, msg);
                    break;
                default:
            }
            // 鐐瑰嚮閲嶅彂鎸夐挳锛岄噸鍙戞秷鎭�
            holder.resend.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    showResendDialog(holder, sendingAnim, msg);
                }
            });

        } else {
            if (mIsGroup) {
                holder.displayName.setVisibility(View.VISIBLE);
                holder.displayName.setText(msg.getFromName());
            }
        }
    }
    //姝ｅ湪鍙戦�鏂囧瓧鎴栬闊�
    private void sendingTextOrVoice(ViewHolder holder, Animation sendingAnim, Message msg) {
        holder.sendingIv.setVisibility(View.VISIBLE);
        holder.sendingIv.startAnimation(sendingAnim);
        holder.resend.setVisibility(View.GONE);
        //娑堟伅姝ｅ湪鍙戦�锛岄噸鏂版敞鍐屼竴涓洃鍚秷鎭彂閫佸畬鎴愮殑Callback
        if (!msg.isSendCompleteCallbackExists()) {
            msg.setOnSendCompleteCallback(new BasicCallback() {
                @Override
                public void gotResult(final int status, final String desc) {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (status != 0)
                                Toast.makeText(mContext, "发送失败", Toast.LENGTH_SHORT).show();
                            refresh();
                        }

                    });
                }
            });
        }
    }

    private void showResendDialog(final ViewHolder holder, final Animation sendingAnim, final Message msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        View view = LayoutInflater.from(mContext).inflate(
                R.layout.dialog_resend_msg, null);
        builder.setView(view);
        Button cancelBtn = (Button) view.findViewById(R.id.cancel_btn);
        Button resendBtn = (Button) view.findViewById(R.id.resend_btn);
        final Dialog dialog = builder.create();
        dialog.show();
        OnClickListener listener = new OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.cancel_btn:
                        dialog.dismiss();
                        break;
                    case R.id.resend_btn:
                        dialog.dismiss();
                        if (msg.getContentType().equals(ContentType.image)) {
                            sendImage(holder, sendingAnim, msg);
                        } else {
                            ResendTextOrVoice(holder, sendingAnim, msg);
                        }
                        break;
                }
            }
        };
        cancelBtn.setOnClickListener(listener);
        resendBtn.setOnClickListener(listener);
    }

    /**
     *
     * @param path
     * @param imageView
     */
    private void setPictureScale(String path, ImageView imageView) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, opts);
        double imageWidth = opts.outWidth;
        double imageHeight = opts.outHeight;
        if (imageWidth < 100 * mDensity) {
            imageHeight = imageHeight * (100 * mDensity / imageWidth);
            imageWidth = 100 * mDensity;
        }
        ViewGroup.LayoutParams params = imageView.getLayoutParams();
        params.width = (int) imageWidth;
        params.height = (int) imageHeight;
        imageView.setLayoutParams(params);
    }

    private void ResendTextOrVoice(final ViewHolder holder, Animation sendingAnim, Message msg) {
        holder.resend.setVisibility(View.GONE);
        holder.sendingIv.setVisibility(View.VISIBLE);
        holder.sendingIv.startAnimation(sendingAnim);

        if (!msg.isSendCompleteCallbackExists()) {
            msg.setOnSendCompleteCallback(new BasicCallback() {
                @Override
                public void gotResult(final int status, String desc) {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (status != 0) {
                                //                              HandleResponseCode.onHandle(mContext, status);
                                holder.sendingIv.clearAnimation();
                                holder.sendingIv.setVisibility(View.GONE);
                                holder.resend.setVisibility(View.VISIBLE);
                                Log.i(TAG, "Resend message failed!");
                            }
                            refresh();
                        }
                    });
                }
            });
        }
        JMessageClient.sendMessage(msg);
    }

    private void sendImage(final ViewHolder viewHolder, Animation sendingAnim, Message msg) {
        ImageContent imgContent = (ImageContent) msg.getContent();
        final String path = imgContent.getLocalThumbnailPath();
        viewHolder.sendingIv.setVisibility(View.VISIBLE);
        viewHolder.sendingIv.startAnimation(sendingAnim);
        viewHolder.picture.setAlpha(0.8f);
        viewHolder.resend.setVisibility(View.GONE);
        viewHolder.progressTv.setVisibility(View.VISIBLE);
        try {

            msg.setOnContentUploadProgressCallback(new ProgressUpdateCallback() {
                @Override
                public void onProgressUpdate(final double progress) {
                    Log.i("Uploding picture", "progress: " + progress);
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            viewHolder.progressTv.setText((int) (progress * 100) + "%");
                        }
                    });
                }
            });
            if (!msg.isSendCompleteCallbackExists()) {
                msg.setOnSendCompleteCallback(new BasicCallback() {
                    @Override
                    public void gotResult(final int status, String desc) {
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                HandleResponseCode.onHandle(mContext, status);
//                                Picasso.with(mContext).load(new File(path)).into(viewHolder.picture);
                                Log.i("Send picture", "update: ");
                                refresh();
                            }
                        });
                    }
                });
            }
            JMessageClient.sendMessage(msg);
        } catch (Exception e) {
        }
    }

    private void addTolistAndSort(int position) {
        mIndexList.add(position);
        Collections.sort(mIndexList);
    }

    private void handleLocationMsg(Message msg, ViewHolder holder, int position) {

    }

    public static class ViewHolder {
        NetworkImageView headIcon;
        TextView displayName;
        TextView txtContent;
        NetworkImageView picture;
        TextView progressTv;
        ImageButton resend;
        TextView voiceLength;
        ImageView voice;
        ImageView play_pause;
        ImageView readStatus;
        TextView location;
        TextView groupChange;
        ImageView sendingIv;
        TextView musicName;
        TextView artist;
    }
    public String getAvatarFromName(String name){
        Log.v(TAG,name);
        qb = userDao.queryBuilder();
        Property[] myProperties = userDao.getProperties();

        qb.where(myProperties[1].eq(name));
        Log.v(TAG,"count:"+qb.buildCount().count());
        User user = (User) qb.unique();
        return user.getAvatar();
    }

}

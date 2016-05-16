package com.example.testvolley;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

import cn.jpush.im.android.api.Conversation;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.Message;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.api.BasicCallback;

public class ChatController implements OnClickListener, OnScrollListener, View.OnTouchListener {
    private ChatView mChatView;
    private ChatActivity mContext;
    private MsgListAdapter mChatAdapter;
    Conversation mConv;
    private boolean isInputByKeyBoard = true;
    public boolean mMoreMenuVisible = false;
    public static boolean mIsShowMoreMenu = false;
    public static final int UPDATE_LAST_PAGE_LISTVIEW = 1025;
    public static final int UPDATE_CHAT_LISTVIEW = 1026;
    private String mTargetID;
    private long mGroupID;
    private boolean mIsGroup;
    private boolean mFromGroup = false;
    private String mPhotoPath = null;
    private int mContentH;

    private final static String TAG = "ChatController";
    public ChatController(ChatView mChatView, ChatActivity context){
        this.mChatView = mChatView;
        this.mContext = context;
        // 得到消息列表
        initData();

        DisplayMetrics dm = new DisplayMetrics();
        mContext.getWindowManager().getDefaultDisplay().getMetrics(dm);
        mContentH = dm.heightPixels;
    }
    private void initData() {
        Intent intent = mContext.getIntent();
        mTargetID = intent.getStringExtra("targetID");
        Log.i("ChatController", "mTargetID " + mTargetID);
        mGroupID = intent.getLongExtra("groupID", 0);
        mIsGroup = intent.getBooleanExtra("isGroup", false);
        mFromGroup = intent.getBooleanExtra("fromGroup", false);

        // 用targetID得到会话
        Log.i("Tag", "targetID is " + mTargetID);
//	        JMessageClient.enterConversaion(mTargetID);
        mConv = JMessageClient.getConversation(ConversationType.single, mTargetID);
        // 如果之前沒有会话记录并且是群聊
        if (mConv == null && mIsGroup) {
            mConv = Conversation.createConversation(ConversationType.group, mGroupID);
            Log.i("ChatController", "create group success");
            // 是单聊
        } else if (mConv == null && !mIsGroup) {
            mConv = Conversation.createConversation(ConversationType.single, mTargetID);
        }
        mChatView.setChatTitle(mConv.getDisplayName());
        mConv.resetUnreadCount();
        mChatAdapter = new MsgListAdapter(mContext, mIsGroup, mTargetID, mGroupID);
        mChatView.setChatListAdapter(mChatAdapter);
        // 滑动到底部
        mChatView.setToBottom();
        // 设置标志，用于判断是否在会话界面
        ChatActivity.SIGN = mConv.getType() + ":"
                + mConv.getTargetId();
    }
    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            // 返回按钮
            case R.id.iv_return:
                mConv.resetUnreadCount();
                JMessageClient.exitConversaion();
                mContext.finish();
                break;

            // 切换输入
            case R.id.send_music:
//	                isInputByKeyBoard = !isInputByKeyBoard;
//	                if (isInputByKeyBoard) {
//	                    mChatView.isKeyBoard();
//	                    mChatView.mChatInputEt.requestFocus();
//	                    showSoftInput();
                //
//	                }else {
//	                    mChatView.notKeyBoard(mConv, mChatAdapter);
//	                    dismissSoftInput();
//	                }


                Intent intent = new Intent();
                intent.setClass(mContext, MusicSelectActivity.class);

                intent.putExtra("targetID", mTargetID);
                mContext.startActivity(intent);
                break;
            case R.id.chat_input_et:

//	                mChatView.invisibleMoreMenu();
                mIsShowMoreMenu = true;
                showSoftInput();
                break;

            // 发送文本消息
            case R.id.send_msg_btn:
                String msgContent = mChatView.getChatInput();
                mChatView.clearInput();
                mChatView.setToBottom();
                if (msgContent.equals("")) {
                    return;
                }
                TextContent content = new TextContent(msgContent);
                final Message msg = mConv.createSendMessage(content);
                msg.setOnSendCompleteCallback(new BasicCallback() {

                    @Override
                    public void gotResult(final int status, String desc) {
                        Log.i("ChatController", "send callback " + status + " desc " + desc);
                        if (status != 0) {
                            mContext.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                }
                            });
                        }
                        // 发送成功或失败都要刷新一次
                        android.os.Message msg = handler.obtainMessage();
                        msg.what = UPDATE_CHAT_LISTVIEW;
                        Bundle bundle = new Bundle();
                        bundle.putString("desc", desc);
                        msg.setData(bundle);
                        msg.sendToTarget();
                    }
                });
                mChatAdapter.addMsgToList(msg);
                JMessageClient.sendMessage(msg);
                break;




        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mIsShowMoreMenu) {

                    dismissSoftInput();
                    mIsShowMoreMenu = false;
                    mMoreMenuVisible = false;
                    mChatView.setToBottom();
                }
                break;
        }
        return false;
    }

    private void showSoftInput() {
        if (mContext.getWindow().getAttributes().softInputMode == WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (mContext.getCurrentFocus() != null) {
                InputMethodManager imm = ((InputMethodManager) mContext
                        .getSystemService(Activity.INPUT_METHOD_SERVICE));
                imm.showSoftInputFromInputMethod(mChatView.getWindowToken(), 0);
            }
        }
    }

    public void dismissSoftInput() {
        //隐藏软键盘
        InputMethodManager imm = ((InputMethodManager) mContext
                .getSystemService(Activity.INPUT_METHOD_SERVICE));
        if (mContext.getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (mContext.getCurrentFocus() != null)
                imm.hideSoftInputFromWindow(mContext
                                .getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }




    public void releaseMediaPlayer() {
        mChatAdapter.releaseMediaPlayer();
    }

    Handler handler = new Handler() {

        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case UPDATE_LAST_PAGE_LISTVIEW:
                    Log.i("Tag", "收到更新消息列表的消息");
                    mChatAdapter.refresh();
                    mChatView.removeHeadView();
                    break;
                case UPDATE_CHAT_LISTVIEW:
                    mChatAdapter.refresh();
                    break;
            }
        }
    };


    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        int touchPosition = 0;
    }

    // 更新消息列表
    public void addMessage() {
        Log.v(TAG,"add message");
        mChatAdapter.refresh();
        mChatView.setToBottom();
    }

    public MsgListAdapter getAdapter() {
        return mChatAdapter;
    }

    public void setAdapter(MsgListAdapter adapter) {
        mChatAdapter = adapter;
    }

    public Conversation getConversation() {
        return mConv;
    }

    public String getSign() {
        return mConv.getType() + ":" + mConv.getTargetId();
    }

    public String getTargetID() {
        return mTargetID;
    }

    public long getGroupID() {
        return mGroupID;
    }

    public boolean isGroup() {
        return mIsGroup;
    }

    public void refresh() {
        mChatView.setChatTitle(mConv.getDisplayName());
        mChatAdapter.refresh();
    }

    public void addGroupMember(long groupID) {
        //若为当前会话，刷新界面
        if(mIsGroup){
            if (groupID == Long.parseLong(mConv.getTargetId())) {
                mChatAdapter.refresh();
                mChatView.showRightBtn();
            }
        }
    }

    public void removeGroupMember(long groupID) {
        if(mIsGroup){
            if (groupID == Long.parseLong(mConv.getTargetId())) {
                mChatAdapter.refresh();
            }
        }
    }

    public void onGroupExit(long groupID, boolean isCreator) {
        if(mIsGroup){
            if(groupID == Long.parseLong(mConv.getTargetId())){
                if(isCreator)
                    mChatView.dismissRightBtn();
                mChatAdapter.refresh();
            }
        }
    }
}

package com.example.testvolley;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.receiver.MessageEventReceiver;
import com.example.util.ExitApplication;

import java.util.List;

import cn.jpush.im.android.api.Conversation;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.api.event.ConversationRefreshEvent;
import cn.jpush.im.android.api.event.GroupMemberAddedEvent;
import cn.jpush.im.android.api.event.GroupMemberExitEvent;
import cn.jpush.im.android.api.event.GroupMemberRemovedEvent;

/**
 * Created by Administrator on 2016/5/13.
 */
public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "ChatActivity";
    private ChatView mChatView;
    private ChatController mChatController;
    private RefreshChatListReceiver mChatReceiver;
    private String mTargetID;
    public static Context mChatActivity;
    private Handler mHandler;

    public static String SIGN = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        JMessageClient.registerEventReceiver(this);
        setContentView(R.layout.activity_chat);
        ExitApplication.getInstance().addActivity(this);
        mChatActivity = this;
        mChatView = (ChatView) findViewById(R.id.chat_view);
        mChatView.initModule();
        mChatController = new ChatController(mChatView, this);
        mChatView.setListeners(mChatController);
        mChatView.setOnTouchListener(mChatController);
        mChatView.setOnScrollListener(mChatController);
        initReceiver();
        mChatView.setToBottom();
        mHandler = new BaseHandler();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        ChatActivity.SIGN = mChatController.getSign();
        Log.v(TAG,ChatActivity.SIGN);
        NotificationManager manager = (NotificationManager) getApplicationContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        int cancelID = MessageEventReceiver.mNotificationList.indexOf(mChatController.getTargetID());
        if(cancelID != -1)
            manager.cancel(cancelID);
        mChatController.refresh();
        super.onResume();
    }

    @Override
    public void onBackPressed(){
        mTargetID = mChatController.getTargetID();
        ChatActivity.SIGN = "";
        Conversation conv = JMessageClient.getConversation(ConversationType.single,mTargetID);
        conv.resetUnreadCount();
        super.onBackPressed();
    }

    protected void onDestory(){
        JMessageClient.unRegisterEventReceiver(this);
        super.onDestroy();
        unregisterReceiver(mChatReceiver);
        ChatActivity.SIGN = "";
    }

    @Override
    protected void onStop(){
        ChatActivity.SIGN = "";
        if(mChatController.getConversation() != null){
            mChatController.getConversation().resetUnreadCount();
        }
        JMessageClient.unRegisterEventReceiver(this);
        super.onStop();
    }

    // 更新消息的广播
    private void initReceiver() {
        // 注册更新消息列表的广播
        mChatReceiver = new RefreshChatListReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MyApplication.REFRESH_CHATTING_ACTION);
        filter.addAction(MyApplication.REFRESH_CHATTING_ACTION_IMAGE);
        filter.addAction(MyApplication.ADD_GROUP_MEMBER_ACTION);
        filter.addAction(MyApplication.REMOVE_GROUP_MEMBER_ACTION);
        filter.addAction(MyApplication.UPDATE_GROUP_NAME_ACTION);
        registerReceiver(mChatReceiver, filter);
    }

    private class RefreshChatListReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent data) {
            Log.v(TAG,"RefreshChatListReceiver");
            if(data != null){

                final int messageID = data.getIntExtra("messageID", 0);
                mTargetID = data.getStringExtra("targetID");
                boolean isGroup = data.getBooleanExtra("isGroup", false);
//                if(isGroup)
//                    mConv = JMessageClient.getConversation(ConversationType.group, mTargetID);
//                else mConv = JMessageClient.getConversation(ConversationType.single, mTargetID);
                if (data.getAction().equals(
                        MyApplication.REFRESH_CHATTING_ACTION)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (messageID != 0)
                                mChatController.addMessage();
                        }
                    });
                } else if (data.getAction().equals(
                        MyApplication.REFRESH_CHATTING_ACTION_IMAGE)) {
                    // 刷新图片
//                    handleImgRefresh(data, isGroup);
                }
            }
        }

    }

    private void handleImgRefresh(Intent data, boolean isGroup) {
        mTargetID = data.getStringExtra("targetID");
        long groupID = data.getLongExtra("groupID", 0);
        Log.i(TAG, "Refresh Image groupID: " + groupID);
        //判断是否在当前会话中发图片
        if(mTargetID != null){
            if(mTargetID.equals(mChatController.getTargetID())){
                // 可能因为从其他界面回到聊天界面时，MsgListAdapter已经收到更新的消息了
                // 但是ListView没有刷新消息，要重新new Adapter, 并把这个Adapter传到ChatController
                // 保证ChatActivity和ChatController使用同一个Adapter
                mChatController.setAdapter(new MsgListAdapter(ChatActivity.this, isGroup, mTargetID, groupID));
                // 重新绑定Adapter
                mChatView.setChatListAdapter(mChatController.getAdapter());
//                mChatController.refresh();
                mChatController.getAdapter().setSendImg(data.getIntArrayExtra("msgIDs"));
            }
        }else if(groupID != 0){
            if(groupID == mChatController.getGroupID()){
                mChatController.setAdapter(new MsgListAdapter(
                        ChatActivity.this, isGroup, mTargetID, groupID));
                // 重新绑定Adapter
                mChatView.setChatListAdapter(mChatController.getAdapter());
//                mChatController.refresh();
                mChatController.getAdapter().setSendImg(data.getIntArrayExtra("msgIDs"));
            }
        }
    }

    public void onEvent(ConversationRefreshEvent conversationRefreshEvent){
        mHandler.sendEmptyMessage(MyApplication.REFRESH_GROUP_NAME);
    }

    public void onEvent(GroupMemberAddedEvent groupMemberAddedEvent){
        long groupID = groupMemberAddedEvent.getGroupID();
        List<String> members = groupMemberAddedEvent.getMembers();
        Log.i(TAG, "onGroupMemberAdded");
        for (String member : members) {
            Log.i(TAG, member + "join");

            android.os.Message msg = mHandler.obtainMessage();
            msg.what = MyApplication.ADD_GROUP_MEMBER_EVENT;
            Bundle bundle = new Bundle();
            bundle.putLong("groupID", groupID);
            msg.setData(bundle);
            msg.sendToTarget();
        }
    }

    public void onEvent(GroupMemberRemovedEvent groupMemberRemovedEvent){
        long groupID = groupMemberRemovedEvent.getGroupID();
        List<String> members = groupMemberRemovedEvent.getMembers();
        Log.i(TAG, "onGroupMemberRemoved");
        for (String member : members) {
            android.os.Message msg = mHandler.obtainMessage();
            msg.what = MyApplication.REMOVE_GROUP_MEMBER_EVENT;
            Bundle bundle = new Bundle();
            if(member.equals(JMessageClient.getMyInfo().getUserName())){
                bundle.putBoolean("deleted", true);
            }

            bundle.putLong("groupID", groupID);
            msg.setData(bundle);
            msg.sendToTarget();
        }
    }

    public void onEvent(GroupMemberExitEvent groupMemberExitEvent){
        long groupID = groupMemberExitEvent.getGroupID();
        boolean isCreator = groupMemberExitEvent.containsGroupOwner();
        List<String> members = groupMemberExitEvent.getMembers();
        if(isCreator){
            android.os.Message msg = mHandler.obtainMessage();
            msg.what = MyApplication.ON_GROUP_EXIT_EVENT;
            Bundle bundle = new Bundle();
            bundle.putBoolean("isCreator", true);
            bundle.putLong("groupID", groupID);
            msg.setData(bundle);
            msg.sendToTarget();
        }else {
            for(String userName : members){
                android.os.Message msg = mHandler.obtainMessage();
                msg.what = MyApplication.ON_GROUP_EXIT_EVENT;
                Bundle bundle = new Bundle();
                Log.i(TAG, userName + "exit");
                bundle.putLong("groupID", groupID);
                msg.setData(bundle);
                msg.sendToTarget();
            }

        }
    }
    public class BaseHandler extends Handler {

        @Override
        public void handleMessage(android.os.Message msg) {
            handleMsg(msg);
        }
    }

    public void handleMsg(Message message){}

}

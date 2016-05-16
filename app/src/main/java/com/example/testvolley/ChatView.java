package com.example.testvolley;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ChatView extends RelativeLayout{
    private LinearLayout mBackground;
    private ListView mChatListView;
    private ImageButton mReturnBtn;
    private ImageButton mRightBtn;
    private TextView mChatTitle;
    public EditText mChatInputEt;
    private ImageButton mSendMusicBtn;

    private Button mSendMsgBtn;
    private View mLoadingMessage;
    Context mContext;

    public ChatView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        this.mContext = context;
    }
    public void initModule(){
        mChatListView = (ListView) findViewById(R.id.chat_list);
        mReturnBtn = (ImageButton) findViewById(R.id.iv_return);
        mChatTitle = (TextView) findViewById(R.id.title);
        mChatInputEt = (EditText) findViewById(R.id.chat_input_et);
        mSendMusicBtn = (ImageButton) findViewById(R.id.send_music);

        mSendMsgBtn = (Button) findViewById(R.id.send_msg_btn);
        mBackground = (LinearLayout) findViewById(R.id.chat_background);
        mBackground.requestFocus();
        mLoadingMessage = LayoutInflater.from(mContext).inflate(R.layout.loading_message_view,null);
        mChatInputEt.addTextChangedListener(watcher);
        mChatInputEt.setOnFocusChangeListener(listener);
        mChatInputEt.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        mChatInputEt.setSingleLine(false);
        mChatInputEt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE){
//                    dismissMoreMenu();
                    Log.i("ChatView", "dismissMoreMenu()----------");
                }
                return false;
            }
        });
        setMoreMenuHeight();
        mChatInputEt.setMaxLines(4);

    }

    public void setMoreMenuHeight() {
        SharedPreferences sp = mContext.getSharedPreferences("youting", 0);
        int softKeyboardHeight = sp.getInt("SoftKeyboardHeight", 0);
        if(softKeyboardHeight > 0){
            //           mMoreMenuTl.setLayoutParams(new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, softKeyboardHeight));
        }

    }

    private TextWatcher watcher = new TextWatcher(){
        private CharSequence temp = "";
        @Override
        public void afterTextChanged(Editable arg0) {
            // TODO Auto-generated method stub
            if(temp.length() > 0){

                mSendMsgBtn.setVisibility(View.VISIBLE);
            }else{

                mSendMsgBtn.setVisibility(View.GONE);
            }
        }

        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                      int arg3) {
            // TODO Auto-generated method stub

        }

        public void onTextChanged(CharSequence s, int start, int count,
                                  int after) {
            // TODO Auto-generated method stub
            temp = s;
        }

    };

    public void focusTo(boolean inputFocus){
        if(inputFocus){
            mChatInputEt.requestFocus();
            Log.i("ChatView", "show softInput");
            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    OnFocusChangeListener listener = new OnFocusChangeListener() {

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if(hasFocus){
                Log.i("ChatView", "Input focus");
//                showMoreMenu();

            }
        }
    };

    public void setListeners(OnClickListener onClickListener){
        mReturnBtn.setOnClickListener(onClickListener);
        mChatInputEt.setOnClickListener(onClickListener);
        mSendMsgBtn.setOnClickListener(onClickListener);
        mSendMusicBtn.setOnClickListener(onClickListener);


    }

    public void setOnTouchListener(OnTouchListener listener){
        mChatListView.setOnTouchListener(listener);
    }

    public void setChatListAdapter(MsgListAdapter adapter) {
        mChatListView.setAdapter(adapter);
        setToBottom();
    }

    public void setOnScrollListener(OnScrollListener onScrollChangedListener){
        mChatListView.setOnScrollListener(onScrollChangedListener);
    }

    //如果是文字输入
    public void isKeyBoard(){
        mSendMusicBtn.setBackgroundResource(R.mipmap.ic_share);
        mChatInputEt.setVisibility(View.VISIBLE);


        if(mChatInputEt.getText().length() > 0){
            mSendMsgBtn.setVisibility(View.VISIBLE);

        }else {
            mSendMsgBtn.setVisibility(View.GONE);

        }
    }



    public String getChatInput(){
        return mChatInputEt.getText().toString();
    }



    public void setChatTitle(String targetId){
        mChatTitle.setText(targetId);
    }

    public void clearInput() {
        mChatInputEt.setText("");
    }

    public void setToBottom() {
        mChatListView.post(new Runnable() {
            @Override
            public void run() {
                mChatListView.setSelection(mChatListView.getBottom());
            }
        });
    }

    public void removeHeadView() {
        mChatListView.removeHeaderView(mLoadingMessage);
    }

    public void addHeadView() {
        mChatListView.addHeaderView(mLoadingMessage);
    }

    public void dismissRightBtn() {
        mRightBtn.setVisibility(View.GONE);
    }

    public void showRightBtn() {
        mRightBtn.setVisibility(View.VISIBLE);
    }


}

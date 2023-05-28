package com.vanniktech.emoji.sample;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiViewBoard;
import com.vanniktech.emoji.emoji.Emoji;

public class MainActivityBasic extends AppCompatActivity {


    private EmojiViewBoard emojiViewBoard;
    protected EmojiEditText emojiEditText;

    View mRootView = null;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        emojiViewBoard = new MyEmojiViewBoard(this);
        LinearLayout frameView= emojiViewBoard.createFrameView();
        emojiViewBoard.fillUpVerticalLinearView();

        emojiEditText = findViewById(R.id.emojiEditText);
        FrameLayout emojiViewContainer = findViewById(R.id.emojiViewContainer);

        mRootView = emojiViewContainer.getRootView();

        emojiViewBoard.replaceView(emojiViewContainer);


        emojiViewBoard.setup(mRootView);

        ((Button)findViewById(R.id.clickBtn)).setText("Click");

        findViewById(R.id.clickBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MainActivityBasic.this.showKeyboard();
            }
        });

    }

    public void showKeyboard(){

        MyEmojiViewBoard viewBoard = new MyEmojiViewBoard(this);
        LinearLayout frameView=  viewBoard.createFrameView();
//        View customView = new Button(this);
        PopupWindow popupWindow = new PopupWindow(frameView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

// Set the PopupWindow's background to null to avoid any extra padding
        popupWindow.setBackgroundDrawable(null);

// Set the PopupWindow to be focusable so it can handle touch events
        popupWindow.setFocusable(true);

        popupWindow.setAnimationStyle(0);

        ViewGroup rootView = findViewById(android.R.id.content);
        popupWindow.showAtLocation(mRootView, Gravity.BOTTOM, 0, 0);
        int originalHeight = rootView.getHeight();
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                rootView.getLayoutParams().height = originalHeight;
                rootView.requestLayout();
            }
        });
        rootView.getLayoutParams().height = originalHeight - popupWindow.getHeight();
        rootView.requestLayout();

        viewBoard.setup(rootView);

    }

    public class MyEmojiViewBoard extends EmojiViewBoard {


        public MyEmojiViewBoard(Context context) {
            super(context);
        }

        @Override
        public int deleteOneUniChar() {

            if(emojiEditText == null) return -1;

            KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL);
            emojiEditText.dispatchKeyEvent(event);

            return 0;
        }

        @Override
        public int commitOneEmoji(Emoji emoji) {

            if(emojiEditText == null) return -1;

            if (emoji != null) {
                Editable editable = emojiEditText.getText();
                if(editable == null) return -1;
                final int start = emojiEditText.getSelectionStart();
                final int end = emojiEditText.getSelectionEnd();

                String unicodeStr = emoji.getUnicode();
                if (start < 0) {
                    emojiEditText.append(unicodeStr);
                } else {
                    editable.replace(Math.min(start, end), Math.max(start, end), unicodeStr, 0, unicodeStr.length());
                }
            }
            return 0;
        }
    }
}

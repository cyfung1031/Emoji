package com.vanniktech.emoji.sample;


import android.content.Context;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import com.vanniktech.emoji.CustomEventDispatcher;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPager2Util;
import com.vanniktech.emoji.EmojiView2A;
import com.vanniktech.emoji.emoji.Emoji;

public class MainActivity3  extends AppCompatActivity {



    private EmojiEditText emojiEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        CustomEventDispatcher myEventDispatcher =  new CustomEventDispatcher(Looper.getMainLooper());


        FragmentActivity fragmentActivity = this;
        emojiEditText = findViewById(R.id.emojiEditText);
        FrameLayout emojiViewContainer = findViewById(R.id.emojiViewContainer);


        final Context context = this;
        final View rootView = emojiViewContainer.getRootView();


        final EmojiView2A emojiView = new EmojiView2A(context);
        emojiView.setup(fragmentActivity, rootView, myEventDispatcher);
        final  EmojiPager2Util emojiPager2Util = emojiView.getEmojiPager2Util();
//        EmojiViewY.this.addView(emojiView);

        emojiViewContainer.addView(emojiView);


        myEventDispatcher.addEventListener("emojiClick", new CustomEventDispatcher.EventListener() {
            @Override
            public void onEvent(Object eventObject) {
                Emoji emoji = (Emoji) eventObject;
                emojiPager2Util.setInput(emojiEditText, emoji);
            }
        });


        myEventDispatcher.addEventListener("backSpaceToolButton", new CustomEventDispatcher.EventListener() {
            @Override
            public void onEvent(Object eventObject) {
                Emoji emoji = (Emoji) eventObject;
                emojiPager2Util.backspace(emojiEditText);
            }
        });



    }

}

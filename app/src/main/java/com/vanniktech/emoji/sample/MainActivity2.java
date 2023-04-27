package com.vanniktech.emoji.sample;

import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiView;
import com.vanniktech.emoji.EmojiViewX;

public class MainActivity2  extends AppCompatActivity {



    private EmojiViewX emojiView;
    private EmojiEditText emojiEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        emojiView=new EmojiViewX(this);

        emojiEditText = findViewById(R.id.emojiEditText);
        emojiView.setEditText(emojiEditText);
        FrameLayout emojiViewContainer = findViewById(R.id.emojiViewContainer);


        emojiViewContainer.addView(emojiView);

        emojiView.setup(emojiViewContainer);

    }
}

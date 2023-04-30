package com.vanniktech.emoji.sample;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiViewOuter;
import com.vanniktech.emoji.emoji.Emoji;

public class MainActivity4 extends AppCompatActivity {


    private EmojiViewOuter emojiView;
    private EmojiEditText emojiEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        emojiView = new MyEmojiView(this);

        emojiEditText = findViewById(R.id.emojiEditText);
        FrameLayout emojiViewContainer = findViewById(R.id.emojiViewContainer);


        emojiViewContainer.addView(emojiView);

        emojiView.setup(emojiViewContainer);

    }
    public class MyEmojiView extends EmojiViewOuter{


        public MyEmojiView(@NonNull Context context) {
            super(context);
        }

        public MyEmojiView(@NonNull Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
        }

        public MyEmojiView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
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

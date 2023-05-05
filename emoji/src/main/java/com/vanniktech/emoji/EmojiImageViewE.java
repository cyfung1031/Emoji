package com.vanniktech.emoji;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;

import com.vanniktech.emoji.emoji.Emoji;

public abstract class EmojiImageViewE extends AppCompatImageView {


    public EmojiImageViewE(Context context) {
        super(context);
    }

    public EmojiImageViewE(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EmojiImageViewE(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    abstract void updateEmoji(@NonNull final Emoji emoji);
    abstract boolean onEmojiLongPress(final View view);
    abstract void onEmojiClick(final View view);
    abstract void setEmoji(@NonNull final Emoji emoji);


}

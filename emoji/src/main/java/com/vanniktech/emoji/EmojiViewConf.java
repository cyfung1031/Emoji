package com.vanniktech.emoji;

import android.view.View;

import androidx.annotation.NonNull;

import com.vanniktech.emoji.emoji.Emoji;
import com.vanniktech.emoji.listeners.OnEmojiBackspaceClickListener;
import com.vanniktech.emoji.listeners.OnEmojiClickListener;
import com.vanniktech.emoji.listeners.OnEmojiLongClickListener;

public class EmojiViewConf implements OnEmojiClickListener, OnEmojiLongClickListener, OnEmojiBackspaceClickListener {


    @Override
    public void onEmojiBackspaceClicked(View v) {

    }

    @Override
    public void onEmojiClick(@NonNull EmojiImageView emoji, @NonNull Emoji imageView) {

    }

    @Override
    public void onEmojiLongClick(@NonNull EmojiImageView view, @NonNull Emoji emoji) {

    }
}

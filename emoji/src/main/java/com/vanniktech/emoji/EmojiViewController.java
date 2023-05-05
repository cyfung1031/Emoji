package com.vanniktech.emoji;

import android.content.Context;

import com.vanniktech.emoji.emoji.Emoji;

public interface EmojiViewController {

    public void controller(int msgId);

    void setPopupRootImageView(EmojiImageViewGeneral rootImageView);

    void setPopupVariant(Emoji variant);

    void setEmojiViewInner(EmojiViewInner v);
    void setEmojiVariantPopupGeneral(EmojiVariantPopupGeneral v);

    void setRecentEmojiPageUpdateState(int state);

    int getRecentEmojiPageUpdateState();

    EmojiViewInner getEmojiViewInner();

    void setRecentEmojiGridView(EmojiGridInner newView);
    EmojiGridInner getRecentEmojiGridView();
}

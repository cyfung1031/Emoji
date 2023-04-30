package com.vanniktech.emoji;

import android.content.Context;

import com.vanniktech.emoji.emoji.Emoji;

public interface EmojiViewController {

    public void controller(int msgId);

    void setPopupRootImageView(EmojiImageViewGeneral rootImageView);

    void setPopupVariant(Emoji variant);

    void setEmojiViewOuter(EmojiViewOuter v);
    void setEmojiViewInner(EmojiViewInner v);
    void setEmojiVariantPopupGeneral(EmojiVariantPopupGeneral v);

    void setRecentEmojiPageUpdateState(int state);

    int getRecentEmojiPageUpdateState();

    EmojiViewOuter getEmojiViewOuter();
    EmojiViewInner getEmojiViewInner();

    void setRecentEmojiGridView(EmojiGridInner newView);
    EmojiGridInner getRecentEmojiGridView();
}

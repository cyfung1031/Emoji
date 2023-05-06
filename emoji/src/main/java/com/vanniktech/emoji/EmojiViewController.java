package com.vanniktech.emoji;

import android.widget.PopupWindow;

import com.vanniktech.emoji.emoji.Emoji;

public interface EmojiViewController {

    public void controller(int msgId);

    void setPopupRootImageView(EmojiImageViewG rootImageView);

    void setPopupVariant(Emoji variant);

    void setEmojiViewInner(EmojiViewInner v);
    void setEmojiVariantPopupGeneral(EmojiVariantPopupGeneral v);

    void setRecentEmojiPageUpdateState(int state);

    int getRecentEmojiPageUpdateState();

    EmojiViewInner getEmojiViewInner();

    void setRecentEmojiGridView(EmojiGrid newView);
    EmojiGrid getRecentEmojiGridView();

    void setPopupViewWindow(PopupWindow popupWindow);

    PopupWindow getPopupViewWindow();
}

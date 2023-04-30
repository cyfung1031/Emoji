package com.vanniktech.emoji;

import android.widget.EditText;

import com.vanniktech.emoji.emoji.Emoji;
import com.vanniktech.emoji.emoji.EmojiCategory;

public class EmojiPager2Util {
    final RecentEmoji recentEmoji;
    final VariantEmoji variantEmoji;

    public EmojiPager2Util(RecentEmoji recentEmoji, VariantEmoji variantEmoji){
        this. recentEmoji = recentEmoji;
        this.variantEmoji = variantEmoji;
    }

    public void setInput(EditText editText, Emoji emoji){
        Utils.input(editText, emoji);
    }

    public void backspace(EditText editText){
        Utils.backspace(editText);
    }

    public int getPageCount() {
        return EmojiManager.getInstance().getCategories().length + recentAdapterItemCount();
    }

    public int recentAdapterItemCount() {
        return hasRecentEmoji() ? 1 : 0;
    }
    public boolean hasRecentEmoji() {
        return !(recentEmoji instanceof NoRecentEmoji);
    }
    public int numberOfRecentEmojis() {
        if(recentEmoji == null) return 0;
        if(!hasRecentEmoji()) return 0;
        return recentEmoji.getRecentEmojis().size();
    }

    public EmojiCategory[] getEmojiCategories(){
        return EmojiManager.getInstance().getCategories();
    }


}

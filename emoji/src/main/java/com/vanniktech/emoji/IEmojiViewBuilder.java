package com.vanniktech.emoji;


import android.view.View;

import androidx.annotation.CheckResult;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.viewpager2.widget.ViewPager2;

import com.vanniktech.emoji.listeners.OnEmojiBackspaceClickListener;
import com.vanniktech.emoji.listeners.OnEmojiClickListener;
import com.vanniktech.emoji.listeners.OnEmojiPopupDismissListener;
import com.vanniktech.emoji.listeners.OnEmojiPopupShownListener;
import com.vanniktech.emoji.listeners.OnSoftKeyboardCloseListener;
import com.vanniktech.emoji.listeners.OnSoftKeyboardOpenListener;

public interface IEmojiViewBuilder<TBuilder>{



    // Getter methods
    @NonNull
    public View getRootView();

    @ColorInt
    public int getBackgroundColor();

    @ColorInt
    public int getIconColor();

    @ColorInt
    public int getSelectedIconColor();
    @ColorInt
    public int getDividerColor();

    @Nullable
    public ViewPager2.PageTransformer getPageTransformer();

    @NonNull
    public RecentEmoji getRecentEmoji();

    @NonNull
    public VariantEmoji getVariantEmoji();

    @CheckResult
    public TBuilder setOnSoftKeyboardCloseListener(@Nullable final OnSoftKeyboardCloseListener listener);

    @CheckResult public TBuilder setOnEmojiClickListener(@Nullable final OnEmojiClickListener listener);
    @CheckResult public TBuilder setOnSoftKeyboardOpenListener(@Nullable final OnSoftKeyboardOpenListener listener);

    @CheckResult public TBuilder setOnEmojiPopupShownListener(@Nullable final OnEmojiPopupShownListener listener);

    @CheckResult public TBuilder setOnEmojiPopupDismissListener(@Nullable final OnEmojiPopupDismissListener listener);
    @CheckResult public TBuilder setOnEmojiBackspaceClickListener(@Nullable final OnEmojiBackspaceClickListener listener);

    @CheckResult public TBuilder setKeyboardAnimationStyle(@StyleRes final int animation);

    @CheckResult public TBuilder setPageTransformer(@Nullable final ViewPager2.PageTransformer transformer);


}

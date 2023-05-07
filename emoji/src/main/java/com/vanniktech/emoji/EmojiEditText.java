/*
 * Copyright (C) 2016 - Niklas Baudy, Ruben Gees, Mario Đanić and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.vanniktech.emoji;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.CallSuper;
import androidx.annotation.DimenRes;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.appcompat.widget.AppCompatEditText;

import com.vanniktech.emoji.emoji.Emoji;

import java.lang.ref.WeakReference;

/**
 * Reference implementation for an EditText with emoji support.
 */
public class EmojiEditText extends AppCompatEditText implements IEmojiEditable {
    private float emojiSize;
    private boolean disableKeyboardInput;
    private boolean disableUX = true;

    public EmojiEditText(final Context context) {
        this(context, null);
    }

    public EmojiEditText(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        emojiSize = Utils.initTextView(this, attrs);
    }

    public EmojiEditText(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        emojiSize = Utils.initTextView(this, attrs);
    }

    @Override
    @CallSuper
    protected void onTextChanged(final CharSequence text, final int start, final int lengthBefore, final int lengthAfter) {
        if (isInEditMode()) {
            return;
        }

        final Paint.FontMetrics fontMetrics = getPaint().getFontMetrics();
        final float defaultEmojiSize = fontMetrics.descent - fontMetrics.ascent;
        EmojiManager.getInstance().replaceWithImages(getContext(), getText(), emojiSize != 0 ? emojiSize : defaultEmojiSize);
    }

    @Override
    @CallSuper
    public void backspace() {
        Utils.backspace(this);
    }

    @Override
    @CallSuper
    public void input(final Emoji emoji) {
        Utils.input(this, emoji);
    }

    @Override
    public final float getEmojiSize() {
        return emojiSize;
    }

    @Override
    public final void setEmojiSize(@Px final int pixels) {
        setEmojiSize(pixels, true);
    }

    @Override
    public final void setEmojiSize(@Px final int pixels, final boolean shouldInvalidate) {
        emojiSize = pixels;

        if (shouldInvalidate) {
            setText(getText());
        }
    }

    @Override
    public final void setEmojiSizeRes(@DimenRes final int res) {
        setEmojiSizeRes(res, true);
    }

    @Override
    public final void setEmojiSizeRes(@DimenRes final int res, final boolean shouldInvalidate) {
        setEmojiSize(getResources().getDimensionPixelSize(res), shouldInvalidate);
    }

    @Override
    public void setOnFocusChangeListener(OnFocusChangeListener l) {
        final OnFocusChangeListener onFocusChangeListener = getOnFocusChangeListener();
        if (onFocusChangeListener instanceof OnFocusChangeListenerForEmojisOnly) {
            l = ((OnFocusChangeListenerForEmojisOnly) onFocusChangeListener).cloneListener(l);
        }
        super.setOnFocusChangeListener(l);
    }

    public boolean isKeyboardInputDisabled() {
        return disableKeyboardInput;
    }

    /**
     * Disables the keyboard input using a focus change listener and delegating to the previous focus change listener.
     */
    public void disableKeyboardInput(final IPopup emojiPopup) {
        disableKeyboardInput = true;
        super.setOnFocusChangeListener(new OnFocusChangeListenerForEmojisOnly(getOnFocusChangeListener(), emojiPopup));
    }

    /**
     * Disables the keyboard input using a focus change listener and delegating to the previous focus change listener.
     */
    public void disableKeyboardInput(final EmojiPopupGeneral emojiPopup) {
        disableKeyboardInput = true;
//    super.setOnFocusChangeListener(new OnFocusChangeListenerForEmojisOnly(getOnFocusChangeListener(), emojiPopup));
    }

    /**
     * Enables the keyboard input. If it has been disabled before using {@link #disableKeyboardInput(IPopup)} the OnFocusChangeListener will be preserved.
     */
    public void enableKeyboardInput() {
        disableKeyboardInput = false;
        final OnFocusChangeListener onFocusChangeListener = getOnFocusChangeListener();

        if (onFocusChangeListener instanceof OnFocusChangeListenerForEmojisOnly) {
            final OnFocusChangeListenerForEmojisOnly cast = (OnFocusChangeListenerForEmojisOnly) onFocusChangeListener;
            super.setOnFocusChangeListener(cast.onFocusChangeListener);
        }
    }

    /**
     * Forces this EditText to contain only one Emoji.
     */
    public void forceSingleEmoji() {
        SingleEmojiTrait.install(this);
    }

    @Override
    protected void onAttachedToWindow() {
        disableUX = false;
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        disableUX = true;
    }

    @Override
    public boolean onTextContextMenuItem(int id) {
        if (disableUX) return false;
        return super.onTextContextMenuItem(id);
    }

    @Override
    public void selectAll() {
        if (disableUX) return;
        super.selectAll();
    }

    @Override
    public void setSelection(int index) {
        if (disableUX) return;
        super.setSelection(index);
    }

    @Override
    public void setSelection(int start, int stop) {
        if (disableUX) return;
        super.setSelection(start, stop);
    }

    @Override
    public void extendSelection(int index) {
        if (disableUX) return;
        super.extendSelection(index);
    }

    static class OnFocusChangeListenerForEmojisOnly implements OnFocusChangeListener {
        @Nullable
        final OnFocusChangeListener onFocusChangeListener;
        private WeakReference<IPopup> emojiPopupWR = null;

        OnFocusChangeListenerForEmojisOnly(@Nullable final OnFocusChangeListener onFocusChangeListener, final IPopup emojiPopup) {

            this.emojiPopupWR = new WeakReference<>(emojiPopup);

            this.onFocusChangeListener = onFocusChangeListener;
        }

        public IPopup getEmojiPopup() {
            IPopup emojiPopup = emojiPopupWR != null ? emojiPopupWR.get() : null;
            return emojiPopup;
        }

        public OnFocusChangeListenerForEmojisOnly cloneListener(final OnFocusChangeListener l) {
            return new OnFocusChangeListenerForEmojisOnly(l, this.getEmojiPopup());
        }

        @Override
        public void onFocusChange(final View view, final boolean hasFocus) {
            IPopup emojiPopup = getEmojiPopup();
            if (emojiPopup != null) {
                if (hasFocus) {
                    emojiPopup.start();
                    emojiPopup.show();
                } else {
                    emojiPopup.dismiss();
                }
            }

            if (onFocusChangeListener != null) {
                onFocusChangeListener.onFocusChange(view, hasFocus);
            }
        }
    }
}

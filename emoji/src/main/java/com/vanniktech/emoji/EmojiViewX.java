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

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.view.autofill.AutofillManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import androidx.annotation.CheckResult;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager.widget.ViewPager;
import com.vanniktech.emoji.emoji.Emoji;
import com.vanniktech.emoji.listeners.OnEmojiBackspaceClickListener;
import com.vanniktech.emoji.listeners.OnEmojiClickListener;
import com.vanniktech.emoji.listeners.OnEmojiLongClickListener;
import com.vanniktech.emoji.listeners.OnEmojiPopupDismissListener;
import com.vanniktech.emoji.listeners.OnEmojiPopupShownListener;
import com.vanniktech.emoji.listeners.OnSoftKeyboardCloseListener;
import com.vanniktech.emoji.listeners.OnSoftKeyboardOpenListener;
import java.lang.ref.WeakReference;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.O;
import static androidx.core.view.ViewCompat.requestApplyInsets;
import static com.vanniktech.emoji.Utils.backspace;
import static com.vanniktech.emoji.Utils.checkNotNull;

@SuppressWarnings("PMD.GodClass") public final class EmojiViewX extends FrameLayout implements EmojiResultReceiver.Receiver {
    static final int MIN_KEYBOARD_HEIGHT = 50;
    static final int APPLY_WINDOW_INSETS_DURATION = 250;

    @Nullable View rootView = null;
    @Nullable Activity context = null;

    @NonNull RecentEmoji recentEmoji;
    @NonNull VariantEmoji variantEmoji;
    @NonNull EmojiVariantPopup variantPopup;

    EditText editText;

    boolean isPendingOpen;
    boolean isKeyboardOpen;

    private int globalKeyboardHeight;
    private int delay;

    @Nullable OnEmojiPopupShownListener onEmojiPopupShownListener;
    @Nullable OnSoftKeyboardCloseListener onSoftKeyboardCloseListener;
    @Nullable OnSoftKeyboardOpenListener onSoftKeyboardOpenListener;

    @Nullable OnEmojiBackspaceClickListener onEmojiBackspaceClickListener;
    @Nullable OnEmojiClickListener onEmojiClickListener;
    @Nullable OnEmojiPopupDismissListener onEmojiPopupDismissListener;

    int popupWindowHeight;
    int originalImeOptions = -1;

    final EmojiResultReceiver emojiResultReceiver = new EmojiResultReceiver(new Handler(Looper.getMainLooper()));

    final OnEmojiClickListener internalOnEmojiClickListener = new OnEmojiClickListener() {
        @Override public void onEmojiClick(@NonNull final EmojiImageView imageView, @NonNull final Emoji emoji) {
            Utils.input(editText, emoji);

            recentEmoji.addEmoji(emoji);
            variantEmoji.addVariant(emoji);
            imageView.updateEmoji(emoji);

            if (onEmojiClickListener != null) {
                onEmojiClickListener.onEmojiClick(imageView, emoji);
            }

            variantPopup.dismiss();

            recentEmoji.persist();
            variantEmoji.persist();
        }
    };

    final OnEmojiLongClickListener internalOnEmojiLongClickListener = new OnEmojiLongClickListener() {
        @Override public void onEmojiLongClick(@NonNull final EmojiImageView view, @NonNull final Emoji emoji) {
            variantPopup.show(view, emoji);
        }
    };

    final OnEmojiBackspaceClickListener internalOnEmojiBackspaceClickListener = new OnEmojiBackspaceClickListener() {
        @Override public void onEmojiBackspaceClick(final View v) {
            backspace(editText);

            if (onEmojiBackspaceClickListener != null) {
                onEmojiBackspaceClickListener.onEmojiBackspaceClick(v);
            }
        }
    };

    final PopupWindow.OnDismissListener onDismissListener = new PopupWindow.OnDismissListener() {
        @Override public void onDismiss() {
            if (editText instanceof EmojiEditText && ((EmojiEditText) editText).isKeyboardInputDisabled()) {
                editText.clearFocus();
            }
            if (onEmojiPopupDismissListener != null) {
                onEmojiPopupDismissListener.onEmojiPopupDismiss();
            }
        }
    };

    public void setup(@NonNull View view){
        EmojiViewX.Builder builder = EmojiViewX.Builder.fromRootView(view);

        this.context = (Activity) view.getContext();
        this.rootView = view.getRootView();
        // this.editText = editText;
        this.recentEmoji = builder.getRecentEmoji();
        this.variantEmoji = builder.getVariantEmoji();


//        popupWindow = new PopupWindow(context);
        variantPopup = new EmojiVariantPopup(rootView, internalOnEmojiClickListener);

        final EmojiView emojiView = new EmojiView(context,
                internalOnEmojiClickListener, internalOnEmojiLongClickListener, builder);

        emojiView.setOnEmojiBackspaceClickListener(internalOnEmojiBackspaceClickListener);

        EmojiViewX.this.addView(emojiView);

//        popupWindow.setContentView(emojiView);
//        popupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NOT_NEEDED);
//        popupWindow.setBackgroundDrawable(new BitmapDrawable(context.getResources(), (Bitmap) null)); // To avoid borders and overdraw.
//        popupWindow.setOnDismissListener(onDismissListener);
//
//        if (builder.keyboardAnimationStyle != 0) {
//            popupWindow.setAnimationStyle(builder.keyboardAnimationStyle);
//        }
//
//        // Root view might already be laid out in which case we need to manually call start()
//        if (rootView.getParent() != null) {
//            start();
//        }
//
//        rootView.addOnAttachStateChangeListener(new EmojiPopUpOnAttachStateChangeListener(this));



    }

    public void setEditText(@Nullable final EditText editText) {
        this.editText = editText;
    }

    public EmojiViewX(@NonNull Context context) {
        super(context);
    }

    public EmojiViewX(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public EmojiViewX(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }




    public void show() {
        if (Utils.shouldOverrideRegularCondition(context, editText) && originalImeOptions == -1) {
            originalImeOptions = editText.getImeOptions();
        }

        editText.setFocusableInTouchMode(true);
        editText.requestFocus();

        showAtBottomPending();
    }

    private void showAtBottomPending() {
        isPendingOpen = true;
        final InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);

        if (Utils.shouldOverrideRegularCondition(context, editText)) {
            editText.setImeOptions(editText.getImeOptions() | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
            if (inputMethodManager != null) {
                inputMethodManager.restartInput(editText);
            }
        }

        if (inputMethodManager != null) {
            emojiResultReceiver.setReceiver(this);
            inputMethodManager.showSoftInput(editText, InputMethodManager.RESULT_UNCHANGED_SHOWN, emojiResultReceiver);
        }
    }

    public void dismiss() {
        variantPopup.dismiss();
        recentEmoji.persist();
        variantEmoji.persist();

        emojiResultReceiver.setReceiver(null);

        if (originalImeOptions != -1) {
            editText.setImeOptions(originalImeOptions);
            final InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);

            if (inputMethodManager != null) {
                inputMethodManager.restartInput(editText);
            }

            if (SDK_INT >= O) {
                final AutofillManager autofillManager = context.getSystemService(AutofillManager.class);
                if (autofillManager != null) {
                    autofillManager.cancel();
                }
            }
        }
    }

    void showAtBottom() {
        isPendingOpen = false;

        if (onEmojiPopupShownListener != null) {
            onEmojiPopupShownListener.onEmojiPopupShown();
        }
    }

    @Override public void onReceiveResult(final int resultCode, final Bundle data) {
        if (resultCode == 0 || resultCode == 1) {
            showAtBottom();
        }
    }

    public static final class Builder  implements EmojiView.EmojiViewBuilder {
        @NonNull final View rootView;
        @StyleRes int keyboardAnimationStyle;
        @ColorInt int backgroundColor;
        @ColorInt int iconColor;
        @ColorInt int selectedIconColor;
        @ColorInt int dividerColor;
        @Nullable ViewPager.PageTransformer pageTransformer;
        @Nullable OnEmojiPopupShownListener onEmojiPopupShownListener;
        @Nullable OnSoftKeyboardCloseListener onSoftKeyboardCloseListener;
        @Nullable OnSoftKeyboardOpenListener onSoftKeyboardOpenListener;
        @Nullable OnEmojiBackspaceClickListener onEmojiBackspaceClickListener;
        @Nullable OnEmojiClickListener onEmojiClickListener;
        @Nullable OnEmojiPopupDismissListener onEmojiPopupDismissListener;
        @NonNull RecentEmoji recentEmoji;
        @NonNull VariantEmoji variantEmoji;
        int popupWindowHeight;

        private Builder(final View rootView) {
            this.rootView = checkNotNull(rootView, "The root View can't be null");
            initByContext(rootView.getContext());
        }
        private void initByContext(final Context context) {
            this.recentEmoji = new RecentEmojiManager(context);
            this.variantEmoji = new VariantEmojiManager(context);
        }


        // Getter methods
        @NonNull
        public View getRootView() {
            return rootView;
        }

        @StyleRes
        public int getKeyboardAnimationStyle() {
            return keyboardAnimationStyle;
        }

        @ColorInt
        public int getBackgroundColor() {
            return backgroundColor;
        }

        @ColorInt
        public int getIconColor() {
            return iconColor;
        }

        @ColorInt
        public int getSelectedIconColor() {
            return selectedIconColor;
        }

        @ColorInt
        public int getDividerColor() {
            return dividerColor;
        }

        @Nullable
        public ViewPager.PageTransformer getPageTransformer() {
            return pageTransformer;
        }

        @Nullable
        public OnEmojiPopupShownListener getOnEmojiPopupShownListener() {
            return onEmojiPopupShownListener;
        }

        @Nullable
        public OnSoftKeyboardCloseListener getOnSoftKeyboardCloseListener() {
            return onSoftKeyboardCloseListener;
        }

        @Nullable
        public OnSoftKeyboardOpenListener getOnSoftKeyboardOpenListener() {
            return onSoftKeyboardOpenListener;
        }

        @Nullable
        public OnEmojiBackspaceClickListener getOnEmojiBackspaceClickListener() {
            return onEmojiBackspaceClickListener;
        }

        @Nullable
        public OnEmojiClickListener getOnEmojiClickListener() {
            return onEmojiClickListener;
        }

        @Nullable
        public OnEmojiPopupDismissListener getOnEmojiPopupDismissListener() {
            return onEmojiPopupDismissListener;
        }

        @NonNull
        public RecentEmoji getRecentEmoji() {
            return recentEmoji;
        }

        @NonNull
        public VariantEmoji getVariantEmoji() {
            return variantEmoji;
        }

        /**
         * @param rootView The root View of your layout.xml which will be used for calculating the height
         * of the keyboard.
         * @return builder For building the {@link EmojiPopup}.
         */
        @CheckResult public static Builder fromRootView(final View rootView) {
            return new Builder(rootView);
        }

        @CheckResult public Builder setOnSoftKeyboardCloseListener(@Nullable final OnSoftKeyboardCloseListener listener) {
            onSoftKeyboardCloseListener = listener;
            return this;
        }

        @CheckResult public Builder setOnEmojiClickListener(@Nullable final OnEmojiClickListener listener) {
            onEmojiClickListener = listener;
            return this;
        }

        @CheckResult public Builder setOnSoftKeyboardOpenListener(@Nullable final OnSoftKeyboardOpenListener listener) {
            onSoftKeyboardOpenListener = listener;
            return this;
        }

        @CheckResult public Builder setOnEmojiPopupShownListener(@Nullable final OnEmojiPopupShownListener listener) {
            onEmojiPopupShownListener = listener;
            return this;
        }

        @CheckResult public Builder setOnEmojiPopupDismissListener(@Nullable final OnEmojiPopupDismissListener listener) {
            onEmojiPopupDismissListener = listener;
            return this;
        }

        @CheckResult public Builder setOnEmojiBackspaceClickListener(@Nullable final OnEmojiBackspaceClickListener listener) {
            onEmojiBackspaceClickListener = listener;
            return this;
        }

        /**
         * Set PopUpWindow's height.
         * If height is not 0 then this value will be used later on. If it is 0 then the keyboard height will
         * be dynamically calculated and set as {@link PopupWindow} height.
         * @param windowHeight - the height of {@link PopupWindow}
         *
         * @since 0.7.0
         */
        @CheckResult public Builder setPopupWindowHeight(final int windowHeight) {
            this.popupWindowHeight = Math.max(windowHeight, 0);
            return this;
        }

        /**
         * Allows you to pass your own implementation of recent emojis. If not provided the default one
         * {@link RecentEmojiManager} will be used.
         *
         * @since 0.2.0
         */
        @CheckResult public Builder setRecentEmoji(@NonNull final RecentEmoji recent) {
            recentEmoji = checkNotNull(recent, "recent can't be null");
            return this;
        }

        /**
         * Allows you to pass your own implementation of variant emojis. If not provided the default one
         * {@link VariantEmojiManager} will be used.
         *
         * @since 0.5.0
         */
        @CheckResult public Builder setVariantEmoji(@NonNull final VariantEmoji variant) {
            variantEmoji = checkNotNull(variant, "variant can't be null");
            return this;
        }

        @CheckResult public Builder setBackgroundColor(@ColorInt final int color) {
            backgroundColor = color;
            return this;
        }

        @CheckResult public Builder setIconColor(@ColorInt final int color) {
            iconColor = color;
            return this;
        }

        @CheckResult public Builder setSelectedIconColor(@ColorInt final int color) {
            selectedIconColor = color;
            return this;
        }

        @CheckResult public Builder setDividerColor(@ColorInt final int color) {
            dividerColor = color;
            return this;
        }

        @CheckResult public Builder setKeyboardAnimationStyle(@StyleRes final int animation) {
            keyboardAnimationStyle = animation;
            return this;
        }

        @CheckResult public Builder setPageTransformer(@Nullable final ViewPager.PageTransformer transformer) {
            pageTransformer = transformer;
            return this;
        }

        /*
        @CheckResult public EmojiPopup build(@NonNull final EditText editText) {
            EmojiManager.getInstance().verifyInstalled();
            checkNotNull(editText, "EditText can't be null");

            final EmojiPopup emojiPopup = new EmojiPopup(this, editText);
            emojiPopup.onSoftKeyboardCloseListener = onSoftKeyboardCloseListener;
            emojiPopup.onEmojiClickListener = onEmojiClickListener;
            emojiPopup.onSoftKeyboardOpenListener = onSoftKeyboardOpenListener;
            emojiPopup.onEmojiPopupShownListener = onEmojiPopupShownListener;
            emojiPopup.onEmojiPopupDismissListener = onEmojiPopupDismissListener;
            emojiPopup.onEmojiBackspaceClickListener = onEmojiBackspaceClickListener;
            emojiPopup.popupWindowHeight = Math.max(popupWindowHeight, 0);
            return emojiPopup;
        }
        */
    }

    static final class EmojiPopUpOnAttachStateChangeListener implements View.OnAttachStateChangeListener {
        private final WeakReference<EmojiPopup> emojiPopup;

        EmojiPopUpOnAttachStateChangeListener(final EmojiPopup emojiPopup) {
            this.emojiPopup = new WeakReference<>(emojiPopup);
        }

        @Override public void onViewAttachedToWindow(final View v) {
            final EmojiPopup popup = emojiPopup.get();

            if (popup != null) {
                popup.start();
            }
        }

        @Override public void onViewDetachedFromWindow(final View v) {
            final EmojiPopup popup = emojiPopup.get();

            if (popup != null) {
                popup.stop();
            }

            emojiPopup.clear();
            v.removeOnAttachStateChangeListener(this);
        }
    }


    static final class EmojiPopUpOnApplyWindowInsetsListener implements androidx.core.view.OnApplyWindowInsetsListener {
        private final WeakReference<EmojiPopup> emojiPopup;
        int previousOffset;

        EmojiPopUpOnApplyWindowInsetsListener(final EmojiPopup emojiPopup) {
            this.emojiPopup = new WeakReference<>(emojiPopup);
        }

        @Override
        public WindowInsetsCompat onApplyWindowInsets(final View v, final WindowInsetsCompat insets) {
            final EmojiPopup popup = emojiPopup.get();

            if (popup != null) {
                final int offset;

                if (insets.getSystemWindowInsetBottom() < insets.getStableInsetBottom()) {
                    offset = insets.getSystemWindowInsetBottom();
                } else {
                    offset = insets.getSystemWindowInsetBottom() - insets.getStableInsetBottom();
                }

                if (offset != previousOffset || offset == 0) {
                    previousOffset = offset;

                    if (offset > Utils.dpToPx(popup.context, MIN_KEYBOARD_HEIGHT)) {
                        popup.updateKeyboardStateOpened(offset);
                    } else {
                        popup.updateKeyboardStateClosed();
                    }
                }

                return ViewCompat.onApplyWindowInsets(popup.context.getWindow().getDecorView(), insets);
            }

            return insets;
        }
    }

}
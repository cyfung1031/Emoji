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
import android.content.res.Resources;
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

    @Nullable View rootView = null;
    @Nullable Activity context = null;

    @NonNull RecentEmoji recentEmoji;
    @NonNull VariantEmoji variantEmoji;
    @NonNull EmojiVariantPopup variantPopup;

    EditText editText;

    boolean isPendingOpen;

    @Nullable OnEmojiPopupShownListener onEmojiPopupShownListener;

    @Nullable OnEmojiBackspaceClickListener onEmojiBackspaceClickListener;
    @Nullable OnEmojiClickListener onEmojiClickListener;
    int originalImeOptions = -1;

    final EmojiResultReceiver emojiResultReceiver = new EmojiResultReceiver(new Handler(Looper.getMainLooper()));

    Resources resources;


    final OnEmojiClickListener internalOnEmojiClickListener = new OnEmojiClickListener() {
        @Override public void onEmojiClick(@NonNull final EmojiImageView imageView, @NonNull final Emoji emoji) {
            Utils.input(editText, emoji);


            if (onEmojiClickListener != null) {
                onEmojiClickListener.onEmojiClick(imageView, emoji);
            }

            variantPopup.dismiss();
            EmojiViewX.this.post(new Runnable() {
                @Override
                public void run() {


                    recentEmoji.addEmoji(emoji);
                    variantEmoji.addVariant(emoji);
                    imageView.updateEmoji(emoji);

                    recentEmoji.persist();
                    variantEmoji.persist();
                }
            });
        }
    };

    final OnEmojiLongClickListener internalOnEmojiLongClickListener = new OnEmojiLongClickListener() {
        @Override public void onEmojiLongClick(@NonNull final EmojiImageView view, @NonNull final Emoji emoji) {
            variantPopup.show(view, emoji);
        }
    };

    final OnEmojiBackspaceClickListener internalOnEmojiBackspaceClickListener = new OnEmojiBackspaceClickListener() {
        @Override public void onEmojiBackspaceClicked(final View v) {
            backspace(editText);

            if (onEmojiBackspaceClickListener != null) {
                onEmojiBackspaceClickListener.onEmojiBackspaceClicked(v);
            }
        }
    };

    public void setup(@NonNull View view){
        Builder builder = Builder.fromRootView(view);

        this.context = (Activity) view.getContext();
        this.rootView = view.getRootView();
        this.recentEmoji = builder.getRecentEmoji();
        this.variantEmoji = builder.getVariantEmoji();


        variantPopup = new EmojiVariantPopup(rootView, internalOnEmojiClickListener);

        final EmojiView emojiView = new EmojiView(context,
                internalOnEmojiClickListener, internalOnEmojiLongClickListener, builder);

        emojiView.setOnEmojiBackspaceClickListener(internalOnEmojiBackspaceClickListener);

        EmojiViewX.this.addView(emojiView);



    }

    public void setEditText(@Nullable final EditText editText) {
        this.editText = editText;
    }

    public EmojiViewX(@NonNull Context context) {
        super(context);
        init(context);
    }

    public EmojiViewX(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public EmojiViewX(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(final Context context) {
        resources = context.getResources();
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

        @CheckResult public Builder setKeyboardAnimationStyle(@StyleRes final int animation) {
            keyboardAnimationStyle = animation;
            return this;
        }

        @CheckResult public Builder setPageTransformer(@Nullable final ViewPager.PageTransformer transformer) {
            pageTransformer = transformer;
            return this;
        }

    }


}
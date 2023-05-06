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

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.O;
import static androidx.core.view.ViewCompat.requestApplyInsets;
import static com.vanniktech.emoji.Utils.backspace;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
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
import androidx.viewpager2.widget.ViewPager2;

import com.vanniktech.emoji.emoji.Emoji;
import com.vanniktech.emoji.listeners.OnEmojiBackspaceClickListener;
import com.vanniktech.emoji.listeners.OnEmojiClickListener;
import com.vanniktech.emoji.listeners.OnEmojiPopupDismissListener;
import com.vanniktech.emoji.listeners.OnEmojiPopupShownListener;
import com.vanniktech.emoji.listeners.OnSoftKeyboardCloseListener;
import com.vanniktech.emoji.listeners.OnSoftKeyboardOpenListener;

import java.lang.ref.WeakReference;
import java.util.Objects;

public final class EmojiPopupGeneral implements IPopup {
    static final int MIN_KEYBOARD_HEIGHT = 50;
    static final int APPLY_WINDOW_INSETS_DURATION = 250;
    public WeakReference<EditText> editTextWR = null;
    public final EmojiViewInner.EmojiViewBuildController<?> emojiViewController;
//    public WeakReference<EmojiViewInner> mEmojiViewWR = null;
     WeakReference<View> rootViewWR = null;
     WeakReference<Activity>  contextWR = null;
    @NonNull
    final IRecentEmoji recentEmoji;
    @NonNull
    final IVariantEmoji variantEmoji;
    @NonNull
    final EmojiVariantPopupGeneral variantPopup;
    final PopupWindow popupWindow;
    final EmojiResultReceiver emojiResultReceiver = new EmojiResultReceiver(new Handler(Looper.getMainLooper())) {
        @Override
        public void onReceiveResult(int resultCode, Bundle data) {
            if(!this.enabled) return;

            if (resultCode == 0 || resultCode == 1) {

                this.enabled = false;
                EmojiPopupGeneral.this.showAtBottom();
            }
        }
    };
    boolean isPendingOpen;
    boolean isKeyboardOpen;
    @Nullable
    OnEmojiPopupShownListener onEmojiPopupShownListener;
    @Nullable
    OnSoftKeyboardCloseListener onSoftKeyboardCloseListener;
    @Nullable
    OnSoftKeyboardOpenListener onSoftKeyboardOpenListener;

    @Nullable
    OnEmojiBackspaceClickListener onEmojiBackspaceClickListener;
    @Nullable
    OnEmojiClickListener onEmojiClickListener;
    @Nullable
    OnEmojiPopupDismissListener onEmojiPopupDismissListener;

    int popupWindowHeight;
    int originalImeOptions = -1;
    private int globalKeyboardHeight;
    //
//  final OnEmojiClickListener internalOnEmojiClickListener = new OnEmojiClickListener() {
//    @Override public void onEmojiClick(@NonNull final EmojiImageViewE imageView, @NonNull final Emoji emoji) {
//      Utils.input(editText, emoji);
//
//      recentEmoji.addEmoji(emoji);
//      variantEmoji.addVariant(emoji);
//      imageView.updateEmoji(emoji);
//
//      if (onEmojiClickListener != null) {
//        onEmojiClickListener.onEmojiClick(imageView, emoji);
//      }
//
//      variantPopup.dismiss();
//    }
//  };
    private int delay;

    EmojiPopupGeneral(@NonNull final EmojiPopupGeneral.Builder builder, @NonNull final EditText editText) {
        Activity context = Utils.asActivity(builder.rootView.getContext());
        View rootView = builder.rootView.getRootView();
        this.contextWR = new WeakReference<>(context);
        this.rootViewWR = new WeakReference<>(rootView);
        this.editTextWR = new WeakReference<>(editText);
        this.recentEmoji = builder.recentEmoji;
        this.variantEmoji = builder.variantEmoji;

        popupWindow = new PopupWindow(context);
//    variantPopup = new EmojiVariantPopup(rootView, internalOnEmojiClickListener);

        final MyEmojiView myEmojiView = new MyEmojiView(context);
//        mEmojiView = myEmojiView;
        myEmojiView.popupBuilder = builder;
    /*

    final EmojiView emojiView = new EmojiView(context,
            internalOnEmojiClickListener, internalOnEmojiLongClickListener, builder);

     */

//    emojiView.setOnEmojiBackspaceClickListener(internalOnEmojiBackspaceClickListener);

//    popupWindow.setContentView(emojiView);
        popupWindow.setContentView(new FrameLayout(context));
        ((FrameLayout) popupWindow.getContentView()).addView(myEmojiView);
        myEmojiView.setup(rootView);
        variantPopup = myEmojiView.variantPopup;


        this.emojiViewController = myEmojiView.emojiViewController;

        emojiViewController.setPopupViewWindow(popupWindow);

        popupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NOT_NEEDED);
        popupWindow.setBackgroundDrawable(new BitmapDrawable(context.getResources(), (Bitmap) null)); // To avoid borders and overdraw.
        popupWindow.setOnDismissListener(this::onPopupDismiss);

        if (builder.keyboardAnimationStyle != 0) {
            popupWindow.setAnimationStyle(builder.keyboardAnimationStyle);
        }

        // Root view might already be laid out in which case we need to manually call start()
        if (rootView.getParent() != null) {
            start();
        }

        rootView.addOnAttachStateChangeListener(new EmojiPopUpOnAttachStateChangeListener(this));
    }

    public void onPopupDismiss() {
        EditText editText = editTextWR != null ? editTextWR.get() : null;

        if (editText instanceof EmojiEditText && ((EmojiEditText) editText).isKeyboardInputDisabled()) {
            editText.clearFocus();
        }
        if (onEmojiPopupDismissListener != null) {
            onEmojiPopupDismissListener.onEmojiPopupDismiss();
        }
    }

    void updateKeyboardStateOpened(final int keyboardHeight) {

        Activity context = contextWR != null ? contextWR.get() : null;
        if(context == null) return;

        if (popupWindowHeight > 0 && popupWindow.getHeight() != popupWindowHeight) {
            popupWindow.setHeight(popupWindowHeight);
        } else if (popupWindowHeight == 0 && popupWindow.getHeight() != keyboardHeight) {
            popupWindow.setHeight(keyboardHeight);
        }

        if (globalKeyboardHeight != keyboardHeight) {
            globalKeyboardHeight = keyboardHeight;
            delay = APPLY_WINDOW_INSETS_DURATION;
        } else {
            delay = 0;
        }

        final int properWidth = Utils.getProperWidth(context);

        if (popupWindow.getWidth() != properWidth) {
            popupWindow.setWidth(properWidth);
        }

        if (!isKeyboardOpen) {
            isKeyboardOpen = true;
            if (onSoftKeyboardOpenListener != null) {
                onSoftKeyboardOpenListener.onKeyboardOpen(keyboardHeight);
            }
        }

        if (isPendingOpen) {
            showAtBottom();
        }
    }

    void updateKeyboardStateClosed() {
        isKeyboardOpen = false;

        if (onSoftKeyboardCloseListener != null) {
            onSoftKeyboardCloseListener.onKeyboardClose();
        }

        if (isShowing()) {
            dismiss();
        }
    }
//
//    /**
//     * Set PopUpWindow's height.
//     * If height is greater than 0 then this value will be used later on. If it is 0 then the
//     * keyboard height will be dynamically calculated and set as {@link PopupWindow} height.
//     *
//     * @param popupWindowHeight - the height of {@link PopupWindow}
//     */
//    public void setPopupWindowHeight(final int popupWindowHeight) {
//        this.popupWindowHeight = popupWindowHeight >= 0 ? popupWindowHeight : 0;
//    }

    public void toggle() {

        Activity context = contextWR != null ? contextWR.get() : null;
        if(context == null) return;
//        Log.i("WSS", "WW");
        if (!popupWindow.isShowing()) {
            // this is needed because something might have cleared the insets listener
            start();
            requestApplyInsets(context.getWindow().getDecorView());
            show();
        } else {
            dismiss();
        }
    }

    public void show() {

        Activity context = contextWR != null ? contextWR.get() : null;
        if(context == null) return;

        EditText editText = editTextWR != null ? editTextWR.get() : null;
        if(editText == null) return;

        if (Utils.shouldOverrideRegularCondition(context, editText) && originalImeOptions == -1) {
            originalImeOptions = editText.getImeOptions();
        }

        editText.setFocusableInTouchMode(true);
        editText.requestFocus();

        showAtBottomPending();
    }

    private void showAtBottomPending() {

        Activity context = contextWR != null ? contextWR.get() : null;
        if(context == null) return;

        EditText editText = editTextWR != null ? editTextWR.get() : null;
        isPendingOpen = true;
        final InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);

        if (editText != null &&  Utils.shouldOverrideRegularCondition(context, editText)) {
            editText.setImeOptions(editText.getImeOptions() | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
            if (inputMethodManager != null) {
                inputMethodManager.restartInput(editText);
            }
        }

        if (inputMethodManager != null) {
            emojiResultReceiver.enabled = true;
            inputMethodManager.showSoftInput(editText, InputMethodManager.RESULT_UNCHANGED_SHOWN, emojiResultReceiver);
        }
    }

    public boolean isShowing() {
        return popupWindow.isShowing();
    }

    public void dismiss() {
        EditText editText = editTextWR != null ? editTextWR.get() : null;

        popupWindow.dismiss();
        variantPopup.dismiss();
        recentEmoji.persist();
        variantEmoji.persist();

        emojiResultReceiver.enabled = false;


        Activity context = contextWR != null ? contextWR.get() : null;
        if(context == null) return;

        if (originalImeOptions != -1) {
            if (editText != null) {
                editText.setImeOptions(originalImeOptions);

                final InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);

                if (inputMethodManager != null) {
                    inputMethodManager.restartInput(editText);
                }
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
        if (isPendingOpen) {
            isPendingOpen = false;


            Activity context = contextWR != null ? contextWR.get() : null;
            if(context == null) return;

            View rootView = rootViewWR != null ? rootViewWR.get() : null;
            if(rootView == null) return;

            EditText editText = editTextWR != null ? editTextWR.get() : null;
            if(editText == null) return;
            editText.post(new Runnable() {
                @Override
                public void run() {
                    popupWindow.showAtLocation(rootView, Gravity.NO_GRAVITY, 0,
                            Utils.getProperHeight(context) + popupWindowHeight);
                }
            });

            if (onEmojiPopupShownListener != null) {
                onEmojiPopupShownListener.onEmojiPopupShown();
            }
        }
    }

    public void start() {

        Activity context = contextWR != null ? contextWR.get() : null;
        if(context == null) return;
        ViewCompat.setOnApplyWindowInsetsListener(context.getWindow().getDecorView(), new EmojiPopUpOnApplyWindowInsetsListener(this));
    }

    void stop() {
        dismiss();

        Activity context = contextWR != null ? contextWR.get() : null;
        if(context == null) return;
        ViewCompat.setOnApplyWindowInsetsListener(context.getWindow().getDecorView(), null);
        popupWindow.setOnDismissListener(null);
    }

    public static final class Builder implements IEmojiViewBuilder<Builder> {
        @NonNull
        final View rootView;
        @StyleRes
        int keyboardAnimationStyle;
        @ColorInt
        int backgroundColor;
        @ColorInt
        int iconColor;
        @ColorInt
        int selectedIconColor;
        @ColorInt
        int dividerColor;
        @Nullable
        ViewPager2.PageTransformer pageTransformer;
        @Nullable
        OnEmojiPopupShownListener onEmojiPopupShownListener;
        @Nullable
        OnSoftKeyboardCloseListener onSoftKeyboardCloseListener;
        @Nullable
        OnSoftKeyboardOpenListener onSoftKeyboardOpenListener;
        @Nullable
        OnEmojiBackspaceClickListener onEmojiBackspaceClickListener;
        @Nullable
        OnEmojiClickListener onEmojiClickListener;
        @Nullable
        OnEmojiPopupDismissListener onEmojiPopupDismissListener;
        @NonNull
        IRecentEmoji recentEmoji;
        @NonNull
        IVariantEmoji variantEmoji;
        int popupWindowHeight;

        private Builder(final View rootView) {
            this.rootView = Objects.requireNonNull(rootView, "The root View can't be null");
            initByContext(rootView.getContext());
        }

        /**
         * @param rootView The root View of your layout.xml which will be used for calculating the height
         *                 of the keyboard.
         * @return builder For building the {@link EmojiPopupGeneral}.
         */
        @CheckResult
        public static Builder fromRootView(final View rootView) {
            return new Builder(rootView);
        }

        private void initByContext(final Context context) {
            this.recentEmoji = new RecentEmojiManager2(context);
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

        @CheckResult
        public Builder setKeyboardAnimationStyle(@StyleRes final int animation) {
            keyboardAnimationStyle = animation;
            return this;
        }

        @ColorInt
        public int getBackgroundColor() {
            return backgroundColor;
        }

        @CheckResult
        public Builder setBackgroundColor(@ColorInt final int color) {
            backgroundColor = color;
            return this;
        }

        @ColorInt
        public int getIconColor() {
            return iconColor;
        }

        @CheckResult
        public Builder setIconColor(@ColorInt final int color) {
            iconColor = color;
            return this;
        }

        @ColorInt
        public int getSelectedIconColor() {
            return selectedIconColor;
        }

        @CheckResult
        public Builder setSelectedIconColor(@ColorInt final int color) {
            selectedIconColor = color;
            return this;
        }

        @ColorInt
        public int getDividerColor() {
            return dividerColor;
        }

        @CheckResult
        public Builder setDividerColor(@ColorInt final int color) {
            dividerColor = color;
            return this;
        }

        @Nullable
        public ViewPager2.PageTransformer getPageTransformer() {
            return pageTransformer;
        }

        @CheckResult
        public Builder setPageTransformer(@Nullable final ViewPager2.PageTransformer transformer) {
            pageTransformer = transformer;
            return this;
        }

        @Nullable
        public OnEmojiPopupShownListener getOnEmojiPopupShownListener() {
            return onEmojiPopupShownListener;
        }

        @CheckResult
        public Builder setOnEmojiPopupShownListener(@Nullable final OnEmojiPopupShownListener listener) {
            onEmojiPopupShownListener = listener;
            return this;
        }

        @Nullable
        public OnSoftKeyboardCloseListener getOnSoftKeyboardCloseListener() {
            return onSoftKeyboardCloseListener;
        }

        @CheckResult
        public Builder setOnSoftKeyboardCloseListener(@Nullable final OnSoftKeyboardCloseListener listener) {
            onSoftKeyboardCloseListener = listener;
            return this;
        }

        @Nullable
        public OnSoftKeyboardOpenListener getOnSoftKeyboardOpenListener() {
            return onSoftKeyboardOpenListener;
        }

        @CheckResult
        public Builder setOnSoftKeyboardOpenListener(@Nullable final OnSoftKeyboardOpenListener listener) {
            onSoftKeyboardOpenListener = listener;
            return this;
        }

        @Nullable
        public OnEmojiBackspaceClickListener getOnEmojiBackspaceClickListener() {
            return onEmojiBackspaceClickListener;
        }

        @CheckResult
        public Builder setOnEmojiBackspaceClickListener(@Nullable final OnEmojiBackspaceClickListener listener) {
            onEmojiBackspaceClickListener = listener;
            return this;
        }

        @Nullable
        public OnEmojiClickListener getOnEmojiClickListener() {
            return onEmojiClickListener;
        }

        @CheckResult
        public Builder setOnEmojiClickListener(@Nullable final OnEmojiClickListener listener) {
            onEmojiClickListener = listener;
            return this;
        }

        @Nullable
        public OnEmojiPopupDismissListener getOnEmojiPopupDismissListener() {
            return onEmojiPopupDismissListener;
        }

        @CheckResult
        public Builder setOnEmojiPopupDismissListener(@Nullable final OnEmojiPopupDismissListener listener) {
            onEmojiPopupDismissListener = listener;
            return this;
        }

        @NonNull
        public IRecentEmoji getRecentEmoji() {
            return recentEmoji;
        }

        /**
         * Allows you to pass your own implementation of recent emojis. If not provided the default one
         * {@link RecentEmojiManager2} will be used.
         *
         * @since 0.2.0
         */
        @CheckResult
        public Builder setRecentEmoji(@NonNull final IRecentEmoji recent) {
            recentEmoji = Objects.requireNonNull(recent, "recent can't be null");
            return this;
        }

        @NonNull
        public IVariantEmoji getVariantEmoji() {
            return variantEmoji;
        }

        /**
         * Allows you to pass your own implementation of variant emojis. If not provided the default one
         * {@link VariantEmojiManager} will be used.
         *
         * @since 0.5.0
         */
        @CheckResult
        public Builder setVariantEmoji(@NonNull final IVariantEmoji variant) {
            variantEmoji = Objects.requireNonNull(variant, "variant can't be null");
            return this;
        }

//        /**
//         * Set PopUpWindow's height.
//         * If height is not 0 then this value will be used later on. If it is 0 then the keyboard height will
//         * be dynamically calculated and set as {@link PopupWindow} height.
//         *
//         * @param windowHeight - the height of {@link PopupWindow}
//         * @since 0.7.0
//         */
//        @CheckResult
//        public Builder setPopupWindowHeight(final int windowHeight) {
//            this.popupWindowHeight = Math.max(windowHeight, 0);
//            return this;
//        }

        @CheckResult
        public EmojiPopupGeneral build(@NonNull final EditText editText) {
            EmojiManager.getInstance().verifyInstalled();
            Objects.requireNonNull(editText, "EditText can't be null");

            final EmojiPopupGeneral emojiPopup = new EmojiPopupGeneral(this, editText);
      /*
      emojiPopup.onSoftKeyboardCloseListener = onSoftKeyboardCloseListener;
      emojiPopup.onEmojiClickListener = onEmojiClickListener;
      emojiPopup.onSoftKeyboardOpenListener = onSoftKeyboardOpenListener;
      emojiPopup.onEmojiPopupShownListener = onEmojiPopupShownListener;
      emojiPopup.onEmojiPopupDismissListener = onEmojiPopupDismissListener;
      emojiPopup.onEmojiBackspaceClickListener = onEmojiBackspaceClickListener;
      emojiPopup.popupWindowHeight = Math.max(popupWindowHeight, 0);

       */
            return emojiPopup;
        }
    }

    static final class EmojiPopUpOnAttachStateChangeListener implements View.OnAttachStateChangeListener {
        private final WeakReference<EmojiPopupGeneral> emojiPopup;

        EmojiPopUpOnAttachStateChangeListener(final EmojiPopupGeneral emojiPopup) {
            this.emojiPopup = new WeakReference<>(emojiPopup);
        }

        @Override
        public void onViewAttachedToWindow(final View v) {
            final EmojiPopupGeneral popup = emojiPopup.get();

            if (popup != null) {
                popup.start();
            }
        }

        @Override
        public void onViewDetachedFromWindow(final View v) {
            final EmojiPopupGeneral popup = emojiPopup.get();

            if (popup != null) {
                popup.stop();
            }

            emojiPopup.clear();
            v.removeOnAttachStateChangeListener(this);
        }
    }

    static final class EmojiPopUpOnApplyWindowInsetsListener implements OnApplyWindowInsetsListener {
        private final WeakReference<EmojiPopupGeneral> emojiPopup;
        int previousOffset;

        EmojiPopUpOnApplyWindowInsetsListener(final EmojiPopupGeneral emojiPopup) {
            this.emojiPopup = new WeakReference<>(emojiPopup);
        }

        @Override
        public WindowInsetsCompat onApplyWindowInsets(final View v, final WindowInsetsCompat insets) {
            final EmojiPopupGeneral popup = emojiPopup.get();

            if (popup != null) {
                final int offset;

                if (insets.getSystemWindowInsetBottom() < insets.getStableInsetBottom()) {
                    offset = insets.getSystemWindowInsetBottom();
                } else {
                    offset = insets.getSystemWindowInsetBottom() - insets.getStableInsetBottom();
                }

                Activity context = popup.contextWR != null ? popup.contextWR.get() : null;

                if (offset != previousOffset || offset == 0) {
                    previousOffset = offset;

                    if (offset > Utils.dpToPx(context, MIN_KEYBOARD_HEIGHT)) {
                        popup.updateKeyboardStateOpened(offset);
                    } else {
                        popup.updateKeyboardStateClosed();
                    }
                }

                return ViewCompat.onApplyWindowInsets(context.getWindow().getDecorView(), insets);
            }

            return insets;
        }
    }

    class MyEmojiView extends EmojiViewExtended {

        public EmojiPopupGeneral.Builder popupBuilder = null;

        public MyEmojiView(Context context) {
            super(context);
        }

        public MyEmojiView(Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
        }

        public MyEmojiView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        @Override
        public void preSetup(EmojiViewBuildController<?> builder) {
            if (builder != null) {
                builder.setPageTransformer(popupBuilder.getPageTransformer());
            }

        }

        @Override
        public void onEmojiBackspaceClicked(View v) {


            EditText editText = editTextWR != null ? editTextWR.get() : null;
            if(editText != null) {
                backspace(editText);
            }

            if (onEmojiBackspaceClickListener != null) {
                onEmojiBackspaceClickListener.onEmojiBackspaceClicked(v);
            }

        }


        @Override
        public void onEmojiClick(@NonNull EmojiImageViewG imageView, @NonNull Emoji emoji) {

//      Log.i("onEmojiClick", editText.getText()+"");

            EditText editText = editTextWR != null ? editTextWR.get() : null;
            if(editText != null) {
                Utils.input(editText, emoji);
            }

            recentEmoji.addEmoji(emoji);
            variantEmoji.addVariant(emoji);
            imageView.updateEmoji(emoji);

            if (onEmojiClickListener != null) {
                onEmojiClickListener.onEmojiClick(imageView, emoji);
            }

            variantPopup.dismiss();

        }

        @Override
        public void onEmojiLongClick(@NonNull EmojiImageViewG view, @NonNull Emoji emoji) {


            variantPopup.show(view, emoji);
        }


    }


    public static class EmojiResultReceiver extends ResultReceiver {

        public boolean enabled = false;

        /**
         * Create a new EmojiResultReceiver to receive results.  Your
         * {@link #onReceiveResult} method will be called from the thread running
         * <var>handler</var> if given, or from an arbitrary thread if null.
         */
        EmojiResultReceiver(final Handler handler) {
            super(handler);
            enabled = false;
        }

    }



}
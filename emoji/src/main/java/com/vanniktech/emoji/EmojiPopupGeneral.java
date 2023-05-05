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
import static com.vanniktech.emoji.Utils.checkNotNull;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ResultReceiver;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
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

public final class EmojiPopupGeneral implements  IPopup {
    static final int MIN_KEYBOARD_HEIGHT = 50;
    static final int APPLY_WINDOW_INSETS_DURATION = 250;
    public final EditText editText;
    public final EmojiViewInner.EmojiViewBuildController<?> emojiViewController;
    public final EmojiViewInner mEmojiView;
    final View rootView;
    final Activity context;
    @NonNull
    final RecentEmoji recentEmoji;
    @NonNull
    final VariantEmoji variantEmoji;
    @NonNull
    final EmojiVariantPopupGeneral variantPopup;
    final PopupWindow popupWindow;
    final ResultReceiver emojiResultReceiver = new ResultReceiver(new Handler(Looper.getMainLooper())){
      @Override
      protected void onReceiveResult(int resultCode, Bundle resultData) {
        onEmojiResultReceived(resultCode,resultData);
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
    private int delay;


    EmojiPopupGeneral(@NonNull final EmojiPopupGeneral.Builder builder, @NonNull final EditText editText) {
        this.context = Utils.asActivity(builder.rootView.getContext());
        this.rootView = builder.rootView.getRootView();
        this.editText = editText;
        this.recentEmoji = builder.recentEmoji;
        this.variantEmoji = builder.variantEmoji;

        popupWindow = new PopupWindow(context);
//    variantPopup = new EmojiVariantPopup(rootView, internalOnEmojiClickListener);

        final MyEmojiView myEmojiView = new MyEmojiView(context);
        mEmojiView = myEmojiView;
        myEmojiView.popupBuilder = builder;
    /*

    final EmojiView emojiView = new EmojiView(context,
            internalOnEmojiClickListener, internalOnEmojiLongClickListener, builder);

     */

//    emojiView.setOnEmojiBackspaceClickListener(internalOnEmojiBackspaceClickListener);

//    popupWindow.setContentView(emojiView);
        popupWindow.setContentView(new FrameLayout(context));
        ((FrameLayout) popupWindow.getContentView()).addView(myEmojiView);
        myEmojiView.setup(this.rootView);
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
        if (editText instanceof EmojiEditText && ((EmojiEditText) editText).isKeyboardInputDisabled()) {
            editText.clearFocus();
        }
        if (onEmojiPopupDismissListener != null) {
            onEmojiPopupDismissListener.onEmojiPopupDismiss();
        }

    }

    void updateKeyboardStateOpened(final int keyboardHeight) {
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

    /**
     * Set PopUpWindow's height.
     * If height is greater than 0 then this value will be used later on. If it is 0 then the
     * keyboard height will be dynamically calculated and set as {@link PopupWindow} height.
     *
     * @param popupWindowHeight - the height of {@link PopupWindow}
     */
    public void setPopupWindowHeight(final int popupWindowHeight) {
        this.popupWindowHeight = popupWindowHeight >= 0 ? popupWindowHeight : 0;
    }

    public void toggle() {
        Log.i("WSS", "WW");
        if (!popupWindow.isShowing()) {
            // this is needed because something might have cleared the insets listener
            start();
            requestApplyInsets(context.getWindow().getDecorView());
            show();
        } else {
            dismiss();
        }
    }
    private void animateDecorView(View v, final int translationY, final int newHeight) {
        final View decorView = v;

        // Animate translationY
        ObjectAnimator translationYAnimator = ObjectAnimator.ofFloat(decorView, "translationY", translationY);
        translationYAnimator.setDuration(200);

        // Animate height
        ValueAnimator heightAnimator = ValueAnimator.ofInt(decorView.getHeight(), newHeight);
        heightAnimator.setDuration(200);
        heightAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int currentHeight = (int) animation.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = decorView.getLayoutParams();
                layoutParams.height = currentHeight;
                decorView.setLayoutParams(layoutParams);
            }
        });

        // Start both animations together
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(translationYAnimator, heightAnimator);
        animatorSet.start();
    }

    public void printChild(View v0){

        if(!(v0 instanceof ViewGroup) ){
            return;
        }
        ViewGroup rootChildView = (ViewGroup) v0;

        for (int i = 0; i < rootChildView.getChildCount(); i++) {
            View v = rootChildView.getChildAt(i);
            System.out.println("rootGrandChildView" + i + ": " + v.getClass().getName() + ", id : " + v.getId()+ " | "+ v.getHeight());
            printChild(v);
        }
    }

    public void print(View decorView){
//
//        View contentView = decorView.findViewById(android.R.id.content);
//        System.out.println("contentView : " + contentView.getClass().getName() + ", id : " + contentView.getId() + " | "+ contentView.getHeight());
//
//        ViewGroup rootView = (ViewGroup)contentView.getRootView();
//        System.out.println("rootView : " + rootView.getClass().getName() + ", id : " + rootView.getId() + " | "+ rootView.getHeight());
//
//        ViewGroup rootChildView = (ViewGroup)rootView.getChildAt(0);
//        System.out.println("rootChildView : " + rootChildView.getClass().getName() + ", id : " + rootChildView.getId()+ " | "+ rootChildView.getHeight());

        System.out.println("decorView : " + decorView.getClass().getName() + ", id : " + decorView.getId() + " | "+ decorView.getHeight());

        View contentView = decorView.findViewById(android.R.id.content);
        ViewGroup rootView = (ViewGroup)contentView.getRootView();
        System.out.println("rootView : " + rootView.getClass().getName() + ", id : " + rootView.getId() + " | "+ rootView.getHeight());

//        View contentView = decorView.findViewById(android.R.id.content);
        System.out.println("contentView : " + contentView.getClass().getName() + ", id : " + contentView.getId() + " | "+ contentView.getHeight());

        View frameView = getFrameView(decorView);
        System.out.println("frameView : " + frameView.getClass().getName() + ", id : " + frameView.getId() + " | "+ frameView.getHeight());


        printChild(decorView);

    }

    public View getFrameView(View v){
        if(v == null) return null;
        if(!(v instanceof ViewGroup)) return null;
        int viewId = Integer.parseInt((v.getId()+""));
        if(viewId>0) return null;

        ViewGroup vg = (ViewGroup) v;
        if(v.getClass().getName().contains(".FrameLayout"))return v;

        for (int i = 0; i < vg.getChildCount(); i++) {
            View vt = vg.getChildAt(i);
            View f = getFrameView(vt);
            if(f != null) return f;
        }
        return null;


    }

    public View getContentView(View v){

        return v.findViewById(android.R.id.content);

    }

    ViewTreeObserver.OnGlobalLayoutListener listener = new ViewTreeObserver.OnGlobalLayoutListener() {
        // Keep a reference to the last state of the keyboard
//        if(context==null) return;
//        private boolean lastState = context.isKeyboardOpen();

        /**
         * Something in the layout has changed
         * so check if the keyboard is open or closed
         * and if the keyboard state has changed
         * save the new state and invoke the callback
         */
        @Override
        public void onGlobalLayout() {
            showAtBottom();
        }
    };

    public void show() {
        if (Utils.shouldOverrideRegularCondition(context, editText) && originalImeOptions == -1) {
            originalImeOptions = editText.getImeOptions();
        }

        editText.setFocusableInTouchMode(true);
        editText.requestFocus();

        View view = context.getWindow().getDecorView();
        view.getViewTreeObserver().addOnGlobalLayoutListener(listener);

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
//            emojiResultReceiver.setReceiver(this::onEmojiResultReceived);
            inputMethodManager.showSoftInput(editText, InputMethodManager.RESULT_UNCHANGED_SHOWN, emojiResultReceiver);
        }
    }

    public boolean isShowing() {
        return popupWindow.isShowing();
    }

    public void dismiss() {
        popupWindow.dismiss();
        variantPopup.dismiss();
        recentEmoji.persist();
        variantEmoji.persist();

//        emojiResultReceiver.setReceiver(null);

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


        if(isPendingOpen) {
            View view = context.getWindow().getDecorView();
            view.getViewTreeObserver().removeOnGlobalLayoutListener(listener);


            isPendingOpen = false;
//        editText.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                popupWindow.showAtLocation(rootView, Gravity.NO_GRAVITY, 0,
//                        Utils.getProperHeight(context) + popupWindowHeight);
//            }
//        }, delay);

//        if (onEmojiPopupShownListener != null) {
//            onEmojiPopupShownListener.onEmojiPopupShown();
//        }


            popupWindow.showAtLocation(rootView, Gravity.NO_GRAVITY, 0,
                    Utils.getProperHeight(context) + popupWindowHeight);


            if (onEmojiPopupShownListener != null) {
                onEmojiPopupShownListener.onEmojiPopupShown();
            }
        }

    }

    public void onEmojiResultReceived(final int resultCode, final Bundle data) {
        if (resultCode == 0 || resultCode == 1) {
            showAtBottom();
        }
    }

    public void start() {
        ViewCompat.setOnApplyWindowInsetsListener(context.getWindow().getDecorView(), new EmojiPopUpOnApplyWindowInsetsListener(this));
    }

    void stop() {
        dismiss();
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
        RecentEmoji recentEmoji;
        @NonNull
        VariantEmoji variantEmoji;
        int popupWindowHeight;

        private Builder(final View rootView) {
            this.rootView = checkNotNull(rootView, "The root View can't be null");
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
        public RecentEmoji getRecentEmoji() {
            return recentEmoji;
        }

        /**
         * Allows you to pass your own implementation of recent emojis. If not provided the default one
         * {@link RecentEmojiManager2} will be used.
         *
         * @since 0.2.0
         */
        @CheckResult
        public Builder setRecentEmoji(@NonNull final RecentEmoji recent) {
            recentEmoji = checkNotNull(recent, "recent can't be null");
            return this;
        }

        @NonNull
        public VariantEmoji getVariantEmoji() {
            return variantEmoji;
        }

        /**
         * Allows you to pass your own implementation of variant emojis. If not provided the default one
         * {@link VariantEmojiManager} will be used.
         *
         * @since 0.5.0
         */
        @CheckResult
        public Builder setVariantEmoji(@NonNull final VariantEmoji variant) {
            variantEmoji = checkNotNull(variant, "variant can't be null");
            return this;
        }

        /**
         * Set PopUpWindow's height.
         * If height is not 0 then this value will be used later on. If it is 0 then the keyboard height will
         * be dynamically calculated and set as {@link PopupWindow} height.
         *
         * @param windowHeight - the height of {@link PopupWindow}
         * @since 0.7.0
         */
        @CheckResult
        public Builder setPopupWindowHeight(final int windowHeight) {
            this.popupWindowHeight = Math.max(windowHeight, 0);
            return this;
        }

        @CheckResult
        public EmojiPopupGeneral build(@NonNull final EditText editText) {
            EmojiManager.getInstance().verifyInstalled();
            checkNotNull(editText, "EditText can't be null");

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


            backspace(editText);

            if (onEmojiBackspaceClickListener != null) {
                onEmojiBackspaceClickListener.onEmojiBackspaceClicked(v);
            }

        }


        @Override
        public void onEmojiClick(@NonNull EmojiImageViewGeneral imageView, @NonNull Emoji emoji) {

            Log.i("onEmojiClick", editText.getText() + "");
            Utils.input(editText, emoji);

            recentEmoji.addEmoji(emoji);
            variantEmoji.addVariant(emoji);
            imageView.updateEmoji(emoji);

            if (onEmojiClickListener != null) {
                onEmojiClickListener.onEmojiClick(imageView, emoji);
            }

            variantPopup.dismiss();

        }

        @Override
        public void onEmojiLongClick(@NonNull EmojiImageViewGeneral view, @NonNull Emoji emoji) {


            variantPopup.show(view, emoji);
        }


    }


}
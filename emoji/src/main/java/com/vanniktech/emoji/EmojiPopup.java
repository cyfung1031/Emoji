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
import android.view.Gravity;
import android.view.View;
import android.view.autofill.AutofillManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.vanniktech.emoji.emoji.Emoji;
import com.vanniktech.emoji.listeners.OnEmojiBackspaceClickListener;
import com.vanniktech.emoji.listeners.OnEmojiPopupDismissListener;
import com.vanniktech.emoji.listeners.OnEmojiPopupShownListener;
import com.vanniktech.emoji.listeners.OnSoftKeyboardCloseListener;
import com.vanniktech.emoji.listeners.OnSoftKeyboardOpenListener;

import java.lang.ref.WeakReference;
import java.util.Objects;

public final class EmojiPopup implements IPopup {
    static final int MIN_KEYBOARD_HEIGHT = 50;
    static final int APPLY_WINDOW_INSETS_DURATION = 250;
    @NonNull
    final MyEmojiViewBoard emojiViewBoard;
    @NonNull
    final IRecentEmoji recentEmoji;
    @NonNull
    final IVariantEmoji variantEmoji;
    final PopupWindow popupWindow; // PopupWindow is holding mContext
    public WeakReference<EditText> editTextWR = null;
    //    public WeakReference<EmojiViewInner> mEmojiViewWR = null;
    WeakReference<View> rootViewWR = null;
    WeakReference<Activity> contextWR = null;
    @Nullable
    EmojiVariantPopupGeneral variantPopup;
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
    OnEmojiPopupDismissListener onEmojiPopupDismissListener;
    int popupWindowHeight;
    final EmojiResultReceiver emojiResultReceiver = createReceiver(this);
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
    EmojiPopup(@NonNull final View v, @NonNull final EditText editText) {


        Activity context = Utils.asActivity(v.getContext());
        View rootView = v.getRootView();
        this.contextWR = new WeakReference<>(context);
        this.rootViewWR = new WeakReference<>(rootView);
        this.editTextWR = new WeakReference<>(editText);
        this.recentEmoji = new RecentEmojiManagerV8(context);
        this.variantEmoji = new VariantEmojiManager(context);

        popupWindow = new PopupWindow(context);
//    variantPopup = new EmojiVariantPopup(rootView, internalOnEmojiClickListener);

        emojiViewBoard = new MyEmojiViewBoard(context);

        final LinearLayout emojiFrameView = emojiViewBoard.createFrameView();
    /*

    final EmojiView emojiView = new EmojiView(context,
            internalOnEmojiClickListener, internalOnEmojiLongClickListener, builder);

     */

//    emojiView.setOnEmojiBackspaceClickListener(internalOnEmojiBackspaceClickListener);

//    popupWindow.setContentView(emojiView);
        popupWindow.setContentView(new FrameLayout(context));
        ((FrameLayout) popupWindow.getContentView()).addView(emojiFrameView);

//        mEmojiView = myEmojiView;
//        if(r!=null) r.run(this);
//        setup();
    }

    private static EmojiResultReceiver createReceiver(EmojiPopup emojiPopup) {

        final WeakReference<EmojiPopup> emojiPopupGeneralWR = new WeakReference<>(emojiPopup);
        return new EmojiResultReceiver(new Handler(Looper.getMainLooper())) {
            @Override
            public void onReceiveResult(int resultCode, Bundle data) {
                if (!this.enabled) return;

                if (resultCode == 0 || resultCode == 1) {
                    this.enabled = false;
                    final EmojiPopup emojiPopup = emojiPopupGeneralWR.get();
                    if (emojiPopup != null) {
                        emojiPopup.showAtBottom();
                    }
                }
            }
        };

    }

    public static EmojiPopup create(View rootView, EditText editText) {

        EmojiManager.getInstance().verifyInstalled();
        Objects.requireNonNull(editText, "EditText can't be null");


        final EmojiPopup emojiPopup = new EmojiPopup(rootView, editText);
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

    @NonNull
    public EmojiViewBoard getEmojiViewBoard() {
        return emojiViewBoard;
    }

    public void setPageTransformer(@Nullable final ViewPager2.PageTransformer transformer) {
        getEmojiViewBoard().setPageTransformer(transformer);

    }

    public void setup() {
        final Context context = contextWR.get();
        final View rootView = rootViewWR.get();
        assert context != null && rootView != null;

        emojiViewBoard.setup(rootView);
        variantPopup = emojiViewBoard.variantPopup;


        emojiViewBoard.setPopupViewWindow(popupWindow);

        popupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NOT_NEEDED);
        popupWindow.setBackgroundDrawable(new BitmapDrawable(context.getResources(), (Bitmap) null)); // To avoid borders and overdraw.
        popupWindow.setOnDismissListener(this::onPopupDismiss);


        // Root view might already be laid out in which case we need to manually call start()
        if (rootView.getParent() != null) {
            start();
        }

        rootView.addOnAttachStateChangeListener(new EmojiPopUpOnAttachStateChangeListener(this));

    }

    public void setAnimationStyle(@StyleRes int keyboardAnimationStyle) {
        if (popupWindow != null) {
            popupWindow.setAnimationStyle(keyboardAnimationStyle);
        }
    }

    public void onPopupDismiss() {
        EditText editText = editTextWR != null ? editTextWR.get() : null;

        if (editText instanceof IEmojiForceable && ((IEmojiForceable) editText).isKeyboardInputDisabled()) {
            editText.clearFocus();
        }
        if (onEmojiPopupDismissListener != null) {
            onEmojiPopupDismissListener.onEmojiPopupDismiss();
        }
    }

    void updateKeyboardStateOpened(final int keyboardHeight) {

        Activity context = contextWR != null ? contextWR.get() : null;
        if (context == null) return;

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

    void updateKeyboardStateClosed() {
        isKeyboardOpen = false;

        if (onSoftKeyboardCloseListener != null) {
            onSoftKeyboardCloseListener.onKeyboardClose();
        }

        if (isShowing()) {
            dismiss();
        }
    }

    public void toggle() {

        Activity context = contextWR != null ? contextWR.get() : null;
        if (context == null) return;
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
        if (context == null) return;

        EditText editText = editTextWR != null ? editTextWR.get() : null;
        if (editText == null) return;

        if (Utils.shouldOverrideRegularCondition(context, editText) && originalImeOptions == -1) {
            originalImeOptions = editText.getImeOptions();
        }

        editText.setFocusableInTouchMode(true);
        editText.requestFocus();

        showAtBottomPending();
    }

    private void showAtBottomPending() {

        Activity context = contextWR != null ? contextWR.get() : null;
        if (context == null) return;

        EditText editText = editTextWR != null ? editTextWR.get() : null;
        isPendingOpen = true;
        final InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);

        if (editText != null && Utils.shouldOverrideRegularCondition(context, editText)) {
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
        if (variantPopup != null) {
            variantPopup.dismiss();
        }
        recentEmoji.persist();
        variantEmoji.persist();

        emojiResultReceiver.enabled = false;


        Activity context = contextWR != null ? contextWR.get() : null;
        if (context == null) return;

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
            if (context == null) return;

            View rootView = rootViewWR != null ? rootViewWR.get() : null;
            if (rootView == null) return;

            EditText editText = editTextWR != null ? editTextWR.get() : null;
            if (editText == null) return;
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

    @PrivateApi
    public void start() {

        Activity context = contextWR != null ? contextWR.get() : null;
        if (context == null) return;
        ViewCompat.setOnApplyWindowInsetsListener(context.getWindow().getDecorView(), new EmojiPopUpOnApplyWindowInsetsListener(this));
    }

    void stop() {
        dismiss();

        Activity context = contextWR != null ? contextWR.get() : null;
        if (context == null) return;
        ViewCompat.setOnApplyWindowInsetsListener(context.getWindow().getDecorView(), null);
        popupWindow.setOnDismissListener(null);
    }

    static final class EmojiPopUpOnAttachStateChangeListener implements View.OnAttachStateChangeListener {
        private final WeakReference<EmojiPopup> emojiPopup;

        EmojiPopUpOnAttachStateChangeListener(final EmojiPopup emojiPopup) {
            this.emojiPopup = new WeakReference<>(emojiPopup);
        }

        @Override
        public void onViewAttachedToWindow(final View v) {
            final EmojiPopup popup = emojiPopup.get();

            if (popup != null) {
                popup.start();
            }
        }

        @Override
        public void onViewDetachedFromWindow(final View v) {
            final EmojiPopup popup = emojiPopup.get();

            if (popup != null) {
                popup.stop();
            }

            emojiPopup.clear();
            v.removeOnAttachStateChangeListener(this);
        }
    }

    static final class EmojiPopUpOnApplyWindowInsetsListener implements OnApplyWindowInsetsListener {
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

    class MyEmojiViewBoard extends EmojiViewBoard {


        public MyEmojiViewBoard(Context context) {
            super(context);
        }

        @Override
        public void preSetup() {

        }

        @Override
        public void onEmojiBackspaceClicked(View v) {


            EditText editText = editTextWR != null ? editTextWR.get() : null;
            if (editText != null) {
                backspace(editText);
            }

            if (onEmojiBackspaceClickListener != null) {
                onEmojiBackspaceClickListener.onEmojiBackspaceClicked(v);
            }

        }

        @Override
        public int commitOneEmoji(Emoji emoji) {
            EditText editText = editTextWR != null ? editTextWR.get() : null;
            if (editText != null) {
                Utils.input(editText, emoji);
            }
            return 0;
        }

        @Override
        public void onEmojiClick(@NonNull EmojiImageView.EmojiImageViewG imageView, @NonNull Emoji emoji) {
            super.onEmojiClick(imageView, emoji);


        }

        @Override
        public void onEmojiLongClick(@NonNull EmojiImageView.EmojiImageViewG view, @NonNull Emoji emoji) {

            super.onEmojiLongClick(view, emoji);
        }


    }


}
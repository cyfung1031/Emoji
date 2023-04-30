package com.vanniktech.emoji;

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

import static android.view.View.MeasureSpec.makeMeasureSpec;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vanniktech.emoji.emoji.Emoji;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.WeakHashMap;

public final class EmojiVariantPopup2A {
    private static final int MARGIN = 2;

    @NonNull private final WeakReference<View> rootViewWR;
    @Nullable private PopupWindow popupWindow;

    @NonNull final CustomEventDispatcher mEventDispatcher;
    @Nullable private WeakReference<EmojiImageView> rootImageViewWR = null;

    public EmojiVariantPopup2A(@NonNull final View rootView, @NonNull CustomEventDispatcher myEventDispatcher) {
        this.rootViewWR = new WeakReference<>( rootView);
        this.mEventDispatcher = myEventDispatcher;
    }

    public void show(@NonNull final EmojiImageView clickedImage, @NonNull final Emoji emoji) {
        dismiss();

        View rootView = rootViewWR.get();
        if(rootView == null) return;
        rootImageViewWR = new WeakReference<>( clickedImage);

        final View content = initView(clickedImage.getContext(), emoji, clickedImage.getWidth());

        popupWindow = new PopupWindow(content, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
//    popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NOT_NEEDED);
        popupWindow.setBackgroundDrawable(new BitmapDrawable(clickedImage.getContext().getResources(), (Bitmap) null));

        content.measure(makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

        final Point location = Utils.locationOnScreen(clickedImage);
        final Point desiredLocation = new Point(
                location.x - content.getMeasuredWidth() / 2 + clickedImage.getWidth() / 2,
                location.y - content.getMeasuredHeight()
        );

        popupWindow.showAtLocation(rootView, Gravity.NO_GRAVITY, desiredLocation.x, desiredLocation.y);
        clickedImage.getParent().requestDisallowInterceptTouchEvent(true);
        Utils.fixPopupLocation(popupWindow, desiredLocation);
    }

    public void dismiss() {
        rootImageViewWR = null;

        if (popupWindow != null) {
            popupWindow.dismiss();
            popupWindow = null;
        }
    }

    WeakHashMap<ImageView, Emoji> associatedEmojis = new WeakHashMap<>();

    private EmojiImageView createEmojiImageView(Context context){
        EmojiImageView emojiImageView = new EmojiImageView(context);

        // Set layout parameters
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        emojiImageView.setLayoutParams(layoutParams);

        // Set background
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute( Utils.getSelectableBackgroundResId(), outValue, true);
        emojiImageView.setBackgroundResource(outValue.resourceId);


        // Set padding
        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, context.getResources().getDisplayMetrics());
        emojiImageView.setPadding(padding, padding, padding, padding);

        return emojiImageView;
    }

    private View initView(@NonNull final Context context, @NonNull final Emoji emoji, final int width) {
        final View result = View.inflate(context, R.layout.emoji_popup_window_skin, null);
        final LinearLayout imageContainer = result.findViewById(R.id.emojiPopupWindowSkinPopupContainer);

        final List<Emoji> variants = emoji.getBase().getVariants();
        variants.add(0, emoji.getBase());

        final LayoutInflater inflater = LayoutInflater.from(context);

        for (final Emoji variant : variants) {
            final ImageView emojiImage = createEmojiImageView(context);
            final ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) emojiImage.getLayoutParams();
            final int margin = Utils.dpToPx(context, MARGIN);

            // Use the same size for Emojis as in the picker.
            layoutParams.width = width;
            layoutParams.setMargins(margin, margin, margin, margin);
            emojiImage.setImageDrawable(variant.getDrawable(context));
            associatedEmojis.put(emojiImage, variant);

            emojiImage.setOnClickListener(this::onEmojiVariantClicked);

            imageContainer.addView(emojiImage);
        }

        return result;
    }

    public void onEmojiVariantClicked(final View view){
        final Emoji variant =  associatedEmojis.get((ImageView) view);
        if(variant == null) return;
        mEventDispatcher.dispatchEvent("emojiClick", (Object) variant);
        final EmojiImageView rootImageView = rootImageViewWR != null ? rootImageViewWR.get() : null;
        if(rootImageView != null) {
            rootImageView.post(() -> {
                rootImageView.updateEmoji(variant);
            });
        }

    }
}


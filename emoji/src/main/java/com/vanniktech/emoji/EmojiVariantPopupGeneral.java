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

import static android.view.View.MeasureSpec.makeMeasureSpec;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;

import com.vanniktech.emoji.emoji.Emoji;

import java.lang.ref.WeakReference;
import java.util.List;

public final class EmojiVariantPopupGeneral {
    private static final int MARGIN = 2;
    @NonNull
    final EmojiViewBoard emojiViewBoard;
    @Nullable
    EmojiImageView.EmojiImageViewG rootImageView;
    @Nullable
    private WeakReference<View> rootViewWR = null;
    @Nullable
    private PopupWindow popupWindow;

    public EmojiVariantPopupGeneral(@NonNull final View rootView, @NonNull final EmojiViewBoard emojiViewBoard) {
        this.rootViewWR = new WeakReference<>(rootView);
        this.emojiViewBoard = emojiViewBoard;
    }

    static ImageView createImageBtn(Context context) {
        AppCompatImageView result = new EmojiImageView.EmojiImageViewP(context);

        // set layout parameters
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        result.setLayoutParams(params);

        // set padding
        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, context.getResources().getDisplayMetrics());
        result.setPadding(padding, padding, padding, padding);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // set selectableItemBackgroundBorderless as the background
            int[] attrs = new int[]{R.attr.selectableItemBackgroundBorderless};
            TypedValue typedValue = new TypedValue();
            context.getTheme().resolveAttribute(attrs[0], typedValue, true);
            Drawable d = ContextCompat.getDrawable(context, typedValue.resourceId);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                result.setForeground(d);
            } else {
                result.setBackground(d);
            }
        }
        return result;
    }

    public void show(@NonNull final EmojiImageView.EmojiImageViewG clickedImage, @NonNull final Emoji emoji) {
        dismiss();

        View rootView = rootViewWR != null ? rootViewWR.get() : null;
        if (rootView == null) return;

        rootImageView = clickedImage;

        final View content = initView(clickedImage.getContext(), emoji, clickedImage.getWidth());

        popupWindow = new PopupWindow(content, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);
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
        rootImageView.getParent().requestDisallowInterceptTouchEvent(true);
        Utils.fixPopupLocation(popupWindow, desiredLocation);
    }

    public void dismiss() {
        rootImageView = null;

        if (popupWindow != null) {
            popupWindow.dismiss();
            popupWindow = null;
        }
    }

    private View initView(@NonNull final Context context, @NonNull final Emoji emoji, final int width) {

        LayoutInflater inflater = LayoutInflater.from(context);
        final View result = inflater.inflate(R.layout.emoji_popup_window_skin, (ViewGroup) null);
        final LinearLayout imageContainer = result.findViewById(R.id.emojiPopupWindowSkinPopupContainer);

        final List<Emoji> variants = emoji.getBase().getVariants();
        variants.add(0, emoji.getBase());

//    final LayoutInflater inflater = LayoutInflater.from(context);

        for (final Emoji variant : variants) {
//      final ImageView emojiImage = (ImageView) inflater.inflate(R.layout.emoji_adapter_item, imageContainer, false);
            final ImageView emojiImage = createImageBtn(context);
            final ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) emojiImage.getLayoutParams();
            final int margin = Utils.dpToPx(context, MARGIN);

            // Use the same size for Emojis as in the picker.
            layoutParams.width = width;
            layoutParams.setMargins(margin, margin, margin, margin);
            emojiImage.setImageDrawable(variant.getDrawable(context));

            emojiImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    emojiViewBoard.setPopupRootImageView(rootImageView);
                    emojiViewBoard.setPopupVariant(variant);

                    emojiViewBoard.controller(0x3041);
                }
            });

            imageContainer.addView(emojiImage);
        }

        return result;
    }
}

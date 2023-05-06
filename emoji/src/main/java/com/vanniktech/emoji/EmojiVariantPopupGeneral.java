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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vanniktech.emoji.emoji.Emoji;

import java.lang.ref.WeakReference;
import java.util.List;

public final class EmojiVariantPopupGeneral {
  private static final int MARGIN = 2;

  @Nullable private WeakReference<View>  rootViewWR = null;
  @Nullable private PopupWindow popupWindow;

  @NonNull final EmojiViewController controller;
  @Nullable
  EmojiImageViewG rootImageView;

  public EmojiVariantPopupGeneral(@NonNull final View rootView, @NonNull final EmojiViewController controller) {
    this.rootViewWR = new WeakReference<>(rootView);
    this.controller = controller;
  }

  public void show(@NonNull final EmojiImageViewG clickedImage, @NonNull final Emoji emoji) {
    dismiss();

    View rootView =  rootViewWR != null ? rootViewWR.get():null;
    if(rootView==null) return;

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
    final View result = View.inflate(context, R.layout.emoji_popup_window_skin, null);
    final LinearLayout imageContainer = result.findViewById(R.id.emojiPopupWindowSkinPopupContainer);

    final List<Emoji> variants = emoji.getBase().getVariants();
    variants.add(0, emoji.getBase());

    final LayoutInflater inflater = LayoutInflater.from(context);

    for (final Emoji variant : variants) {
      final ImageView emojiImage = (ImageView) inflater.inflate(R.layout.emoji_adapter_item, imageContainer, false);
      final ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) emojiImage.getLayoutParams();
      final int margin = Utils.dpToPx(context, MARGIN);

      // Use the same size for Emojis as in the picker.
      layoutParams.width = width;
      layoutParams.setMargins(margin, margin, margin, margin);
      emojiImage.setImageDrawable(variant.getDrawable(context));

      emojiImage.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(final View view) {
          controller.setPopupRootImageView(rootImageView);
          controller.setPopupVariant(variant);

          controller.controller(0x3041);
        }
      });

      imageContainer.addView(emojiImage);
    }

    return result;
  }
}

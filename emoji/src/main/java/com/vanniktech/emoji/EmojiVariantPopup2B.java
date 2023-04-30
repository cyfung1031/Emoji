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
import android.graphics.Point;
import android.os.Build;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vanniktech.emoji.emoji.Emoji;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.WeakHashMap;

public final class EmojiVariantPopup2B {
  private static final int MARGIN = 2;

  @NonNull
  private final WeakReference<View> rootViewWR;
  @NonNull
  final CustomEventDispatcher mEventDispatcher;
  @Nullable
  private WeakReference<EmojiImageView> rootImageViewWR = null;
  private FrameLayout emojiContainer;

  public EmojiVariantPopup2B(@NonNull final View rootView, @NonNull CustomEventDispatcher myEventDispatcher) {
    this.rootViewWR = new WeakReference<>(rootView);
    this.mEventDispatcher = myEventDispatcher;
  }

  public void show(@NonNull final EmojiImageView clickedImage, @NonNull final Emoji emoji) {
    dismiss();

    View rootView = rootViewWR.get();
    if (rootView == null) return;
    rootImageViewWR = new WeakReference<>(clickedImage);

    final View content = initView(clickedImage.getContext(), emoji, clickedImage.getWidth());

    emojiContainer = new FrameLayout(rootView.getContext());
    emojiContainer.addView(content);
    emojiContainer.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        dismiss();
      }
    });

    if (rootView instanceof ViewGroup) {
      ((ViewGroup) rootView).addView(emojiContainer);
    }

    content.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

    final Point location = Utils.locationOnScreen(clickedImage);
    final Point desiredLocation = new Point(
            location.x - content.getMeasuredWidth() / 2 + clickedImage.getWidth() / 2,
            location.y - content.getMeasuredHeight()
    );

    WindowManager windowManager = (WindowManager) rootView.getContext().getSystemService(Context.WINDOW_SERVICE);
    Display display = windowManager.getDefaultDisplay();
    Point screenSize = new Point();
    display.getSize(screenSize);

    // Check if the popup is exceeding the screen dimensions
    if (desiredLocation.x + content.getMeasuredWidth() > screenSize.x) {
      desiredLocation.x = screenSize.x - content.getMeasuredWidth();
    }
    if (desiredLocation.x < 0) {
      desiredLocation.x = 0;
    }

    FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    layoutParams.gravity = Gravity.NO_GRAVITY;
    layoutParams.leftMargin = desiredLocation.x;
    layoutParams.topMargin = desiredLocation.y;

    content.setLayoutParams(layoutParams);

    clickedImage.getParent().requestDisallowInterceptTouchEvent(true);
  }

  public void dismiss() {
    rootImageViewWR = null;

    if (emojiContainer != null) {
      View rootView = rootViewWR.get();
      if (rootView instanceof ViewGroup) {
        ((ViewGroup) rootView).removeView(emojiContainer);
      }
      emojiContainer = null;
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

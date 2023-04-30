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
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.vanniktech.emoji.emoji.Emoji;
import com.vanniktech.emoji.listeners.OnEmojiClickListener;
import com.vanniktech.emoji.listeners.OnEmojiLongClickListener;

public final class EmojiImageView extends AppCompatImageView {
  private static final int VARIANT_INDICATOR_PART_AMOUNT = 6;
  private static final int VARIANT_INDICATOR_PART = 5;

  Emoji currentEmoji;

  OnEmojiClickListener clickListener;
  OnEmojiLongClickListener longClickListener;

  private final Paint variantIndicatorPaint = new Paint();
  private final Path variantIndicatorPath = new Path();

  private final Point variantIndicatorTop = new Point();
  private final Point variantIndicatorBottomRight = new Point();
  private final Point variantIndicatorBottomLeft = new Point();

  private boolean hasVariants;

  static private int mDesiredWidth = 0;
  static private int mDesiredHeight = 0;


  public void init(Context context){

    if(mDesiredWidth == 0){
      Resources resources = getResources();
      mDesiredWidth = resources.getDimensionPixelSize(R.dimen.emoji_grid_view_column_width);
      mDesiredHeight = mDesiredWidth;
    }


    setScaleType(ScaleType.FIT_CENTER);
    setFrame(0,0,mDesiredWidth,mDesiredHeight);

    variantIndicatorPaint.setColor(Utils.resolveColor(context, R.attr.emojiDivider, R.color.emoji_divider));
    variantIndicatorPaint.setStyle(Paint.Style.FILL);
    variantIndicatorPaint.setAntiAlias(true);
  }
  public EmojiImageView(Context context) {
    super(context);
    init(context);
  }

  public EmojiImageView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context);
  }

  public EmojiImageView(final Context context, final AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  @Override public void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
    setMeasuredDimension(mDesiredWidth, mDesiredHeight);
//    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

//    final int measuredWidth = getMeasuredWidth();
    //noinspection SuspiciousNameCombination
//    setMeasuredDimension(measuredWidth, measuredWidth);
  }

  @Override protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);

    variantIndicatorTop.x = w;
    variantIndicatorTop.y = h / VARIANT_INDICATOR_PART_AMOUNT * VARIANT_INDICATOR_PART;
    variantIndicatorBottomRight.x = w;
    variantIndicatorBottomRight.y = h;
    variantIndicatorBottomLeft.x = w / VARIANT_INDICATOR_PART_AMOUNT * VARIANT_INDICATOR_PART;
    variantIndicatorBottomLeft.y = h;

    variantIndicatorPath.rewind();
    variantIndicatorPath.moveTo(variantIndicatorTop.x, variantIndicatorTop.y);
    variantIndicatorPath.lineTo(variantIndicatorBottomRight.x, variantIndicatorBottomRight.y);
    variantIndicatorPath.lineTo(variantIndicatorBottomLeft.x, variantIndicatorBottomLeft.y);
    variantIndicatorPath.close();
  }

  @Override protected void onDraw(final Canvas canvas) {
    super.onDraw(canvas);

    if (hasVariants && getDrawable() != null) {
      canvas.drawPath(variantIndicatorPath, variantIndicatorPaint);
    }
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
  }

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();

  }

  void setEmoji(@NonNull final Emoji emoji) {
    if (!emoji.equals(currentEmoji)) {

//      if(getDrawable() != null) setImageDrawable(null);

      currentEmoji = emoji;
      hasVariants = emoji.getBase().hasVariants();


      setOnClickListener(this::onEmojiClick);

      setOnLongClickListener(hasVariants ? this::onEmojiLongPress : null);

//      ImageBackgroundLoader ibl =  ImageBackgroundLoader.build(mContext);


      loadDrawable(getContext(),EmojiImageView.this, currentEmoji);


//      LoadDrawableTask.getInstance().loadDrawable(getContext(),EmojiImageView.this, currentEmoji);

    }
  }

  static void loadDrawable(Context context, ImageView imageView, Emoji currentEmoji){

    final Drawable drawable = currentEmoji.getDrawable(context);

        imageView.setImageDrawable(drawable);

  }

  void onEmojiClick(final View view){

    if (clickListener != null) {
      clickListener.onEmojiClick(EmojiImageView.this, currentEmoji);
    }
  }

  boolean onEmojiLongPress(final View view){

    longClickListener.onEmojiLongClick(EmojiImageView.this, currentEmoji);

    return true;
  }

  /**
   * Updates the emoji image directly. This should be called only for updating the variant
   * displayed (of the same base emoji), since it does not run asynchronously and does not update
   * the internal listeners.
   *
   * @param emoji The new emoji variant to show.
   */
  public void updateEmoji(@NonNull final Emoji emoji) {
    if (!emoji.equals(currentEmoji)) {
      currentEmoji = emoji;

      setImageDrawable(emoji.getDrawable(this.getContext()));
    }
  }

  void setOnEmojiClickListener(@Nullable final OnEmojiClickListener listener) {
    this.clickListener = listener;
  }

  void setOnEmojiLongClickListener(@Nullable final OnEmojiLongClickListener listener) {
    this.longClickListener = listener;
  }
}

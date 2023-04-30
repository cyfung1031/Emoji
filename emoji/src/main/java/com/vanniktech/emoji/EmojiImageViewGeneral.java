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
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.vanniktech.emoji.emoji.Emoji;

public final class EmojiImageViewGeneral extends AppCompatImageView {
  private static final int VARIANT_INDICATOR_PART_AMOUNT = 6;
  private static final int VARIANT_INDICATOR_PART = 5;

  Emoji currentEmoji;


  private static final Paint variantIndicatorPaint = new Paint();
  private static final Path variantIndicatorPath = new Path();

  private static final PointF variantIndicatorTop = new PointF();
  private static final PointF variantIndicatorBottomRight = new PointF();
  private static final PointF variantIndicatorBottomLeft = new PointF();

  private boolean hasVariants;

  private EmojiViewController emojiViewController = null;



  public EmojiImageViewGeneral(Context context) {
    super(context);
  }

  public EmojiImageViewGeneral(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public EmojiImageViewGeneral(final Context context, final AttributeSet attrs) {
    super(context, attrs);
  }

  @Override public void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
//    setMeasuredDimension(mDesiredWidth, mDesiredHeight);
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

//    final int measuredWidth = getMeasuredWidth();
//    noinspection SuspiciousNameCombination
//    setMeasuredDimension(measuredWidth, measuredWidth);


    final int measuredWidth = getMeasuredWidth();
//    final int measuredHeight = getMeasuredHeight();
    final int dimWidth = measuredWidth + intWidthAdjust;
    final int dimHeight = measuredWidth + intHeightAdjust;
    setMeasuredDimension(dimWidth, dimHeight);

  }

  @Override protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);

    float w2 = w-intGridPadding*0.5f;
    float h2 = h-intGridPadding*0.5f;

    float h2k = ((float)(h2 * VARIANT_INDICATOR_PART)) / ((float)VARIANT_INDICATOR_PART_AMOUNT);
    float w2k = ((float)(w2 * VARIANT_INDICATOR_PART)) / ((float)VARIANT_INDICATOR_PART_AMOUNT);

    variantIndicatorTop.x = w2;
    variantIndicatorTop.y = h2k;
    variantIndicatorBottomRight.x = w2;
    variantIndicatorBottomRight.y = h2;
    variantIndicatorBottomLeft.x = w2k;
    variantIndicatorBottomLeft.y = h2;

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

    setVisibility(View.VISIBLE);
    super.onAttachedToWindow();
//    Log.i("image","attach");


  }

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      cancelPendingInputEvents();
    }
    setVisibility(View.GONE);
  }



  void setEmoji(@NonNull final Emoji emoji) {
    if (!emoji.equals(currentEmoji)) {

//      if(getDrawable() != null) setImageDrawable(null);

      currentEmoji = emoji;
      hasVariants = emoji.getBase().hasVariants();

      setOnLongClickListener(hasVariants ? this::onEmojiLongPress : null);

//      ImageBackgroundLoader ibl =  ImageBackgroundLoader.build(mContext);


      loadDrawable(getContext(), EmojiImageViewGeneral.this, currentEmoji);


//      LoadDrawableTask.getInstance().loadDrawable(getContext(),EmojiImageView.this, currentEmoji);

    }
  }


  private boolean onTouchListener(View view, MotionEvent me) {
//
//    final int actionMasked = me.getActionMasked();
//    final int actionIndex = me.getActionIndex();
//
//    final int actionPointerId = me.getPointerId(actionIndex);
//
//    final boolean maskedDown = actionMasked == MotionEvent.ACTION_DOWN || actionMasked == MotionEvent.ACTION_POINTER_DOWN;
//    final boolean maskedCancel = actionMasked == MotionEvent.ACTION_CANCEL || actionMasked == MotionEvent.ACTION_UP || actionMasked == MotionEvent.ACTION_POINTER_UP;
//    if(maskedCancel){
//      if(actionMasked == MotionEvent.ACTION_CANCEL ) {
//
//      }else {
//        this.onEmojiClick(view);
//      }
//    }
    return false; // consume event
  }


  static void loadDrawable(Context context, ImageView imageView, Emoji currentEmoji){

    final Drawable drawable = currentEmoji.getDrawable(context);

        imageView.setImageDrawable(drawable);

  }

  void onEmojiClick(final View view){

      emojiViewController.setPopupRootImageView(EmojiImageViewGeneral.this);
      emojiViewController.setPopupVariant(currentEmoji);
      emojiViewController.controller(0x3042);
//      clickListener.onEmojiClick(EmojiImageViewGeneral.this, currentEmoji);

  }

  boolean onEmojiLongPress(final View view){
    emojiViewController.setPopupRootImageView(EmojiImageViewGeneral.this);
    emojiViewController.setPopupVariant(currentEmoji);
    emojiViewController.controller(0x6042);

//    longClickListener.onEmojiLongClick(EmojiImageViewGeneral.this, currentEmoji);

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

  int intPadding = 0;
  int intWidthAdjust = 0;
  int intHeightAdjust = 0;

  int intGridPadding = 0;
  void init(@NonNull final EmojiViewInner.EmojiViewBuildController<?> emojiViewController) {
    this.emojiViewController = emojiViewController;


    Context context = getContext();
    intPadding = emojiViewController.getEmojiPadding(context);
    intWidthAdjust = emojiViewController.getEmojiWidthAdjust(context);
    intHeightAdjust = emojiViewController.getEmojiHeightAdjust(context);
    intGridPadding = emojiViewController.getGridPadding(context);

    setPadding(intPadding,intPadding,intPadding,intPadding);


    setScaleType(ScaleType.FIT_CENTER);
//    setFrame(0,0,mDesiredWidth,mDesiredHeight);

    variantIndicatorPaint.setColor(Utils.resolveColor(context, R.attr.emojiDivider, R.color.emoji_divider));
    variantIndicatorPaint.setStyle(Paint.Style.FILL);
    variantIndicatorPaint.setAntiAlias(true);

    // -------------------------



    setOnTouchListener(this::onTouchListener);
    setOnClickListener(this::onEmojiClick);

  }




}

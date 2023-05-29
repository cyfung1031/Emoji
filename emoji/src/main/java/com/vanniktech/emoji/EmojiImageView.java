package com.vanniktech.emoji;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;

import com.vanniktech.emoji.emoji.Emoji;


public class EmojiImageView {


    static public abstract class EmojiImageViewE extends AppCompatImageView {


        protected static final int VARIANT_INDICATOR_PART_AMOUNT = 6;
        protected static final int VARIANT_INDICATOR_PART = 5;
        protected Emoji currentEmoji;




        protected final Paint variantIndicatorPaint = new Paint();
        protected final Path variantIndicatorPath = new Path();

        protected final PointF variantIndicatorTop = new PointF();
        protected final PointF variantIndicatorBottomRight = new PointF();
        protected final PointF variantIndicatorBottomLeft = new PointF();

        protected boolean hasVariants;

        public EmojiImageViewE(Context context) {
            super(context);
        }

        public EmojiImageViewE(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public EmojiImageViewE(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }


        abstract boolean onEmojiLongPress(final View view);
        abstract void onEmojiClick(final View view);


        @Override protected void onDraw(final Canvas canvas) {
            super.onDraw(canvas);

            if (hasVariants && getDrawable() != null) {
                canvas.drawPath(variantIndicatorPath, variantIndicatorPaint);
            }
        }

        protected void sizeChange(float w2, float h2){

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


        static void loadDrawable(Context context, ImageView imageView, Emoji currentEmoji){

            final Drawable drawable = currentEmoji.getDrawable(context);

            imageView.setImageDrawable(drawable);

        }


        public void setEmoji(@NonNull final Emoji emoji) {
            if (!emoji.equals(currentEmoji)) {

//      if(getDrawable() != null) setImageDrawable(null);

                currentEmoji = emoji;
                hasVariants = emoji.getBase().hasVariants();

                setOnLongClickListener(hasVariants ? this::onEmojiLongPress : null);

//      ImageBackgroundLoader ibl =  ImageBackgroundLoader.build(mContext);


                loadDrawable(getContext(), EmojiImageViewE.this, currentEmoji);


//      LoadDrawableTask.getInstance().loadDrawable(getContext(),EmojiImageView.this, currentEmoji);

            }
        }

    }


    public static final class EmojiImageViewG extends EmojiImageView.EmojiImageViewE {


        private EmojiViewBoard emojiViewBoard = null;



        public EmojiImageViewG(Context context) {
            super(context);
        }

        public EmojiImageViewG(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        public EmojiImageViewG(final Context context, final AttributeSet attrs) {
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
            sizeChange(w2,h2);
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





        private boolean onTouchListener(View view, MotionEvent me) {
            return false; // consume event
        }


        public void onEmojiClick(final View view){

            emojiViewBoard.setPopupRootImageView(EmojiImageViewG.this);
            emojiViewBoard.setPopupVariant(currentEmoji);
            emojiViewBoard.controller(0x3042);
//      clickListener.onEmojiClick(EmojiImageViewGeneral.this, currentEmoji);

        }

        public boolean onEmojiLongPress(final View view){
            emojiViewBoard.setPopupRootImageView(EmojiImageViewG.this);
            emojiViewBoard.setPopupVariant(currentEmoji);
            emojiViewBoard.controller(0x6042);

//    longClickListener.onEmojiLongClick(EmojiImageViewGeneral.this, currentEmoji);

            return true;
        }


        int intPadding = 0;
        int intWidthAdjust = 0;
        int intHeightAdjust = 0;

        int intGridPadding = 0;
        void init(@NonNull final EmojiViewBoard emojiViewBoard) {
            this.emojiViewBoard = emojiViewBoard;


            Context context = getContext();
            intPadding = emojiViewBoard.getEmojiPadding(context);
            intWidthAdjust = emojiViewBoard.getEmojiWidthAdjust(context);
            intHeightAdjust = emojiViewBoard.getEmojiHeightAdjust(context);
            intGridPadding = emojiViewBoard.getGridPadding(context);

            setPadding(intPadding,intPadding,intPadding,intPadding);


            setScaleType(ScaleType.FIT_CENTER);
//    setFrame(0,0,mDesiredWidth,mDesiredHeight);

            variantIndicatorPaint.setColor(Utils.resolveColor(context, R.attr.emojiDividerColor, R.color.emoji_divider_color));
            variantIndicatorPaint.setStyle(Paint.Style.FILL);
            variantIndicatorPaint.setAntiAlias(true);

            // -------------------------



            setOnTouchListener(this::onTouchListener);
            setOnClickListener(this::onEmojiClick);

        }




    }


    static public final class EmojiImageViewP extends EmojiImageView.EmojiImageViewE {



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

            variantIndicatorPaint.setColor(Utils.resolveColor(context, R.attr.emojiDividerColor, R.color.emoji_divider_color));
            variantIndicatorPaint.setStyle(Paint.Style.FILL);
            variantIndicatorPaint.setAntiAlias(true);
        }
        public EmojiImageViewP(Context context) {
            super(context);
            init(context);
        }

        public EmojiImageViewP(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            init(context);
        }

        public EmojiImageViewP(final Context context, final AttributeSet attrs) {
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

            float w2 = w-0;
            float h2 = h-0;

            sizeChange(w2,h2);
        }




        public void onEmojiClick(final View view){

        }

        public boolean onEmojiLongPress(final View view){

            return true;
        }


        EmojiImageViewG clickedImage = null;
        Emoji variant = null;
        EmojiViewBoard emojiViewBoard = null;
        public void setOnClickListener(EmojiViewBoard emojiViewBoard, EmojiImageViewG clickedImage, Emoji variant) {
            this.clickedImage = clickedImage;
            this.variant = variant;
            this.emojiViewBoard = emojiViewBoard;
            this.setOnClickListener(this::onClick);
        }


        public void onClick(final View view) {
            if(emojiViewBoard!=null){
                emojiViewBoard.setPopupRootImageView(clickedImage);
                emojiViewBoard.setPopupVariant(variant);

                emojiViewBoard.controller(0x3041);

            }
        }

    }


}
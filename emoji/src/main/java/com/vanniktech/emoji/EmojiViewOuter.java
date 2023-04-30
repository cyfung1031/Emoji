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

import static com.vanniktech.emoji.Utils.backspace;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.annotation.CheckResult;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import com.vanniktech.emoji.emoji.Emoji;
import com.vanniktech.emoji.listeners.OnEmojiBackspaceClickListener;
import com.vanniktech.emoji.listeners.OnEmojiClickListener;

import java.lang.ref.WeakReference;

public class EmojiViewOuter extends FrameLayout {


    final HandlerThread bHt = createBackgroundThread();


    final Handler bH = new Handler(bHt.getLooper());

    @NonNull
    RecentEmoji recentEmoji;
    @NonNull
    VariantEmoji variantEmoji;
    @NonNull
    EmojiVariantPopupGeneral variantPopup;
//    EditText editText;

    EmojiViewController emojiViewController = null;

    public int deleteOneUniChar(){
        // TODO
        return 0; // otherwise, tell task number to create following task
    }

    public int commitOneEmoji(Emoji emoji){
        // TODO
        return 0; // otherwise, tell task number to create following task
    }


    private  HandlerThread createBackgroundThread(){
        HandlerThread ht =  new HandlerThread("backgroundWorkOnEmojiView");
        ht.start();
        return ht;
    }
    public EmojiViewOuter(@NonNull Context context) {
        super(context);
        init(context);
    }

    public EmojiViewOuter(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public EmojiViewOuter(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void onEmojiLongClick(@NonNull final EmojiImageViewGeneral view, @NonNull final Emoji emoji) {

        variantPopup.show(view, emoji);
    }

    public void executeTask(int nextTaskNum){
        // TODO
    }

    public void onEmojiBackspaceClicked(final View v) {

        int nextTaskNum = deleteOneUniChar();

        executeTask(nextTaskNum);

//        backspace(editText);
    }

    public void onEmojiClick(@NonNull final EmojiImageViewGeneral imageView, @NonNull final Emoji emoji) {

        int nextTaskNum = commitOneEmoji(emoji);

        executeTask(nextTaskNum);


//        if (onEmojiClickListener != null) {
//            onEmojiClickListener.onEmojiClick(imageView, emoji);
//        }
        variantPopup.dismiss();
        bH.post(new Runnable() {
            @Override
            public void run() {


                recentEmoji.addEmoji(emoji);
                variantEmoji.addVariant(emoji);
                imageView.updateEmoji(emoji);

                recentEmoji.persist();
                variantEmoji.persist();

                emojiViewController.setRecentEmojiPageUpdateState(1);
            }
        });
    }

    public void setup(@NonNull View container) {



        EmojiViewControllerBase emojiViewController = new EmojiViewControllerBase(getContext());
        this.emojiViewController= emojiViewController;
        emojiViewController.setEmojiViewOuter(this);

        this.recentEmoji = emojiViewController.getRecentEmoji();
        this.variantEmoji = emojiViewController.getVariantEmoji();

        variantPopup = new EmojiVariantPopupGeneral(container.getRootView(), emojiViewController);

        final EmojiViewInner emojiView = new EmojiViewInner(getContext());
        emojiView.init(emojiViewController);

        emojiView.setOnEmojiBackspaceClickListener(emojiViewController);

        EmojiViewOuter.this.addView(emojiView);


    }


    private void init(final Context context) {
    }


    public static final class EmojiViewControllerBase implements EmojiViewInner.EmojiViewBuildController<EmojiViewControllerBase> {
        
        EmojiViewControllerBase(){}

        @Nullable
        ViewPager2.PageTransformer pageTransformer2;
        @NonNull
        RecentEmoji recentEmoji;
        @NonNull
        VariantEmoji variantEmoji;

        private EmojiViewControllerBase(Context context) {
            initByContext(context);
        }

        /**
         * @param context The context of your view.
         * @return builder For building the {@link EmojiPopup}.
         */
        @CheckResult
        public static EmojiViewControllerBase fromRootView(Context context) {
            return new EmojiViewControllerBase(context);
        }

        private void initByContext(final Context context) {
            this.recentEmoji = new RecentEmojiManager2(context);
            this.variantEmoji = new VariantEmojiManager(context);
        }

        @ColorInt
        public int getBackgroundColor(Context context) {

            return Utils.resolveColor(context, R.attr.emojiBackground, R.color.emoji_background);

        }

        @ColorInt
        public int getIconColor(Context context) {

//            return Utils.resolveColor(context, R.attr.emojiIcons, R.color.emoji_icons);



            final TypedValue value = new TypedValue();
            context.getTheme().resolveAttribute(R.attr.emojiIconsToolNormal , value, true);
            return value.data;

        }

        @ColorInt
        public int getSelectedIconColor(Context context) {


//            final TypedValue value = new TypedValue();
//            context.getTheme().resolveAttribute(R.attr.colorAccent, value, true);
//            return value.data;



            final TypedValue value = new TypedValue();
            context.getTheme().resolveAttribute(R.attr.emojiIconsToolSelected , value, true);
            return value.data;

        }

        @ColorInt
        public int getDividerColor(Context context) {

            return Utils.resolveColor(context, R.attr.emojiDivider, R.color.emoji_divider);
        }

        @Nullable
        public ViewPager2.PageTransformer getPageTransformer(Context context) {
            return pageTransformer2;
        }


        @NonNull
        public RecentEmoji getRecentEmoji() {
            return recentEmoji;
        }

        @NonNull
        public VariantEmoji getVariantEmoji() {
            return variantEmoji;
        }


        public int recentAdapterItemCount() {
            return hasRecentEmoji() ? 1 : 0;
        }
        public boolean hasRecentEmoji() {
            return !(recentEmoji instanceof NoRecentEmoji);
        }
        public int numberOfRecentEmojis() {
            if(recentEmoji == null) return 0;
            if(!hasRecentEmoji()) return 0;
            return recentEmoji.getRecentEmojis().size();
        }

        @Override
        public int getGridWidth(Context context) {
            return context.getResources().getDimensionPixelSize(R.dimen.emoji_grid_view_column_width) ;
        }

        @Override
        public int getGridPadding(Context context) {
            return context.getResources().getDimensionPixelSize(R.dimen.emoji_grid_view_1dp) - 1 ;
        }

        @Override
        public int getEmojiPadding(Context context) {
            return 9;
        }

        @Override
        public int getEmojiWidthAdjust(Context context) {
            return 0;
        }

        @Override
        public int getEmojiHeightAdjust(Context context) {
            return -24;
        }

        @Override
        public boolean getTabButtonSmoothTransitionEnabled() {
            return true;
        }


        public void controller(int msgId){
            EmojiImageViewGeneral popupRootImageView = this.popupRootImageView != null ?  this.popupRootImageView.get():null;
            Emoji popupVariant = this.popupVariant != null ?  this.popupVariant.get():null;
            EmojiViewOuter emojiViewOuter = this.emojiViewOuter != null ?  this.emojiViewOuter.get():null;
            switch (msgId){
                case 0x3041:
                case 0x3042:

                    if(popupRootImageView!=null && popupVariant!=null && emojiViewOuter != null){
                        emojiViewOuter.onEmojiClick(popupRootImageView, popupVariant);
                    }
                    break;

                case 0x6042:

                    if(popupRootImageView!=null && popupVariant!=null && emojiViewOuter != null){
                        emojiViewOuter.onEmojiLongClick(popupRootImageView, popupVariant);
                    }

                    break;

                case 0x3051:

                    if( emojiViewOuter != null){
                        emojiViewOuter.onEmojiBackspaceClicked(null);
                    }


                    break;

            }

        }

        public WeakReference<EmojiImageViewGeneral> popupRootImageView = null;
        public WeakReference<Emoji> popupVariant = null;

        @Override
        public void setPopupRootImageView(EmojiImageViewGeneral rootImageView) {
            popupRootImageView = new WeakReference<>(rootImageView);

        }

        @Override
        public void setPopupVariant(Emoji variant) {
            popupVariant = new WeakReference<>(variant);

        }

        WeakReference<EmojiViewOuter> emojiViewOuter = null;
        @Override
        public void setEmojiViewOuter(EmojiViewOuter v) {
            emojiViewOuter = new WeakReference<>(v);

        }

        WeakReference<EmojiViewInner> emojiViewInner = null;
        @Override
        public void setEmojiViewInner(EmojiViewInner v) {
            emojiViewInner = new WeakReference<>(v);

        }

        WeakReference<EmojiVariantPopupGeneral> emojiVariantPopupGeneral = null;
        @Override
        public void setEmojiVariantPopupGeneral(EmojiVariantPopupGeneral v) {
            emojiVariantPopupGeneral = new WeakReference<>(v);

        }


        int recentEmojiPageUpdateState = 0;
        @Override
        public void setRecentEmojiPageUpdateState(int state) {
            recentEmojiPageUpdateState =state;
        }

        @Override
        public int getRecentEmojiPageUpdateState() {
            return recentEmojiPageUpdateState;
        }

        @Override
        public EmojiViewOuter getEmojiViewOuter() {
            return emojiViewOuter != null ?  emojiViewOuter.get():null;
        }

        @Override
        public EmojiViewInner getEmojiViewInner() {
            return emojiViewInner != null ? emojiViewInner.get() : null;
        }


        WeakReference<EmojiGridInner> recentEmojiGridViewWR = null;
        public void setRecentEmojiGridView(EmojiGridInner newView){
            recentEmojiGridViewWR = new WeakReference<>(newView);

        }
        public EmojiGridInner getRecentEmojiGridView(){
            return recentEmojiGridViewWR != null ? recentEmojiGridViewWR.get(): null;
        }


    }


}
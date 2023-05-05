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

import static com.vanniktech.emoji.Utils.asListWithoutDuplicates;
import static com.vanniktech.emoji.Utils.checkNotNull;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.vanniktech.emoji.emoji.Emoji;
import com.vanniktech.emoji.emoji.EmojiCategory;
import com.vanniktech.emoji.listeners.OnEmojiClickListener;
import com.vanniktech.emoji.listeners.OnEmojiLongClickListener;

import java.lang.ref.WeakReference;
import java.util.Collection;

class EmojiGridInner extends GridView {
    protected EmojiGridInner.EmojiArrayAdapterGeneral emojiArrayAdapterG = null;
    boolean isRecentEmojiGridView = false;
    private WeakReference<RecentEmoji> recentEmojisWR = null;

    EmojiGridInner(final Context context) {
        super(context);
    }

    private EmojiViewController emojiViewController = null;

    boolean clearAdapter(){
        EmojiArrayAdapterGeneral adapter = this.emojiArrayAdapterG;
        if(adapter != null) {
            adapter.clear();
            return true;
        }
        return false;
    }

    public void destroyView(){

        EmojiGridInner emojiGridInner = (EmojiGridInner) this;
        boolean isAdapterValid =  emojiGridInner.clearAdapter();
        if(isAdapterValid){
            emojiGridInner.emojiArrayAdapterG = null;
            emojiGridInner.isRecentEmojiGridView = false;
        }
    }

    private void initInner(@NonNull EmojiViewInner.EmojiViewBuildController<?> emojiViewController,
                           @NonNull Emoji[] emojis) {

        this.emojiViewController = emojiViewController;

        // ------------------------------------


        final Resources resources = getResources();
//        final int width = resources.getDimensionPixelSize(R.dimen.emoji_grid_view_column_width);
//        final int spacing = resources.getDimensionPixelSize(R.dimen.emoji_grid_view_spacing);

        int gridWidth = emojiViewController.getGridWidth(getContext());
        int gridPadding = emojiViewController.getGridPadding(getContext());
        final int width = gridWidth + 2*gridPadding;
        final  int spacing = 0;
        final int padding = gridPadding;

        setColumnWidth(width);
        setHorizontalSpacing(spacing);
        setVerticalSpacing(spacing);
        setPadding(padding, padding, padding, padding);
        setNumColumns(AUTO_FIT);
        setStretchMode(STRETCH_COLUMN_WIDTH);
        setClipToPadding(false);
        setVerticalScrollBarEnabled(false);

        setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS); // optional
        ViewCompat.setNestedScrollingEnabled(this, true);

        // ------------------------------------



        EmojiArrayAdapterGeneral emojiArrayAdapter = new EmojiArrayAdapterGeneral(getContext(), emojis,emojiViewController);

//        emojiArrayAdapterWR =new WeakReference<>(emojiArrayAdapter);
        emojiArrayAdapterG = emojiArrayAdapter;

        setAdapter(emojiArrayAdapter);
    }

    public EmojiGridInner init(@NonNull final EmojiViewInner.EmojiViewBuildController<?> emojiViewController,
                               @NonNull final RecentEmoji recentEmoji) {
        isRecentEmojiGridView = true;

        recentEmojisWR =new WeakReference<>(recentEmoji);

        final Collection<Emoji> emojis = recentEmoji.getRecentEmojis();
        initInner(emojiViewController, emojis.toArray(new Emoji[0]));

        return this;
    }


    public EmojiGridInner init(@NonNull final EmojiViewInner.EmojiViewBuildController<?> emojiViewController,
                               @NonNull final EmojiCategory category) {
        isRecentEmojiGridView = false;
        recentEmojisWR = null;

        initInner(emojiViewController, category.getEmojis());

        return this;
    }

    public void invalidateEmojis() {

        if (isRecentEmojiGridView) {

            if(recentEmojisWR == null) return;
            EmojiGridInner.EmojiArrayAdapterGeneral emojiArrayAdapter =emojiArrayAdapterG;
            RecentEmoji recentEmojis = recentEmojisWR.get();

            if(emojiArrayAdapter == null || recentEmojis == null) return;

            emojiArrayAdapter.updateEmojis(recentEmojis.getRecentEmojis());
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        setAdapter(null);
//        emojiArrayAdapter.clear();
//        emojiArrayAdapter = null;
    }



    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        EmojiGridInner.EmojiArrayAdapterGeneral emojiArrayAdapter =emojiArrayAdapterG;
        if (emojiArrayAdapter != null) {
            setAdapter(emojiArrayAdapter);
        }
    }



    @Override
    protected void onDraw(Canvas canvas) {

        EmojiGridInner.EmojiArrayAdapterGeneral emojiArrayAdapter =emojiArrayAdapterG;
        if (emojiArrayAdapter == null) return;
        super.onDraw(canvas);
    }

    public void destroyItem(){
        emojiArrayAdapterG.clear();
        emojiArrayAdapterG = null;
        isRecentEmojiGridView = false;
        recentEmojisWR= null;


    }

//    int changedInt = 0;

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
//        deferNotifyDataSetChanged();
//        Log.i("WWS","SSQ - "+ (b-t)+" - "+ changed);
//
//
//        if(changed){
//            changedInt = 0;
//            postInvalidateDelayed(30);
//        }
//
//        View view = this;
//
//        if(changedInt > 10) {
//            changedInt = 0;
//            return;
//        }
////
//
//            view.postDelayed(() -> {
//                emojiArrayAdapterG.notifyDataSetInvalidated();
//                changedInt++;
////            view.requestLayout();
//            }, 30);


//        view.postInvalidateDelayed(100);

    }


    final static class EmojiArrayAdapterGeneral extends ArrayAdapter<Emoji> {
        @Nullable private final VariantEmoji variantManager;

        @NonNull private final EmojiViewInner.EmojiViewBuildController<?> emojiViewController;

        EmojiArrayAdapterGeneral(@NonNull final Context context, @NonNull final Emoji[] emojis,
                                 @NonNull final EmojiViewInner.EmojiViewBuildController<?> emojiViewController) {
            super(context, 0, asListWithoutDuplicates(emojis));

            this.variantManager = emojiViewController.getVariantEmoji();
            this.emojiViewController = emojiViewController;
        }

        public static EmojiImageViewGeneral createEmojiImageView(Context context) {
            EmojiImageViewGeneral emojiImageView = new EmojiImageViewGeneral(context);

            // Set layout parameters
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            emojiImageView.setLayoutParams(layoutParams);

            // Set background
//    TypedValue typedValue = new TypedValue();
//    context.getTheme().resolveAttribute( Utils.getSelectableBackgroundResId(), typedValue, true);
//    emojiImageView.setBackgroundResource(typedValue.resourceId);

            emojiImageView.setBackgroundResource(0);

            emojiImageView.setBackground(ResourcesCompat.getDrawable( context.getResources(), R.drawable.emoji_normal, null));




            // Set padding
//    int paddingInPixels = (int) TypedValue.applyDimension(
//            TypedValue.COMPLEX_UNIT_DIP,
//            2,
//            context.getResources().getDisplayMetrics());
//    emojiImageView.setPadding(paddingInPixels, paddingInPixels, paddingInPixels, paddingInPixels);

            return emojiImageView;
        }

        @Override @NonNull public View getView(final int position, final View convertView, @NonNull final ViewGroup parent) {
            EmojiImageViewGeneral image = (EmojiImageViewGeneral) convertView;

            final Context context = getContext();

            if (image == null) {
                image = (EmojiImageViewGeneral) createEmojiImageView(context);
                image.init(emojiViewController);
            }

            final Emoji emoji = checkNotNull(getItem(position), "emoji == null");
            final Emoji variantToUse = variantManager == null ? emoji : variantManager.getVariant(emoji);
            image.setContentDescription(emoji.getUnicode());
            image.setEmoji(variantToUse);

            return image;
        }

        void updateEmojis(final Collection<Emoji> emojis) {
            clear();
            addAll(emojis);
            notifyDataSetChanged();
        }



    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // Intercept touch events to prevent the parent view from scrolling when
        // the user is scrolling the GridView
        boolean intercepted = super.onInterceptTouchEvent(ev);
        Log.i("EmojiGridInner", intercepted +"|"+ canScrollVertically(-1) +" | "+ canScrollVertically(1));
        if (intercepted && (canScrollVertically(-1) || canScrollVertically(1)) ) {
            getParent().requestDisallowInterceptTouchEvent(true);
            return true;
        }
        return intercepted;
    }

}

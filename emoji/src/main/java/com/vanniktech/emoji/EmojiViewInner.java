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
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.adapter.FragmentViewHolder;
import androidx.viewpager2.widget.ViewPager2;

import com.vanniktech.emoji.emoji.EmojiCategory;
import com.vanniktech.emoji.listeners.RepeatListener;

import java.lang.ref.WeakReference;
import java.util.List;

public class EmojiViewInner extends LinearLayout {
    private static final long INITIAL_INTERVAL = 500; //ms
    private static final int NORMAL_INTERVAL = 50; //ms
    @ColorInt
    protected int themeAccentColor = 0;
    @ColorInt
    protected int themeIconColor = 0;
    protected AppCompatImageView[] emojiTabs = null;
    protected EmojiViewInner.EmojiGridPagerAdapter emojiPager2Adapter = null;
    protected int emojiTabLastSelectedIndex = -1;
    protected ViewPager2 emojisPager2;
    protected EmojiViewController emojiViewController = null;
    boolean isTabButtonSmoothTransitionEnabled = false;
    boolean disAllowParentVerticalScroll = false;

    public EmojiViewInner(final Context context) {
        super(context);
    }

    public EmojiViewInner(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

//    @Nullable
//    @Override
//    protected Parcelable onSaveInstanceState() {
//        Log.i("EmojiViewInner", "onSaveInstanceState");
//        return super.onSaveInstanceState();
//    }
//
//    @Override
//    protected void onRestoreInstanceState(Parcelable state) {
//        Log.i("EmojiViewInner", "onRestoreInstanceState");
//        super.onRestoreInstanceState(state);
//    }
//
//    @Override
//    protected void onConfigurationChanged(Configuration newConfig) {
//        Log.i("EmojiViewInner", "onConfigurationChanged");
//        super.onConfigurationChanged(newConfig);
//    }


    public EmojiViewInner(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    static public void tintImageView(AppCompatImageView v, int color) {
        Drawable d = v.getDrawable();
        if (d != null) {
            DrawableCompat.setTint(d, color);
        }
    }
    public static Drawable getTintedDrawable(Drawable inputDrawable, @ColorInt int color) {
        Drawable wrapDrawable = DrawableCompat.wrap(inputDrawable.mutate());
        DrawableCompat.setTint(wrapDrawable, color);
        DrawableCompat.setTintMode(wrapDrawable, PorterDuff.Mode.SRC_IN);
        return wrapDrawable;
    }

    public void init(@NonNull final EmojiViewBuildController<?> builder) {

        builder.setEmojiViewInner(this);
        emojiViewController = builder;
        isTabButtonSmoothTransitionEnabled = builder.getTabButtonSmoothTransitionEnabled();

        Context context = getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        inflater.inflate(R.layout.emoji_view2, this, true);
        setOrientation(VERTICAL);

        builder.setEmojiViewInner(this);

        setBackgroundColor(builder.getBackgroundColor(context));
        themeIconColor = builder.getIconColor(context);

        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorAccent, value, true);
        themeAccentColor = builder.getSelectedIconColor(context);

//        emojisPager = findViewById(R.id.emojiViewPager);

        emojisPager2 = findViewById(R.id.emojiViewPager);
        emojisPager2.setSaveEnabled(false);
        final View emojiDivider = findViewById(R.id.emojiViewDivider);
        emojiDivider.setBackgroundColor(builder.getDividerColor(context));

        ViewPager2.PageTransformer pt = builder.getPageTransformer(context);
        if (pt != null) {
            emojisPager2.setPageTransformer(pt);
        }

        final LinearLayout emojisTab = findViewById(R.id.emojiViewTab);
//        final ViewPager.OnPageChangeListener pageChangeHandler = new OnPageChangeListener(emojiViewController);
//        emojisPager.addOnPageChangeListener(pageChangeHandler);


        final ViewPager2.OnPageChangeCallback pageChangeHandler2 = new OnPageChangeListener2(emojiViewController);
        emojisPager2.registerOnPageChangeCallback(pageChangeHandler2);

        final EmojiCategory[] categories = EmojiManager.getInstance().getCategories();

        emojiPager2Adapter = new EmojiViewInner.EmojiGridPagerAdapter((FragmentActivity) this.getContext(), builder);
        emojiTabs = new AppCompatImageView[builder.recentAdapterItemCount() + categories.length + 1];

        if (builder.hasRecentEmoji()) {
            emojiTabs[0] = inflateButton(context, R.drawable.emoji_recent, R.string.emoji_category_recent, emojisTab);
        }

        for (int i = 0; i < categories.length; i++) {
            emojiTabs[i + builder.recentAdapterItemCount()] = inflateButton(context, categories[i].getIcon(), categories[i].getCategoryName(), emojisTab);
        }

        emojiTabs[emojiTabs.length - 1] = inflateButton(context, R.drawable.emoji_backspace, R.string.emoji_backspace, emojisTab);

        handleOnClicks();

        emojisPager2.setAdapter(emojiPager2Adapter);
        emojisPager2.setOffscreenPageLimit(1);


        final int startIndex = builder.hasRecentEmoji() ? emojiPager2Adapter.numberOfRecentEmojis() > 0 ? 0 : 1 : 0;
        emojisPager2.setCurrentItem(startIndex, false);
        pageChangeHandler2.onPageSelected(startIndex);

    }

    @Override
    protected void onDetachedFromWindow() {
//        Log.i("12312","123123");
        super.onDetachedFromWindow();
        if (emojiPager2Adapter != null) emojiPager2Adapter.toggleOFF();
        if (emojiViewController != null) {
//            Log.i("2323","545");
//            emojiViewController.setPopupViewWindow(null);
//            emojiViewController.setEmojiViewInner(null);
        }

    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (emojiViewController != null) emojiViewController.setEmojiViewInner(this);
        if (emojiPager2Adapter != null) emojiPager2Adapter.toggleON();
    }

    public void onEmojiTabButtonClicked(View v) {
        for (int i = 0; i < emojiTabs.length - 1; i++) {
            if (emojiTabs[i] == v) {
                emojisPager2.setCurrentItem(i, isTabButtonSmoothTransitionEnabled);
                break;
            }
        }
    }

    private void handleOnClicks() {
        for (int i = 0; i < emojiTabs.length - 1; i++) {
            emojiTabs[i].setOnClickListener(this::onEmojiTabButtonClicked);
        }

        emojiTabs[emojiTabs.length - 1].setOnTouchListener(new RepeatListener(INITIAL_INTERVAL, NORMAL_INTERVAL, this::onBackSpaceToolButtonRepeatedlyClicked));
    }

    public void onBackSpaceToolButtonRepeatedlyClicked(final View view) {
        emojiViewController.controller(0x3051);
    }

    public void setOnEmojiBackspaceClickListener(@Nullable final EmojiViewController emojiViewController) {
        this.emojiViewController = emojiViewController;
    }

    private AppCompatImageView inflateButtonInner(Context context) {

        // 1. Create a new ImageButton object
        AppCompatImageView imageButton = new AppCompatImageView(context);

        // 2. Set the layout parameters, including width, height, and weight
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.weight = 1;
        imageButton.setLayoutParams(layoutParams);

        // 3. Apply the background, padding, and scale type attributes

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            imageButton.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.emoji_bottom_line, null));

        }
        imageButton.setPadding(4, 4, 4, 4);
        imageButton.setScaleType(ImageView.ScaleType.FIT_CENTER);

        return imageButton;
    }

    private AppCompatImageView inflateButton(final Context context, @DrawableRes final int btnDrawableResId, @StringRes final int categoryName, final ViewGroup parent) {
//        final ImageButton button = (ImageButton) LayoutInflater.from(context).inflate(R.layout.emoji_view_category, parent, false);
        final AppCompatImageView button = inflateButtonInner(context);


        Drawable d = ResourcesCompat.getDrawable(context.getResources(), btnDrawableResId, null);
        if (d != null) {
            d = DrawableCompat.wrap(d.mutate());
            if (themeIconColor != 0) {
                DrawableCompat.setTintMode(d, PorterDuff.Mode.SRC_IN);
                DrawableCompat.setTint(d, themeIconColor);
            }


//            d
//            res = new BitmapDrawable(context.getResources(), resBitmap);
//            res.setTint(textColor);

//            int myColor = Color.argb(255, 255, 0, 0);
//            d=d.mutate();
//            DrawableCompat.setTintMode(d, PorterDuff.Mode.SRC_IN);
//            DrawableCompat.setTint(d, themeIconColor);

        }
        button.setImageDrawable(d);

        button.setContentDescription(context.getString(categoryName));

        parent.addView(button);

        return button;
    }

    public boolean isDisAllowParentVerticalScroll() {
        return disAllowParentVerticalScroll;
    }

    public void setDisAllowParentVerticalScroll(boolean disAllowParentVerticalScroll) {
        this.disAllowParentVerticalScroll = disAllowParentVerticalScroll;
    }

    public interface EmojiViewBuilder<TBuilder> {


        @ColorInt
        public int getBackgroundColor(Context context);

        @ColorInt
        public int getIconColor(Context context);

        @ColorInt
        public int getSelectedIconColor(Context context);

        @ColorInt
        public int getDividerColor(Context context);

        @Nullable

        public void setPageTransformer(@Nullable final ViewPager2.PageTransformer transformer);

        @Nullable
        public ViewPager2.PageTransformer getPageTransformer(Context context);

        @NonNull
        public IRecentEmoji getRecentEmoji();

        @NonNull
        public IVariantEmoji getVariantEmoji();


        public int recentAdapterItemCount();

        public boolean hasRecentEmoji();

        public int numberOfRecentEmojis();


        int getGridWidth(Context context);

        int getGridPadding(Context context);

        int getEmojiPadding(Context context);

        int getEmojiWidthAdjust(Context context);

        int getEmojiHeightAdjust(Context context);

        boolean getTabButtonSmoothTransitionEnabled();

    }


    public interface EmojiViewBuildController<T> extends EmojiViewController, EmojiViewBuilder<T> {

    }

    //    private static class OnPageChangeListener implements  ViewPager.OnPageChangeListener{
    private static class OnPageChangeListener2 extends ViewPager2.OnPageChangeCallback {

        final EmojiViewController emojiViewController;

        OnPageChangeListener2(EmojiViewController emojiViewController) {
            this.emojiViewController = emojiViewController;
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            // No-op.
        }


        @Override
        public void onPageSelected(final int i) {
            EmojiViewInner v = emojiViewController.getEmojiViewInner();

            if (v.emojiTabLastSelectedIndex != i) {
                final int lastSelectedIndex = v.emojiTabLastSelectedIndex;
                v.emojiTabLastSelectedIndex = i;

                final AppCompatImageView[] emojiTabs = v.emojiTabs;

                if (lastSelectedIndex >= 0 && lastSelectedIndex < v.emojiTabs.length) {
                    emojiTabs[lastSelectedIndex].setSelected(false);
//                    emojiTabs[lastSelectedIndex].setImageDrawable(getTintedDrawable(emojiTabs[lastSelectedIndex].getDrawable(), v.themeIconColor));
                    tintImageView(emojiTabs[lastSelectedIndex], v.themeIconColor);

//                    emojiTabs[lastSelectedIndex].setColorFilter(v.themeIconColor, PorterDuff.Mode.SRC_IN);
                }

                emojiTabs[i].setSelected(true);

//                emojiTabs[i].setImageDrawable(getTintedDrawable(emojiTabs[i].getDrawable(), v.themeAccentColor));
                tintImageView(emojiTabs[i], v.themeAccentColor);
//                emojiTabs[i].setColorFilter(v.themeAccentColor, PorterDuff.Mode.SRC_IN);

            }
        }

        private void updateRecentEmojis() {
            EmojiViewInner v = emojiViewController.getEmojiViewInner();

            v.emojiPager2Adapter.invalidateRecentEmojis();
            emojiViewController.setRecentEmojiPageUpdateState(0);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

            EmojiViewInner v = emojiViewController.getEmojiViewInner();

            int recentEmojiPageUpdateState = emojiViewController.getRecentEmojiPageUpdateState();
            if (recentEmojiPageUpdateState == 1) {
                if (v.emojiTabLastSelectedIndex == 0) {
                    emojiViewController.setRecentEmojiPageUpdateState(2);
                } else {
                    updateRecentEmojis();
                }
            } else if (recentEmojiPageUpdateState == 2 && state == ViewPager2.SCROLL_STATE_SETTLING) {

                if (v.emojiTabLastSelectedIndex != 0) {
                    emojiViewController.setRecentEmojiPageUpdateState(1);
                    v.emojisPager2.post(this::updateRecentEmojis);
                } else {
                    emojiViewController.setRecentEmojiPageUpdateState(1);
                }
            }

        }
    }

    static public class EmojiGridFragment extends Fragment {

        private static final int RECENT_POSITION = 0;
        private static final String ARG_INDEX = "index_of_fragment";
        protected WeakReference<EmojiGridPagerAdapter> parentAdapterWR = null;
        private WeakReference<PopupWindow> relatedWindowWR = null;
        private WeakReference<EmojiViewInner> emojiViewInnerWR;
        //        private static final String ARG_EVC = "emoji_view_controller";
        @SuppressWarnings("unused")
        private int indexOfFragment;

        public static EmojiGridFragment newInstance(int index, EmojiViewInner emojiViewInner, EmojiGridPagerAdapter emojiGridPagerAdapter) {
            EmojiGridFragment fragment = new EmojiGridFragment();
            fragment.emojiViewInnerWR = new WeakReference<>(emojiViewInner);
            fragment.parentAdapterWR = new WeakReference<>(emojiGridPagerAdapter);
            Bundle args = new Bundle();
            args.putInt(ARG_INDEX, index);
            fragment.setArguments(args);
            return fragment;
        }

        public EmojiViewBuildController<?> getEmojiViewBuildController() {
            EmojiViewInner emojiViewInner = emojiViewInnerWR != null ? emojiViewInnerWR.get() : null;

            if (emojiViewInner != null && emojiViewInner.emojiViewController instanceof EmojiViewBuildController)
                return (EmojiViewBuildController<?>) emojiViewInner.emojiViewController;

            return null;
        }

        public static void setEmojiViewBuildController(EmojiViewBuildController<?> emojiViewBuildController) {

        }

        public void destroySelf() {
            // Call onDestroy and remove the fragment
            onDestroy();
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .remove(this)
                        .commit();
            }
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                indexOfFragment = getArguments().getInt(ARG_INDEX);
            }
            // Retain the fragment instance across configuration changes
            setRetainInstance(true);
//            if(emojiViewInnerWR.get()==null){
//                destroySelf();
//            }
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            EmojiGrid v = new EmojiGrid(this.getContext());


            v.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.MATCH_PARENT));
            return v;


        }

        public void prepareView(@NonNull EmojiGrid newView) {

            EmojiViewBuildController<?> emojiViewController = getEmojiViewBuildController();

            if (emojiViewController == null) return;

            Bundle args = getArguments();
            assert args != null;
            int position = args.getInt(ARG_INDEX);

            if (position == 0) {
                emojiViewController.setRecentEmojiGridView(newView);
            }

            IRecentEmoji recentEmoji = emojiViewController.getRecentEmoji();

            if (emojiViewController.hasRecentEmoji() && position == RECENT_POSITION) {
                newView.init(emojiViewController, recentEmoji);
                emojiViewController.setRecentEmojiGridView(newView);
            } else {
                final EmojiCategory[] emojiCategories = EmojiManager.getInstance().getCategories();
                newView.init(emojiViewController,
                        emojiCategories[position - emojiViewController.recentAdapterItemCount()]);
            }


        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            // View is EmojiGrid extended from GridView. So, AbsListView.LayoutParams

            view.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.MATCH_PARENT));

            final EmojiGrid newView = (EmojiGrid) view;

            EmojiViewBuildController<?> emojiViewController = getEmojiViewBuildController();

            if (emojiViewController == null) {
                ViewParent parentView = view.getParent();
                if (parentView instanceof ViewGroup) {

                    ((ViewGroup) parentView).removeView(view);
                }
            } else {
                prepareView(newView);
            }


        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            View v = getView();
            if (v instanceof EmojiGrid) {
                EmojiGrid emojiGrid = (EmojiGrid) v;
                emojiGrid.destroyView();
            }
        }

        public EmojiGrid getGridViewIfFragmentIsRecentEmoji() {

            Bundle args = getArguments();
            if (args != null) {

                int position = args.getInt(ARG_INDEX);

                if (position == RECENT_POSITION) {
                    View v = getView();
                    if (v instanceof EmojiGrid) {
                        EmojiViewBuildController<?> emojiViewController = getEmojiViewBuildController();

                        if (emojiViewController != null) {

                            return (EmojiGrid) v;
                        }
                    }
                }

            }
            return null;

        }

        public void updateRecentEmojiGridView() {

            EmojiViewBuildController<?> emojiViewController = getEmojiViewBuildController();
            EmojiGrid recentEmojiGrid = getGridViewIfFragmentIsRecentEmoji();
            if (recentEmojiGrid != null && emojiViewController != null) {
                emojiViewController.setRecentEmojiGridView(recentEmojiGrid);
            }

        }

        public int getIndex() {

            Bundle args = getArguments();
            if (args != null) {

                int position = args.getInt(ARG_INDEX);

                return position;
            }
            return -1;
        }

        @Override
        public void onAttach(@NonNull Context context) {


//            Log.i("Fragment", "onAttach " +getIndex());


            EmojiViewBuildController<?> emojiViewController = getEmojiViewBuildController();
            if (emojiViewController != null) {
                relatedWindowWR = new WeakReference<>(emojiViewController.getPopupViewWindow());

                EmojiViewInner emojiViewInner = emojiViewController.getEmojiViewInner();
                if (emojiViewInner instanceof EmojiViewExtended) {

                    ((EmojiViewExtended) emojiViewInner).bH.post(this::updateRecentEmojiGridView);
                }
            } else {

                EmojiGridPagerAdapter parentAdapter = parentAdapterWR != null ? parentAdapterWR.get() : null;
                if (parentAdapter != null) {
                    parentAdapter.toggleOFF();
                    parentAdapter.notifyAll();
                }
            }

            super.onAttach(context);


        }

        @Override
        public void onDetach() {


//            Log.i("Fragment", "onDetach" +getIndex());
            super.onDetach();

            EmojiViewBuildController<?> emojiViewController = getEmojiViewBuildController();
            if (emojiViewController != null) {

                EmojiViewInner emojiViewInner = emojiViewController.getEmojiViewInner();
                if (emojiViewInner instanceof EmojiViewExtended) {

                    ((EmojiViewExtended) emojiViewInner).bH.post(this::detachRecentEmojiGrid);
                }

            }
        }

        public void detachRecentEmojiGrid() {

            EmojiViewBuildController<?> emojiViewController = getEmojiViewBuildController();
            EmojiGrid recentEmojiGrid = getGridViewIfFragmentIsRecentEmoji();
            if (recentEmojiGrid != null && emojiViewController != null) {
                emojiViewController.setRecentEmojiGridView(null);
            }
        }
    }

    public class EmojiGridPagerAdapter extends FragmentStateAdapter {

        private final EmojiViewBuildController<?> emojiViewController;

        private final int categoriesLen;
        private final int numOfFragments;
        private int numOfFragmentsCurrent;


        public EmojiGridPagerAdapter(@NonNull FragmentActivity fm, final EmojiViewBuildController<?> emojiViewController) {
            super(fm.getSupportFragmentManager(), fm.getLifecycle());

            this.emojiViewController = emojiViewController;
            categoriesLen = EmojiManager.getInstance().getCategories().length;
            this.numOfFragments = categoriesLen + emojiViewController.recentAdapterItemCount();
            this.numOfFragmentsCurrent = this.numOfFragments;
        }

        public void toggleON() {
            if (numOfFragments > 0) {
                int previousCount = numOfFragmentsCurrent;
                this.numOfFragmentsCurrent = numOfFragments;
                if (previousCount != numOfFragments) {
                    notifyItemRangeInserted(0, numOfFragments);
                } else {
                    notifyItemRangeChanged(0, numOfFragments);
                }
            }
        }

        public void toggleOFF() {
            if (numOfFragments > 0) {
                int previousCount = numOfFragmentsCurrent;
                this.numOfFragmentsCurrent = 0;
                if (previousCount != 0) {
                    notifyItemRangeRemoved(0, numOfFragments);
                } else {
                    notifyItemRangeChanged(0, 0);
                }
            }
        }


        @NonNull
        @Override
        public Fragment createFragment(int position) {
            Fragment fragment = EmojiGridFragment.newInstance(position, EmojiViewInner.this, EmojiGridPagerAdapter.this);

//            fragment.
            return fragment;
        }

        @Override
        public int getItemCount() {
            return numOfFragmentsCurrent;
        }


        int numberOfRecentEmojis() {

            IRecentEmoji recentEmoji = emojiViewController.getRecentEmoji();
            IVariantEmoji variantManager = emojiViewController.getVariantEmoji();

            if (recentEmoji == null) return 0;
            if (!emojiViewController.hasRecentEmoji()) return 0;
            return recentEmoji.getRecentEmojis().size();
        }

        void invalidateRecentEmojis() {

            EmojiGrid recentEmojiGridView = emojiViewController.getRecentEmojiGridView();
//            EmojiGridInner recentEmojiGridView = recentEmojiGridViewWR != null ? recentEmojiGridViewWR.get() : null;
            if (recentEmojiGridView != null) {
                recentEmojiGridView.invalidateEmojis();
            }
        }


        @Override
        public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
            super.onDetachedFromRecyclerView(recyclerView);
        }


        @Override
        public void onBindViewHolder(@NonNull FragmentViewHolder holder, int position, @NonNull List<Object> payloads) {

            if (holder.itemView instanceof EmojiGrid) {
//                Log.i("2323", "455");
            }
            super.onBindViewHolder(holder, position, payloads);
        }

        @Override
        public void onViewDetachedFromWindow(@NonNull FragmentViewHolder holder) {
            if (holder.itemView instanceof EmojiGrid) {
//                Log.i("2323", "234");
            }
            super.onViewDetachedFromWindow(holder);
        }
    }


}
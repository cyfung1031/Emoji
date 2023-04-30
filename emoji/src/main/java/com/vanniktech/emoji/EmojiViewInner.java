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
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.adapter.FragmentViewHolder;
import androidx.viewpager2.widget.ViewPager2;

import com.vanniktech.emoji.emoji.EmojiCategory;
import com.vanniktech.emoji.listeners.RepeatListener;

import java.lang.ref.WeakReference;

public final class EmojiViewInner extends LinearLayout {
    private static final long INITIAL_INTERVAL = 500; //ms
    private static final int NORMAL_INTERVAL = 50; //ms
    @ColorInt
    private int themeAccentColor = 0;
    @ColorInt
    private int themeIconColor = 0;
    private ImageButton[] emojiTabs = null;
//    private EmojiViewInner.EmojiPagerAdapter emojiPagerAdapter = null;
    private EmojiViewInner.EmojiGridPagerAdapter emojiPager2Adapter = null;
    private int emojiTabLastSelectedIndex = -1;
    private EmojiViewController emojiViewController = null;

//    private ViewPager emojisPager;
private ViewPager2 emojisPager2;

boolean isTabButtonSmoothTransitionEnabled = false;

    public EmojiViewInner(final Context context) {
        super(context);
    }

    public EmojiViewInner(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public EmojiViewInner(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void init( @NonNull final EmojiViewBuildController<?> builder) {

        builder.setEmojiViewInner(this);
        emojiViewController = builder;
        EmojiGridFragment.setEmojiViewBuildController(builder);
        isTabButtonSmoothTransitionEnabled = builder.getTabButtonSmoothTransitionEnabled();

        Context context = getContext();

        View.inflate(context, R.layout.emoji_view2, this);
        setOrientation(VERTICAL);

        builder.setEmojiViewInner(this);

        setBackgroundColor(builder.getBackgroundColor(context));
        themeIconColor = builder.getIconColor(context);

        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorAccent, value, true);
        themeAccentColor = builder.getSelectedIconColor(context);

//        emojisPager = findViewById(R.id.emojiViewPager);
        emojisPager2 = findViewById(R.id.emojiViewPager);
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

//        emojiPagerAdapter = new EmojiViewInner.EmojiPagerAdapter(builder);
        emojiPager2Adapter = new EmojiViewInner.EmojiGridPagerAdapter((FragmentActivity) this.getContext(), builder);
        emojiTabs = new ImageButton[builder.recentAdapterItemCount() + categories.length + 1];

        if (builder.hasRecentEmoji()) {
            emojiTabs[0] = inflateButton(context, R.drawable.emoji_recent, R.string.emoji_category_recent, emojisTab);
        }

        for (int i = 0; i < categories.length; i++) {
            emojiTabs[i + builder.recentAdapterItemCount()] = inflateButton(context, categories[i].getIcon(), categories[i].getCategoryName(), emojisTab);
        }

        emojiTabs[emojiTabs.length - 1] = inflateButton(context, R.drawable.emoji_backspace, R.string.emoji_backspace, emojisTab);

//        handleOnClicks(emojisPager);
        handleOnClicks();

//        emojisPager.setAdapter(emojiPagerAdapter);
        emojisPager2.setAdapter(emojiPager2Adapter);
        emojisPager2.setOffscreenPageLimit(1);

//        final int startIndex = builder.hasRecentEmoji() ? emojiPagerAdapter.numberOfRecentEmojis() > 0 ? 0 : 1 : 0;
//        emojisPager.setCurrentItem(startIndex);
//        pageChangeHandler.onPageSelected(startIndex);


        final int startIndex = builder.hasRecentEmoji() ? emojiPager2Adapter.numberOfRecentEmojis() > 0 ? 0 : 1 : 0;
        emojisPager2.setCurrentItem(startIndex, false);
        pageChangeHandler2.onPageSelected(startIndex);

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
            emojiTabs[i].setOnTouchListener(this::onEmojiTabButtonTouched);
        }

        emojiTabs[emojiTabs.length - 1].setOnTouchListener(new RepeatListener(INITIAL_INTERVAL, NORMAL_INTERVAL, this::onBackSpaceToolButtonRepeatedlyClicked));
    }

    public boolean onEmojiTabButtonTouched(View view, MotionEvent me) {

//        final int actionMasked = me.getActionMasked();
//        final int actionIndex = me.getActionIndex();
//
//        final int actionPointerId = me.getPointerId(actionIndex);
//
//        final boolean maskedDown = actionMasked == MotionEvent.ACTION_DOWN || actionMasked == MotionEvent.ACTION_POINTER_DOWN;
//        final boolean maskedCancel = actionMasked == MotionEvent.ACTION_CANCEL || actionMasked == MotionEvent.ACTION_UP || actionMasked == MotionEvent.ACTION_POINTER_UP;
//
//        if(actionMasked == MotionEvent.ACTION_DOWN ){
//            view.post(new Runnable() {
//                @Override
//                public void run() {
//                    emojisPager2.setOffscreenPageLimit(emojiPager2Adapter.getItemCount());
//                }
//            });
//        }else if (maskedCancel && actionMasked != MotionEvent.ACTION_POINTER_UP){
//            view.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//
//                    emojisPager2.setOffscreenPageLimit(1);
//                }
//            }, 80);
//        }

        return false;
    }

    public void onBackSpaceToolButtonRepeatedlyClicked(final View view) {

        emojiViewController.controller(0x3051);
    }

    public void setOnEmojiBackspaceClickListener(@Nullable final EmojiViewController emojiViewController ) {
        this.emojiViewController = emojiViewController;
    }


    private ImageButton inflateButtonInner(Context context ){

        // 1. Create a new ImageButton object
        ImageButton imageButton = new ImageButton(context);

// 2. Set the layout parameters, including width, height, and weight
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.weight = 1;
        imageButton.setLayoutParams(layoutParams);

// 3. Apply the background, padding, and scale type attributes
//        TypedValue outValue = new TypedValue();
//        context.getTheme().resolveAttribute(R.drawable.emoji_bottom_line, outValue, true);
//        imageButton.setBackgroundResource(outValue.resourceId);

        // Create a new ShapeDrawable object with a line shape
//        ShapeDrawable lineDrawable = new ShapeDrawable(new RectShape());
//
//// Set the stroke properties of the line
//        lineDrawable.getPaint().setColor(getResources().getColor(android.R.color.black));
//        lineDrawable.getPaint().setStyle(Paint.Style.STROKE);
//        lineDrawable.getPaint().setStrokeWidth(1);
//
//// Set the bounds of the line to only draw at the bottom
//        int bottomBorderWidth = 2; // The width of the bottom border line
//        lineDrawable.setBounds(0, 0, imageButton.getWidth(), bottomBorderWidth);

//        imageButton.setBackground(lineDrawable);

        imageButton.setBackground(ResourcesCompat.getDrawable( context.getResources(), R.drawable.emoji_bottom_line, null));

        imageButton.setPadding(4, 4, 4, 4);
        imageButton.setScaleType(ImageView.ScaleType.FIT_CENTER);

// 4. Add the new ImageButton to a parent layout (e.g., a LinearLayout)
//        LinearLayout parentLayout = findViewById(R.id.parent_layout); // Replace with the ID of the parent layout in your code
//        parentLayout.addView(imageButton);

        return imageButton;
    }

    private ImageButton inflateButton(final Context context, @DrawableRes final int btnDrawableResId, @StringRes final int categoryName, final ViewGroup parent) {
//        final ImageButton button = (ImageButton) LayoutInflater.from(context).inflate(R.layout.emoji_view_category, parent, false);
        final ImageButton button = inflateButtonInner(context);

        button.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), btnDrawableResId, null));
        button.setColorFilter(themeIconColor, PorterDuff.Mode.SRC_IN);
        button.setContentDescription(context.getString(categoryName));

        parent.addView(button);

        return button;
    }

//    private static class OnPageChangeListener implements  ViewPager.OnPageChangeListener{
    private static class OnPageChangeListener2 extends ViewPager2.OnPageChangeCallback {

        final EmojiViewController emojiViewController;
        OnPageChangeListener2( EmojiViewController emojiViewController){
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
                if (i == 0) {
//                        emojiPagerAdapter.invalidateRecentEmojis();
                }

                final ImageButton[] emojiTabs = v.emojiTabs;

                if (lastSelectedIndex >= 0 && lastSelectedIndex < v.emojiTabs.length) {
                    emojiTabs[lastSelectedIndex].setSelected(false);
                    emojiTabs[lastSelectedIndex].setColorFilter(v.themeIconColor, PorterDuff.Mode.SRC_IN);
                }

                emojiTabs[i].setSelected(true);
                emojiTabs[i].setColorFilter(v.themeAccentColor, PorterDuff.Mode.SRC_IN);

            }
        }

        private void updateRecentEmojis(){
            EmojiViewInner v = emojiViewController.getEmojiViewInner();

            v.emojiPager2Adapter.invalidateRecentEmojis();
            emojiViewController.setRecentEmojiPageUpdateState(0);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

            EmojiViewInner v = emojiViewController.getEmojiViewInner();

            int recentEmojiPageUpdateState = emojiViewController.getRecentEmojiPageUpdateState();
            if(recentEmojiPageUpdateState == 1) {
                if(v.emojiTabLastSelectedIndex == 0) {
                    emojiViewController.setRecentEmojiPageUpdateState(2);
                }else{
                    updateRecentEmojis();
                }
            }else if(recentEmojiPageUpdateState == 2 && state== ViewPager2.SCROLL_STATE_SETTLING){

                if(v.emojiTabLastSelectedIndex != 0) {
                    emojiViewController.setRecentEmojiPageUpdateState(1);
                    v.emojisPager2.post(this::updateRecentEmojis);
                } else {
                    emojiViewController.setRecentEmojiPageUpdateState(1);
                }
            }

        }
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
        public ViewPager2.PageTransformer getPageTransformer(Context context);

        @NonNull
        public RecentEmoji getRecentEmoji();

        @NonNull
        public VariantEmoji getVariantEmoji();


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


    public interface EmojiViewBuildController<T> extends EmojiViewController, EmojiViewBuilder<T>{

    }

//    static class EmojiTabsClickListener implements OnClickListener {
//        private final ViewPager2 emojisPager2;
//        private final int position;
//
//        EmojiTabsClickListener(final ViewPager2 emojisPager2, final int position) {
//            this.emojisPager2 = emojisPager2;
//            this.position = position;
//        }
//
//        @Override
//        public void onClick(final View v) {
//
//
//            emojisPager2.setCurrentItem(position);
//        }
//    }


    static public class EVCR extends WeakReference<EmojiViewBuildController<?>>{

        public EVCR(EmojiViewBuildController<?> referent) {
            super(referent);
        }

    }



    static public class EmojiGridFragment extends Fragment {
        private static final int RECENT_POSITION = 0;
        private static final String ARG_INDEX = "index_of_fragment";
//        private static final String ARG_EVC = "emoji_view_controller";
        private int indexOfFragment;
        private static final EVCR[] emojiViewBuildControllers = new EVCR[]{null};

        public static EmojiGridFragment newInstance(int index) {
            EmojiGridFragment fragment = new EmojiGridFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_INDEX, index);
            fragment.setArguments(args);
            return fragment;
        }

        public static void setEmojiViewBuildController(EmojiViewBuildController<?> emojiViewBuildController){
            emojiViewBuildControllers[0] = new EVCR( emojiViewBuildController);
        }
        public static EmojiViewBuildController<?> getEmojiViewBuildController(){
            EVCR emojiViewBuildControllerWR = emojiViewBuildControllers[0];
            return emojiViewBuildControllerWR!=null ? emojiViewBuildControllerWR.get():null;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                indexOfFragment = getArguments().getInt(ARG_INDEX);
            }
        }



        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            EmojiGridInner v = new EmojiGridInner(this.getContext());

            v.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            return v;


        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

            view.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            //            EmojiGridInner emojiGridView = new EmojiGridInner(getContext());
//            emojiGridView.init(indexOfFragment);

            final EmojiGridInner newView = (EmojiGridInner) view;

            EmojiViewBuildController<?> emojiViewController = getEmojiViewBuildController();

            assert emojiViewController != null;

            Bundle args = getArguments();
            assert args != null;
            int position =args.getInt(ARG_INDEX);


            if(position == 0) {
                emojiViewController.setRecentEmojiGridView(newView);
            }


            RecentEmoji recentEmoji = emojiViewController.getRecentEmoji();
            VariantEmoji variantManager = emojiViewController.getVariantEmoji();

            if (emojiViewController.hasRecentEmoji() && position == RECENT_POSITION) {
                  newView.init(emojiViewController, recentEmoji);
                  emojiViewController.setRecentEmojiGridView(newView);
//                 recentEmojiGridViewWR = new WeakReference<>(newView);
            } else {
                final EmojiCategory[] emojiCategories = EmojiManager.getInstance().getCategories();
                  newView.init(emojiViewController,
                        emojiCategories[position - emojiViewController.recentAdapterItemCount()]);
            }
//            view.requestApplyInsets();

//            pager.addView(newView);

        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            View v = getView();
            if(v instanceof EmojiGridInner){
                EmojiGridInner emojiGridInner = (EmojiGridInner) v;
                EmojiArrayAdapterGeneral adapter = emojiGridInner.emojiArrayAdapterG;
                if(adapter != null){
                    adapter.clear();
                    emojiGridInner.emojiArrayAdapterG = null;
                    emojiGridInner.isRecentEmojiGridView = false;
                }
            }
//            Log.i("WWS", "destory");
        }

        public EmojiGridInner getGridViewIfFragmentIsRecentEmoji(){

            Bundle args = getArguments();
            if(args != null){

                int position =args.getInt(ARG_INDEX);

                if(position == RECENT_POSITION) {
                    View v = getView();
                    if(v instanceof  EmojiGridInner){
                        EmojiViewBuildController<?> emojiViewController = getEmojiViewBuildController();

                        if(emojiViewController != null) {

                            return (EmojiGridInner) v;
                        }
                    }
                }

            }
            return null;

        }

        public void updateRecentEmojiGridView(){

            EmojiViewBuildController<?> emojiViewController = getEmojiViewBuildController();
            EmojiGridInner recentEmojiGrid =getGridViewIfFragmentIsRecentEmoji();
            if(recentEmojiGrid != null && emojiViewController != null){
                emojiViewController.setRecentEmojiGridView( recentEmojiGrid);
            }

        }

        public int getIndex(){

            Bundle args = getArguments();
            if(args != null) {

                int position = args.getInt(ARG_INDEX);

                return position;
            }
            return -1;
        }

        @Override
        public void onAttach(@NonNull Context context) {


//            Log.i("Fragment", "onAttach " +getIndex());


            EmojiViewBuildController<?> emojiViewController = getEmojiViewBuildController();
            if(emojiViewController != null) {
                emojiViewController.getEmojiViewOuter().bH.post(this::updateRecentEmojiGridView);
            }

            super.onAttach(context);



        }

        @Override
        public void onDetach() {


//            Log.i("Fragment", "onDetach" +getIndex());
            super.onDetach();

            EmojiViewBuildController<?> emojiViewController = getEmojiViewBuildController();
            if(emojiViewController != null) {
                emojiViewController.getEmojiViewOuter().bH.post(this::detachRecentEmojiGrid);
            }
        }

        public void detachRecentEmojiGrid(){

            EmojiViewBuildController<?> emojiViewController = getEmojiViewBuildController();
            EmojiGridInner recentEmojiGrid =getGridViewIfFragmentIsRecentEmoji();
            if(recentEmojiGrid != null && emojiViewController != null){
                emojiViewController.setRecentEmojiGridView( null);
            }
        }
    }


    public class EmojiGridPagerAdapter extends FragmentStateAdapter {
        private static final int RECENT_POSITION = 0;

        private final EmojiViewBuildController<?> emojiViewController;

//        private WeakReference<EmojiGridInner> recentEmojiGridViewWR = null;

        private final int categoriesLen;
        private final int numOfFragments;

        public EmojiGridPagerAdapter(@NonNull FragmentActivity fragmentActivity, final EmojiViewBuildController<?> emojiViewController) {
            super(fragmentActivity);

            this.emojiViewController =emojiViewController;
            categoriesLen = EmojiManager.getInstance().getCategories().length;
            this.numOfFragments = categoriesLen + emojiViewController.recentAdapterItemCount();
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return EmojiGridFragment.newInstance(position);
        }

        @Override
        public int getItemCount() {
            return numOfFragments;
        }


        int numberOfRecentEmojis() {

            RecentEmoji recentEmoji = emojiViewController.getRecentEmoji();
            VariantEmoji variantManager = emojiViewController.getVariantEmoji();

            if (recentEmoji == null) return 0;
            if (!emojiViewController.hasRecentEmoji()) return 0;
            return recentEmoji.getRecentEmojis().size();
        }

        void invalidateRecentEmojis() {

            EmojiGridInner recentEmojiGridView = emojiViewController.getRecentEmojiGridView();
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
        public void onViewDetachedFromWindow(@NonNull FragmentViewHolder holder) {
            super.onViewDetachedFromWindow(holder);
        }
    }

    static public final class EmojiPagerAdapter extends PagerAdapter {
        private static final int RECENT_POSITION = 0;

        private final EmojiViewBuildController<?> emojiViewController;

        private WeakReference<EmojiGridInner> recentEmojiGridViewWR = null;

        private final int categoriesLen;

        public EmojiPagerAdapter(
                final EmojiViewBuildController<?> emojiViewController
        ) {
            this.emojiViewController =emojiViewController;
            categoriesLen = EmojiManager.getInstance().getCategories().length;
        }


        @Override
        public int getCount() {
            return categoriesLen + emojiViewController.recentAdapterItemCount();
        }

        @Override
        @NonNull
        public Object instantiateItem(@NonNull final ViewGroup pager, final int position) {
            final EmojiGridInner newView;


            RecentEmoji recentEmoji = emojiViewController.getRecentEmoji();
            VariantEmoji variantManager = emojiViewController.getVariantEmoji();

            if (emojiViewController.hasRecentEmoji() && position == RECENT_POSITION) {
                newView = new EmojiGridInner(pager.getContext()).init(emojiViewController, recentEmoji);
                recentEmojiGridViewWR = new WeakReference<>(newView);
            } else {
                final EmojiCategory[] emojiCategories = EmojiManager.getInstance().getCategories();
                newView = new EmojiGridInner(pager.getContext()).init(emojiViewController,
                        emojiCategories[position - emojiViewController.recentAdapterItemCount()]);
            }

            pager.addView(newView);
            return newView;
        }


        @Override
        public void destroyItem(final ViewGroup pager, final int position, @NonNull final Object view) {
            pager.removeView((View) view);
            if(view instanceof  EmojiGridInner){
                ((EmojiGridInner) view).destroyItem();
            }

            if (emojiViewController.hasRecentEmoji() && position == RECENT_POSITION) {
                recentEmojiGridViewWR = null;
            }
        }

        @Override
        public boolean isViewFromObject(final View view, @NonNull final Object object) {
            return view.equals(object);
        }

        int numberOfRecentEmojis() {

            RecentEmoji recentEmoji = emojiViewController.getRecentEmoji();
            VariantEmoji variantManager = emojiViewController.getVariantEmoji();

            if (recentEmoji == null) return 0;
            if (!emojiViewController.hasRecentEmoji()) return 0;
            return recentEmoji.getRecentEmojis().size();
        }

        void invalidateRecentEmojis() {
            EmojiGridInner recentEmojiGridView = recentEmojiGridViewWR != null ? recentEmojiGridViewWR.get() : null;
            if (recentEmojiGridView != null) {
                recentEmojiGridView.invalidateEmojis();
            }
        }
    }

}
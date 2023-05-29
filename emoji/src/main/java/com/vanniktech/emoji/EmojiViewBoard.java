package com.vanniktech.emoji;

import static android.view.View.MeasureSpec.makeMeasureSpec;
import static com.vanniktech.emoji.Utils.asListWithoutDuplicates;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.adapter.FragmentViewHolder;
import androidx.viewpager2.widget.ViewPager2;

import com.vanniktech.emoji.emoji.Emoji;
import com.vanniktech.emoji.emoji.EmojiCategory;
import com.vanniktech.emoji.listeners.RepeatListener;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class EmojiViewBoard {
    private static final long INITIAL_INTERVAL = 500; //ms
    private static final int NORMAL_INTERVAL = 50; //ms
    final HandlerThread bHt = createBackgroundThread();
    final Handler bH = new Handler(bHt.getLooper());
    public WeakReference<EmojiImageView.EmojiImageViewG> popupRootImageView = null;
    public WeakReference<Emoji> popupVariant = null;
    @ColorInt
    protected int themeAccentColor = 0;
    @ColorInt
    protected int themeIconColor = 0;
    protected AppCompatImageView[] emojiTabs = null;
    protected EmojiViewBoard.EmojiGridPagerAdapter emojiPager2Adapter = null;
    protected int emojiTabLastSelectedIndex = -1;
    protected ViewPager2 emojisPager2;
    IRecentEmoji recentEmoji = null;
    IVariantEmoji variantEmoji = null;
    @Nullable
    EmojiVariantPopup variantPopup = null;
    int recentEmojiPageUpdateState = 0;
    WeakReference<EmojiGrid> recentEmojiGridViewWR = null;
    @Nullable
    ViewPager2.PageTransformer pageTransformer2;
    boolean isTabButtonSmoothTransitionEnabled = false;
    boolean disAllowParentVerticalScroll = false;
    private WeakReference<LinearLayout> frameViewWR = null;
    private WeakReference<Context> mContextWR = null;
    @Nullable
    private WeakReference<View> containerViewWR = null;

    public EmojiViewBoard(Context context) {
        mContextWR = new WeakReference<>(context);
    }

    private static HandlerThread createBackgroundThread() {
        HandlerThread ht = new HandlerThread("backgroundWorkOnEmojiView");
        ht.start();
        return ht;
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

    public LinearLayout getFrameView() {
        return frameViewWR != null ? frameViewWR.get() : null;
    }

    public void replaceView(View viewToReplace) {
        Activity activity = (Activity) viewToReplace.getContext();
        if (activity == null) return;
        View replacementView = getFrameView();

        ViewGroup parentView = (ViewGroup) viewToReplace.getParent();
        int index = parentView.indexOfChild(viewToReplace);
        parentView.removeView(viewToReplace);
        parentView.addView(replacementView, index);

    }

    public void fillUpVerticalLinearView() {
        LinearLayout frameView = getFrameView();
        if (frameView == null) return;

        frameView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1
        ));
    }

    public int deleteOneUniChar() {
        // TODO
        return 0; // otherwise, tell task number to create following task
    }

    public int commitOneEmoji(Emoji emoji) {
        // TODO
        return 0; // otherwise, tell task number to create following task
    }

    @Nullable
    public Context getContext() {
        return mContextWR != null ? mContextWR.get() : null;
    }

    public LinearLayout createFrameView() {
        Context context = getContext();
        assert context != null;
        LinearLayout frameView = new LinearLayout(context);
        frameViewWR = new WeakReference<>(frameView);
        return frameView;
    }

    public void preSetup() {
        // TODO
    }

    public void controller(int msgId) {
        EmojiImageView.EmojiImageViewG popupRootImageView = this.popupRootImageView != null ? this.popupRootImageView.get() : null;
        Emoji popupVariant = this.popupVariant != null ? this.popupVariant.get() : null;
        switch (msgId) {
            case 0x3041:
            case 0x3042:

                if (popupRootImageView != null && popupVariant != null) {
                    this.onEmojiClick(popupRootImageView, popupVariant);
                }
                break;

            case 0x6042:


                if (popupRootImageView != null && popupVariant != null) {
                    this.onEmojiLongClick(popupRootImageView, popupVariant);
                }

                break;

            case 0x3051:

                this.onEmojiBackspaceClicked(null);


                break;

        }

    }

    public void setPopupRootImageView(EmojiImageView.EmojiImageViewG rootImageView) {
        popupRootImageView = new WeakReference<>(rootImageView);

    }

    public void setPopupVariant(Emoji variant) {
        popupVariant = new WeakReference<>(variant);

    }

    public int getRecentEmojiPageUpdateState() {
        return recentEmojiPageUpdateState;
    }

    public void setRecentEmojiPageUpdateState(int state) {
        recentEmojiPageUpdateState = state;
    }

    public EmojiGrid getRecentEmojiGridView() {
        return recentEmojiGridViewWR != null ? recentEmojiGridViewWR.get() : null;
    }

    public void setRecentEmojiGridView(EmojiGrid newView) {
        recentEmojiGridViewWR = new WeakReference<>(newView);

    }

    @ColorInt
    public int getBackgroundColor(Context context) {

        return Utils.resolveColor(context, R.attr.emojiBackgroundColor, R.color.emoji_background_color);

    }

    @ColorInt
    public int getIconColor(Context context) {

//            return Utils.resolveColor(context, R.attr.emojiPrimaryColor, R.color.emoji_primary_color);


//            final TypedValue value = new TypedValue();
//            context.getTheme().resolveAttribute(R.attr.emojiIconsToolNormal , value, true);
//            return value.data;

        return Utils.resolveColor(context, R.attr.emojiIconsToolNormal, R.color.emoji_tool_color_normal);

    }

    @ColorInt
    public int getSelectedIconColor(Context context) {


//            final TypedValue value = new TypedValue();
//            context.getTheme().resolveAttribute(R.attr.colorAccent, value, true);
//            return value.data;


//            final TypedValue value = new TypedValue();
//            context.getTheme().resolveAttribute(R.attr.emojiIconsToolSelected , value, true);
//            return value.data;


        return Utils.resolveColor(context, R.attr.emojiIconsToolSelected, R.color.emoji_tool_color_selected);

    }

    @ColorInt
    public int getDividerColor(Context context) {

        return Utils.resolveColor(context, R.attr.emojiDividerColor, R.color.emoji_divider_color);
    }

    @Nullable
    public ViewPager2.PageTransformer getPageTransformer(Context context) {
        return pageTransformer2;
    }

    public void setPageTransformer(@Nullable ViewPager2.PageTransformer transformer) {
        pageTransformer2 = transformer;
    }

    public IRecentEmoji getRecentEmoji() {
        return recentEmoji;
    }

    public IVariantEmoji getVariantEmoji() {
        return variantEmoji;
    }

    public int recentAdapterItemCount() {
        return hasRecentEmoji() ? 1 : 0;
    }

    public boolean hasRecentEmoji() {
        return !(recentEmoji instanceof NoRecentEmoji);
    }

    public int numberOfRecentEmojis() {
        if (recentEmoji == null) return 0;
        if (!hasRecentEmoji()) return 0;
        return recentEmoji.getRecentEmojis().size();
    }

    public int getGridWidth(Context context) {
        return context.getResources().getDimensionPixelSize(R.dimen.emoji_grid_view_column_width);
    }

    public int getGridPadding(Context context) {
        return context.getResources().getDimensionPixelSize(R.dimen.emoji_grid_view_1dp) - 1;
    }

    public int getEmojiPadding(Context context) {
        return 9;
    }

    public int getEmojiWidthAdjust(Context context) {
        return 0;
    }

    public int getEmojiHeightAdjust(Context context) {
        return -24;
    }

    public boolean getTabButtonSmoothTransitionEnabled() {
        return true;
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
        this.controller(0x3051);
    }

    public boolean isDisAllowParentVerticalScroll() {
        return disAllowParentVerticalScroll;
    }

    public void setDisAllowParentVerticalScroll(boolean disAllowParentVerticalScroll) {
        this.disAllowParentVerticalScroll = disAllowParentVerticalScroll;
    }

    public void init() {


        isTabButtonSmoothTransitionEnabled = this.getTabButtonSmoothTransitionEnabled();

        Context context = getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        LinearLayout frameView = getFrameView();

        inflater.inflate(R.layout.emoji_view2, frameView, true);
        frameView.setOrientation(LinearLayout.VERTICAL);

        frameView.setBackgroundColor(getBackgroundColor(context));


        themeIconColor = getIconColor(context);

        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorAccent, value, true);
        themeAccentColor = getSelectedIconColor(context);

//        emojisPager = findViewById(R.id.emojiViewPager);

        emojisPager2 = frameView.findViewById(R.id.emojiViewPager);
        emojisPager2.setSaveEnabled(false);
        final View emojiDividerColor = frameView.findViewById(R.id.emojiViewDivider);
        emojiDividerColor.setBackgroundColor(getDividerColor(context));

        ViewPager2.PageTransformer pt = getPageTransformer(context);
        if (pt != null) {
            emojisPager2.setPageTransformer(pt);
        }

        final LinearLayout emojisTab = frameView.findViewById(R.id.emojiViewTab);
//        final ViewPager.OnPageChangeListener pageChangeHandler = new OnPageChangeListener(emojiViewController);
//        emojisPager.addOnPageChangeListener(pageChangeHandler);


        final ViewPager2.OnPageChangeCallback pageChangeHandler2 = new EmojiViewBoard.OnPageChangeListener2(this);
        emojisPager2.registerOnPageChangeCallback(pageChangeHandler2);

        final EmojiCategory[] categories = EmojiManager.getInstance().getCategories();

        emojiPager2Adapter = new EmojiViewBoard.EmojiGridPagerAdapter((FragmentActivity) this.getContext(), this);
        emojiTabs = new AppCompatImageView[recentAdapterItemCount() + categories.length + 1];

        if (hasRecentEmoji()) {
            emojiTabs[0] = inflateButton(context, R.drawable.emoji_recent, R.string.emoji_category_recent, emojisTab);
        }

        for (int i = 0; i < categories.length; i++) {
            emojiTabs[i + recentAdapterItemCount()] = inflateButton(context, categories[i].getIcon(), categories[i].getCategoryName(), emojisTab);
        }

        emojiTabs[emojiTabs.length - 1] = inflateButton(context, R.drawable.emoji_backspace, R.string.emoji_backspace, emojisTab);

        handleOnClicks();

        emojisPager2.setAdapter(emojiPager2Adapter);
        emojisPager2.setOffscreenPageLimit(1);


        final int startIndex = hasRecentEmoji() ? emojiPager2Adapter.numberOfRecentEmojis() > 0 ? 0 : 1 : 0;
        emojisPager2.setCurrentItem(startIndex, false);
        pageChangeHandler2.onPageSelected(startIndex);

    }

    public void executeTask(int nextTaskNum) {
        // TODO
    }

    public void onEmojiBackspaceClicked(final View v) {

        int nextTaskNum = deleteOneUniChar();

        executeTask(nextTaskNum);

//        backspace(editText);
    }

    public void onEmojiLongClick(@NonNull final EmojiImageView.EmojiImageViewG view, @NonNull final Emoji emoji) {

        if (variantPopup != null) {
            variantPopup.show(this, view, emoji);
        }
    }

    public void onEmojiClick(@NonNull final EmojiImageView.EmojiImageViewG imageView, @NonNull final Emoji emoji) {

        int nextTaskNum = commitOneEmoji(emoji);

        executeTask(nextTaskNum);


        if (variantPopup != null) {
            variantPopup.dismiss();
        }
        this.backgroundUpdateRecentEmoji(imageView, emoji);
    }

    public void backgroundUpdateRecentEmoji(@NonNull final EmojiImageView.EmojiImageViewG imageView, @NonNull final Emoji emoji) {

        final EmojiViewBoard emojiViewBoard = this;
        bH.post(new Runnable() {
            @Override
            public void run() {

                recentEmoji.addEmoji(emoji);
                variantEmoji.addVariant(emoji);
                imageView.updateEmoji(emoji);

                recentEmoji.persist();
                variantEmoji.persist();

                emojiViewBoard.setRecentEmojiPageUpdateState(1);
            }
        });
    }

    @Nullable
    public View getContainerRoot() {
        View container = this.getContainer();
        return container != null ? container.getRootView() : null;
    }

    @Nullable
    public View getContainer() {
        return containerViewWR != null ? containerViewWR.get() : null;
    }

    public void setContainerView(final @NonNull View containerView) {

        this.containerViewWR = new WeakReference<>(containerView);
    }

    public void setup(final @NonNull View containerView) {


        setContainerView(containerView);

        setup();

    }


    public void setup() {


        final Context context = getContext();
        assert context != null;

        this.recentEmoji = new RecentEmojiManagerV8(context);
        this.variantEmoji = new VariantEmojiManager(context);


        variantPopup = new EmojiViewBoard.EmojiVariantPopup();
        preSetup();
        this.init();

//        this.setOnEmojiBackspaceClickListener(emojiViewController);


    }


    //    private static class OnPageChangeListener implements  ViewPager.OnPageChangeListener{
    public static class OnPageChangeListener2 extends ViewPager2.OnPageChangeCallback {

        final EmojiViewBoard emojiViewBoard;

        OnPageChangeListener2(EmojiViewBoard emojiViewBoard) {
            this.emojiViewBoard = emojiViewBoard;
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            // No-op.
        }


        @Override
        public void onPageSelected(final int i) {

            final EmojiViewBoard v = this.emojiViewBoard;

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
            final EmojiViewBoard v = this.emojiViewBoard;

            v.emojiPager2Adapter.invalidateRecentEmojis();
            v.setRecentEmojiPageUpdateState(0);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            final EmojiViewBoard v = this.emojiViewBoard;

            int recentEmojiPageUpdateState = v.getRecentEmojiPageUpdateState();
            if (recentEmojiPageUpdateState == 1) {
                if (v.emojiTabLastSelectedIndex == 0) {
                    v.setRecentEmojiPageUpdateState(2);
                } else {
                    updateRecentEmojis();
                }
            } else if (recentEmojiPageUpdateState == 2 && state == ViewPager2.SCROLL_STATE_SETTLING) {

                if (v.emojiTabLastSelectedIndex != 0) {
                    v.setRecentEmojiPageUpdateState(1);
                    v.emojisPager2.post(this::updateRecentEmojis);
                } else {
                    v.setRecentEmojiPageUpdateState(1);
                }
            }

        }
    }


    static public class EmojiGridFragment extends Fragment {

        private static final int RECENT_POSITION = 0;
        private static final String ARG_INDEX = "index_of_fragment";
        protected WeakReference<EmojiViewBoard.EmojiGridPagerAdapter> parentAdapterWR = null;
        private WeakReference<EmojiViewBoard> emojiViewBoardWR = null;
        //        private static final String ARG_EVC = "emoji_view_controller";
        @SuppressWarnings("unused")
        private int indexOfFragment;

        public static EmojiViewBoard.EmojiGridFragment newInstance(int index, EmojiViewBoard emojiViewBoard, EmojiViewBoard.EmojiGridPagerAdapter emojiGridPagerAdapter) {
            EmojiViewBoard.EmojiGridFragment fragment = new EmojiViewBoard.EmojiGridFragment();
            fragment.emojiViewBoardWR = new WeakReference<>(emojiViewBoard);
            fragment.parentAdapterWR = new WeakReference<>(emojiGridPagerAdapter);
            Bundle args = new Bundle();
            args.putInt(ARG_INDEX, index);
            fragment.setArguments(args);
            return fragment;
        }

        public EmojiViewBoard getEmojiViewBoard() {
            EmojiViewBoard emojiViewBoard = emojiViewBoardWR != null ? emojiViewBoardWR.get() : null;

            return emojiViewBoard;
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
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            EmojiGrid v = new EmojiGrid(this.getContext());


            v.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.MATCH_PARENT));
            return v;


        }

        public void prepareView(@NonNull EmojiGrid newView) {

            EmojiViewBoard emojiViewBoard = getEmojiViewBoard();

            if (emojiViewBoard == null) return;

            Bundle args = getArguments();
            assert args != null;
            int position = args.getInt(ARG_INDEX);

            if (position == 0) {
                emojiViewBoard.setRecentEmojiGridView(newView);
            }

            IRecentEmoji recentEmoji = emojiViewBoard.getRecentEmoji();

            if (emojiViewBoard.hasRecentEmoji() && position == RECENT_POSITION) {
                newView.init(emojiViewBoard, recentEmoji);
                emojiViewBoard.setRecentEmojiGridView(newView);
            } else {
                final EmojiCategory[] emojiCategories = EmojiManager.getInstance().getCategories();
                newView.init(emojiViewBoard,
                        emojiCategories[position - emojiViewBoard.recentAdapterItemCount()]);
            }


        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            // View is EmojiGrid extended from GridView. So, AbsListView.LayoutParams

            view.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.MATCH_PARENT));

            final EmojiGrid newView = (EmojiGrid) view;

            EmojiViewBoard emojiViewBoard = getEmojiViewBoard();

            if (emojiViewBoard == null) {
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
                        EmojiViewBoard emojiViewBoard = getEmojiViewBoard();

                        if (emojiViewBoard != null) {

                            return (EmojiGrid) v;
                        }
                    }
                }

            }
            return null;

        }

        public void updateRecentEmojiGridView() {

            EmojiViewBoard emojiViewBoard = getEmojiViewBoard();
            EmojiGrid recentEmojiGrid = getGridViewIfFragmentIsRecentEmoji();
            if (recentEmojiGrid != null && emojiViewBoard != null) {
                emojiViewBoard.setRecentEmojiGridView(recentEmojiGrid);
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

            EmojiViewBoard emojiViewBoard = getEmojiViewBoard();
            if (emojiViewBoard != null) {


                emojiViewBoard.bH.post(this::updateRecentEmojiGridView);

            } else {

                EmojiViewBoard.EmojiGridPagerAdapter parentAdapter = parentAdapterWR != null ? parentAdapterWR.get() : null;
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

            EmojiViewBoard emojiViewBoard = getEmojiViewBoard();
            if (emojiViewBoard != null) {

                emojiViewBoard.bH.post(this::detachRecentEmojiGrid);

            }
        }

        public void detachRecentEmojiGrid() {

            EmojiViewBoard emojiViewBoard = getEmojiViewBoard();
            EmojiGrid recentEmojiGrid = getGridViewIfFragmentIsRecentEmoji();
            if (recentEmojiGrid != null && emojiViewBoard != null) {
                emojiViewBoard.setRecentEmojiGridView(null);
            }
        }
    }

    public static class EmojiGrid extends GridView {
        static final int UPDATE_ON_ANY = 1;
        static final int UPDATE_ON_CHANGED_FROM_EXTERNAL_ONLY = 2;
        protected EmojiViewBoard.EmojiGrid.EmojiArrayAdapterGeneral emojiArrayAdapterG = null;
        boolean isRecentEmojiGridView = false;
        private EmojiViewBoard emojiViewBoard = null;

        EmojiGrid(final Context context) {
            super(context);
        }

        boolean clearAdapter() {
            EmojiViewBoard.EmojiGrid.EmojiArrayAdapterGeneral adapter = this.emojiArrayAdapterG;
            if (adapter != null) {
                adapter.clear();
                return true;
            }
            return false;
        }

        public void destroyView() {

            EmojiViewBoard.EmojiGrid emojiGrid = (EmojiViewBoard.EmojiGrid) this;
            boolean isAdapterValid = emojiGrid.clearAdapter();
            if (isAdapterValid) {
                emojiGrid.emojiArrayAdapterG = null;
                emojiGrid.isRecentEmojiGridView = false;
            }
        }

        private void initInner(@NonNull EmojiViewBoard emojiViewBoard,
                               @NonNull Emoji[] emojis) {

            this.emojiViewBoard = emojiViewBoard;

            // ------------------------------------


            final Resources resources = getResources();
//        final int width = resources.getDimensionPixelSize(R.dimen.emoji_grid_view_column_width);
//        final int spacing = resources.getDimensionPixelSize(R.dimen.emoji_grid_view_spacing);

            int gridWidth = emojiViewBoard.getGridWidth(getContext());
            int gridPadding = emojiViewBoard.getGridPadding(getContext());
            final int width = gridWidth + 2 * gridPadding;
            final int spacing = 0;
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


            EmojiViewBoard.EmojiGrid.EmojiArrayAdapterGeneral emojiArrayAdapter = new EmojiViewBoard.EmojiGrid.EmojiArrayAdapterGeneral(getContext(), emojis, emojiViewBoard);

//        emojiArrayAdapterWR =new WeakReference<>(emojiArrayAdapter);
            emojiArrayAdapterG = emojiArrayAdapter;

            setAdapter(emojiArrayAdapter);
        }

        public EmojiViewBoard.EmojiGrid init(@NonNull final EmojiViewBoard emojiViewBoard,
                                             @NonNull final IRecentEmoji recentEmoji) {

            this.emojiViewBoard = emojiViewBoard;

            isRecentEmojiGridView = true;


            final Collection<Emoji> emojis = recentEmoji.getRecentEmojis();
            initInner(emojiViewBoard, emojis.toArray(new Emoji[0]));

            return this;
        }

        public EmojiViewBoard.EmojiGrid init(@NonNull final EmojiViewBoard emojiViewBoard,
                                             @NonNull final EmojiCategory category) {
            isRecentEmojiGridView = false;

            initInner(emojiViewBoard, category.getEmojis());

            return this;
        }

        public void invalidateEmojis(int updateOn) {

            if (isRecentEmojiGridView) {

                EmojiViewBoard.EmojiGrid.EmojiArrayAdapterGeneral emojiArrayAdapter = emojiArrayAdapterG;
                IRecentEmoji recentEmojis = emojiViewBoard.recentEmoji;

                if (emojiArrayAdapter == null || recentEmojis == null) return;

                boolean updateFromExternal = recentEmojis.isUpdatedExternally();
                if (updateFromExternal) {
                    recentEmojis.clear(); // clear the emojis and force reload.
                }
                boolean update = false;
                switch (updateOn) {
                    case UPDATE_ON_ANY:
                        update = true;
                        break;
                    case UPDATE_ON_CHANGED_FROM_EXTERNAL_ONLY:
                        update = updateFromExternal;
                        break;
                }
                if (update) {
                    emojiArrayAdapter.updateEmojis(recentEmojis.getRecentEmojis());
                }


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

            if (isRecentEmojiGridView) {

                invalidateEmojis(UPDATE_ON_CHANGED_FROM_EXTERNAL_ONLY); // update according to the database
            }

            EmojiViewBoard.EmojiGrid.EmojiArrayAdapterGeneral emojiArrayAdapter = emojiArrayAdapterG;
            if (emojiArrayAdapter != null) {
                setAdapter(emojiArrayAdapter);
            }
        }


        @Override
        protected void onDraw(Canvas canvas) {

            EmojiViewBoard.EmojiGrid.EmojiArrayAdapterGeneral emojiArrayAdapter = emojiArrayAdapterG;
            if (emojiArrayAdapter == null) return;
            super.onDraw(canvas);
        }

        public void destroyItem() {
            emojiArrayAdapterG.clear();
            emojiArrayAdapterG = null;
            isRecentEmojiGridView = false;


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

        private boolean isDisAllowParentVerticalScroll() {

            boolean isDisAllowParentVerticalScroll = false;
            if (emojiViewBoard != null) {
                isDisAllowParentVerticalScroll = emojiViewBoard.isDisAllowParentVerticalScroll();

            }
            return isDisAllowParentVerticalScroll;
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            // Intercept touch events to prevent the parent view from scrolling when
            // the user is scrolling the GridView
            boolean intercepted = super.onInterceptTouchEvent(ev);
//        Log.i("EmojiGridInner", intercepted +"|"+ canScrollVertically(-1) +" | "+ canScrollVertically(1));
            if (intercepted) {

                if (isDisAllowParentVerticalScroll() || canScrollVertically(-1) || canScrollVertically(1)) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                    return true;
                }
            }
            return intercepted;
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev) {
//        requestFocusFromTouch();
            return super.onTouchEvent(ev);
        }

        public final static class EmojiArrayAdapterGeneral extends ArrayAdapter<Emoji> {
            @Nullable
            private final IVariantEmoji variantManager;

            @NonNull
            private final EmojiViewBoard emojiViewBoard;

            EmojiArrayAdapterGeneral(@NonNull final Context context, @NonNull final Emoji[] emojis,
                                     @NonNull final EmojiViewBoard emojiViewBoard) {
                super(context, 0, asListWithoutDuplicates(emojis));

                this.variantManager = emojiViewBoard.getVariantEmoji();
                this.emojiViewBoard = emojiViewBoard;
            }

            public static EmojiImageView.EmojiImageViewG createEmojiImageView(Context context) {
                EmojiImageView.EmojiImageViewG emojiImageView = new EmojiImageView.EmojiImageViewG(context);

                // for API 16, AbsListView instead ViewGroup. It is because it is under GridView.

                // Set layout parameters
                AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(
                        AbsListView.LayoutParams.MATCH_PARENT,
                        AbsListView.LayoutParams.WRAP_CONTENT);
                emojiImageView.setLayoutParams(layoutParams);

                // Set background
//    TypedValue typedValue = new TypedValue();
//    context.getTheme().resolveAttribute( Utils.getSelectableBackgroundResId(), typedValue, true);
//    emojiImageView.setBackgroundResource(typedValue.resourceId);

                emojiImageView.setBackgroundResource(0);

                emojiImageView.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.emoji_normal, null));


                // Set padding
//    int paddingInPixels = (int) TypedValue.applyDimension(
//            TypedValue.COMPLEX_UNIT_DIP,
//            2,
//            context.getResources().getDisplayMetrics());
//    emojiImageView.setPadding(paddingInPixels, paddingInPixels, paddingInPixels, paddingInPixels);

                return emojiImageView;
            }

            @Override
            @NonNull
            public View getView(final int position, final View convertView, @NonNull final ViewGroup parent) {
                EmojiImageView.EmojiImageViewG image = (EmojiImageView.EmojiImageViewG) convertView;

                final Context context = getContext();

                if (image == null) {
                    image = (EmojiImageView.EmojiImageViewG) createEmojiImageView(context);
                    image.init(this.emojiViewBoard);
                }

                final Emoji emoji = Objects.requireNonNull(getItem(position), "emoji == null");
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
    }

    public static class EmojiGridPagerAdapter extends FragmentStateAdapter {

        private final EmojiViewBoard mEmojiViewBoard;

        private final int categoriesLen;
        private final int numOfFragments;
        private int numOfFragmentsCurrent;


        public EmojiGridPagerAdapter(@NonNull FragmentActivity fm, final EmojiViewBoard emojiViewBoard) {
            super(fm.getSupportFragmentManager(), fm.getLifecycle());

            this.mEmojiViewBoard = emojiViewBoard;
            categoriesLen = EmojiManager.getInstance().getCategories().length;
            this.numOfFragments = categoriesLen + emojiViewBoard.recentAdapterItemCount();
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

//            fragment.
            return EmojiGridFragment.newInstance(position, this.mEmojiViewBoard, this);
        }

        @Override
        public int getItemCount() {
            return numOfFragmentsCurrent;
        }


        int numberOfRecentEmojis() {
            final EmojiViewBoard emojiViewBoard = this.mEmojiViewBoard;

            IRecentEmoji recentEmoji = emojiViewBoard.getRecentEmoji();
            IVariantEmoji variantManager = emojiViewBoard.getVariantEmoji();

            if (recentEmoji == null) return 0;
            if (!emojiViewBoard.hasRecentEmoji()) return 0;
            return recentEmoji.getRecentEmojis().size();
        }

        void invalidateRecentEmojis() {
            final EmojiViewBoard emojiViewBoard = this.mEmojiViewBoard;

            EmojiGrid recentEmojiGridView = emojiViewBoard.getRecentEmojiGridView();
//            EmojiGridInner recentEmojiGridView = recentEmojiGridViewWR != null ? recentEmojiGridViewWR.get() : null;
            if (recentEmojiGridView != null) {
                recentEmojiGridView.invalidateEmojis(EmojiGrid.UPDATE_ON_ANY);
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


    static public final class EmojiVariantPopup {
        private static final int MARGIN = 2;
        @Nullable
        private PopupWindow popupWindow = null;

        public EmojiVariantPopup() {
        }

        static EmojiImageView.EmojiImageViewP createImageBtn(Context context) {
            EmojiImageView.EmojiImageViewP result = new EmojiImageView.EmojiImageViewP(context);

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

        public void show(@NonNull final EmojiViewBoard emojiViewBoard, @NonNull final EmojiImageView.EmojiImageViewG clickedImage, @NonNull final Emoji emoji) {
            dismiss();


            View rootView = emojiViewBoard.getContainerRoot();
            if (rootView == null) return;
            @NonNull final Context context = clickedImage.getContext();
            final int width = clickedImage.getWidth();


            LayoutInflater inflater = LayoutInflater.from(context);
            final View viewContent = inflater.inflate(R.layout.emoji_popup_window_skin, (ViewGroup) null);
            final LinearLayout imageContainer = viewContent.findViewById(R.id.emojiPopupWindowSkinPopupContainer);

            final List<Emoji> variants = emoji.getBase().getVariants();
            variants.add(0, emoji.getBase());

            for (final Emoji variant : variants) {
                final EmojiImageView.EmojiImageViewP emojiImage = createImageBtn(context);
                final ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) emojiImage.getLayoutParams();
                final int margin = Utils.dpToPx(context, MARGIN);

                // Use the same size for Emojis as in the picker.
                layoutParams.width = width;
                layoutParams.setMargins(margin, margin, margin, margin);
                emojiImage.setImageDrawable(variant.getDrawable(context));

                emojiImage.setOnClickListener(emojiViewBoard, clickedImage, variant);

                imageContainer.addView(emojiImage);
            }

            popupWindow = new PopupWindow(viewContent, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
            popupWindow.setFocusable(true);
            popupWindow.setOutsideTouchable(true);
            popupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NOT_NEEDED);
            popupWindow.setBackgroundDrawable(new BitmapDrawable(context.getResources(), (Bitmap) null));

            viewContent.measure(makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

            final Point location = Utils.locationOnScreen(clickedImage);
            final Point desiredLocation = new Point(
                    location.x - viewContent.getMeasuredWidth() / 2 + width / 2,
                    location.y - viewContent.getMeasuredHeight()
            );
            popupWindow.showAtLocation(rootView, Gravity.NO_GRAVITY, desiredLocation.x, desiredLocation.y);
            clickedImage.getParent().requestDisallowInterceptTouchEvent(true);
            Utils.fixPopupLocation(popupWindow, desiredLocation);
        }

        public void dismiss() {

            if (popupWindow != null) {
                popupWindow.dismiss();
                popupWindow = null;
            }
        }

    }


}

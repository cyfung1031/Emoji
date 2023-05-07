package com.vanniktech.emoji;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager2.widget.ViewPager2;

import com.vanniktech.emoji.emoji.Emoji;

import java.lang.ref.WeakReference;

public class EmojiViewExtended extends EmojiViewInner{
    public EmojiViewExtended(Context context) {
        super(context);
    }

    public EmojiViewExtended(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public EmojiViewExtended(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    final HandlerThread bHt = createBackgroundThread();


    final Handler bH = new Handler(bHt.getLooper());


    private static HandlerThread createBackgroundThread(){
        HandlerThread ht =  new HandlerThread("backgroundWorkOnEmojiView");
        ht.start();
        return ht;
    }


    @SuppressWarnings("unused")
    public void addedInto(ViewGroup container){
        container.addView(this);
    }

    public void fillUpVerticalLinearView(){

        this.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1
        ));
    }

    public void replaceView(View viewToReplace){
        Activity activity = (Activity) viewToReplace.getContext();
        if(activity == null) return;
        View replacementView = this;

        ViewGroup parentView = (ViewGroup) viewToReplace.getParent();
        int index = parentView.indexOfChild(viewToReplace);
        parentView.removeView(viewToReplace);
        parentView.addView(replacementView, index);

    }


    @NonNull
    IRecentEmoji recentEmoji;
    @NonNull
    IVariantEmoji variantEmoji;


    @NonNull
    EmojiVariantPopupGeneral variantPopup;


    EmojiViewBuildController<?> emojiViewController = null;


    public int deleteOneUniChar(){
        // TODO
        return 0; // otherwise, tell task number to create following task
    }

    public int commitOneEmoji(Emoji emoji){
        // TODO
        return 0; // otherwise, tell task number to create following task
    }


    public void setRecentEmoji(@NonNull IRecentEmoji recentEmoji) {
        this.recentEmoji = recentEmoji;
    }

    public void setVariantEmoji(@NonNull IVariantEmoji variantEmoji) {
        this.variantEmoji = variantEmoji;
    }

    public void backgroundUpdateRecentEmoji(@NonNull final EmojiImageViewG imageView, @NonNull final Emoji emoji){

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


    public void preSetup(EmojiViewBuildController<?> builder){
        // TODO
    }


    public void setup(@NonNull View container) {

        final Context context = getContext();

        this.recentEmoji = new RecentEmojiManager2(context);
        this.variantEmoji = new VariantEmojiManager(context);

        EmojiViewExtended.EmojiViewControllerBase emojiViewController = new EmojiViewExtended.EmojiViewControllerBase(context);
        preSetup(emojiViewController);
        emojiViewController.recentEmoji = this.recentEmoji;
        emojiViewController.variantEmoji = this.variantEmoji;


        this.emojiViewController= emojiViewController;

        variantPopup = new EmojiVariantPopupGeneral(container.getRootView(), emojiViewController);

        this.init(emojiViewController);

        this.setOnEmojiBackspaceClickListener(emojiViewController);




    }


    public void executeTask(int nextTaskNum){
        // TODO
    }


    public void onEmojiLongClick(@NonNull final EmojiImageViewG view, @NonNull final Emoji emoji) {

        variantPopup.show(view, emoji);
    }

    public void onEmojiBackspaceClicked(final View v) {

        int nextTaskNum = deleteOneUniChar();

        executeTask(nextTaskNum);

//        backspace(editText);
    }

    public void onEmojiClick(@NonNull final EmojiImageViewG imageView, @NonNull final Emoji emoji) {

        int nextTaskNum = commitOneEmoji(emoji);

        executeTask(nextTaskNum);


//        if (onEmojiClickListener != null) {
//            onEmojiClickListener.onEmojiClick(imageView, emoji);
//        }
        variantPopup.dismiss();
        this.backgroundUpdateRecentEmoji(imageView, emoji);
    }


    public static final class EmojiViewControllerBase implements EmojiViewInner.EmojiViewBuildController<EmojiViewExtended.EmojiViewControllerBase> {

        EmojiViewControllerBase(){}

        @Nullable
        ViewPager2.PageTransformer pageTransformer2;
        @NonNull
        IRecentEmoji recentEmoji;
        @NonNull
        IVariantEmoji variantEmoji;

        protected EmojiViewControllerBase(Context context) {
            initByContext(context);
        }


        private void initByContext(final Context context) {
        }

        @ColorInt
        public int getBackgroundColor(Context context) {

            return Utils.resolveColor(context, R.attr.emojiBackground, R.color.emoji_background);

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

            return Utils.resolveColor(context, R.attr.emojiDivider, R.color.emoji_divider);
        }

        @Nullable
        public ViewPager2.PageTransformer getPageTransformer(Context context) {
            return pageTransformer2;
        }

        @Nullable
        @Override
        public void setPageTransformer(@Nullable ViewPager2.PageTransformer transformer) {
            pageTransformer2 = transformer;
        }

        @NonNull
        public IRecentEmoji getRecentEmoji() {
            return recentEmoji;
        }

        @NonNull
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
            EmojiImageViewG popupRootImageView = this.popupRootImageView != null ?  this.popupRootImageView.get():null;
            Emoji popupVariant = this.popupVariant != null ?  this.popupVariant.get():null;
            EmojiViewInner emojiViewInner = this.emojiViewInner != null ?  this.emojiViewInner.get():null;
            switch (msgId){
                case 0x3041:
                case 0x3042:

                    if(popupRootImageView!=null && popupVariant!=null && emojiViewInner instanceof EmojiViewExtended){
                        ((EmojiViewExtended)emojiViewInner).onEmojiClick(popupRootImageView, popupVariant);
                    }
                    break;

                case 0x6042:


                    if(popupRootImageView!=null && popupVariant!=null && emojiViewInner instanceof EmojiViewExtended){
                        ((EmojiViewExtended)emojiViewInner).onEmojiLongClick(popupRootImageView, popupVariant);
                    }

                    break;

                case 0x3051:

                    if( emojiViewInner instanceof EmojiViewExtended){
                        ((EmojiViewExtended)emojiViewInner).onEmojiBackspaceClicked(null);
                    }

                    break;

            }

        }

        public WeakReference<EmojiImageViewG> popupRootImageView = null;
        public WeakReference<Emoji> popupVariant = null;

        @Override
        public void setPopupRootImageView(EmojiImageViewG rootImageView) {
            popupRootImageView = new WeakReference<>(rootImageView);

        }

        @Override
        public void setPopupVariant(Emoji variant) {
            popupVariant = new WeakReference<>(variant);

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
        public EmojiViewInner getEmojiViewInner() {
            return emojiViewInner != null ? emojiViewInner.get() : null;
        }


        WeakReference<EmojiGrid> recentEmojiGridViewWR = null;
        public void setRecentEmojiGridView(EmojiGrid newView){
            recentEmojiGridViewWR = new WeakReference<>(newView);

        }
        public EmojiGrid getRecentEmojiGridView(){
            return recentEmojiGridViewWR != null ? recentEmojiGridViewWR.get(): null;
        }

        PopupWindow popupWindow = null;

        @Override
        public void setPopupViewWindow(PopupWindow popupWindow) {
            this.popupWindow =popupWindow;
        }

        @Override
        public PopupWindow getPopupViewWindow() {
            return popupWindow;
        }
    }


}

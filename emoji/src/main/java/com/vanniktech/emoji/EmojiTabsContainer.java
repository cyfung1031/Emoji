package com.vanniktech.emoji;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.res.ResourcesCompat;

import com.vanniktech.emoji.emoji.EmojiCategory;

public class EmojiTabsContainer extends LinearLayout {

    public EmojiTabsContainer(Context context) {
        super(context);
    }

    public EmojiTabsContainer(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public EmojiTabsContainer(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    ImageButton[] emojiTabs = null;


    private ImageButton inflateButton(final Context context, @DrawableRes final int btnDrawableResId, @StringRes final int categoryName, final ViewGroup parent) {

//        final ImageButton button = (ImageButton) LayoutInflater.from(context).inflate(R.layout.emoji_view_category, parent, false);
//

        ImageButton button = new ImageButton(context);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
        button.setLayoutParams(layoutParams);
        int[] attrs = new int[]{android.R.attr.selectableItemBackground};
        try (TypedArray typedArray = context.obtainStyledAttributes(attrs)){
            Drawable drawable = typedArray.getDrawable(0);
            button.setImageDrawable(drawable);
        }
        button.setPadding(4, 4, 4, 4);
        button.setScaleType(ImageView.ScaleType.FIT_CENTER);


        Resources resources = context.getResources();
//
        button.setImageDrawable(ResourcesCompat.getDrawable(resources, btnDrawableResId, null));
        button.setColorFilter(themeIconColor, PorterDuff.Mode.SRC_IN);
        button.setContentDescription(context.getString(categoryName));
//
//        if(parent != null) {
//        parent.addView(button);
//        }


        return button;
    }

    int backgroundColor = 0;
    int themeIconColor = 0;
    int themeAccentColor = 0;

    public void setupEmojiTabs(EmojiPager2Util emojiPager2Util){

        setOrientation(LinearLayout.HORIZONTAL);
        Context context = getContext().getApplicationContext();


        int backgroundColor =  Utils.resolveColor(context, R.attr.emojiBackground, R.color.emoji_background);
        int themeIconColor  = Utils.resolveColor(context, R.attr.emojiIcons, R.color.emoji_icons);

        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorAccent, value, true);
        int themeAccentColor = value.data;



        EmojiCategory[] emojiCategories= emojiPager2Util.getEmojiCategories();
        boolean hasRecentEmoji=emojiPager2Util.hasRecentEmoji();
        int idxOffset = (hasRecentEmoji?1:0);


        emojiTabs = new ImageButton[ idxOffset+ emojiCategories.length + 1];

        if (hasRecentEmoji) {
            emojiTabs[0] = inflateButton(context, R.drawable.emoji_recent, R.string.emoji_category_recent, this);
        }

        for (int i = 0; i < emojiCategories.length; i++) {
            emojiTabs[i + idxOffset] = inflateButton(context, emojiCategories[i].getIcon(), emojiCategories[i].getCategoryName(), this);
        }

        emojiTabs[emojiTabs.length - 1] = inflateButton(context, R.drawable.emoji_backspace, R.string.emoji_backspace, this);

        for (ImageButton emojiTab : emojiTabs) {
//            if(emojiTab.getParent() instanceof ViewGroup) ((ViewGroup) emojiTab.getParent()).removeView(emojiTab);
            emojiTab.setImageDrawable(ResourcesCompat.getDrawable( getContext().getResources(), emojiCategories[1].getIcon(), null));
            this.addView(emojiTab);
        }


        this.post(new Runnable() {
            @Override
            public void run() {

                for (ImageButton emojiTab : emojiTabs) {
//            if(emojiTab.getParent() instanceof ViewGroup) ((ViewGroup) emojiTab.getParent()).removeView(emojiTab);
                    emojiTab.setImageDrawable(ResourcesCompat.getDrawable( getContext().getResources(), emojiCategories[1].getIcon(), null));
//                    this.addView(emojiTab);
                }

            }
        });

    }
}

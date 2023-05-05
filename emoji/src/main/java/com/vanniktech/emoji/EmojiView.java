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

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PorterDuff;

import androidx.annotation.CheckResult;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import androidx.core.content.res.ResourcesCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.vanniktech.emoji.emoji.EmojiCategory;
import com.vanniktech.emoji.listeners.OnEmojiBackspaceClickListener;
import com.vanniktech.emoji.listeners.OnEmojiClickListener;
import com.vanniktech.emoji.listeners.OnEmojiLongClickListener;
import com.vanniktech.emoji.listeners.OnEmojiPopupDismissListener;
import com.vanniktech.emoji.listeners.OnEmojiPopupShownListener;
import com.vanniktech.emoji.listeners.OnSoftKeyboardCloseListener;
import com.vanniktech.emoji.listeners.OnSoftKeyboardOpenListener;
import com.vanniktech.emoji.listeners.RepeatListener;

@SuppressLint("ViewConstructor") public final class EmojiView extends LinearLayout {




  private static final long INITIAL_INTERVAL = 500; //ms
  private static final int NORMAL_INTERVAL = 50; //ms

  @ColorInt protected final int themeAccentColor;
  @ColorInt protected final int themeIconColor;

  protected final ImageButton[] emojiTabs;
  protected final EmojiView.EmojiPagerAdapter emojiPagerAdapter;

  @Nullable OnEmojiBackspaceClickListener onEmojiBackspaceClickListener;

  protected int emojiTabLastSelectedIndex = -1;


  public EmojiView(final Context context,
                                                                                            final OnEmojiClickListener onEmojiClickListener,
                                                                                            final OnEmojiLongClickListener onEmojiLongClickListener, @NonNull final IEmojiViewBuilder<?> builder) {
    super(context);

    View.inflate(context, R.layout.emoji_view, this);

    setOrientation(VERTICAL);
    setBackgroundColor(builder.getBackgroundColor() != 0 ? builder.getBackgroundColor() : Utils.resolveColor(context, R.attr.emojiBackground, R.color.emoji_background));
    themeIconColor = builder.getIconColor() != 0 ? builder.getIconColor() : Utils.resolveColor(context, R.attr.emojiIcons, R.color.emoji_icons);

    final TypedValue value = new TypedValue();
    context.getTheme().resolveAttribute(R.attr.colorAccent, value, true);
    themeAccentColor = builder.getSelectedIconColor() != 0 ? builder.getSelectedIconColor() : value.data;

    final ViewPager emojisPager = findViewById(R.id.emojiViewPager);
    final View emojiDivider = findViewById(R.id.emojiViewDivider);
    emojiDivider.setBackgroundColor(builder.getDividerColor() != 0 ? builder.getDividerColor() : Utils.resolveColor(context, R.attr.emojiDivider, R.color.emoji_divider));

    if (builder.getPageTransformer() != null) {
//      emojisPager.setPageTransformer(true, builder.getPageTransformer());
    }

    final LinearLayout emojisTab = findViewById(R.id.emojiViewTab);
    final ViewPager.OnPageChangeListener pageChangeHandler = new ViewPager.OnPageChangeListener() {
      @Override
      public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        // No-op.
      }


      @Override public void onPageSelected(final int i) {
        if (emojiTabLastSelectedIndex != i) {
          final int lastSelectedIndex = emojiTabLastSelectedIndex;
          emojiTabLastSelectedIndex = i;
          if (i == 0) {
            emojiPagerAdapter.invalidateRecentEmojis();
          }

          if (lastSelectedIndex >= 0 && lastSelectedIndex < emojiTabs.length) {
            emojiTabs[lastSelectedIndex].setSelected(false);
            emojiTabs[lastSelectedIndex].setColorFilter(themeIconColor, PorterDuff.Mode.SRC_IN);
          }

          emojiTabs[i].setSelected(true);
          emojiTabs[i].setColorFilter(themeAccentColor, PorterDuff.Mode.SRC_IN);

        }
      }

      @Override
      public void onPageScrollStateChanged(int state) {
        // No-op.
      }
    };
    emojisPager.addOnPageChangeListener(pageChangeHandler);

    final EmojiCategory[] categories = EmojiManager.getInstance().getCategories();

    emojiPagerAdapter = new EmojiView.EmojiPagerAdapter(onEmojiClickListener, onEmojiLongClickListener, builder.getRecentEmoji(), builder.getVariantEmoji());
    emojiTabs = new ImageButton[emojiPagerAdapter.recentAdapterItemCount() + categories.length + 1];

    if (emojiPagerAdapter.hasRecentEmoji()) {
      emojiTabs[0] = inflateButton(context, R.drawable.emoji_recent, R.string.emoji_category_recent, emojisTab);
    }

    for (int i = 0; i < categories.length; i++) {
      emojiTabs[i + emojiPagerAdapter.recentAdapterItemCount()] = inflateButton(context, categories[i].getIcon(), categories[i].getCategoryName(), emojisTab);
    }

    emojiTabs[emojiTabs.length - 1] = inflateButton(context, R.drawable.emoji_backspace, R.string.emoji_backspace, emojisTab);

    handleOnClicks(emojisPager);

    emojisPager.setAdapter(emojiPagerAdapter);

    final int startIndex = emojiPagerAdapter.hasRecentEmoji() ? emojiPagerAdapter.numberOfRecentEmojis() > 0 ? 0 : 1 : 0;
    emojisPager.setCurrentItem(startIndex);
    pageChangeHandler.onPageSelected(startIndex);
  }

  private void handleOnClicks(final ViewPager emojisPager) {
    for (int i = 0; i < emojiTabs.length - 1; i++) {
      emojiTabs[i].setOnClickListener(new EmojiTabsClickListener(emojisPager, i));
    }

    emojiTabs[emojiTabs.length - 1].setOnTouchListener(new RepeatListener(INITIAL_INTERVAL, NORMAL_INTERVAL, this::onBackSpaceToolButtonRepeatedlyClicked));
  }

  public void onBackSpaceToolButtonRepeatedlyClicked(final View view) {
      if (onEmojiBackspaceClickListener != null) {
        onEmojiBackspaceClickListener.onEmojiBackspaceClicked(view);
      }
    }

  public void setOnEmojiBackspaceClickListener(@Nullable final OnEmojiBackspaceClickListener onEmojiBackspaceClickListener) {
    this.onEmojiBackspaceClickListener = onEmojiBackspaceClickListener;
  }

  private ImageButton inflateButton(final Context context, @DrawableRes final int btnDrawableResId, @StringRes final int categoryName, final ViewGroup parent) {
    final ImageButton button = (ImageButton) LayoutInflater.from(context).inflate(R.layout.emoji_view_category, parent, false);

    button.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), btnDrawableResId, null));
    button.setColorFilter(themeIconColor, PorterDuff.Mode.SRC_IN);
    button.setContentDescription(context.getString(categoryName));

    parent.addView(button);

    return button;
  }



  static class EmojiTabsClickListener implements OnClickListener {
    private final ViewPager emojisPager;
    private final int position;

    EmojiTabsClickListener(final ViewPager emojisPager, final int position) {
      this.emojisPager = emojisPager;
      this.position = position;
    }

    @Override public void onClick(final View v) {
      emojisPager.setCurrentItem(position);
    }
  }


  static public final class EmojiPagerAdapter extends PagerAdapter {
    private static final int RECENT_POSITION = 0;

    private final OnEmojiClickListener listener;
    private final OnEmojiLongClickListener longListener;
    private final RecentEmoji recentEmoji;
    private final VariantEmoji variantManager;

    private EmojiGridView recentEmojiGridView;

    EmojiPagerAdapter(
            final OnEmojiClickListener listener,
            final OnEmojiLongClickListener longListener,
            final RecentEmoji recentEmoji,
            final VariantEmoji variantManager
    ) {
      this.listener = listener;
      this.longListener = longListener;
      this.recentEmoji = recentEmoji;
      this.variantManager = variantManager;
    }

    boolean hasRecentEmoji() {
      return !(recentEmoji instanceof NoRecentEmoji);
    }

    int recentAdapterItemCount() {
      return hasRecentEmoji() ? 1 : 0;
    }

    @Override public int getCount() {
      return EmojiManager.getInstance().getCategories().length + recentAdapterItemCount();
    }

    @Override @NonNull public Object instantiateItem(@NonNull final ViewGroup pager, final int position) {
      final EmojiGridView newView;

      if (hasRecentEmoji() && position == RECENT_POSITION) {
        newView = new EmojiGridView(pager.getContext()).init(listener, longListener, recentEmoji);
        recentEmojiGridView = newView;
      } else {
        newView = new EmojiGridView(pager.getContext()).init(listener, longListener,
                EmojiManager.getInstance().getCategories()[position - recentAdapterItemCount()], variantManager);
      }

      pager.addView(newView);
      return newView;
    }

    @Override public void destroyItem(final ViewGroup pager, final int position, @NonNull final Object view) {
      pager.removeView((View) view);

      if (hasRecentEmoji() && position == RECENT_POSITION) {
        recentEmojiGridView = null;
      }
    }

    @Override public boolean isViewFromObject(final View view, @NonNull final Object object) {
      return view.equals(object);
    }

    int numberOfRecentEmojis() {
      if(recentEmoji == null) return 0;
      if(!hasRecentEmoji()) return 0;
      return recentEmoji.getRecentEmojis().size();
    }

    void invalidateRecentEmojis() {
      if (recentEmojiGridView != null) {
        recentEmojiGridView.invalidateEmojis();
      }
    }
  }

}
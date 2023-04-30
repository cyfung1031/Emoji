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

import static java.util.concurrent.TimeUnit.SECONDS;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.CheckResult;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import com.vanniktech.emoji.emoji.EmojiCategory;
import com.vanniktech.emoji.listeners.OnEmojiBackspaceClickListener;
import com.vanniktech.emoji.listeners.OnEmojiClickListener;
import com.vanniktech.emoji.listeners.OnEmojiLongClickListener;
import com.vanniktech.emoji.listeners.OnEmojiPopupDismissListener;
import com.vanniktech.emoji.listeners.OnEmojiPopupShownListener;
import com.vanniktech.emoji.listeners.OnSoftKeyboardCloseListener;
import com.vanniktech.emoji.listeners.OnSoftKeyboardOpenListener;
import com.vanniktech.emoji.listeners.RepeatListener;

@SuppressLint("ViewConstructor")
public final class EmojiView2B extends LinearLayout {
  private static final long INITIAL_INTERVAL = 500; //ms
  private static final int NORMAL_INTERVAL = 50; //ms

  @ColorInt
  private final int themeAccentColor;
  @ColorInt
  private final int themeIconColor;

  private final ImageButton[] emojiTabs;
  private final EmojiView2B.EmojiPagerAdapter emojiPagerAdapter;

  @Nullable
  OnEmojiBackspaceClickListener onEmojiBackspaceClickListener;

  private int emojiTabLastSelectedIndex = -1;

  @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
  public EmojiView2B(final Context context,
                     final OnEmojiClickListener onEmojiClickListener,
                     final OnEmojiLongClickListener onEmojiLongClickListener, @NonNull final EmojiViewBuilder<?> builder) {
    super(context);
    Resources resources = context.getResources();

    View.inflate(context, R.layout.emoji_view, this);

    setOrientation(VERTICAL);
    setBackgroundColor(builder.getBackgroundColor() != 0 ? builder.getBackgroundColor() : Utils.resolveColor(context, R.attr.emojiBackground, R.color.emoji_background));
    themeIconColor = builder.getIconColor() != 0 ? builder.getIconColor() : Utils.resolveColor(context, R.attr.emojiIcons, R.color.emoji_icons);

    final TypedValue value = new TypedValue();
    context.getTheme().resolveAttribute(R.attr.colorAccent, value, true);
    themeAccentColor = builder.getSelectedIconColor() != 0 ? builder.getSelectedIconColor() : value.data;

    final ViewPager2 emojisPager = findViewById(R.id.emojiViewPager);
    final View emojiDivider = findViewById(R.id.emojiViewDivider);
    emojiDivider.setBackgroundColor(builder.getDividerColor() != 0 ? builder.getDividerColor() : Utils.resolveColor(context, R.attr.emojiDivider, R.color.emoji_divider));

    if (builder.getPageTransformer() != null) {
//      emojisPager.setPageTransformer(builder.getPageTransformer());
    }

    final LinearLayout emojisTab = findViewById(R.id.emojiViewTab);
    final ViewPager2.OnPageChangeCallback pageChangeHandler = new ViewPager2.OnPageChangeCallback() {
      @Override
      public void onPageSelected(final int i) {
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
    };
    emojisPager.registerOnPageChangeCallback(pageChangeHandler);

    final EmojiCategory[] categories = EmojiManager.getInstance().getCategories();

    emojiPagerAdapter = new EmojiView2B.EmojiPagerAdapter(onEmojiClickListener, onEmojiLongClickListener, builder.getRecentEmoji(), builder.getVariantEmoji());
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
    emojisPager.setOffscreenPageLimit(1);

    final int startIndex = emojiPagerAdapter.hasRecentEmoji() ? emojiPagerAdapter.numberOfRecentEmojis() > 0 ? 0 : 1 : 0;
    emojisPager.setCurrentItem(startIndex);
    pageChangeHandler.onPageSelected(startIndex);
  }

  private void handleOnClicks(final ViewPager2 emojisPager) {
    for (int i = 0; i < emojiTabs.length - 1; i++) {
      emojiTabs[i].setOnClickListener(new EmojiTabsClickListener(emojisPager, i));
    }

    emojiTabs[emojiTabs.length - 1].setOnTouchListener(new RepeatListener(INITIAL_INTERVAL, NORMAL_INTERVAL, new OnClickListener() {
      @Override
      public void onClick(final View view) {
        if (onEmojiBackspaceClickListener != null) {
          onEmojiBackspaceClickListener.onEmojiBackspaceClicked(view);
        }
      }
    }));
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
    private final ViewPager2 emojisPager;
    private final int position;

    EmojiTabsClickListener(final ViewPager2 emojisPager, final int position) {
      this.emojisPager = emojisPager;
      this.position = position;
    }

    @Override
    public void onClick(final View v) {
      emojisPager.setCurrentItem(position);
    }
  }

  public interface EmojiViewBuilder<TBuilder>{



    // Getter methods
    @NonNull
    public View getRootView();

    @ColorInt
    public int getBackgroundColor();

    @ColorInt
    public int getIconColor();

    @ColorInt
    public int getSelectedIconColor();
    @ColorInt
    public int getDividerColor();

    @Nullable
    public ViewPager.PageTransformer getPageTransformer();

    @NonNull
    public RecentEmoji getRecentEmoji();

    @NonNull
    public VariantEmoji getVariantEmoji();

    @CheckResult public TBuilder setOnSoftKeyboardCloseListener(@Nullable final OnSoftKeyboardCloseListener listener);

    @CheckResult public TBuilder setOnEmojiClickListener(@Nullable final OnEmojiClickListener listener);
    @CheckResult public TBuilder setOnSoftKeyboardOpenListener(@Nullable final OnSoftKeyboardOpenListener listener);

    @CheckResult public TBuilder setOnEmojiPopupShownListener(@Nullable final OnEmojiPopupShownListener listener);

    @CheckResult public TBuilder setOnEmojiPopupDismissListener(@Nullable final OnEmojiPopupDismissListener listener);
    @CheckResult public TBuilder setOnEmojiBackspaceClickListener(@Nullable final OnEmojiBackspaceClickListener listener);

    @CheckResult public TBuilder setKeyboardAnimationStyle(@StyleRes final int animation);

    @CheckResult public TBuilder setPageTransformer(@Nullable final ViewPager.PageTransformer transformer);


  }



  static final class EmojiPagerAdapter extends RecyclerView.Adapter<EmojiPagerAdapter.EmojiPageViewHolder> {
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

    @NonNull
    @Override
    public EmojiPageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      Context context = parent.getContext();
      EmojiGridView emojiGridView = new EmojiGridView(context);
      emojiGridView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
      return new EmojiPageViewHolder(emojiGridView);
    }

    @Override
    public void onBindViewHolder(@NonNull EmojiPageViewHolder holder, int position) {
      if (hasRecentEmoji() && position == RECENT_POSITION) {
        holder.emojiGridView.init(listener, longListener, recentEmoji);
        recentEmojiGridView = holder.emojiGridView;
      } else {
        EmojiCategory category = EmojiManager.getInstance().getCategories()[position - recentAdapterItemCount()];
        holder.emojiGridView.init(listener, longListener, category, variantManager);
      }
    }

    @Override
    public int getItemCount() {
      return EmojiManager.getInstance().getCategories().length + recentAdapterItemCount();
    }

    static class EmojiPageViewHolder extends RecyclerView.ViewHolder {
      final EmojiGridView emojiGridView;

      EmojiPageViewHolder(EmojiGridView emojiGridView) {
        super(emojiGridView);
        this.emojiGridView = emojiGridView;
      }
    }
  }

}
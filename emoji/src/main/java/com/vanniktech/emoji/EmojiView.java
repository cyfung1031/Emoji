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
import android.content.res.Resources;
import android.graphics.PorterDuff;

import androidx.annotation.CheckResult;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import androidx.core.content.res.ResourcesCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.content.res.AppCompatResources;
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

import static java.util.concurrent.TimeUnit.SECONDS;

@SuppressLint("ViewConstructor") public final class EmojiView extends LinearLayout implements ViewPager.OnPageChangeListener {
  private static final long INITIAL_INTERVAL = SECONDS.toMillis(1) / 2;
  private static final int NORMAL_INTERVAL = 50;

  @ColorInt private final int themeAccentColor;
  @ColorInt private final int themeIconColor;

  private final ImageButton[] emojiTabs;
  private final EmojiPagerAdapter emojiPagerAdapter;

  @Nullable OnEmojiBackspaceClickListener onEmojiBackspaceClickListener;

  private int emojiTabLastSelectedIndex = -1;

  private Resources resources;

  @SuppressWarnings({ "PMD.CyclomaticComplexity", "PMD.NPathComplexity" }) public EmojiView(final Context context,
                                                                                            final OnEmojiClickListener onEmojiClickListener,
                                                                                            final OnEmojiLongClickListener onEmojiLongClickListener, @NonNull final EmojiViewBuilder<?> builder) {
    super(context);
    resources = context.getResources(); // be aware of memory leakage

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
      emojisPager.setPageTransformer(true, builder.getPageTransformer());
    }

    final LinearLayout emojisTab = findViewById(R.id.emojiViewTab);
    emojisPager.addOnPageChangeListener(this);

    final EmojiCategory[] categories = EmojiManager.getInstance().getCategories();

    emojiPagerAdapter = new EmojiPagerAdapter(onEmojiClickListener, onEmojiLongClickListener, builder.getRecentEmoji(), builder.getVariantEmoji());
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
    onPageSelected(startIndex);
  }

  private void handleOnClicks(final ViewPager emojisPager) {
    for (int i = 0; i < emojiTabs.length - 1; i++) {
      emojiTabs[i].setOnClickListener(new EmojiTabsClickListener(emojisPager, i));
    }

    emojiTabs[emojiTabs.length - 1].setOnTouchListener(new RepeatListener(INITIAL_INTERVAL, NORMAL_INTERVAL, new OnClickListener() {
      @Override public void onClick(final View view) {
        if (onEmojiBackspaceClickListener != null) {
          onEmojiBackspaceClickListener.onEmojiBackspaceClick(view);
        }
      }
    }));
  }

  public void setOnEmojiBackspaceClickListener(@Nullable final OnEmojiBackspaceClickListener onEmojiBackspaceClickListener) {
    this.onEmojiBackspaceClickListener = onEmojiBackspaceClickListener;
  }

  private ImageButton inflateButton(final Context context, @DrawableRes final int btnDrawableResId, @StringRes final int categoryName, final ViewGroup parent) {
    final ImageButton button = (ImageButton) LayoutInflater.from(context).inflate(R.layout.emoji_view_category, parent, false);


    button.setImageDrawable(ResourcesCompat.getDrawable(resources, btnDrawableResId, null));
    button.setColorFilter(themeIconColor, PorterDuff.Mode.SRC_IN);
    button.setContentDescription(context.getString(categoryName));

    parent.addView(button);

    return button;
  }

  @Override public void onPageSelected(final int i) {
    if (emojiTabLastSelectedIndex != i) {
      if (i == 0) {
        emojiPagerAdapter.invalidateRecentEmojis();
      }

      if (emojiTabLastSelectedIndex >= 0 && emojiTabLastSelectedIndex < emojiTabs.length) {
        emojiTabs[emojiTabLastSelectedIndex].setSelected(false);
        emojiTabs[emojiTabLastSelectedIndex].setColorFilter(themeIconColor, PorterDuff.Mode.SRC_IN);
      }

      emojiTabs[i].setSelected(true);
      emojiTabs[i].setColorFilter(themeAccentColor, PorterDuff.Mode.SRC_IN);

      emojiTabLastSelectedIndex = i;
    }
  }

  @Override public void onPageScrolled(final int i, final float v, final int i2) {
    // No-op.
  }

  @Override public void onPageScrollStateChanged(final int i) {
    // No-op.
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

  public interface EmojiViewBuilder<TBuilder>{



    // Getter methods
    @NonNull
    public View getRootView();

    @StyleRes
    public int getKeyboardAnimationStyle();

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


    @Nullable
    public OnEmojiPopupShownListener getOnEmojiPopupShownListener();

    @Nullable
    public OnSoftKeyboardCloseListener getOnSoftKeyboardCloseListener();

    @Nullable
    public OnSoftKeyboardOpenListener getOnSoftKeyboardOpenListener();

    @Nullable
    public OnEmojiBackspaceClickListener getOnEmojiBackspaceClickListener();


    @Nullable
    public OnEmojiClickListener getOnEmojiClickListener();

    @Nullable
    public OnEmojiPopupDismissListener getOnEmojiPopupDismissListener();

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
    @CheckResult public TBuilder setPopupWindowHeight(final int windowHeight);
    @CheckResult public TBuilder setRecentEmoji(@NonNull final RecentEmoji recent);
    @CheckResult public TBuilder setVariantEmoji(@NonNull final VariantEmoji variant);

    @CheckResult public TBuilder setBackgroundColor(@ColorInt final int color);

    @CheckResult public TBuilder setIconColor(@ColorInt final int color);

    @CheckResult public TBuilder setSelectedIconColor(@ColorInt final int color);

    @CheckResult public TBuilder setDividerColor(@ColorInt final int color);

    @CheckResult public TBuilder setKeyboardAnimationStyle(@StyleRes final int animation);

    @CheckResult public TBuilder setPageTransformer(@Nullable final ViewPager.PageTransformer transformer);


  }
}
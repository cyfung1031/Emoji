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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.vanniktech.emoji.emoji.Emoji;

import java.util.Collection;

final class EmojiArrayAdapterGeneral extends ArrayAdapter<Emoji> {
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

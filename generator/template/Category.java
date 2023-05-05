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

package com.vanniktech.emoji.<%= package %>.category;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import com.vanniktech.emoji.emoji.EmojiCategory;
import com.vanniktech.emoji.<%= package %>.R;
import com.vanniktech.emoji.<%= package %>.<%= name %>;

public final class <%= category %>Category implements EmojiCategory {
  private static final <%= name %>[] EMOJIS = CategoryUtils.concatAll(<%= chunks %>);

  @Override @NonNull public <%= name %>[] getEmojis() {
    return EMOJIS;
  }

  @Override @DrawableRes public int getIcon() {
    return R.drawable.emoji_<%= package %>_category_<%= icon %>;
  }

  @Override @StringRes public int getCategoryName() {
    return R.string.emoji_<%= package %>_category_<%= icon %>;
  }
}

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

package com.vanniktech.emoji.<%= package %>;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.emoji.text.EmojiCompat;
import android.text.Spannable;
import com.vanniktech.emoji.IEmojiProvider;
import com.vanniktech.emoji.IEmojiReplacer;
import com.vanniktech.emoji.emoji.EmojiCategory;
<%= imports %>

public final class <%= name %>Provider implements IEmojiProvider, IEmojiReplacer {
  public <%= name %>Provider(@NonNull final EmojiCompat emojiCompat) {
    if (emojiCompat == null) {
      throw new NullPointerException();
    }
  }

  @Override @NonNull public EmojiCategory[] getCategories() {
    return new EmojiCategory[] {
      <%= categories %>
    };
  }

@Override public void replaceWithImages(final Context context, final Spannable text, final float emojiSize, final IEmojiReplacer fallback) {
    if (EmojiCompat.get().getLoadState() != EmojiCompat.LOAD_STATE_SUCCEEDED
            || EmojiCompat.get().process(text, 0, text.length()) != text) {
        fallback.replaceWithImages(context, text, emojiSize, null);
    }
  }
}

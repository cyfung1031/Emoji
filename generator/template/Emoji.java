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
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import android.util.LruCache;

import com.vanniktech.emoji.emoji.CacheKey;
import com.vanniktech.emoji.emoji.Emoji;

import java.lang.ref.SoftReference;

public class <%= name %> extends Emoji {
  private static final int CACHE_SIZE = 100;
  private static final int SPRITE_SIZE = 64;
  private static final int SPRITE_SIZE_INC_BORDER = 66;
  private static final int NUM_STRIPS = <%= strips %>;

  private static final Object LOCK = new Object();

  private static final SoftReference[] STRIP_REFS = new SoftReference[NUM_STRIPS];
  private static final LruCache<CacheKey, Bitmap> BITMAP_CACHE = new LruCache<>(CACHE_SIZE);

  static {
    for (int i = 0; i < NUM_STRIPS; i++) {
      STRIP_REFS[i] = new SoftReference<Bitmap>(null);
    }
  }

  private final int x;
  private final int y;

  public <%= name %>(@NonNull final int[] codePoints, @NonNull final String[] shortcodes, final int x, final int y,
                     final boolean isDuplicate) {
    super(codePoints, shortcodes, -1, isDuplicate);

    this.x = x;
    this.y = y;
  }

  public <%= name %>(final int codePoint, @NonNull final String[] shortcodes, final int x, final int y,
                     final boolean isDuplicate) {
    super(codePoint, shortcodes, -1, isDuplicate);

    this.x = x;
    this.y = y;
  }

  public <%= name %>(final int codePoint, @NonNull final String[] shortcodes, final int x, final int y,
                     final boolean isDuplicate, final Emoji... variants) {
    super(codePoint, shortcodes, -1, isDuplicate, variants);

    this.x = x;
    this.y = y;
  }

  public <%= name %>(@NonNull final int[] codePoints, @NonNull final String[] shortcodes, final int x, final int y,
                     final boolean isDuplicate, final Emoji... variants) {
    super(codePoints, shortcodes, -1, isDuplicate, variants);

    this.x = x;
    this.y = y;
  }

  @Override @NonNull public Drawable getDrawable(final Context context) {
    final CacheKey key = new CacheKey(x, y);
    final Bitmap bitmap = BITMAP_CACHE.get(key);
    if (bitmap != null) {
      return new BitmapDrawable(context.getResources(), bitmap);
    }
    final Bitmap strip = loadStrip(context);
    final Bitmap cut = Bitmap.createBitmap(strip, 1, y * SPRITE_SIZE_INC_BORDER + 1, SPRITE_SIZE, SPRITE_SIZE);
    BITMAP_CACHE.put(key, cut);
    return new BitmapDrawable(context.getResources(), cut);
  }
  
    
    // Add this static array at the class level
    private static final int[] SHEET_RESOURCE_IDS = {
        R.drawable.emoji_<%= package %>_sheet_0,
        R.drawable.emoji_<%= package %>_sheet_1,
        R.drawable.emoji_<%= package %>_sheet_2,
        R.drawable.emoji_<%= package %>_sheet_3,
        R.drawable.emoji_<%= package %>_sheet_4,
        R.drawable.emoji_<%= package %>_sheet_5,
        R.drawable.emoji_<%= package %>_sheet_6,
        R.drawable.emoji_<%= package %>_sheet_7,
        R.drawable.emoji_<%= package %>_sheet_8,
        R.drawable.emoji_<%= package %>_sheet_9,
        R.drawable.emoji_<%= package %>_sheet_10,
        R.drawable.emoji_<%= package %>_sheet_11,
        R.drawable.emoji_<%= package %>_sheet_12,
        R.drawable.emoji_<%= package %>_sheet_13,
        R.drawable.emoji_<%= package %>_sheet_14,
        R.drawable.emoji_<%= package %>_sheet_15,
        R.drawable.emoji_<%= package %>_sheet_16,
        R.drawable.emoji_<%= package %>_sheet_17,
        R.drawable.emoji_<%= package %>_sheet_18,
        R.drawable.emoji_<%= package %>_sheet_19,
        R.drawable.emoji_<%= package %>_sheet_20,
        R.drawable.emoji_<%= package %>_sheet_21,
        R.drawable.emoji_<%= package %>_sheet_22,
        R.drawable.emoji_<%= package %>_sheet_23,
        R.drawable.emoji_<%= package %>_sheet_24,
        R.drawable.emoji_<%= package %>_sheet_25,
        R.drawable.emoji_<%= package %>_sheet_26,
        R.drawable.emoji_<%= package %>_sheet_27,
        R.drawable.emoji_<%= package %>_sheet_28,
        R.drawable.emoji_<%= package %>_sheet_29,
        R.drawable.emoji_<%= package %>_sheet_30,
        R.drawable.emoji_<%= package %>_sheet_31,
        R.drawable.emoji_<%= package %>_sheet_32,
        R.drawable.emoji_<%= package %>_sheet_33,
        R.drawable.emoji_<%= package %>_sheet_34,
        R.drawable.emoji_<%= package %>_sheet_35,
        R.drawable.emoji_<%= package %>_sheet_36,
        R.drawable.emoji_<%= package %>_sheet_37,
        R.drawable.emoji_<%= package %>_sheet_38,
        R.drawable.emoji_<%= package %>_sheet_39,
        R.drawable.emoji_<%= package %>_sheet_40,
        R.drawable.emoji_<%= package %>_sheet_41,
        R.drawable.emoji_<%= package %>_sheet_42,
        R.drawable.emoji_<%= package %>_sheet_43,
        R.drawable.emoji_<%= package %>_sheet_44,
        R.drawable.emoji_<%= package %>_sheet_45,
        R.drawable.emoji_<%= package %>_sheet_46,
        R.drawable.emoji_<%= package %>_sheet_47,
        R.drawable.emoji_<%= package %>_sheet_48,
        R.drawable.emoji_<%= package %>_sheet_49,
        R.drawable.emoji_<%= package %>_sheet_50,
        R.drawable.emoji_<%= package %>_sheet_51,
        R.drawable.emoji_<%= package %>_sheet_52,
        R.drawable.emoji_<%= package %>_sheet_53,
        R.drawable.emoji_<%= package %>_sheet_54,
        R.drawable.emoji_<%= package %>_sheet_55
    };

  private Bitmap loadStrip(final Context context) {
    Bitmap strip = (Bitmap) STRIP_REFS[x].get();
    if (strip == null) {
      synchronized (LOCK) {
        strip = (Bitmap) STRIP_REFS[x].get();
        if (strip == null) {
          final Resources resources = context.getResources();
          final int resId = SHEET_RESOURCE_IDS[x]; // Replace getIdentifier with array indexing
          strip = BitmapFactory.decodeResource(resources, resId);
          STRIP_REFS[x] = new SoftReference<>(strip);
        }
      }
    }
    return strip;
  }

  @Override public void destroy() {
    synchronized (LOCK) {
      BITMAP_CACHE.evictAll();
      for (int i = 0; i < NUM_STRIPS; i++) {
        final Bitmap strip = (Bitmap) STRIP_REFS[i].get();
        if (strip != null) {
          strip.recycle();
          STRIP_REFS[i].clear();
        }
      }
    }
  }
}

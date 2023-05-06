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

package com.vanniktech.emoji.emoji;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public abstract class Emoji implements Serializable {
    private static final long serialVersionUID = 3L;
    private static final List<Emoji> EMPTY_EMOJI_LIST = emptyList();

    @NonNull
    private final String unicode;
    @NonNull
    private final String[] shortcodes;
    @DrawableRes
    private final int iconResId;
    private final boolean isDuplicate;
    @NonNull
    private final List<Emoji> variants;
    WeakReference<Drawable> cacheDrawable = null;
    @Nullable
    private Emoji base;


    public Emoji(@NonNull final int[] codePoints, @NonNull final String[] shortcodes,
                 @DrawableRes final int iconResId, final boolean isDuplicate) {
        this(codePoints, shortcodes, iconResId, isDuplicate, new Emoji[0]);
    }


    public Emoji(final int codePoint, @NonNull final String[] shortcodes,
                 @DrawableRes final int iconResId, final boolean isDuplicate) {
        this(codePoint, shortcodes, iconResId, isDuplicate, new Emoji[0]);
    }

    public Emoji(final int codePoint, @NonNull final String[] shortcodes,
                 @DrawableRes final int iconResId, final boolean isDuplicate,
                 final Emoji... variants) {
        this(new int[]{codePoint}, shortcodes, iconResId, isDuplicate, variants);
    }

    public Emoji(@NonNull final int[] codePoints, @NonNull final String[] shortcodes,
                 @DrawableRes final int iconResId, final boolean isDuplicate,
                 final Emoji... variants) {
        this.unicode = new String(codePoints, 0, codePoints.length);
        this.shortcodes = shortcodes;
        this.iconResId = iconResId;
        this.isDuplicate = isDuplicate;
        this.variants = variants.length == 0 ? EMPTY_EMOJI_LIST : asList(variants);
        for (final Emoji variant : variants) {
            variant.base = this;
        }
    }

    public int getIconResId() {
        return iconResId;
    }

    public abstract int getIconResIdX();

    @NonNull
    public String getUnicode() {
        return unicode;
    }

    @Nullable
    public List<String> getShortcodes() {
        return asList(shortcodes);
    }

    @NonNull
    public Drawable getDrawable(final Context context) {
        return Objects.requireNonNull(ResourcesCompat.getDrawable(context.getResources(), iconResId, null));
    }

    @NonNull
    public Drawable getDrawable(final Resources resources) {
        return Objects.requireNonNull(ResourcesCompat.getDrawable(resources, iconResId, null));
    }

    public Drawable getCacheDrawable() {
        return cacheDrawable != null ? cacheDrawable.get() : null;
    }

    public void setCacheDrawable(Drawable drawable) {
        if (drawable == null) cacheDrawable = null;
        else cacheDrawable = new WeakReference<>(drawable);
    }


    public boolean isDuplicate() {
        return isDuplicate;
    }

    @NonNull
    public List<Emoji> getVariants() {
        return new ArrayList<>(variants);
    }

    @NonNull
    public Emoji getBase() {
        Emoji result = this;

        while (result.base != null) {
            result = result.base;
        }

        return result;
    }

    public int getLength() {
        return unicode.length();
    }

    public boolean hasVariants() {
        return !variants.isEmpty();
    }

    public void destroy() {
        // For inheritors to override.
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Emoji emoji = (Emoji) o;

        return iconResId == emoji.iconResId
                && unicode.equals(emoji.unicode)
                && Arrays.equals(shortcodes, emoji.shortcodes)
                && variants.equals(emoji.variants);
    }

    @Override
    public int hashCode() {
        int result = unicode.hashCode();
        result = 31 * result + Arrays.hashCode(shortcodes);
        result = 31 * result + iconResId;
        result = 31 * result + variants.hashCode();
        return result;
    }
}

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

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.text.Editable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.vanniktech.emoji.emoji.Emoji;

import java.util.ArrayList;
import java.util.List;

public final class Utils {

    static final int NO_UPDATE_FLAG = -1;

    private Utils() {
        throw new AssertionError("No instances.");
    }

    @PrivateApi public static float initTextView(final TextView textView, final AttributeSet attrs) {
        if (!textView.isInEditMode()) {
            EmojiManager.getInstance().verifyInstalled();
        }

        final Paint.FontMetrics fontMetrics = textView.getPaint().getFontMetrics();
        final float defaultEmojiSize = fontMetrics.descent - fontMetrics.ascent;

        final float emojiSize;

        if (attrs == null) {
            emojiSize = defaultEmojiSize;
        } else {
            try (TypedArray a = textView.getContext().obtainStyledAttributes(attrs, R.styleable.EmojiTextView)) {
                emojiSize = a.getDimension(R.styleable.EmojiTextView_emojiSize, defaultEmojiSize);
            }
        }

        textView.setText(textView.getText());
        return emojiSize;
    }

    static int dpToPx(@NonNull final Context context, final float dp) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics()) + 0.5f);
    }

    static int getOrientation(final Context context) {
        return context.getResources().getConfiguration().orientation;
    }

    static int getProperWidth(final Activity activity) {
        final Rect rect = Utils.windowVisibleDisplayFrame(activity);
        return Utils.getOrientation(activity) == Configuration.ORIENTATION_PORTRAIT ? rect.right : getScreenWidth(activity);
    }

    static boolean shouldOverrideRegularCondition(@NonNull final Context context, final EditText editText) {
        if ((editText.getImeOptions() & EditorInfo.IME_FLAG_NO_EXTRACT_UI) == 0) {
            return getOrientation(context) == Configuration.ORIENTATION_LANDSCAPE;
        }

        return false;
    }

    static int getProperHeight(final Activity activity) {
        return Utils.windowVisibleDisplayFrame(activity).bottom;
    }

    static int getScreenWidth(@NonNull final Activity context) {
        return dpToPx(context, context.getResources().getConfiguration().screenWidthDp);
    }

    @NonNull
    static Point locationOnScreen(@NonNull final View view) {
        final int[] location = new int[2];
        view.getLocationOnScreen(location);
        return new Point(location[0], location[1]);
    }

    @NonNull
    static Rect windowVisibleDisplayFrame(@NonNull final Activity context) {
        final Rect result = new Rect();
        context.getWindow().getDecorView().getWindowVisibleDisplayFrame(result);
        return result;
    }

    @PrivateApi public static void backspace(@NonNull final EditText editText) {
        final KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL);
        editText.dispatchKeyEvent(event);
    }

    static List<Emoji> asListWithoutDuplicates(final Emoji[] emojis) {
        final List<Emoji> result = new ArrayList<>(emojis.length);

        for (final Emoji emoji : emojis) {
            if (!emoji.isDuplicate()) {
                result.add(emoji);
            }
        }

        return result;
    }

    @PrivateApi public static void input(@NonNull final EditText editText, @Nullable final Emoji emoji) {
        if (emoji != null) {
            final int start = editText.getSelectionStart();
            final int end = editText.getSelectionEnd();
            final String unicode = emoji.getUnicode();
            final Editable text = editText.getText();
            if (start >= 0) {
                final int min = Math.min(start, end);
                final int max = Math.max(start, end);
                text.replace(min, max, unicode, 0, unicode.length());
            } else {
                text.append(unicode);
            }
        }
    }

    static Activity asActivity(@NonNull final Context context) {
        for (Context result = context; result instanceof ContextWrapper; result = ((ContextWrapper) result).getBaseContext()) {
            if (result instanceof Activity) {
                return (Activity) result;
            }
        }

        throw new IllegalArgumentException("The passed Context is not an Activity.");
    }

    static void fixPopupLocation(@NonNull final PopupWindow popupWindow, @NonNull final Point desiredLocation) {
        View contentView = popupWindow.getContentView();
        if (contentView == null) return;
        contentView.post(new Runnable() {
            @Override
            public void run() {
                final Point actualLocation = locationOnScreen(popupWindow.getContentView());

                if (!(actualLocation.x == desiredLocation.x && actualLocation.y == desiredLocation.y)) {
                    final int differenceX = actualLocation.x - desiredLocation.x;
                    final int differenceY = actualLocation.y - desiredLocation.y;

                    final int fixedOffsetX;
                    final int fixedOffsetY;

                    if (actualLocation.x > desiredLocation.x) {
                        fixedOffsetX = desiredLocation.x - differenceX;
                    } else {
                        fixedOffsetX = desiredLocation.x + differenceX;
                    }

                    if (actualLocation.y > desiredLocation.y) {
                        fixedOffsetY = desiredLocation.y - differenceY;
                    } else {
                        fixedOffsetY = desiredLocation.y + differenceY;
                    }

                    popupWindow.update(fixedOffsetX, fixedOffsetY, NO_UPDATE_FLAG, NO_UPDATE_FLAG);
                }
            }
        });
    }

    @ColorInt
    static int resolveColor(@NonNull final Context context, @AttrRes final int resource, @ColorRes final int fallback) {
        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(resource, value, true);

        final int resolvedColor = value.resourceId != 0 ?
                ContextCompat.getColor(context, value.resourceId) :
                value.data;

        return resolvedColor != 0 ? resolvedColor :
                ContextCompat.getColor(context, fallback);
    }

}

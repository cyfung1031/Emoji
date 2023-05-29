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

package com.vanniktech.emoji.sample;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopupBoard;

public class CustomViewActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final CustomView customView = new CustomView(this, null);
        setContentView(customView);
        customView.setUpEmojiPopup();
    }

    static final class CustomView extends LinearLayout {

        CustomView(final Context context, @Nullable final AttributeSet attrs) {
            super(context, attrs);
            LayoutInflater inflater = LayoutInflater.from(context);
            inflater.inflate(R.layout.view_custom, (ViewGroup) this, true);
            setOrientation(VERTICAL);
        }

        void setUpEmojiPopup() {
            final EmojiEditText editText = findViewById(R.id.customViewEditText);

            final EmojiPopupBoard emojiPopup = EmojiPopupBoard.create(this, editText);

            emojiPopup.setAnimationStyle(R.style.emoji_fade_animation_style);
            emojiPopup.setPageTransformer(new PageTransformer());
            emojiPopup.setup();


            final Button emojiButton = findViewById(R.id.customViewButton);
            editText.disableKeyboardInput(emojiPopup);
            editText.forceSingleEmoji();
            emojiButton.setOnClickListener(ignore -> editText.requestFocus());

        }
    }
}

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

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.text.Editable;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;

import com.vanniktech.emoji.EmojiEditText;

// We don't care about duplicated code in the sample.
public class MainActivityEmojiEditText extends MainActivityBase {

  @Override
  public void setupOnCreate() {

    setContentView(R.layout.activity_main);

    chatAdapter = new ChatAdapter();

    final Button button = findViewById(R.id.main_activity_material_button);
    button.setText("Text with emojis \uD83D\uDE18\uD83D\uDE02\uD83E\uDD8C");
    editText = findViewById(R.id.main_activity_chat_bottom_message_edittext);
    rootView = findViewById(R.id.main_activity_root_view);
    emojiButton = findViewById(R.id.main_activity_emoji);
    final ImageView sendButton = findViewById(R.id.main_activity_send);

    emojiButton.setColorFilter(ContextCompat.getColor(this, R.color.emoji_primary_color), PorterDuff.Mode.SRC_IN);
    sendButton.setColorFilter(ContextCompat.getColor(this, R.color.emoji_primary_color), PorterDuff.Mode.SRC_IN);

    final CheckBox forceEmojisOnly = findViewById(R.id.main_activity_force_emojis_only);
    forceEmojisOnly.setOnCheckedChangeListener((ignore, isChecked) -> {
      if (isChecked) {
        editText.clearFocus();
        emojiButton.setVisibility(GONE);
        editText.disableKeyboardInput(emojiPopup);
      } else {
        emojiButton.setVisibility(VISIBLE);
        editText.enableKeyboardInput();
      }
    });

    emojiButton.setOnClickListener(ignore -> {
      emojiPopup.toggle();
    });

    sendButton.setOnClickListener(ignore -> {
      Editable editable = editText.getText();
      final String text = editable != null ? editable.toString().trim() : "";

      if (text.length() > 0) {
        chatAdapter.add(text);

        editText.setText("");
      }
    });
  }

  EmojiEditText editText;

  @Override
  public EmojiEditText getEditText() {
    return editText;
  }

  public void setEditText(EmojiEditText editText) {
    this.editText = editText;
  }

  @Override
  public boolean customOptionsItemSelected(MenuItem item, int itemId) {
    if (itemId == R.id.menuMainCustomView) {
      emojiPopup.dismiss();
      startActivity(new Intent(this, CustomViewActivity.class));
      return true;
    }
    return false;
  }
}

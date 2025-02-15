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

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.vanniktech.emoji.EmojiInformation;
import com.vanniktech.emoji.EmojiTextView;
import com.vanniktech.emoji.EmojiUtils;
import java.util.ArrayList;
import java.util.List;

final class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
  private final List<String> texts = new ArrayList<>();

  @Override public ChatViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
    final LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
    return new ChatViewHolder(layoutInflater.inflate(R.layout.item_adapter_chat, parent, false));
  }

  @Override public void onBindViewHolder(final ChatViewHolder chatViewHolder, final int position) {
    final String text = texts.get(position);

    final EmojiInformation emojiInformation = EmojiUtils.emojiInformation(text);
    final int res;

    if (emojiInformation.isOnlyEmojis && emojiInformation.emojis.size() == 1) {
      res = R.dimen.emoji_size_single_emoji;
    } else if (emojiInformation.isOnlyEmojis && emojiInformation.emojis.size() > 1) {
      res = R.dimen.emoji_size_only_emojis;
    } else {
      res = R.dimen.emoji_size_default;
    }

    chatViewHolder.textView.setEmojiSizeRes(res, false);
    chatViewHolder.textView.setText(text);
  }

  @Override public int getItemCount() {
    return texts.size();
  }

  public void add(final String text) {
    texts.add(text);
    notifyItemInserted(texts.size());
  }

  static class ChatViewHolder extends RecyclerView.ViewHolder {
    final EmojiTextView textView;

    ChatViewHolder(final View view) {
      super(view);

      textView = view.findViewById(R.id.itemAdapterChatTextView);
    }
  }
}

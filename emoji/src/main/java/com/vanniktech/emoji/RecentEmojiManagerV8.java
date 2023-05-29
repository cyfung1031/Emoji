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

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.vanniktech.emoji.emoji.Emoji;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.TreeSet;

public final class RecentEmojiManagerV8 implements IRecentEmoji {
    static final int EMOJI_GUESS_SIZE = 5;
    static final int MAX_RECENTS = 40;
    private static final String PREFERENCE_NAME = "emoji-recent-manager";
    private static final String TIME_DELIMITER = ";";
    private static final String EMOJI_DELIMITER = "~";
    private static final String RECENT_EMOJIS = "recent-emojis";
    @NonNull
    private final SharedPreferences sharedPreferences;
    private long totalAdd = 0L;
    private long lastTimeStamp = 0L;
    @NonNull
    private EmojiSet emojiSet = new EmojiSet();

    private static long managerTimeStamp = 0L;

    public RecentEmojiManagerV8(@NonNull final Context context) {
        sharedPreferences = context.getApplicationContext().getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    public boolean isUpdatedExternally(){
        return managerTimeStamp != lastTimeStamp;
    }
    @NonNull
    public Collection<Emoji> getRecentEmojis() {
        if (emojiSet.isEmpty()) {

            final String savedRecentEmojis = sharedPreferences.getString(RECENT_EMOJIS, "");

            if (!savedRecentEmojis.isEmpty()) {
                final StringTokenizer stringTokenizer = new StringTokenizer(savedRecentEmojis, EMOJI_DELIMITER);

                while (stringTokenizer.hasMoreTokens()) {
                    final String token = stringTokenizer.nextToken();

                    final String[] parts = token.split(TIME_DELIMITER);

                    if (parts.length == 4) {
                        final Emoji emoji = EmojiManager.getInstance().findEmoji(parts[0]);

                        if (emoji != null && emoji.getLength() == parts[0].length()) {
                            final long timestamp = Long.parseLong(parts[1]);
                            if (timestamp > lastTimeStamp) lastTimeStamp = timestamp;
                            final long clickCount = Long.parseLong(parts[2]);
                            final long indexRef = Long.parseLong(parts[3]);

                            emojiSet.add(new Data(emoji, timestamp, clickCount, indexRef));
                        }

                        managerTimeStamp = lastTimeStamp;
                    }
                }
            }
        }

        return emojiSet.getEmojis();
    }

    public void clear() {
        emojiSet.clear();
        totalAdd = 0L;
    }

    @Override
    public void addEmoji(@NonNull final Emoji emoji) {
        long clickCount = 1;
        if (emojiSet.size() > 0) {
            long lastClickCount = emojiSet.last().clickCount;
            if (clickCount < lastClickCount) clickCount = lastClickCount;
        }
        long timeStamp = System.currentTimeMillis();
        if (timeStamp > lastTimeStamp) {
            lastTimeStamp = timeStamp;
            managerTimeStamp = timeStamp;
            totalAdd = 0;
        } else {
            totalAdd++;
        }
        emojiSet.add(new Data(emoji, timeStamp, clickCount, totalAdd));
    }

    @Override
    public void persist() {
        if (!emojiSet.isEmpty()) {
            final StringBuilder stringBuilder = new StringBuilder(emojiSet.size() * EMOJI_GUESS_SIZE);

            for (Data data : emojiSet) {
                stringBuilder.append(data.emoji.getUnicode())
                        .append(TIME_DELIMITER)
                        .append(data.timestamp)
                        .append(TIME_DELIMITER)
                        .append(data.clickCount)
                        .append(TIME_DELIMITER)
                        .append(data.orderRef)
                        .append(EMOJI_DELIMITER);
            }

            stringBuilder.setLength(stringBuilder.length() - EMOJI_DELIMITER.length());

            sharedPreferences.edit().putString(RECENT_EMOJIS, stringBuilder.toString()).apply();
        }
    }

    public int size(){
        return emojiSet.size();
    }

    private static class EmojiSet extends TreeSet<Data> {
        private static final Comparator<Data> COMPARATOR = new Comparator<Data>() {
            @Override
            public int compare(Data data1, Data data2) {
                int c;
                c = Long.compare(data2.getClickCount(), data1.getClickCount());
                if (c != 0) return c;
                c = Long.compare(data2.getTimestamp(), data1.getTimestamp());
                if (c != 0) return c;
                return Long.compare(data2.getOrderRef(), data1.getOrderRef());
            }
        };

        EmojiSet() {
            super(COMPARATOR);
        }

        Collection<Emoji> getEmojis() {
            Collection<Emoji> sortedEmojis = new ArrayList<>(size());

            for (Data data : this) {
                sortedEmojis.add(data.emoji);
            }

            return sortedEmojis;
        }

        @Override
        public boolean add(Data data) {
            Iterator<Data> iterator = iterator();

            Data found = null;
            while (iterator.hasNext()) {
                Data existingData = iterator.next();
                if (existingData.emoji.getBase().equals(data.emoji.getBase())) {
                    found = existingData;
                    iterator.remove();
                    break;
                }
            }
            if (found != null) {
                found.timestamp = data.timestamp;
                found.clickCount++;
                data = found;
            } else if (size() >= MAX_RECENTS) {
                remove(last());
            }
            return super.add(data);
        }
    }

    static class Data {
        final Emoji emoji;
        long timestamp;
        long clickCount;
        long orderRef;

        Data(final Emoji emoji, final long timestamp, final long clickCount, final long orderRef) {
            this.emoji = emoji;
            this.timestamp = timestamp;
            this.clickCount = clickCount;
            this.orderRef = orderRef;
        }


        long getTimestamp() {
            return timestamp;
        }

        long getClickCount() {
            return clickCount;
        }

        long getOrderRef() {
            return orderRef;
        }
    }
}
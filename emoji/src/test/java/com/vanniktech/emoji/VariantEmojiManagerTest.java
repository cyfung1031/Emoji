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

import androidx.test.core.app.ApplicationProvider;

import com.vanniktech.emoji.emoji.Emoji;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public final class VariantEmojiManagerTest {
//  @Rule public final ExpectedException expectedException = ExpectedException.none();

    private VariantEmojiManager variantEmojiManager;

    private Emoji variant1;
    private Emoji variant2;
    private Emoji variant3;
    private Emoji base;
    private Emoji emoji;

    @Before
    public void setUp() {
        variantEmojiManager = new VariantEmojiManager(ApplicationProvider.getApplicationContext());

        emoji = new Emoji(0x1f437, new String[]{"test"}, R.drawable.emoji_recent, false);
        variant1 = new Emoji(0x1f55b, new String[]{"test"}, R.drawable.emoji_recent, false);
        variant2 = new Emoji(0x1f55c, new String[]{"test"}, R.drawable.emoji_recent, false);
        variant3 = new Emoji(0x1f55d, new String[]{"test"}, R.drawable.emoji_recent, false);
        base = new Emoji(0x1f55a, new String[]{"test"}, R.drawable.emoji_recent, false, variant1, variant2, variant3);
    }

    @Test
    public void getVariantDefault() {

        Assertions.assertEquals(variantEmojiManager.getVariant(emoji), emoji);
    }

    @Test
    public void getVariantUsingOnlyVariants() {
        variantEmojiManager.addVariant(variant2);

        Assertions.assertEquals(variantEmojiManager.getVariant(base), variant2);
        Assertions.assertEquals(variantEmojiManager.getVariant(variant1), variant2);
        Assertions.assertEquals(variantEmojiManager.getVariant(variant2), variant2);
        Assertions.assertEquals(variantEmojiManager.getVariant(variant3), variant2);
    }

    @Test
    public void getVariantUsingOnlyVariantsBeforeBase() {
        variantEmojiManager.addVariant(variant1);
        variantEmojiManager.addVariant(base);

        Assertions.assertEquals(variantEmojiManager.getVariant(variant1), base);
    }

    @Test
    public void getVariantUsingSame() {
        variantEmojiManager.addVariant(variant1);
        variantEmojiManager.addVariant(variant1);

        Assertions.assertEquals(variantEmojiManager.getVariant(variant1), variant1);
    }

    @Test
    public void persist() {
        variantEmojiManager.addVariant(variant1);
        variantEmojiManager.addVariant(variant2);

        variantEmojiManager.persist();

        EmojiManager.install(TestEmojiProvider.from(variant1, variant2));
        final VariantEmojiManager sharedPrefsManager = new VariantEmojiManager(ApplicationProvider.getApplicationContext());

        Assertions.assertEquals(sharedPrefsManager.getVariant(base), variant2);
    }

    @Test
    public void persistEmpty() {
        variantEmojiManager.persist();

        EmojiManager.install(TestEmojiProvider.from(variant1, variant2));
        final VariantEmojiManager sharedPrefsManager = new VariantEmojiManager(ApplicationProvider.getApplicationContext());

        Assertions.assertEquals(sharedPrefsManager.getVariant(base), base);
    }
}
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import android.os.Looper;
import android.text.InputFilter;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.test.core.app.ApplicationProvider;

import com.pushtorefresh.private_constructor_checker.PrivateConstructorChecker;
import com.vanniktech.emoji.emoji.Emoji;
import com.vanniktech.emoji.emoji.EmojiCategory;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class UtilsTest {

  @Test
  public void constructorShouldBePrivate() {
    AssertionError thrown = assertThrows(
            AssertionError.class,
            () -> PrivateConstructorChecker.forClass(Utils.class)
                    .expectedTypeOfException(AssertionError.class)
                    .expectedExceptionMessage("No instances.")
                    .check()
    );
    assertThat(thrown.getMessage()).isEqualTo("No instances.");
  }

  @Test public void checkNull() {
    assertThrows(NullPointerException.class, () -> {
      Objects.requireNonNull(null, "param is null");
    }, "param is null");
  }


  @Test public void checkNotNull() {
    Objects.requireNonNull("valid", "null is null");
  }


  @Test
  public void asListFilter() {
    final Emoji[] emojis = new Emoji[]{
            new Emoji("\u1234".codePointAt(0), new String[]{"test"}, R.drawable.emoji_backspace, false),
            new Emoji("\u1234".codePointAt(0), new String[]{"test"}, R.drawable.emoji_backspace, true),
    };

    final List<Emoji> filtered = Utils.asListWithoutDuplicates(emojis);

    assertThat(filtered).containsExactly(emojis[0]);
  }

  @Test
  public void filterTest01(){

    EditText editText = new EditText(ApplicationProvider.getApplicationContext());

    final List<InputFilter> filters = new ArrayList<>(Arrays.asList(editText.getFilters()));
    filters.add(new OnlyEmojisInputFilter());
    editText.setFilters(filters.toArray(new InputFilter[0]));
  }
  @Test
  public void filterTest02(){

    EditText editText = new EditText(ApplicationProvider.getApplicationContext());

    final List<InputFilter> filters = new ArrayList<>(Arrays.asList(editText.getFilters()));
    filters.add(new MaximalNumberOfEmojisInputFilter(1));
    editText.setFilters(filters.toArray(new InputFilter[0]));
  }

  @Test
  public void imageLoaderTest01(){
    ParallelPreloadImages.main(ApplicationProvider.getApplicationContext(), new EmojiCategory[]{});
  }
  @Test
  public void imageLoaderTest02(){
    final Emoji emoji = new Emoji(0x1f437, new String[]{"test"}, R.drawable.emoji_recent, false);
    new ImageLoadingTask(new ImageView(ApplicationProvider.getApplicationContext()), emoji);
  }
  @Test
  public void imageLoaderTest03(){
    final Emoji emoji = new Emoji(0x1f437, new String[]{"test"}, R.drawable.emoji_recent, false);
    new ImageLoadingTaskSimple(new ImageView(ApplicationProvider.getApplicationContext()), emoji);
  }

  private CountDownLatch lock = new CountDownLatch(1);
  @Test
  public void eventTest() throws InterruptedException {
    CustomEventDispatcher customEventDispatcher =  new CustomEventDispatcher(Looper.myLooper());
    final int[] k = new int[]{1};
    customEventDispatcher.addEventListener("test", new CustomEventDispatcher.EventListener() {
      @Override
      public void onEvent(Object eventObject) {

        k[0]++;
      }
    });
    customEventDispatcher.dispatchEvent("test", null);

    lock.await(40, TimeUnit.MILLISECONDS);

    assertThat(k[0]).isEqualTo(2);

  }

}

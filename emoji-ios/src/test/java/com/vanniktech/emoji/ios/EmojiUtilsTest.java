package com.vanniktech.emoji.ios;

import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.EmojiUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import java.util.Arrays;
import java.util.List;

public class EmojiUtilsTest {

  @Before
  public void setUp() {
    EmojiManager.install(new IosEmojiProvider());
  }

  @Test
  public void starWithVariantSelector() {
    String s = "⭐️⭐️⭐️";
    Assert.assertEquals(true, EmojiUtils.isOnlyEmojis(s));
    Assert.assertEquals(3, EmojiUtils.emojisCount(s));
  }

  @Ignore("https://github.com/vanniktech/Emoji/issues/485")
  @Test
  public void isOnlyEmojis() {
    List<String> emojis = Arrays.asList(
            "🗯",
            "🗨",
            "🕳",
            "❤️",
            "❣️️"
    );

    for (String emoji : emojis) {
      Assert.assertEquals(false, EmojiUtils.isOnlyEmojis("f" + emoji));
      Assert.assertEquals(false, EmojiUtils.isOnlyEmojis(emoji + "f"));
      Assert.assertEquals(1, EmojiUtils.emojisCount(emoji));
      Assert.assertEquals(true, EmojiUtils.isOnlyEmojis(emoji));
    }
  }
}

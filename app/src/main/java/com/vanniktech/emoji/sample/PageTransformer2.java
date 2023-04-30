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

import android.view.View;

import androidx.viewpager2.widget.ViewPager2;

public final class PageTransformer2 implements ViewPager2.PageTransformer {
  private static final float MIN_SCALE = 0.9f;
  private static final float MIN_ALPHA = 0.1f;

  @Override public void transformPage(final View page, final float position) {
    if (position < -1) {  // [-Infinity,-1)
      // This page is way off-screen to the left.
      page.setAlpha(0);
    } else if (position <= 1) { // [-1,1]
      float diff = 1 - Math.abs(position);
      float scale = Math.max(MIN_SCALE, diff);
      float alpha = Math.max(MIN_ALPHA, diff);
      page.setScaleX(scale);
      page.setScaleY(scale);
      page.setAlpha(alpha);
    } else {  // (1,+Infinity]
      // This page is way off-screen to the right.
      page.setAlpha(0);
    }
  }
}

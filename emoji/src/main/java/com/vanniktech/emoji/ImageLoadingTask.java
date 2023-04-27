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
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.vanniktech.emoji.emoji.Emoji;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

final class ImageLoadingTask {
  /* package-private */ final WeakReference<ImageView> imageViewReference;
  /* package-private */ final WeakReference<Context> contextReference;
  /* package-private */ final Emoji emoji;
  private static final ExecutorService executor = Executors.newSingleThreadExecutor();
  private Future<?> future;

  ImageLoadingTask(final ImageView imageView, final Emoji emoji) {
    imageViewReference = new WeakReference<>(imageView);
    contextReference = new WeakReference<>(imageView.getContext());
    this.emoji = emoji;
    future = executor.submit(loadImageRunnable);
  }

  private final Runnable loadImageRunnable = new Runnable() {
    @Override
    public void run() {
      final Context context = contextReference.get();

      if (context != null) {
        final Drawable drawable = emoji.getDrawable(context);
        imageViewReference.get().post(new Runnable() {
          @Override
          public void run() {
            ImageView imageView = imageViewReference.get();
            if (imageView != null) {
              imageView.setImageDrawable(drawable);
            }
          }
        });
      }
    }
  };

  void cancel() {
    if (future != null) {
      future.cancel(true);
      future = null;
    }
  }
}
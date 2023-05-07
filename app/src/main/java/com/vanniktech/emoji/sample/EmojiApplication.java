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

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.StrictMode;
import android.widget.PopupWindow;

import androidx.appcompat.app.AppCompatDelegate;

import com.vanniktech.emoji.EmojiImageViewG;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.EmojiViewInner;
import com.vanniktech.emoji.emoji.Emoji;
import com.vanniktech.emoji.ios.IosEmojiProvider;

public class EmojiApplication extends Application {
  @Override
  public void onCreate() {
    super.onCreate();
    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    EmojiManager.install(new IosEmojiProvider());

    boolean isDebuggable = 0 != (getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE);
    if (isDebuggable) {
      enableStrictMode();
    }
  }

  private void enableStrictMode() {
    /*

    StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().build());
    StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().build());
     */
    // 监测当前线程（UI线程）上的网络、磁盘读写等耗时操作
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
              .detectDiskReads()  // 监测读磁盘
              .detectDiskWrites()  // 监测写磁盘
              .detectNetwork()      // 监测网络操作
              .detectCustomSlowCalls()  // 监测哪些方法执行慢
              .detectResourceMismatches()  // 监测资源不匹配
              .penaltyLog()   // 打印日志，也可设置为弹窗提示penaltyDialog()或者直接使进程死亡penaltyDeath()
              .penaltyDropBox()  //监测到将信息存到Dropbox文件夹 data/system/dropbox
              .build());
    }

    // 监测VM虚拟机进程级别的Activity泄漏或者其它资源泄漏
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
              .detectActivityLeaks()  // 监测内存泄露情况
              .detectLeakedSqlLiteObjects()  // SqlLite资源未关闭，如cursor
              .detectLeakedClosableObjects()  // Closable资源未关闭，如文件流
              .detectCleartextNetwork()  // 监测明文网络
              .setClassInstanceLimit(PopupWindow.class, 3)  // 设置某个类的实例上限，可用于内存泄露提示
              .setClassInstanceLimit(Emoji.class, 400)  // 设置某个类的实例上限，可用于内存泄露提示
              .setClassInstanceLimit(EmojiImageViewG.class, 3)  // 设置某个类的实例上限，可用于内存泄露提示
              .setClassInstanceLimit(EmojiPopup.class, 3)  // 设置某个类的实例上限，可用于内存泄露提示
              .setClassInstanceLimit(EmojiViewInner.class, 3)  // 设置某个类的实例上限，可用于内存泄露提示
              .detectLeakedRegistrationObjects()  // 监测广播或者ServiceConnection是否有解注册
              .penaltyLog()
              .build());
    }
  }
}

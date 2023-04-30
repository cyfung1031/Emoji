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

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.vanniktech.emoji.emoji.Emoji;
import com.vanniktech.emoji.emoji.EmojiCategory;
import com.vanniktech.emoji.listeners.RepeatListener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;

@SuppressLint("ViewConstructor") public class EmojiView2A extends LinearLayout {
  private static final long INITIAL_INTERVAL = 500; //ms
  private static final int NORMAL_INTERVAL = 50; // ms

  @ColorInt private  int themeAccentColor;
  @ColorInt private  int themeIconColor;

  private  ImageButton[] emojiTabs;
  private  ViewPager2 emojiViewPager2;

  private int emojiTabLastSelectedIndex = -1;



  RecentEmoji recentEmoji = null;
  VariantEmoji variantEmoji = null;

  private int isEmojiRecentUpdated = 0;


   EmojiPager2Util emojiPager2Util;

  public EmojiPager2Util getEmojiPager2Util() {
    return emojiPager2Util;
  }

  @SuppressWarnings({ "PMD.CyclomaticComplexity", "PMD.NPathComplexity" }) public EmojiView2A(final Context context)  {
    super(context);
  }

  public void viewInitialize(){
    // for override

    Context context =getContext();


    View.inflate(context, R.layout.emoji_view2, this);

    setOrientation(VERTICAL);
    setBackgroundColor(Utils.resolveColor(context, R.attr.emojiBackground, R.color.emoji_background));
    themeIconColor = Utils.resolveColor(context, R.attr.emojiIcons, R.color.emoji_icons);

    final TypedValue value = new TypedValue();
    context.getTheme().resolveAttribute(R.attr.colorAccent, value, true);
    themeAccentColor = value.data;

    final View emojiDivider = findViewById(R.id.emojiViewDivider);
    emojiDivider.setBackgroundColor(Utils.resolveColor(context, R.attr.emojiDivider, R.color.emoji_divider));

  }

  public void setup(FragmentActivity fragmentActivity,View rootView,  CustomEventDispatcher mEventDispatcher){

    Context context =getContext();

    final EmojiView2A.BuilderBase builder = new EmojiView2A.BuilderBase ();


    this.recentEmoji = builder.recentEmoji = new RecentEmojiManager(context);
    this.variantEmoji = builder.variantEmoji = new VariantEmojiManager(context);


    final EmojiVariantPopup2 variantPopup = new EmojiVariantPopup2(rootView, mEventDispatcher);

    this.mEventDispatcher = mEventDispatcher;


    emojiPager2Util = new EmojiPager2Util(recentEmoji, variantEmoji);

    viewInitialize();

    final ViewPager2 emojisPager = findViewById(R.id.emojiViewPager);

    final LinearLayout emojisTab = findViewById(R.id.emojiViewTab);

    final EmojiCategory[] categories = EmojiManager.getInstance().getCategories();
    emojiTabs = new ImageButton[emojiPager2Util.recentAdapterItemCount() + categories.length + 1];


    if (emojiPager2Util.hasRecentEmoji()) {
      emojiTabs[0] = inflateButton(context, R.drawable.emoji_recent, R.string.emoji_category_recent, emojisTab);
    }

    for (int i = 0; i < categories.length; i++) {
      emojiTabs[i + emojiPager2Util.recentAdapterItemCount()] = inflateButton(context, categories[i].getIcon(), categories[i].getCategoryName(), emojisTab);
    }

    emojiTabs[emojiTabs.length - 1] = inflateButton(context, R.drawable.emoji_backspace, R.string.emoji_backspace, emojisTab);

    for (int i = 0; i < emojiTabs.length - 1; i++) {
      emojiTabs[i].setOnClickListener(this::emojiTabClickHandler);
    }
    emojiTabs[emojiTabs.length - 1].setOnTouchListener(new RepeatListener(INITIAL_INTERVAL, NORMAL_INTERVAL, this::onBackSpaceToolButtonRepeatedlyClicked));



    emojiViewPager2 = emojisPager;
    emojiViewPager2.setOffscreenPageLimit(1);
    ArrayList<ArrayList<Emoji>> emojiPages = new ArrayList<>();
    EmojiCategory[] emojiCategories =  emojiPager2Util.getEmojiCategories();

    for (EmojiCategory emojiCategory : emojiCategories) {
      Emoji[] emojiList = emojiCategory.getEmojis();
      emojiPages.add(new ArrayList<Emoji>(Arrays.asList(emojiList)));
    }

    final EmojiView2A.EmojiPager2Adapter emojiPager2Adapter = new EmojiView2A.EmojiPager2Adapter(fragmentActivity, emojiPages, emojiPager2Util);


    emojiPager2Adapter.setEmojiViewConf(new EmojiViewConf(){

      @Override public void onEmojiClick(@NonNull final EmojiImageView imageView, @NonNull final Emoji emoji) {

        EmojiView2A.this.mEventDispatcher.dispatchEvent("emojiClick", emoji);
      }
      @Override public void onEmojiLongClick(@NonNull final EmojiImageView view, @NonNull final Emoji emoji) {
        variantPopup.show(view, emoji);

      }

    });

    emojiViewPager2.setAdapter(emojiPager2Adapter);
    ViewPager2.OnPageChangeCallback pageChangeCallback = new ViewPager2.OnPageChangeCallback() {

      @Override
      public void onPageScrollStateChanged(int state) {
        super.onPageScrollStateChanged(state);
        if(isEmojiRecentUpdated == 1) {
          if(emojiTabLastSelectedIndex == 0) {
            isEmojiRecentUpdated = 2;
          }else{
            emojiPager2Adapter.invalidateRecentEmojis();
            isEmojiRecentUpdated = 0;
          }
        }else if(isEmojiRecentUpdated == 2 && state==ViewPager2.SCROLL_STATE_SETTLING){

          if(emojiTabLastSelectedIndex != 0) {
            isEmojiRecentUpdated = 1;
            emojiViewPager2.post(new Runnable() {
              @Override
              public void run() {
                emojiPager2Adapter.invalidateRecentEmojis();
                isEmojiRecentUpdated = 0;
              }
            });
          } else {
            isEmojiRecentUpdated = 1;
          }
        }
      }

      @Override public void onPageSelected(final int i) {
        if (emojiTabLastSelectedIndex != i) {
          final int lastSelectedIndex = emojiTabLastSelectedIndex;
          emojiTabLastSelectedIndex = i;

          if (lastSelectedIndex >= 0 && lastSelectedIndex < emojiTabs.length) {
            emojiTabs[lastSelectedIndex].setSelected(false);
            emojiTabs[lastSelectedIndex].setColorFilter(themeIconColor, PorterDuff.Mode.SRC_IN);
          }

          emojiTabs[i].setSelected(true);
          emojiTabs[i].setColorFilter(themeAccentColor, PorterDuff.Mode.SRC_IN);

        }



      }
    };

    emojiViewPager2.registerOnPageChangeCallback(pageChangeCallback);

    final int startIndex = emojiPager2Util.hasRecentEmoji() ? emojiPager2Util.numberOfRecentEmojis() > 0 ? 0 : 1 : 0;
    emojisPager.setCurrentItem(startIndex);
    pageChangeCallback.onPageSelected(startIndex);



    mEventDispatcher.addEventListener("emojiClick", new CustomEventDispatcher.EventListener() {
      @Override
      public void onEvent(Object eventObject) {

        isEmojiRecentUpdated = 1;
      }
    });



    HandlerThread bHt = new HandlerThread("backgroundWorkOnEmojiView");
    if(!bHt.isAlive()) bHt.start();

    Handler bH = new Handler(bHt.getLooper());

    final RecentEmoji recentEmoji = this.recentEmoji;
    final VariantEmoji variantEmoji= this.variantEmoji;

    mEventDispatcher.addEventListener("emojiClick", new CustomEventDispatcher.EventListener() {
      @Override
      public void onEvent(Object eventObject) {
        variantPopup.dismiss();


        final Emoji emoji = (Emoji) eventObject;

        bH.post(new Runnable() {
          @Override
          public void run() {

            recentEmoji.addEmoji(emoji);
            variantEmoji.addVariant(emoji);

            recentEmoji.persist();
            variantEmoji.persist();
          }
        });


      }
    });


    // load all images async
//    emojiViewPager2.postDelayed(new Runnable() {
//      @Override
//      public void run() {
//        ParallelPreloadImages.main(context, emojiCategories);
//      }
//    },400);


  }



  public void emojiTabClickHandler(View v){

    int oldPosition = emojiTabLastSelectedIndex;
    int newPosition = -1;
    for(int i =0; i<emojiTabs.length;i++){
      if(emojiTabs[i] == v){
        newPosition = i;
        break;
      }
    }
    if(newPosition>=0 && oldPosition != newPosition){
      emojiViewPager2.setCurrentItem(newPosition, true);
    }
  }

  private void onBackSpaceToolButtonRepeatedlyClicked(final View view){
    mEventDispatcher.dispatchEvent("backSpaceToolButton", (Object) null);
  }

  private ImageButton inflateButton(final Context context, @DrawableRes final int btnDrawableResId, @StringRes final int categoryName, final ViewGroup parent) {
    final ImageButton button = (ImageButton) LayoutInflater.from(context).inflate(R.layout.emoji_view_category, parent, false);

    button.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), btnDrawableResId, null));
    button.setColorFilter(themeIconColor, PorterDuff.Mode.SRC_IN);
    button.setContentDescription(context.getString(categoryName));

    parent.addView(button);

    return button;
  }

  CustomEventDispatcher mEventDispatcher = null;


  public static class BuilderBase   {

    @NonNull RecentEmoji recentEmoji;
    @NonNull VariantEmoji variantEmoji;



  }




  public class EmojiPager2Adapter extends FragmentStateAdapter {
    private ArrayList<ArrayList<Emoji>> emojiPages;


    private EmojiViewConf emojiViewConf = null;

    public void setEmojiViewConf(EmojiViewConf emojiViewConf) {
      this.emojiViewConf = emojiViewConf;
    }

    public EmojiViewConf getEmojiViewConf() {
      return emojiViewConf;
    }

    boolean hasRecentEmoji = false;
    final EmojiPager2Util emojiPager2Util;
    public EmojiPager2Adapter(@NonNull FragmentActivity fragmentActivity, ArrayList<ArrayList<Emoji>> emojiPages, EmojiPager2Util emojiPager2Util) {
      super(fragmentActivity);
      this.emojiPages = emojiPages;
      hasRecentEmoji =emojiPager2Util.hasRecentEmoji();
      this.emojiPager2Util = emojiPager2Util;
    }


    CustomEventDispatcher mEventDispatcher = null;

    private WeakReference<EmojiPageFragment> recentEmojiGridViewWR = null;

    @NonNull
    @Override
    public Fragment createFragment(int position) {
      EmojiPageFragment fragment;
      if(position == 0 && hasRecentEmoji){
        fragment = new EmojiPageFragment(new ArrayList<>(emojiPager2Util.recentEmoji.getRecentEmojis()));
        fragment.setEmojiViewConf(emojiViewConf);
        recentEmojiGridViewWR = new WeakReference<>(fragment);
      }else {
        int posOffset = hasRecentEmoji?1:0;
        fragment = new EmojiPageFragment(emojiPages.get(position - posOffset));
        fragment.setEmojiViewConf(emojiViewConf);
      }
      fragment.setEventDispatcher(mEventDispatcher);
      return fragment;
    }

    @Override
    public int getItemCount() {
      return emojiPages.size() + (hasRecentEmoji?1:0);
    }



    public void invalidateRecentEmojis() {
      EmojiPageFragment recentEmojiGridView = recentEmojiGridViewWR!=null ? recentEmojiGridViewWR.get()  : null;
      if (recentEmojiGridView != null) {
//            recentEmojiGridView
//                    emojiArrayAdapter.updateEmojis(recentEmojis.getRecentEmojis());
        if(emojiPager2Util!= null && emojiPager2Util.recentEmoji != null){

          recentEmojiGridView.invalidateEmojis(emojiPager2Util.recentEmoji.getRecentEmojis());
        }

      }
    }
  }



}
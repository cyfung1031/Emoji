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

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.provider.FontRequest;
import androidx.emoji.text.EmojiCompat;
import androidx.emoji.text.FontRequestEmojiCompatConfig;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.EmojiPopupBoard;
import com.vanniktech.emoji.facebook.FacebookEmojiProvider;
import com.vanniktech.emoji.google.GoogleEmojiProvider;
import com.vanniktech.emoji.googlecompat.GoogleCompatEmojiProvider;
import com.vanniktech.emoji.ios.IosEmojiProvider;
import com.vanniktech.emoji.material.MaterialEmojiLayoutFactory;
import com.vanniktech.emoji.twitter.TwitterEmojiProvider;
// import timber.log.Timber;

// We don't care about duplicated code in the sample.
public class MainActivityBase extends AppCompatActivity {
    static final String TAG = "MainActivity";

    ChatAdapter chatAdapter;
    EmojiPopupBoard emojiPopup;
    ViewGroup rootView;
    ImageView emojiButton;
    EmojiCompat emojiCompat;
    private EditText mEditText;

    public EditText getEditText() {
        return null;
    }

    public void setEditText(EditText editText) {
    }

    public void setupOnCreate() {

    }

    @Override
    @SuppressLint("SetTextI18n")
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        getLayoutInflater().setFactory2(new MaterialEmojiLayoutFactory((LayoutInflater.Factory2) getDelegate()));

        super.onCreate(savedInstanceState);

        setupOnCreate();

        final RecyclerView recyclerView = findViewById(R.id.main_activity_recycler_view);
        recyclerView.setAdapter(chatAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));


        final EditText editText = getEditText();

        emojiPopup = EmojiPopupBoard.create(rootView, editText);

        emojiPopup.setPageTransformer(new PageTransformer());
        emojiPopup.setup();
                /*
            .setOnEmojiBackspaceClickListener(ignore -> Log.d(TAG, "Clicked on Backspace"))
            .setOnEmojiClickListener((ignore, ignore2) -> Log.d(TAG, "Clicked on emoji"))
            .setOnEmojiPopupShownListener(() -> emojiButton.setImageResource(R.drawable.ic_keyboard))
            .setOnSoftKeyboardOpenListener(ignore -> Log.d(TAG, "Opened soft keyboard"))
            .setOnEmojiPopupDismissListener(() -> emojiButton.setImageResource(R.drawable.emoji_ios_category_smileysandpeople))
            .setOnSoftKeyboardCloseListener(() -> Log.d(TAG, "Closed soft keyboard"))
            .setKeyboardAnimationStyle(R.style.emoji_fade_animation_style)
                */
        //.setRecentEmoji(NoRecentEmoji.INSTANCE) // Uncomment this to hide recent emojis.

        // emojiPopup.getEmojiViewBoard().setPageTransformer(new PageTransformer());


    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean customOptionsItemSelected(MenuItem item, int itemId) {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (customOptionsItemSelected(item, itemId)) return true;

        if (itemId == R.id.menuMainShowDialog) {
            emojiPopup.dismiss();
            MainDialog.show(this);
            return true;
        } else if (itemId == R.id.menuMainVariantIos) {
            EmojiManager.destroy();
            EmojiManager.install(new IosEmojiProvider());
            recreate();
            return true;
        } else if (itemId == R.id.menuMainGoogle) {
            EmojiManager.destroy();
            EmojiManager.install(new GoogleEmojiProvider());
            recreate();
            return true;
        } else if (itemId == R.id.menuMainTwitter) {
            EmojiManager.destroy();
            EmojiManager.install(new TwitterEmojiProvider());
            recreate();
            return true;
        } else if (itemId == R.id.menuMainFacebook) {
            EmojiManager.destroy();
            EmojiManager.install(new FacebookEmojiProvider());
            recreate();
            return true;
        } else if (itemId == R.id.menuMainGoogleCompat) {
            if (emojiCompat == null) {
                emojiCompat = EmojiCompat.init(new FontRequestEmojiCompatConfig(this,
                        new FontRequest("com.google.android.gms.fonts", "com.google.android.gms", "Noto Color Emoji Compat", R.array.com_google_android_gms_fonts_certs)
                ).setReplaceAll(true));
            }
            EmojiManager.destroy();
            EmojiManager.install(new GoogleCompatEmojiProvider(emojiCompat));
            recreate();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }

    }


    @Override
    protected void onDestroy() {
        // dismiss the popup window when the activity is about to be destroyed
        if (emojiPopup != null && emojiPopup.isShowing()) {
            emojiPopup.dismiss();
        }
        super.onDestroy();
    }


    //    @Override
//    public void recreate() {
//        // Get a reference to the fragment manager
//        FragmentManager fragmentManager = getSupportFragmentManager();
//
//        // Begin a new transaction to remove and add the fragments to the fragment manager
//        FragmentTransaction transaction = fragmentManager.beginTransaction();
//
//        // Remove all fragments from the fragment manager
//        for (Fragment fragment : fragmentManager.getFragments()) {
//            transaction.remove(fragment);
//        }
//
//        // Commit the transaction
//        transaction.commit();
//
//        // Call the superclass method to finish recreating the activity
//        super.recreate();
//    }


    @Override
    public void onBackPressed() {
        if (emojiPopup != null && emojiPopup.isShowing()) {
            emojiPopup.dismiss();
        } else {
            super.onBackPressed();
        }
    }

}

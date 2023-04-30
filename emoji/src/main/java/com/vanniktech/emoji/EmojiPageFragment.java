package com.vanniktech.emoji;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vanniktech.emoji.emoji.Emoji;

import java.util.ArrayList;
import java.util.Collection;

public class EmojiPageFragment extends Fragment {
    private RecyclerView emojiRecyclerView;
    final int overSpaceLines = 2;
    private EmojiAdapter emojiAdapter;
    private ArrayList<Emoji> emojis;


    private EmojiViewConf emojiViewConf = null;

    public void setEmojiViewConf(EmojiViewConf emojiViewConf) {
        this.emojiViewConf = emojiViewConf;
    }

    public EmojiViewConf getEmojiViewConf() {
        return emojiViewConf;
    }

    public EmojiPageFragment(ArrayList<Emoji> emojis) {
        this.emojis = emojis;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // Set the windowSoftInputMode to adjustPan or adjustNothing
        if (getActivity() != null) {
//            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            // For adjustNothing, use the following line instead:
            // getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        }

        emojiRecyclerView = (RecyclerView) createEmojiPageContainer(container);

        emojiRecyclerView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//        emojiRecyclerView.setHasFixedSize(true);

        emojiAdapter = new EmojiAdapter(emojis);
        emojiAdapter.setEmojiViewConf(emojiViewConf);
        emojiAdapter.setEventDispatcher(mEventDispatcher);
        emojiRecyclerView.setAdapter(emojiAdapter);

        Resources resources = getResources();
        final int columnWidth = resources.getDimensionPixelSize(R.dimen.emoji_grid_view_column_width);
        final int spacing = resources.getDimensionPixelSize(R.dimen.emoji_grid_view_spacing);
//
//        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 1, GridLayoutManager.HORIZONTAL, false) {
//            @Override
//            public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
//                super.onLayoutChildren(recycler, state);
//                int width = getWidth() - getPaddingLeft() - getPaddingRight();
//                int columnCount = Math.max(1, width / (columnWidth * 2 + spacing));
//                setSpanCount(columnCount);
//            }
//        };

        final int mOverSpace = (columnWidth + spacing) * overSpaceLines;

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 1, GridLayoutManager.VERTICAL, false) {

            int overSpace = mOverSpace;
            @Override
            public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
                super.onLayoutChildren(recycler, state);
                int width = getWidth() - getPaddingLeft() - getPaddingRight();
                int columnCount = Math.max(1, width / (columnWidth ));
                setSpanCount(columnCount);
            }



            @Override
            protected void calculateExtraLayoutSpace(@NonNull RecyclerView.State state, @NonNull int[] extraLayoutSpace) {

                super.calculateExtraLayoutSpace(state, extraLayoutSpace);
                extraLayoutSpace[0] += overSpace;
                extraLayoutSpace[1] += overSpace;
            }
        };
        gridLayoutManager.setItemPrefetchEnabled(true);
        gridLayoutManager.setInitialPrefetchItemCount(64);


        emojiRecyclerView.setPadding(spacing, spacing, spacing, spacing);

        emojiRecyclerView.setLayoutManager(gridLayoutManager);

        emojiRecyclerView.addItemDecoration(new SpacingItemDecoration(spacing));

        emojiRecyclerView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return emojiRecyclerView;
    }

    private View createEmojiPageContainer(ViewGroup container) {

        final Context context = getContext();
        if(context == null ) return null;

// Create a new RecyclerView instance
        RecyclerView emojiRecyclerView = new RecyclerView(context);

// Set the layout manager to a GridLayoutManager with 3 columns
        int spanCount = 3;
        GridLayoutManager layoutManager = new GridLayoutManager(context, spanCount);
        emojiRecyclerView.setLayoutManager(layoutManager);

// Set the RecyclerView's layout parameters to match the parent
        emojiRecyclerView.setLayoutParams(new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

// Set any other desired properties on the RecyclerView
        emojiRecyclerView.setHasFixedSize(true);
        emojiRecyclerView.setNestedScrollingEnabled(false);

// Add the RecyclerView to its parent ViewGroup
        if(container != null) {
            container.addView(emojiRecyclerView);
        }
        return emojiRecyclerView;
    }

    CustomEventDispatcher mEventDispatcher = null;

    public void setEventDispatcher(CustomEventDispatcher myEventDispatcher) {
        mEventDispatcher = myEventDispatcher;
    }

    private class SpacingItemDecoration extends RecyclerView.ItemDecoration {
        private final int spacing;

        SpacingItemDecoration(int spacing) {
            this.spacing = spacing;
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {

                outRect.left = spacing;
                outRect.right =spacing;
                outRect.top = spacing;
                outRect.bottom = spacing;

        }
    }
    @SuppressLint("NotifyDataSetChanged")
    public void invalidateEmojis(Collection<Emoji> newEmoji){

        synchronized (emojiAdapter) {
            emojis.clear();
            emojis.addAll(newEmoji);
            emojiAdapter.notifyDataSetChanged();
        }

//        emojiAdapter.updateEmojis( recentEmojis.getRecentEmojis());
    }




    public static class EmojiViewHolder extends RecyclerView.ViewHolder {
        public EmojiImageView emojiImageView;

        public EmojiViewHolder(EmojiImageView emojiImageView) {
            super(emojiImageView);
            this.emojiImageView = emojiImageView;
        }
    }
    public static class EmojiAdapter extends RecyclerView.Adapter<EmojiPageFragment.EmojiViewHolder> {
        private ArrayList<Emoji> emojis;

        private EmojiViewConf emojiViewConf = null;

        public void setEmojiViewConf(EmojiViewConf emojiViewConf) {
            this.emojiViewConf = emojiViewConf;
        }

        public EmojiViewConf getEmojiViewConf() {
            return emojiViewConf;
        }

        public EmojiAdapter(ArrayList<Emoji> emojis) {
            this.emojis = emojis;
        }

        @Override
        public EmojiPageFragment.EmojiViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        EmojiImageView emojiImageView = new EmojiImageView(parent.getContext());

            EmojiImageView emojiImageView = (EmojiImageView) LayoutInflater.from(parent.getContext()).inflate(R.layout.emoji_adapter_item, parent, false);;

            if(emojiViewConf != null){
                emojiImageView.setOnEmojiClickListener(emojiViewConf);
                emojiImageView.setOnEmojiLongClickListener(emojiViewConf);
            }



            /*


             */

        /*

          EmojiImageView image = (EmojiImageView) convertView;

    final Context context = getContext();

    if (image == null) {
      image = (EmojiImageView) LayoutInflater.from(context).inflate(R.layout.emoji_adapter_item, parent, false);

      image.setOnEmojiClickListener(listener);
      image.setOnEmojiLongClickListener(longListener);
    }

    final Emoji emoji = checkNotNull(getItem(position), "emoji == null");
    final Emoji variantToUse = variantManager == null ? emoji : variantManager.getVariant(emoji);
    image.setContentDescription(emoji.getUnicode());
    image.setEmoji(variantToUse);

    return image;
         */




            int mDesiredWidth = parent.getResources().getDimensionPixelSize(R.dimen.emoji_grid_view_column_width);

            emojiImageView.setLayoutParams(new ViewGroup.LayoutParams(mDesiredWidth, mDesiredWidth));
            return new EmojiPageFragment.EmojiViewHolder(emojiImageView);
        }

        @Override
        public void onBindViewHolder(EmojiPageFragment.EmojiViewHolder holder, int position) {
            Emoji emoji = emojis.get(position);
            holder.emojiImageView.setEmoji(emoji);





        }

        @Override
        public int getItemCount() {
            return emojis.size();
        }

        CustomEventDispatcher mEventDispatcher = null;

        public void setEventDispatcher(CustomEventDispatcher myEventDispatcher) {
            mEventDispatcher = myEventDispatcher;
        }



    }



}

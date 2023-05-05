package com.vanniktech.emoji;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import com.vanniktech.emoji.emoji.Emoji;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoadDrawableTask {
    private static final LoadDrawableTask INSTANCE = new LoadDrawableTask();
    private final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    protected final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    private LoadDrawableTask() {
    }

    public static LoadDrawableTask getInstance() {
        return INSTANCE;
    }

    public void loadDrawable(final Context context, final ImageView imageView, final Emoji currentEmoji) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                final Drawable drawable = currentEmoji.getDrawable(context);

                mainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImageDrawable(drawable);
                    }
                });
            }
        });
    }
}
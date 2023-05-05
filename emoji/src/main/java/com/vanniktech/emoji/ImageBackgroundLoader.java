package com.vanniktech.emoji;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

import com.vanniktech.emoji.emoji.Emoji;

import java.lang.ref.WeakReference;
import java.util.HashMap;

public class ImageBackgroundLoader {
    static ImageBackgroundLoader globalImageBackgroundLoader = null;
    static ImageBackgroundLoader build(Context context){
        if(globalImageBackgroundLoader != null) return globalImageBackgroundLoader;
        globalImageBackgroundLoader = new ImageBackgroundLoader(context);
        return globalImageBackgroundLoader;
    }
    final protected WeakReference<Context> mContextWR;
    final private Handler myHandler;
    final private HandlerThread myHandlerThread;

    final protected HashMap<Emoji, Drawable> hMap;
    private ImageBackgroundLoader(final Context context){
        hMap = new HashMap<>();
        mContextWR = new WeakReference<>(context);
        myHandlerThread = new HandlerThread("backgroundImageLoader");
        if(!myHandlerThread.isAlive()) myHandlerThread.start();
        myHandler= new MyHandler(this, myHandlerThread.getLooper());
    }

    public void loadImageAsync(final Emoji emoji){
        myHandler.sendMessage(myHandler.obtainMessage(5, emoji));
    }
    public void loadImageAsync(final Runnable runnable){
        myHandler.sendMessage(myHandler.obtainMessage(6, runnable));
    }
    public Drawable loadImageSync(final Emoji emoji){
        Drawable drawable = hMap.get(emoji);
        if(drawable == null){
            final Context context = mContextWR.get();
            if(context == null) return null;
            drawable = emoji.getDrawable(context);
            hMap.put(emoji, drawable);
        }
        return drawable;
    }


    private static class MyHandler extends Handler{

        final private ImageBackgroundLoader ibl;
        public MyHandler(ImageBackgroundLoader ibl, Looper looper){
            super(looper);
            this.ibl = ibl;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            if(msg.what == 5){
                Emoji emoji = (Emoji) msg.obj;
                if(ibl.hMap.containsKey(emoji)) return;
                final Context context = ibl.mContextWR.get();
                if(context == null) return;
                Drawable drawable = emoji.getDrawable(context);
                ibl.hMap.put(emoji, drawable);
            }else if (msg.what == 6){
                Runnable emoji = (Runnable) msg.obj;
                emoji.run();

            }
        }
    }
}

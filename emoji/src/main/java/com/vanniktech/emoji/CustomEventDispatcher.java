package com.vanniktech.emoji;

import android.os.Handler;
import android.os.Looper;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class CustomEventDispatcher {
    private final Handler handler;
    private final ConcurrentHashMap<String, List<EventListener>> eventListeners;

    public CustomEventDispatcher(Looper looper) {
        handler = new Handler(looper);
        eventListeners = new ConcurrentHashMap<>();
    }

    public interface EventListener {
        void onEvent(Object eventObject);
    }

    public void addEventListener(String eventKey, EventListener eventListener) {
        List<EventListener> listeners = eventListeners.get(eventKey);
        if (listeners == null) {
            List<EventListener> newListeners = new CopyOnWriteArrayList<>();
            listeners = eventListeners.putIfAbsent(eventKey, newListeners);
            if (listeners == null) {
                listeners = newListeners;
            }
        }
        listeners.add(eventListener);
    }

    public void removeEventListener(String eventKey, EventListener eventListener) {
        List<EventListener> listeners = eventListeners.get(eventKey);
        if (listeners != null) {
            listeners.remove(eventListener);
            if (listeners.isEmpty()) {
                eventListeners.remove(eventKey);
            }
        }
    }

    public void dispatchEvent(String eventKey, Object eventObject) {
        List<EventListener> listeners = eventListeners.get(eventKey);
        if (listeners != null) {
            for (EventListener eventListener : listeners) {
                handler.post(() -> eventListener.onEvent(eventObject));
            }
        }
    }
}
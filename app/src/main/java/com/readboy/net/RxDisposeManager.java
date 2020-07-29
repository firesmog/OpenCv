package com.readboy.net;


import android.util.ArrayMap;

import java.util.Set;

import io.reactivex.disposables.Disposable;

public class RxDisposeManager {

    private static volatile RxDisposeManager sInstance = null;

    private ArrayMap<Object, Disposable> maps;

    public static RxDisposeManager get() {

        if (sInstance == null) {
            synchronized (RxDisposeManager.class) {
                if (sInstance == null) {
                    sInstance = new RxDisposeManager();
                }
            }
        }
        return sInstance;
    }

    private RxDisposeManager() {
        maps = new ArrayMap<>();
    }


    public void add(Object tag, Disposable disposable) {
        maps.put(tag, disposable);
    }


    public void remove(Object tag) {
        if (!maps.isEmpty()) {
            maps.remove(tag);
        }
    }

    public void removeAll() {
        if (!maps.isEmpty()) {
            maps.clear();
        }
    }


    public void cancel(Object tag) {
        if (maps.isEmpty()) {
            return;
        }
        if (maps.get(tag) == null) {
            return;
        }
        if (!maps.get(tag).isDisposed()) {
            maps.get(tag).dispose();
            maps.remove(tag);
        }
    }

    public void cancelAll() {
        if (maps.isEmpty()) {
            return;
        }
        Set<Object> keys = maps.keySet();
        for (Object apiKey : keys) {
            cancel(apiKey);
        }
    }
}

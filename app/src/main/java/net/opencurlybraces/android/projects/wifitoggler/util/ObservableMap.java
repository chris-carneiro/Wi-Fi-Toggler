package net.opencurlybraces.android.projects.wifitoggler.util;

import android.util.Log;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by chris on 28/07/15.
 */
public class ObservableMap extends java.util.Observable {

    private static final String TAG = "ObservableSystem";
    private final ConcurrentHashMap<String, Boolean> mMap;

    public ObservableMap(int capacity) {
        mMap = new ConcurrentHashMap<>(capacity);
    }

    public void put(String key, boolean isCorrect) {
        if (mMap.get(key) == null || mMap.get(key) != isCorrect) {
            mMap.put(key, isCorrect);
            setChanged();
            notifyObservers(this);
        }
    }

    public boolean get(String key) {
        return (mMap.get(key) != null ? mMap.get(key) : false);
    }

    public ConcurrentHashMap<String, Boolean> getMap() {
        return mMap;
    }
}

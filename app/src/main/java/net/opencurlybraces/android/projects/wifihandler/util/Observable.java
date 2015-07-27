package net.opencurlybraces.android.projects.wifihandler.util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Observer;


/**
 * Based on {@link java.util.Observable}<BR/> <p>This class wraps Observer objects with {@link
 * WeakReference}, an attempt to prevent activity context leaks</p> {@inheritDoc}
 */
public class Observable extends java.util.Observable {

    List<WeakReference<Observer>> mObservers = new ArrayList<>();

    public Observable() {
        super();
    }

    public void addObserver(Observer observer) {
        if (observer == null) {
            throw new NullPointerException("observer == null");
        }
        WeakReference<Observer> weakRefObserver = new WeakReference<>(observer);

        synchronized (this) {
            if (!mObservers.contains(weakRefObserver))
                mObservers.add(weakRefObserver);
        }
    }

    /**
     * Removes the specified observer from the list of observers. Passing null won't do anything.
     *
     * @param observer the observer to remove.
     */
    public synchronized void deleteObserver(Observer observer) {
        if (observer == null) {
            throw new NullPointerException("observer == null");
        }
        WeakReference<Observer> weakRefObserver = new WeakReference<>(observer);
        mObservers.remove(weakRefObserver);
    }

    /**
     * If {@code hasChanged()} returns {@code true}, calls the {@code update()} method for every
     * Observer in the list of observers using the specified argument. Afterwards calls {@code
     * clearChanged()}.
     *
     * @param data the argument passed to {@code update()}.
     */
    @SuppressWarnings ("unchecked")
    public void notifyObservers(Object data) {
        int size = 0;
        WeakReference<Observer>[] arrays = null;
        synchronized (this) {
            if (hasChanged()) {
                clearChanged();
                size = mObservers.size();
                arrays = new WeakReference[size];
                mObservers.toArray(arrays);
            }
        }
        if (arrays != null) {
            for (WeakReference<Observer> weakRefObserver : arrays) {
                Observer observer = weakRefObserver.get();
                if (observer == null) continue;
                observer.update(this, data);
            }
        }
    }
}

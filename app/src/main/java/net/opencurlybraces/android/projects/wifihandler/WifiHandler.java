package net.opencurlybraces.android.projects.wifihandler;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import net.opencurlybraces.android.projects.wifihandler.service.WifiHandlerService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by chris on 24/07/15.
 */
public class WifiHandler extends Application {

    private static final String TAG = "WifiHandlerApp";

    private static ObservableSystemSettings mObservableSystemSettings = null;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        mObservableSystemSettings = new ObservableSystemSettings();

        doSystemSettingsPreCheck();
    }

    private void doSystemSettingsPreCheck() {
        Intent checkSettings = new Intent(this, WifiHandlerService.class);
        checkSettings.setAction(WifiHandlerService.ACTION_STARTUP_SETTINGS_PRECHECK);
        startService(checkSettings);
    }

    public static void registerSettingObserver(Observer observer) {
        Log.d(TAG, "registerSettingObserver");

        mObservableSystemSettings.addObserver(observer);
    }

    public static void unRegisterSettingObserver(Observer observer) {
        Log.d(TAG, "unRegisterSettingObserver");
        mObservableSystemSettings.deleteObserver(observer);
    }

    public static List<String> getIncorrectSettings() {
        Set<Map.Entry<String, Boolean>> settings = mObservableSystemSettings.getSettingsState()
                .entrySet();
        List<String> incorrectSettingsKeys = new ArrayList<>(settings.size());
        for (Map.Entry<String, Boolean> entry : settings) {
            if (!entry.getValue()) {
                incorrectSettingsKeys.add(entry.getKey());
            }
        }
        return incorrectSettingsKeys;
    }

    public static boolean hasWrongSettingsForFirstLaunch() {
        Log.d(TAG, "hasWrongSettingsForFirstLaunch");
        if (mObservableSystemSettings.getSettingsState().containsValue(false)) {
            return true;
        }
        return false;
    }

    public static boolean hasWrongSettingsForAutoToggle() {
        Log.d(TAG, "hasWrongSettingsForAutoToggle");
        Map<String,Boolean> settings = mObservableSystemSettings.getSettingsState();
        for (Map.Entry<String, Boolean> entry : settings.entrySet()) {
            String setting = entry.getKey();
            boolean correct = entry.getValue();
           if(!Config.STARTUP_CHECK_WIFI_SETTINGS.equals(setting) && !correct) {
               return true;
           }
        }
        return false;
    }

    public static boolean isCorrectSetting(String key) {
        Log.d(TAG, "isCorrectSetting");
        return mObservableSystemSettings.getSetting(key);
    }

    public static void setSetting(String key, boolean isCorrect) {
        Log.d(TAG, "setSetting");
        mObservableSystemSettings.setSetting(key, isCorrect);
    }

    private static class ObservableSystemSettings extends Observable {

        private final ConcurrentHashMap<String, Boolean> mSettingsState;

        public ObservableSystemSettings() {
            mSettingsState = new ConcurrentHashMap<>(Config
                    .SYSTEM_SETTINGS_CHECKS);
        }

        public void setSetting(String key, boolean isCorrect) {
            Log.d(TAG, " ObservableSystemSettings setSetting");
            if (mSettingsState.get(key) == null || mSettingsState.get(key) != isCorrect) {
                mSettingsState.put(key, isCorrect);
                setChanged();
                notifyObservers(this);
            }
        }

        public boolean getSetting(String key) {
            Log.d(TAG, " ObservableSystemSettings getSetting");
            return (mSettingsState.get(key) != null ? mSettingsState.get(key) : false);
        }

        public ConcurrentHashMap<String, Boolean> getSettingsState() {
            return mSettingsState;
        }
    }
}

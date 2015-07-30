package net.opencurlybraces.android.projects.wifihandler;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import net.opencurlybraces.android.projects.wifihandler.data.DataAsyncQueryHandler;
import net.opencurlybraces.android.projects.wifihandler.util.ObservableMap;
import net.opencurlybraces.android.projects.wifihandler.util.StartupUtils;

import java.util.Map;
import java.util.Observer;

/**
 * Created by chris on 24/07/15.
 */
public class WifiHandler extends Application {

    private static final String TAG = "WifiHandlerApp";

    private static ObservableMap mObservableSystemSettings = null;
    private DataAsyncQueryHandler mDataAsyncQueryHandler = null;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        if (mDataAsyncQueryHandler == null) {
            mDataAsyncQueryHandler = new DataAsyncQueryHandler(getContentResolver(), null);
        }
        firstStartCheck.execute();
    }

    private AsyncTask<Void, Void, Void> firstStartCheck = new AsyncTask<Void, Void, Void>() {
        @Override
        protected Void doInBackground(Void... params) {
            mObservableSystemSettings = new ObservableMap(Config.SYSTEM_SETTINGS_CHECKS);
            StartupUtils.doSystemSettingsPreCheck(WifiHandler.this);
            return null;
        }
    };

    public static void registerSettingObserver(Observer observer) {
        Log.d(TAG, "registerSettingObserver");

        mObservableSystemSettings.addObserver(observer);
    }

    public static void unRegisterSettingObserver(Observer observer) {
        Log.d(TAG, "unRegisterSettingObserver observer" + observer.getClass());
        mObservableSystemSettings.deleteObserver(observer);
    }

    public static boolean hasWrongSettingsForFirstLaunch() {
        Log.d(TAG, "hasWrongSettingsForFirstLaunch");
        if (mObservableSystemSettings.getMap().containsValue(false)) {
            return true;
        }
        return false;
    }

    public static boolean hasWrongSettingsForAutoToggle() {
        Log.d(TAG, "hasWrongSettingsForAutoToggle");
        Map<String, Boolean> settings = mObservableSystemSettings.getMap();
        for (Map.Entry<String, Boolean> entry : settings.entrySet()) {
            String setting = entry.getKey();
            boolean correct = entry.getValue();
            if (!Config.STARTUP_CHECK_WIFI_SETTINGS.equals(setting) && !correct) {
                return true;
            }
        }
        return false;
    }

    public static boolean isCorrectSetting(String key) {
        Log.d(TAG, "isCorrectSetting");
        return mObservableSystemSettings.get(key);
    }

    public static void setSetting(String key, boolean isCorrect) {
        Log.d(TAG, "put");
        mObservableSystemSettings.put(key, isCorrect);
    }

}

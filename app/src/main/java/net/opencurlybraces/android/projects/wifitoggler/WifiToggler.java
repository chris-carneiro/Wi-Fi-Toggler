package net.opencurlybraces.android.projects.wifitoggler;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import net.opencurlybraces.android.projects.wifitoggler.util.DeletedSavedWifiSweepingTask;
import net.opencurlybraces.android.projects.wifitoggler.util.ObservableMap;
import net.opencurlybraces.android.projects.wifitoggler.util.StartupUtils;

import java.util.Map;
import java.util.Observer;

/**
 * Created by chris on 24/07/15.
 */
public class WifiToggler extends Application {

    private static final String TAG = "WifiTogglerApp";

    private static ObservableMap mObservableSystemSettings = null;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        performDeviceSettingsCheck();
    }

    private void performDeviceSettingsCheck() {
        firstStartCheck.execute();
    }

    private AsyncTask<Void, Void, Void> firstStartCheck = new AsyncTask<Void, Void, Void>() {
        @Override
        protected Void doInBackground(Void... params) {
            mObservableSystemSettings = new ObservableMap(Config.SYSTEM_SETTINGS_CHECKS);
            StartupUtils.doSystemSettingsPreCheck(WifiToggler.this);
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
        return mObservableSystemSettings.get(key);
    }

    public static void setSetting(String key, boolean isCorrect) {
        mObservableSystemSettings.put(key, isCorrect);
    }

    /**
     * Aligns Wifi networks from Database with the system saved networks. For instance when the user
     * deletes one or more system saved network, this will traverse all networks from System and DB,
     * if there are more networks in DB, it finds which was/were deleted from system and remove it
     * from DB
     *
     * @param context
     */
    public static void removeDeletedSavedWifiFromDB(final Context context) {
        new DeletedSavedWifiSweepingTask(context).execute();
    }
}

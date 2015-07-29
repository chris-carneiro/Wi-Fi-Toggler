package net.opencurlybraces.android.projects.wifihandler;

import android.app.Application;
import android.content.ContentProviderOperation;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.os.AsyncTask;
import android.util.Log;

import net.opencurlybraces.android.projects.wifihandler.data.DataAsyncQueryHandler;
import net.opencurlybraces.android.projects.wifihandler.data.provider.WifiHandlerContract;
import net.opencurlybraces.android.projects.wifihandler.data.table.SavedWifi;
import net.opencurlybraces.android.projects.wifihandler.ui.StartupSettingsCheckActivity;
import net.opencurlybraces.android.projects.wifihandler.util.NetworkUtils;
import net.opencurlybraces.android.projects.wifihandler.util.ObservableMap;
import net.opencurlybraces.android.projects.wifihandler.util.PrefUtils;
import net.opencurlybraces.android.projects.wifihandler.util.StartupUtils;

import java.util.ArrayList;
import java.util.List;
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
//            startupCheck();
            return null;
        }
    };


//    private void startupCheck() {
//        int startupMode = StartupUtils.appStartMode(this);
//        switch (startupMode) {
//            case StartupUtils.FIRST_TIME:
//            case StartupUtils.FIRST_TIME_FOR_VERSION:
////                handleFirstLaunch();
//                break;
//        }
//    }


//    private void handleFirstLaunch() {
//        Log.d(TAG, "handleFirstLaunch");
//        if (WifiHandler.hasWrongSettingsForFirstLaunch()) {
//            launchStartupCheckActivity();
//        } else {
//            PrefUtils.markSettingsCorrectAtFirstLaunch(this);
//            loadSavedWifiIntoDatabase();
//        }
//    }

//    private void loadSavedWifiIntoDatabase() {
//        handleSavedWifiInsert();
//    }

//    private void handleSavedWifiInsert() {
//        Log.d(TAG, "handleSavedWifiInsert");
//        List<WifiConfiguration> savedWifis = NetworkUtils.getSavedWifiSync(this);
//        try {
//            List<ContentProviderOperation> batch = SavedWifi.buildBatch(savedWifis);
//
//            insertSavedWifiBatchAsync((ArrayList<ContentProviderOperation>) batch);
//        } catch (IllegalArgumentException e) {
//            Log.d(TAG, "Nothing to build");
//            //TODO handle
//        }
//
//    }

//    private void insertSavedWifiBatchAsync(ArrayList<ContentProviderOperation> batch) {
//        if (batch == null) return;
//        mDataAsyncQueryHandler.startInsertBatch(5, null, WifiHandlerContract
//                .AUTHORITY, batch);
//
//    }

//    private void launchStartupCheckActivity() {
//        Log.d(TAG, "launchStartupCheckActivity");
//        Intent startupCheck = new Intent(this, StartupSettingsCheckActivity.class);
//        startupCheck.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(startupCheck);
//    }

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

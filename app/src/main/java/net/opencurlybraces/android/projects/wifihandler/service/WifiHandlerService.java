package net.opencurlybraces.android.projects.wifihandler.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import net.opencurlybraces.android.projects.wifihandler.R;
import net.opencurlybraces.android.projects.wifihandler.data.DataAsyncQueryHandler;
import net.opencurlybraces.android.projects.wifihandler.data.provider.WifiHandlerContract;
import net.opencurlybraces.android.projects.wifihandler.data.table.SavedWifi;
import net.opencurlybraces.android.projects.wifihandler.receiver.WifiAdapterStateReceiver;
import net.opencurlybraces.android.projects.wifihandler.receiver.WifiConnectionStateReceiver;
import net.opencurlybraces.android.projects.wifihandler.receiver.WifiScanResultsReceiver;
import net.opencurlybraces.android.projects.wifihandler.ui.SavedWifiListActivity;
import net.opencurlybraces.android.projects.wifihandler.util.NetworkUtils;
import net.opencurlybraces.android.projects.wifihandler.util.PrefUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Service that handles wifi events (enabling/disabling ...) and then sends data results when needed
 * to a view controller. <BR/> Created by chris on 01/06/15.
 */
public class WifiHandlerService extends Service implements DataAsyncQueryHandler
        .AsyncQueryListener, NetworkUtils.SavedWifiConfigurationListener {

    private static final String TAG = "WifiHandlerService";

    private static final String SERVICE_ACTION_PREFIX = "net.opencurlybraces.android" +
            ".projects.wifihandler.service.action.";

    /**
     * Intent actions reserved to switch button
     */
    public static final String ACTION_HANDLE_PAUSE_WIFI_HANDLER = SERVICE_ACTION_PREFIX +
            "action.ACTION_HANDLE_PAUSE_WIFI_HANDLER";

    public static final String ACTION_HANDLE_ACTIVATE_WIFI_HANDLER =
            SERVICE_ACTION_PREFIX + "ACTION_HANDLE_ACTIVATE_WIFI_HANDLER";

    /**
     * Intent actions reserved to notification actions
     */
    public static final String ACTION_HANDLE_NOTIFICATION_ACTION_ACTIVATE = SERVICE_ACTION_PREFIX
            + "ACTION_HANDLE_NOTIFICATION_ACTION_ACTIVATE";

    public static final String ACTION_HANDLE_NOTIFICATION_ACTION_PAUSE = SERVICE_ACTION_PREFIX +
            "ACTION_HANDLE_NOTIFICATION_ACTION_PAUSE";

    public static final String ACTION_HANDLE_SAVED_WIFI_INSERT = SERVICE_ACTION_PREFIX +
            "ACTION_HANDLE_SAVED_WIFI_INSERT";

    public static final String ACTION_HANDLE_SAVED_WIFI_UPDATE_CONNECT = SERVICE_ACTION_PREFIX +
            "ACTION_HANDLE_SAVED_WIFI_UPDATE_CONNECT";

    public static final String ACTION_HANDLE_SAVED_WIFI_UPDATE_DISCONNECT = SERVICE_ACTION_PREFIX +
            "ACTION_HANDLE_SAVED_WIFI_UPDATE_DISCONNECT";

    public static final String ACTION_HANDLE_INSERT_NEW_CONNECTED_WIFI = SERVICE_ACTION_PREFIX +
            "ACTION_HANDLE_INSERT_NEW_CONNECTED_WIFI";

    private WifiManager mWifiManager;
    private WifiScanResultsReceiver mWifiScanResultsReceiver = null;
    private WifiAdapterStateReceiver mWifiAdapterStateReceiver = null;
    private WifiConnectionStateReceiver mWifiConnectionStateReceiver = null;
    private DataAsyncQueryHandler mDataAsyncQueryHandler = null;


    private static final int NOTIFICATION_ID = 100;
    private static final int TOKEN_INSERT = 2;
    private static final int TOKEN_UPDATE = 3;
    private static final int TOKEN_INSERT_BATCH = 5;


    @Override
    public void onCreate() {
        Log.d(TAG, "OnCreate");
        super.onCreate();

        lazyInit();

        registerScanResultReceiver();
        registerWifiStateReceiver();
        registerWifiSupplicantStateReceiver();

    }

    private void lazyInit() {
        if (mWifiManager == null) {
            mWifiManager = (WifiManager) getSystemService(Context
                    .WIFI_SERVICE);
        }

        if (mDataAsyncQueryHandler == null) {
            mDataAsyncQueryHandler = new DataAsyncQueryHandler(getContentResolver(), this);
        }

        if (mWifiAdapterStateReceiver == null) {
            mWifiAdapterStateReceiver = new WifiAdapterStateReceiver();
        }

        if (mWifiConnectionStateReceiver == null) {
            mWifiConnectionStateReceiver = new WifiConnectionStateReceiver();
        }
        if (mWifiScanResultsReceiver == null) {
            mWifiScanResultsReceiver = new WifiScanResultsReceiver();
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "OnDestroy");
        unregisterReceiver(mWifiScanResultsReceiver);
        unregisterReceiver(mWifiAdapterStateReceiver);
        unregisterReceiver(mWifiConnectionStateReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Intent=" + (intent != null ? intent.getAction() : null));
        if (intent == null) return START_REDELIVER_INTENT;

        switch (intent.getAction()) {
            case ACTION_HANDLE_NOTIFICATION_ACTION_ACTIVATE:
                activateWifiHandler();
                buildForegroundNotification();
                sendLocalBroadcastAction
                        (ACTION_HANDLE_NOTIFICATION_ACTION_ACTIVATE);
                break;
            case ACTION_HANDLE_NOTIFICATION_ACTION_PAUSE:
                pauseWifiHandler();
                buildDismissableNotification();
                sendLocalBroadcastAction(ACTION_HANDLE_NOTIFICATION_ACTION_PAUSE);
                stopSelf();
                break;
            case ACTION_HANDLE_PAUSE_WIFI_HANDLER:
                pauseWifiHandler();
                buildDismissableNotification();
                stopSelf();
                break;
            case ACTION_HANDLE_ACTIVATE_WIFI_HANDLER:
                activateWifiHandler();
                buildForegroundNotification();
                break;
            case ACTION_HANDLE_SAVED_WIFI_INSERT:
                handleSavedWifiInsert();
                break;
            case ACTION_HANDLE_SAVED_WIFI_UPDATE_CONNECT:
                updateConnectedWifi(intent);
                break;
            case ACTION_HANDLE_SAVED_WIFI_UPDATE_DISCONNECT:
                updateWifiDisconnected();
                break;
            case ACTION_HANDLE_INSERT_NEW_CONNECTED_WIFI:
                //TODO maybe here notify the user the new ssid is now auto toggle
                insertNewConnectedWifi(intent);
                break;
        }

        return START_REDELIVER_INTENT;

    }

    private void insertNewConnectedWifi(Intent intent) {
        String ssidToInsert = intent.getStringExtra(WifiConnectionStateReceiver
                .EXTRA_CURRENT_SSID);
        ContentValues values = new ContentValues();
        values.put(SavedWifi.SSID, ssidToInsert);
        values.put(SavedWifi.STATUS, NetworkUtils.WifiAdapterStatus.CONNECTED);

        mDataAsyncQueryHandler.startInsert(TOKEN_INSERT, null, SavedWifi.CONTENT_URI,
                values);
    }

    private void updateConnectedWifi(Intent intent) {
        String ssid = intent.getStringExtra(WifiConnectionStateReceiver
                .EXTRA_CURRENT_SSID);
        ContentValues cv = new ContentValues();
        cv.put(SavedWifi.STATUS, NetworkUtils.WifiAdapterStatus
                .CONNECTED);
        mDataAsyncQueryHandler.startUpdate(TOKEN_UPDATE, null, SavedWifi
                        .CONTENT_URI,
                cv,
                SavedWifi.SSID + "=?", new String[]{ssid});
    }

    private void updateWifiDisconnected() {
        ContentValues values = new ContentValues();
        values.put(SavedWifi.STATUS, NetworkUtils.WifiAdapterStatus.DISCONNECTED);
        mDataAsyncQueryHandler.startUpdate(TOKEN_UPDATE, null, SavedWifi.CONTENT_URI,
                values,
                SavedWifi.STATUS + "=?", new String[]{String.valueOf(NetworkUtils
                        .WifiAdapterStatus.CONNECTED)});
    }

    private void handleSavedWifiInsert() {
        Log.d(TAG, "handleSavedWifiInsert");
        NetworkUtils.getSavedWifiAsync(mWifiManager, this);

    }

    private void insertSavedWifiBatchAsync(ArrayList<ContentProviderOperation> batch) {
        if (batch == null) return;
        mDataAsyncQueryHandler.startInsertBatch(TOKEN_INSERT_BATCH, null, WifiHandlerContract
                .AUTHORITY, batch);

    }

    private void registerScanResultReceiver() {
        Log.d(TAG, "registerScanResultReceiver");
        IntentFilter wifiScanFilter = new IntentFilter();
        wifiScanFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

        registerReceiver(mWifiScanResultsReceiver, wifiScanFilter);
    }


    private void registerWifiStateReceiver() {
        Log.d(TAG, "registerWifiStateReceiver");
        IntentFilter wifiStateFilter = new IntentFilter();
        wifiStateFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);

        registerReceiver(mWifiAdapterStateReceiver, wifiStateFilter);
    }

    private void registerWifiSupplicantStateReceiver() {
        Log.d(TAG, "registerWifiSupplicantStateReceiver");
        IntentFilter wifiSupplicantFilter = new IntentFilter();
        wifiSupplicantFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        registerReceiver(mWifiConnectionStateReceiver, wifiSupplicantFilter);
    }

    //    private void registerManifestReceiver(Class<? extends BroadcastReceiver> receiverClass) {
    //        PackageManager pm = getPackageManager();
    //        ComponentName compName =
    //                new ComponentName(getApplicationContext(),
    //                        receiverClass);
    //        pm.setComponentEnabledSetting(
    //                compName,
    //                PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
    //                PackageManager.DONT_KILL_APP);
    //    }
    //
    //    private void unregisterManifestReceiver(Class<? extends BroadcastReceiver>
    // receiverClass) {
    //        PackageManager pm = getPackageManager();
    //        ComponentName compName =
    //                new ComponentName(getApplicationContext(),
    //                        receiverClass);
    //        pm.setComponentEnabledSetting(
    //                compName,
    //                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
    //                PackageManager.DONT_KILL_APP);
    //    }


    private void sendLocalBroadcastAction(String action) {
        Intent switchIntent = new Intent();
        switchIntent.setAction(action);
        LocalBroadcastManager.getInstance(this).sendBroadcast(switchIntent);
    }

    private void pauseWifiHandler() {
        PrefUtils.setWifiHandlerActive(this, false);
    }


    private void activateWifiHandler() {
        PrefUtils.setWifiHandlerActive(this, true);
    }


    private void buildDismissableNotification() {
        NotificationManager notifManager = (NotificationManager) getSystemService(Context
                .NOTIFICATION_SERVICE);
        Resources res = getResources();
        Intent notificationIntent = new Intent(this, SavedWifiListActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent intent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(this)
                .setContentTitle(res.getString(R.string.app_name))
                .setContentText(res.getString(R.string
                        .paused_wifi_handler_notification_context_title))
                .setTicker(res.getString(R.string.disable_notification_ticker_content))
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentIntent(intent);

        notifBuilder.addAction(0, res.getString(R.string.enable_action_title)
                , createActivateWifiHandlerIntent());
        Notification notification = notifBuilder.build();
        notifManager.notify(NOTIFICATION_ID, notification);

        stopForeground(false);

    }

    private void buildForegroundNotification() {
        Resources res = getResources();

        Intent notificationIntent = new Intent(this, SavedWifiListActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent intent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(this)
                .setContentTitle(res.getString(R.string.app_name))
                .setContentText(res.getString(R.string
                        .active_wifi_handler_notification_context_title))
                .setTicker(res.getString(R.string.enable_notification_ticker_content))
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentIntent(intent);


        notifBuilder.addAction(0, res.getString(R.string.disable_action_title)
                , createPauseWifiHandlerIntent());

        Notification notification = notifBuilder.build();
        startForeground(NOTIFICATION_ID, notification);

    }

    private PendingIntent createPauseWifiHandlerIntent() {
        Intent pauseIntent = new Intent(ACTION_HANDLE_NOTIFICATION_ACTION_PAUSE,
                null, this, WifiHandlerService.class);

        return PendingIntent.getService(this, 0, pauseIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent createActivateWifiHandlerIntent() {
        Intent activateIntent = new Intent(ACTION_HANDLE_NOTIFICATION_ACTION_ACTIVATE,
                null, this, WifiHandlerService.class);

        return PendingIntent.getService(this, 0, activateIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onInsertBatchComplete(int token, Object cookie, ContentProviderResult[] results) {
        Log.d(TAG, "onInsertBatchComplete: Async Batch Insert complete, stopping service");

        PrefUtils.setSavedWifiInsertComplete(this, (results != null && results.length > 0));

        if (!PrefUtils.isWifiHandlerActive(this)) {
            stopSelf();
        }
    }

    @Override
    public void onQueryComplete(int token, Object cookie, Cursor cursor) {
        Log.d(TAG, "onQueryComplete: Query Complete token=" + token + " cookie=" + cookie);

    }

    @Override
    public void onUpdateComplete(int token, Object cookie, int result) {
        Log.d(TAG, "onUpdateComplete: Async Update complete");
    }

    @Override
    public void onSavedWifiLoaded(List<WifiConfiguration> savedWifis) {
        Log.d(TAG, "onSavedWifiLoaded savedWifis count" + (savedWifis != null ? savedWifis.size()
                : null));
        try {
            List<ContentProviderOperation> batch = SavedWifi.buildBatch(savedWifis);

            insertSavedWifiBatchAsync((ArrayList<ContentProviderOperation>) batch);
        } catch (IllegalArgumentException e) {
            Log.d(TAG, "Nothing to build");
            //TODO handle
        }
    }
}

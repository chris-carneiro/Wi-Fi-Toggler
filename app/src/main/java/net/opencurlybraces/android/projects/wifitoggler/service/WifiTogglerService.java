package net.opencurlybraces.android.projects.wifitoggler.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
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
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import net.opencurlybraces.android.projects.wifitoggler.Config;
import net.opencurlybraces.android.projects.wifitoggler.R;
import net.opencurlybraces.android.projects.wifitoggler.WifiToggler;
import net.opencurlybraces.android.projects.wifitoggler.data.DataAsyncQueryHandler;
import net.opencurlybraces.android.projects.wifitoggler.data.provider.WifiTogglerContract;
import net.opencurlybraces.android.projects.wifitoggler.data.table.SavedWifi;
import net.opencurlybraces.android.projects.wifitoggler.receiver.AirplaneModeStateReceiver;
import net.opencurlybraces.android.projects.wifitoggler.receiver.HotspotModeStateReceiver;
import net.opencurlybraces.android.projects.wifitoggler.receiver.ScanAlwaysAvailableReceiver;
import net.opencurlybraces.android.projects.wifitoggler.receiver.WifiAdapterStateReceiver;
import net.opencurlybraces.android.projects.wifitoggler.receiver.WifiConnectionStateReceiver;
import net.opencurlybraces.android.projects.wifitoggler.receiver.WifiScanResultsReceiver;
import net.opencurlybraces.android.projects.wifitoggler.ui.SavedWifiListActivity;
import net.opencurlybraces.android.projects.wifitoggler.util.CheckPassiveScanHandler;
import net.opencurlybraces.android.projects.wifitoggler.util.NetworkUtils;
import net.opencurlybraces.android.projects.wifitoggler.util.PrefUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Service that handles wifi events (enabling/disabling ...) and then sends data results when needed
 * to a view controller. <BR/> Created by chris on 01/06/15.
 */
public class WifiTogglerService extends Service implements DataAsyncQueryHandler
        .AsyncQueryListener {

    private static final String TAG = "WifiTogglerService";

    private static final String SERVICE_ACTION_PREFIX = "net.opencurlybraces.android" +
            ".projects.wifitoggler.service.action.";

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

    public static final String ACTION_FINISH_STARTUP_CHECK_ACTIVITY = SERVICE_ACTION_PREFIX
            + "ACTION_FINISH_STARTUP_CHECK_ACTIVITY";


    public static final String ACTION_STARTUP_SETTINGS_PRECHECK = SERVICE_ACTION_PREFIX + ""
            + "ACTION_STARTUP_SETTINGS_PRECHECK";


    private WifiScanResultsReceiver mWifiScanResultsReceiver = null;
    private WifiAdapterStateReceiver mWifiAdapterStateReceiver = null;
    private WifiConnectionStateReceiver mWifiConnectionStateReceiver = null;
    private DataAsyncQueryHandler mDataAsyncQueryHandler = null;
    private ScanAlwaysAvailableReceiver mScanAlwaysAvailableReceiver = null;
    private AirplaneModeStateReceiver mAirplaneModeStateReceiver = null;
    private HotspotModeStateReceiver mHotspotModeStateReceiver = null;


    private static final int TOKEN_INSERT = 2;
    private static final int TOKEN_UPDATE = 3;
    private static final int TOKEN_INSERT_BATCH = 5;
    private CheckPassiveScanHandler mCheckPassiveScanHandler;
    private final int CHECK_SCAN_ALWAYS_AVAILABLE = 3;

    @Override
    public void onCreate() {
        Log.d(TAG, "OnCreate");
        super.onCreate();
        if (Config.DEBUG_MODE)
            Toast.makeText(this, "service created", Toast.LENGTH_LONG).show();
        lazyInit();
        registerReceivers();
        schedulePassiveScanCheck();
    }


    private void registerReceivers() {
        registerReceiver(mWifiScanResultsReceiver, WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(mWifiAdapterStateReceiver, WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(mWifiConnectionStateReceiver, WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        registerReceiver(mScanAlwaysAvailableReceiver, ScanAlwaysAvailableReceiver
                .CHECK_SCAN_ALWAYS_AVAILABLE_REQUEST_ACTION);
        registerReceiver(mAirplaneModeStateReceiver, Intent.ACTION_AIRPLANE_MODE_CHANGED);
        registerReceiver(mHotspotModeStateReceiver, HotspotModeStateReceiver
                .HOTSPOT_STATE_CHANGED_ACTION);
    }

    private void schedulePassiveScanCheck() {
        mCheckPassiveScanHandler.sendMessageDelayed(Message.obtain(mCheckPassiveScanHandler,
                        CHECK_SCAN_ALWAYS_AVAILABLE),
                Config.CHECK_SCAN_ALWAYS_AVAILABLE_REQUEST_INTERVAL);
    }

    private void lazyInit() {
        if (mCheckPassiveScanHandler == null) {
            mCheckPassiveScanHandler = new CheckPassiveScanHandler(this,
                    CHECK_SCAN_ALWAYS_AVAILABLE, Config
                    .CHECK_SCAN_ALWAYS_AVAILABLE_REQUEST_INTERVAL);
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

        if (mScanAlwaysAvailableReceiver == null) {
            mScanAlwaysAvailableReceiver = new ScanAlwaysAvailableReceiver();
        }

        if (mHotspotModeStateReceiver == null) {
            mHotspotModeStateReceiver = new HotspotModeStateReceiver();
        }

        if (mAirplaneModeStateReceiver == null) {
            mAirplaneModeStateReceiver = new AirplaneModeStateReceiver();
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "OnDestroy");
        if (Config.DEBUG_MODE)
            Toast.makeText(this, "service destroyed", Toast.LENGTH_LONG).show();

        pauseWifiToggler();
        buildDismissableNotification();
        mCheckPassiveScanHandler.removeMessages(CHECK_SCAN_ALWAYS_AVAILABLE);
        unregisterReceivers();
        NetworkUtils.dismissNotification(this, Config.NOTIFICATION_ID_WARNING);
    }

    private void unregisterReceivers() {
        unregisterReceiver(mWifiScanResultsReceiver);
        unregisterReceiver(mWifiAdapterStateReceiver);
        unregisterReceiver(mWifiConnectionStateReceiver);
        unregisterReceiver(mScanAlwaysAvailableReceiver);
        unregisterReceiver(mAirplaneModeStateReceiver);
        unregisterReceiver(mHotspotModeStateReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Intent=" + (intent != null ? intent.getAction() : null));
        if (intent == null) return START_NOT_STICKY;

        switch (intent.getAction()) {
            case ACTION_HANDLE_NOTIFICATION_ACTION_ACTIVATE:
                activateWifiToggler();
                buildForegroundNotification();
                sendLocalBroadcastAction
                        (ACTION_HANDLE_NOTIFICATION_ACTION_ACTIVATE);
                break;
            case ACTION_HANDLE_NOTIFICATION_ACTION_PAUSE:
                sendLocalBroadcastAction(ACTION_HANDLE_NOTIFICATION_ACTION_PAUSE);
                stopSelf();
                break;
            case ACTION_HANDLE_PAUSE_WIFI_HANDLER:
                stopSelf();
                break;
            case ACTION_HANDLE_ACTIVATE_WIFI_HANDLER:
                activateWifiToggler();
                handleNotifications();
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
            case ACTION_STARTUP_SETTINGS_PRECHECK:
                settingsPreCheck();
                break;
        }

        return START_NOT_STICKY;

    }

    private void handleNotifications() {
        buildForegroundNotification();

        if (WifiToggler.hasWrongSettingsForAutoToggle()) {
            NetworkUtils.buildWarningNotification(this);
        }
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
        List<WifiConfiguration> savedWifis = NetworkUtils.getSavedWifiSync(this);
        try {
            List<ContentProviderOperation> batch = SavedWifi.buildBatch(savedWifis);

            insertSavedWifiBatchAsync((ArrayList<ContentProviderOperation>) batch);
        } catch (IllegalArgumentException e) {
            Log.d(TAG, "Nothing to build");
            //TODO handle
        }

    }

    private void insertSavedWifiBatchAsync(ArrayList<ContentProviderOperation> batch) {
        if (batch == null) return;
        mDataAsyncQueryHandler.startInsertBatch(TOKEN_INSERT_BATCH, null, WifiTogglerContract
                .AUTHORITY, batch);

    }

    private void registerReceiver(BroadcastReceiver receiver, final String action) {
        Log.d(TAG, "registerReceiver action=" + action);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(action);
        registerReceiver(receiver, intentFilter);
    }

    private void settingsPreCheck() {
        Log.d(TAG, "settingsCheck");
        WifiToggler.setSetting(Config.SCAN_ALWAYS_AVAILABLE_SETTINGS, NetworkUtils
                .isScanAlwaysAvailable(this));

        WifiToggler.setSetting(Config.AIRPLANE_SETTINGS, !NetworkUtils
                .isAirplaneModeEnabled(this));

        WifiToggler.setSetting(Config.HOTSPOT_SETTINGS, !NetworkUtils
                .isHotspotEnabled(this));

        WifiToggler.setSetting(Config.STARTUP_CHECK_WIFI_SETTINGS, NetworkUtils
                .isWifiEnabled(this));
    }

    private void sendLocalBroadcastAction(String action) {
        Intent switchIntent = new Intent();
        switchIntent.setAction(action);
        LocalBroadcastManager.getInstance(this).sendBroadcast(switchIntent);
    }

    private void pauseWifiToggler() {
        PrefUtils.setWifiTogglerActive(this, false);
    }


    private void activateWifiToggler() {
        PrefUtils.setWifiTogglerActive(this, true);
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
                        .paused_wifi_toggler_notification_context_title))
                .setTicker(res.getString(R.string.disable_notification_ticker_content))
                .setSmallIcon(R.drawable.notif_icon)
                .setColor(getResources().getColor(R.color.material_orange_400))
                .setContentIntent(intent);

        notifBuilder.addAction(0, res.getString(R.string.enable_action_title)
                , createActivateWifiTogglerIntent());
        Notification notification = notifBuilder.build();
        notifManager.notify(Config.NOTIFICATION_ID_WIFI_HANDLER_STATE, notification);

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
                        .active_wifi_toggler_notification_context_title))
                .setTicker(res.getString(R.string.enable_notification_ticker_content))
                .setSmallIcon(R.drawable.notif_icon)
                .setColor(getResources().getColor(R.color.material_teal_400))
                .setContentIntent(intent);


        notifBuilder.addAction(0, res.getString(R.string.disable_action_title)
                , createPauseWifiTogglerIntent());

        Notification notification = notifBuilder.build();
        startForeground(Config.NOTIFICATION_ID_WIFI_HANDLER_STATE, notification);

    }

    private PendingIntent createPauseWifiTogglerIntent() {
        Intent pauseIntent = new Intent(ACTION_HANDLE_NOTIFICATION_ACTION_PAUSE,
                null, this, WifiTogglerService.class);

        return PendingIntent.getService(this, 0, pauseIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent createActivateWifiTogglerIntent() {
        Intent activateIntent = new Intent(ACTION_HANDLE_NOTIFICATION_ACTION_ACTIVATE,
                null, this, WifiTogglerService.class);

        return PendingIntent.getService(this, 0, activateIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onInsertBatchComplete(int token, Object cookie, ContentProviderResult[] results) {
        Log.d(TAG, "onInsertBatchComplete: Async Batch Insert complete, stopping service");

        PrefUtils.setSavedWifiInsertComplete(this, (results != null && results.length > 0));

        Intent handleSavedWifiInsert = new Intent(this, WifiTogglerService.class);
        handleSavedWifiInsert.setAction(WifiTogglerService.ACTION_HANDLE_ACTIVATE_WIFI_HANDLER);
        startService(handleSavedWifiInsert);

        sendLocalBroadcastAction(ACTION_FINISH_STARTUP_CHECK_ACTIVITY);

    }

    @Override
    public void onQueryComplete(int token, Object cookie, Cursor cursor) {
        Log.d(TAG, "onQueryComplete: Query Complete token=" + token + " cookie=" + cookie);

    }

    @Override
    public void onUpdateComplete(int token, Object cookie, int result) {
        Log.d(TAG, "onUpdateComplete: Async Update complete");
    }
}

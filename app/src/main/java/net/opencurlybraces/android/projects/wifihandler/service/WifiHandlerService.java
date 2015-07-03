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
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import net.opencurlybraces.android.projects.wifihandler.R;
import net.opencurlybraces.android.projects.wifihandler.SavedWifiListActivity;
import net.opencurlybraces.android.projects.wifihandler.data.DataAsyncQueryHandler;
import net.opencurlybraces.android.projects.wifihandler.data.provider.WifiHandlerContract;
import net.opencurlybraces.android.projects.wifihandler.data.table.SavedWifi;
import net.opencurlybraces.android.projects.wifihandler.receiver.WifiScanResultsReceiver;
import net.opencurlybraces.android.projects.wifihandler.receiver.WifiStateReceiver;
import net.opencurlybraces.android.projects.wifihandler.receiver.WifiSupplicantStateReceiver;
import net.opencurlybraces.android.projects.wifihandler.util.PrefUtils;
import net.opencurlybraces.android.projects.wifihandler.util.WifiUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Service that handles wifi events (enabling/disabling ...) and then sends data results when needed
 * to a view controller. <BR/> Created by chris on 01/06/15.
 */
public class WifiHandlerService extends Service implements DataAsyncQueryHandler
        .AsyncQueryListener {

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
    //    public static final String ACTION_UNREGISTER_SCAN_RESULT_RECEIVER = "net
    // .opencurlybraces" +
    //            ".android" +
    //            ".projects" +
    //            ".wifihandler.service.action.ACTION_UNREGISTER_SCAN_RESULT_RECEIVER";
    //
    //    public static final String ACTION_REGISTER_SCAN_RESULT_RECEIVER = "net.opencurlybraces" +
    //            ".android" +
    //            ".projects" +
    //            ".wifihandler.service.action.ACTION_REGISTER_SCAN_RESULT_RECEIVER";

    private WifiManager mWifiManager;
    private WifiScanResultsReceiver mWifiScanResultsReceiver = null;
    private WifiStateReceiver mWifiStateReceiver = null;
    private WifiSupplicantStateReceiver mWifiSupplicantStateReceiver = null;
    private static final int NOTIFICATION_ID = 100;
    private static final String[] PROJECTION = new String[]{SavedWifi._ID, SavedWifi
            .SSID, SavedWifi.STATUS};

    private DataAsyncQueryHandler mDataAsyncQueryHandler = null;

    private static final int TOKEN_QUERY = 1;
    private static final int TOKEN_UPDATE = 3;
    private static final int TOKEN_INSERT_BATCH = 5;

    @Override
    public void onCreate() {
        Log.d(TAG, "OnCreate");
        super.onCreate();
        if (mWifiManager == null) {
            mWifiManager = (WifiManager) getSystemService(Context
                    .WIFI_SERVICE);
        }

        if (mDataAsyncQueryHandler == null) {
            mDataAsyncQueryHandler = new DataAsyncQueryHandler(getContentResolver(), this);
        }

        registerScanResultReceiver();
        registerWifiStateReceiver();
        registerWifiSupplicantStateReceiver();

    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "OnDestroy");
        unregisterReceiver(mWifiScanResultsReceiver);
        unregisterReceiver(mWifiStateReceiver);
        unregisterReceiver(mWifiSupplicantStateReceiver);
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
                handleUserWifiInsert();
                break;
            case ACTION_HANDLE_SAVED_WIFI_UPDATE_CONNECT:
                String ssid = intent.getStringExtra(WifiSupplicantStateReceiver.EXTRA_CURRENT_SSID);
                int newState = intent.getIntExtra(WifiStateReceiver.EXTRA_SAVED_WIFI_NEW_STATE, -1);

                startQuery(newState, SavedWifi.SSID + "=?", new String[]{ssid});
                break;
            case ACTION_HANDLE_SAVED_WIFI_UPDATE_DISCONNECT:
                int stateDisconnect = intent.getIntExtra(WifiStateReceiver
                        .EXTRA_SAVED_WIFI_NEW_STATE, -1);

                startQuery(stateDisconnect, SavedWifi.STATUS + "=?", new String[]{String.valueOf
                        (WifiConfiguration.Status.CURRENT)});

                break;
            //            case ACTION_UNREGISTER_SCAN_RESULT_RECEIVER:
            //                Log.d(TAG, "unregisterScanresultsReceiver");
            //                unregisterReceiver(mWifiScanResultsReceiver);
            //                break;
            //            case ACTION_REGISTER_SCAN_RESULT_RECEIVER:
            //                registerScanResultReceiver();
            //                break;

        }

        return START_REDELIVER_INTENT;

    }

    private void handleUserWifiInsert() {
        Log.d(TAG, "handleUserWifiInsert");
        List<WifiConfiguration> configuredWifis = null;
        try {
            configuredWifis = WifiUtils.getConfiguredWifis(mWifiManager);
            List<ContentProviderOperation> batch = SavedWifi.buildBatch(configuredWifis);
            insertBatchAsync((ArrayList<ContentProviderOperation>) batch);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "Nothing to build");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void insertBatchAsync(ArrayList<ContentProviderOperation> batch) {
        if (batch == null) return;
        mDataAsyncQueryHandler.startInsertBatch(TOKEN_INSERT_BATCH, null, WifiHandlerContract
                .AUTHORITY, batch);

    }

    private void disableWifiAdapter() {
        mWifiManager.setWifiEnabled(false);
    }

    /**
     * Issue an async Query given the selection params. Note that wifiState here is used to pass
     * wifi state value to the #onQueryComplete callback
     *
     * @param wifiState
     * @param where
     * @param whereArgs
     */
    private void startQuery(int wifiState, String where, String[] whereArgs) {
        mDataAsyncQueryHandler.startQuery(TOKEN_QUERY, wifiState, SavedWifi.CONTENT_URI, PROJECTION,
                where,
                whereArgs, null);
    }

    private String getRowIdFromCursor(final Cursor cursor) {
        String rowId = null;
        if (cursor != null) {
            if (cursor.getCount() > 1) {
                return null;
            }
            if (cursor.moveToFirst()) {
                rowId = cursor.getString(0);
                cursor.close();
            }
        }
        return rowId;
    }

    private void registerScanResultReceiver() {
        Log.d(TAG, "registerScanResultReceiver");
        IntentFilter wifiScanFilter = new IntentFilter();
        wifiScanFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mWifiScanResultsReceiver = new WifiScanResultsReceiver();
        registerReceiver(mWifiScanResultsReceiver, wifiScanFilter);
    }


    private void registerWifiStateReceiver() {
        Log.d(TAG, "registerWifiStateReceiver");
        IntentFilter wifiStateFilter = new IntentFilter();
        wifiStateFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mWifiStateReceiver = new WifiStateReceiver();
        registerReceiver(mWifiStateReceiver, wifiStateFilter);
    }

    private void registerWifiSupplicantStateReceiver() {
        Log.d(TAG, "registerWifiSupplicantStateReceiver");
        IntentFilter wifiSupplicantFilter = new IntentFilter();
        wifiSupplicantFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        mWifiSupplicantStateReceiver = new WifiSupplicantStateReceiver();
        registerReceiver(mWifiSupplicantStateReceiver, wifiSupplicantFilter);
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
        Log.d(TAG,"activateWifiHandler");
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
        Log.d(TAG, "Async Batch Insert complete, stopping service");
        stopSelf();
    }

    @Override
    public void onQueryComplete(int token, Object cookie, Cursor cursor) {
        Log.d(TAG, "Query Complete token=" + token + " cookie=" + cookie);

        if (cursor == null || cursor.getCount() <= 0) return;

        String rowId = getRowIdFromCursor(cursor);

        Uri uri = SavedWifi.buildConfiguredWifiUri(rowId);

        ContentValues value = new ContentValues(1);
        value.put(SavedWifi.STATUS, (int) cookie);

        mDataAsyncQueryHandler.startUpdate(TOKEN_UPDATE, cookie, uri, value, SavedWifi
                ._ID + "=?", new String[]{rowId});
    }

    @Override
    public void onUpdateComplete(int token, int wifiState, int result) {
        Log.d(TAG, "Async Update complete, stopping service, wifiState=" + wifiState);

        if (wifiState == WifiConfiguration.Status.DISABLED) {
            disableWifiAdapter();
        }
    }
}

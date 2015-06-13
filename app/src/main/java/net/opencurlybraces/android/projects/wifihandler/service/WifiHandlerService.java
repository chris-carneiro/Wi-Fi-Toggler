package net.opencurlybraces.android.projects.wifihandler.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import net.opencurlybraces.android.projects.wifihandler.receiver.WifiScanResultsReceiver;
import net.opencurlybraces.android.projects.wifihandler.util.PrefUtils;

/**
 * Service that handles wifi events (enabling/disabling ...) and then sends data results when needed
 * to a view controller. <BR/> Created by chris on 01/06/15.
 */
public class WifiHandlerService extends Service {

    private static final String TAG = "WifiHandlerService";

    /**
     * Intent actions reserved to switch button
     */
    public static final String ACTION_HANDLE_PAUSE_WIFI_HANDLER = "net.opencurlybraces" +
            ".android" +
            ".projects" +
            ".wifihandler.service.action.ACTION_HANDLE_PAUSE_WIFI_HANDLER";


    public static final String ACTION_HANDLE_ACTIVATE_WIFI_HANDLER = "net.opencurlybraces" +
            ".android" +
            ".projects" +
            ".wifihandler.service.action.ACTION_HANDLE_ACTIVATE_WIFI_HANDLER";


    /**
     * Intent actions reserved to notification actions
     */
    public static final String ACTION_HANDLE_NOTIFICATION_ACTION_ACTIVATE = "net.opencurlybraces" +
            ".android" +
            ".projects" +
            ".wifihandler.service.action.ACTION_HANDLE_NOTIFICATION_ACTION_ACTIVATE";

    public static final String ACTION_HANDLE_NOTIFICATION_ACTION_PAUSE = "net.opencurlybraces" +
            ".android" +
            ".projects" +
            ".wifihandler.service.action.ACTION_HANDLE_NOTIFICATION_ACTION_PAUSE";

    private WifiManager mWifiManager;
    private WifiScanResultsReceiver mWifiScanResultsReceiver = null;
    private static final int NOTIFICATION_ID = 100;

    @Override
    public void onCreate() {
        super.onCreate();
        if (mWifiManager == null) {
            mWifiManager = (WifiManager) getSystemService(Context
                    .WIFI_SERVICE);
        }

        registerScanResultReceiver();

    }

    @Override
    public void onDestroy() {
        if (mWifiScanResultsReceiver != null) {
            unregisterReceiver(mWifiScanResultsReceiver);
        }
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
        }

        return START_NOT_STICKY;

    }

    private void registerScanResultReceiver() {
        IntentFilter wifiScanFilter = new IntentFilter();
        wifiScanFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mWifiScanResultsReceiver = new WifiScanResultsReceiver();
        registerReceiver(mWifiScanResultsReceiver, wifiScanFilter);
    }

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

        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(this)
                .setContentTitle("Wifi Handler")
                .setContentText("Test")
                .setTicker("WifiHandler is Disabled")
                .setSmallIcon(android.R.drawable.ic_dialog_info);

        notifBuilder.addAction(0, "Activate Wifi Handler"
                , createActivateWifiHandlerIntent());
        Notification notification = notifBuilder.build();
        notifManager.notify(NOTIFICATION_ID, notification);

        stopForeground(false);

    }

    private void buildForegroundNotification() {
        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(this)
                .setContentTitle("Wifi Handler")
                .setContentText("Test")
                .setTicker("WifiHandler is Active")
                .setSmallIcon(android.R.drawable.ic_dialog_info);

        notifBuilder.addAction(0, "Pause Wifi Handler"
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
}

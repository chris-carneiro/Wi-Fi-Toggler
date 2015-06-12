package net.opencurlybraces.android.projects.wifihandler.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import net.opencurlybraces.android.projects.wifihandler.receiver.WifiScanResultsReceiver;
import net.opencurlybraces.android.projects.wifihandler.util.PrefUtils;

/**
 * Service that handles wifi events (enabling/disabling ...) and then sends data results when needed
 * to a view controller. <BR/> Created by chris on 01/06/15.
 */
public class WifiHandlerService extends Service {

    private static final String TAG = "WifiHandlerService";


    public static final String ACTION_HANDLE_FOREGROUND_NOTIFICATION = "net.opencurlybraces" +
            ".android" +
            ".projects" +
            ".wifihandler.service.action.ACTION_HANDLE_FOREGROUND_NOTIFICATION";

    public static final String ACTION_HANDLE_PAUSE_WIFI_HANDLER = "net.opencurlybraces" +
            ".android" +
            ".projects" +
            ".wifihandler.service.action.ACTION_HANDLE_PAUSE_WIFI_HANDLER";

    public static final String ACTION_STOP_FOREGROUND_NOTIFICATION = "net.opencurlybraces" +
            ".android" +
            ".projects" +
            ".wifihandler.service.action.ACTION_STOP_FOREGROUND_NOTIFICATION";

    public static final String ACTION_HANDLE_ACTIVATE_WIFI_HANDLER = "net.opencurlybraces" +
            ".android" +
            ".projects" +
            ".wifihandler.service.action.ACTION_HANDLE_ACTIVATE_WIFI_HANDLER";

    private WifiManager mWifiManager;

    private static final int NOTIFICATION_ID = 100;

    @Override
    public void onCreate() {
        super.onCreate();
        if (mWifiManager == null) {
            mWifiManager = (WifiManager) getSystemService(Context
                    .WIFI_SERVICE);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_STICKY;
        switch (intent.getAction()) {

            case ACTION_HANDLE_FOREGROUND_NOTIFICATION:
                buildForegroundNotification();
                break;
            case ACTION_HANDLE_PAUSE_WIFI_HANDLER:
                pauseWifiHandler();
                updateNotification();
                break;
            case ACTION_STOP_FOREGROUND_NOTIFICATION:
                updateNotification();
                break;
            case ACTION_HANDLE_ACTIVATE_WIFI_HANDLER:
                activateWifiHandler();
                break;
        }

        return START_STICKY;

    }

    private void pauseWifiHandler() {
        PrefUtils.setWifiHandlerActive(this, false);
        unRegisterScanReceiver();
    }


    private void activateWifiHandler() {
        PrefUtils.setWifiHandlerActive(this, true);
        registerScanReceiver();
    }

    private void unRegisterScanReceiver() {
        Log.d(TAG, "unRegisterScanReceiver");
        ComponentName component = new ComponentName(this, WifiScanResultsReceiver.class);

        getPackageManager().setComponentEnabledSetting(component, PackageManager
                .COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

        int status = getPackageManager().getComponentEnabledSetting(component);

        if (status == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            Log.d(TAG, "receiver is enabled");
        } else if (status == PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
            Log.d(TAG, "receiver is disabled");
        }

    }

    private void registerScanReceiver() {
        Log.d(TAG, "registerScanReceiver");
        ComponentName component = new ComponentName(this, WifiScanResultsReceiver.class);

        getPackageManager().setComponentEnabledSetting(component, PackageManager
                .COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

        int status = getPackageManager().getComponentEnabledSetting(component);

        if (status == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            Log.d(TAG, "receiver is enabled");
        } else if (status == PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
            Log.d(TAG, "receiver is disabled");
        }

    }

    private void updateNotification() {
        NotificationManager notifManager = (NotificationManager) getSystemService(Context
                .NOTIFICATION_SERVICE);

        stopForeground(false);

        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(this)
                .setContentTitle("Wifi Handler")
                .setContentText("Test")
                .setTicker("WifiHandler is Disabled")
                .setSmallIcon(android.R.drawable.ic_dialog_info);

        notifBuilder.addAction(0, "Activate Wifi Handler"
                , createActivateWifiHandlerIntent());
        Notification notification = notifBuilder.build();
        notifManager.notify(NOTIFICATION_ID, notification);

    }

    private void buildForegroundNotification() {
        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(this)
                .setContentTitle("Wifi Handler")
                .setContentText("Test")
                .setTicker("WifiHandler is Active")
                .setSmallIcon(android.R.drawable.ic_dialog_info);

        notifBuilder.addAction(0, "Pause Wifi Handler"
                , createPauseIntent());

        Notification notification = notifBuilder.build();
        startForeground(NOTIFICATION_ID, notification);

    }

    private PendingIntent createPauseIntent() {
        Intent pauseIntent = new Intent(ACTION_HANDLE_PAUSE_WIFI_HANDLER,
                null, this, WifiHandlerService.class);

        return PendingIntent.getService(this, 0, pauseIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private PendingIntent createActivateWifiHandlerIntent() {
        Intent activateIntent = new Intent(ACTION_HANDLE_ACTIVATE_WIFI_HANDLER,
                null, this, WifiHandlerService.class);

        return PendingIntent.getService(this, 0, activateIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
    }
}

package net.opencurlybraces.android.projects.wifihandler.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

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
                sendLocalBroadcastAction(ACTION_HANDLE_PAUSE_WIFI_HANDLER);
                break;
            case ACTION_HANDLE_ACTIVATE_WIFI_HANDLER:
                activateWifiHandler();
                buildForegroundNotification();
                sendLocalBroadcastAction(ACTION_HANDLE_ACTIVATE_WIFI_HANDLER);
                break;
        }

        return START_STICKY;

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
                , createPauseWifiHandlerIntent());

        Notification notification = notifBuilder.build();
        startForeground(NOTIFICATION_ID, notification);

    }

    private PendingIntent createPauseWifiHandlerIntent() {
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

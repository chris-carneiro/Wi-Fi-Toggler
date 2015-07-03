package net.opencurlybraces.android.projects.wifihandler.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import net.opencurlybraces.android.projects.wifihandler.service.WifiHandlerService;

/**
 * Created by chris on 15/06/15.
 */
public class WifiStateReceiver extends BroadcastReceiver {
    private static final String TAG = "WifiStateReceiver";

    public static final String EXTRA_SAVED_WIFI_NEW_STATE = "net.opencurlybraces.android" +
            ".projects.wifihandler.receiver.saved_wifi_new_state";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "WIFI_STATE_CHANGED_ACTION Received");
        if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);

            if (WifiManager.WIFI_STATE_DISABLED == wifiState) {

                Log.d(TAG, "WIFI_STATE_DISABLED Event received");
                Intent updateSavedWifi = new Intent(context, WifiHandlerService
                        .class);
                updateSavedWifi.putExtra(EXTRA_SAVED_WIFI_NEW_STATE, WifiConfiguration
                        .Status.DISABLED);
                updateSavedWifi.setAction(WifiHandlerService
                        .ACTION_HANDLE_SAVED_WIFI_UPDATE_DISCONNECT);
                context.startService(updateSavedWifi);

            }
        }
    }
}
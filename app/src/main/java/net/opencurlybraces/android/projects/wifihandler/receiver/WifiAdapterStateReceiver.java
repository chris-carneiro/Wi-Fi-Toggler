package net.opencurlybraces.android.projects.wifihandler.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.util.Log;

import net.opencurlybraces.android.projects.wifihandler.Config;
import net.opencurlybraces.android.projects.wifihandler.WifiHandler;
import net.opencurlybraces.android.projects.wifihandler.service.WifiHandlerService;
import net.opencurlybraces.android.projects.wifihandler.util.NetworkUtils;
import net.opencurlybraces.android.projects.wifihandler.util.PrefUtils;

/**
 * Created by chris on 15/06/15.
 */
public class WifiAdapterStateReceiver extends BroadcastReceiver {
    private static final String TAG = "WifiAdapterState";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "WIFI_STATE_CHANGED_ACTION Received");
        if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);

            if (WifiManager.WIFI_STATE_DISABLED == wifiState) {
                
                if (PrefUtils.isWifiHandlerActive(context)) {
                    Log.d(TAG, "WIFI_STATE_DISABLED Event received");
                    Intent updateSavedWifi = new Intent(context, WifiHandlerService
                            .class);
                    updateSavedWifi.setAction(WifiHandlerService
                            .ACTION_HANDLE_SAVED_WIFI_UPDATE_DISCONNECT);

                    context.startService(updateSavedWifi);
                }

                WifiHandler.setSetting(Config.STARTUP_CHECK_WIFI_SETTINGS, false);
            } else {
                WifiHandler.setSetting(Config.STARTUP_CHECK_WIFI_SETTINGS, true);
            }
        }
    }
}
package net.opencurlybraces.android.projects.wifitoggler.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.util.Log;

import net.opencurlybraces.android.projects.wifitoggler.Config;
import net.opencurlybraces.android.projects.wifitoggler.WifiToggler;
import net.opencurlybraces.android.projects.wifitoggler.service.WifiTogglerService;
import net.opencurlybraces.android.projects.wifitoggler.util.PrefUtils;

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

                if (PrefUtils.isWifiTogglerActive(context)) {
                    Log.d(TAG, "WIFI_STATE_DISABLED Event received");
                    Intent updateSavedWifi = new Intent(context, WifiTogglerService
                            .class);
                    updateSavedWifi.setAction(WifiTogglerService
                            .ACTION_HANDLE_SAVED_WIFI_UPDATE_DISCONNECT);

                    context.startService(updateSavedWifi);

                    PrefUtils.setDisableWifiScheduled(context, false);
                }

                WifiToggler.setSetting(Config.STARTUP_CHECK_WIFI_SETTINGS, false);
            } else {
                Log.d(TAG, "WIFI_STATE_CHANGED_ACTION Received=" + wifiState);
                WifiToggler.setSetting(Config.STARTUP_CHECK_WIFI_SETTINGS, true);
               WifiToggler.removeDeletedSavedWifiFromDB(context);
            }
        }
    }
}
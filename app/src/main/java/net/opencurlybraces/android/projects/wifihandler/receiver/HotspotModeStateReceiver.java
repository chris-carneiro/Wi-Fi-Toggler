package net.opencurlybraces.android.projects.wifihandler.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import net.opencurlybraces.android.projects.wifihandler.Config;
import net.opencurlybraces.android.projects.wifihandler.WifiHandler;
import net.opencurlybraces.android.projects.wifihandler.util.NetworkUtils;


public class HotspotModeStateReceiver extends BroadcastReceiver {

    private static final String TAG = "Hotspot receiver";
    public static final String HOTSPOT_STATE_CHANGED_ACTION = "android.net.wifi" +
            ".WIFI_AP_STATE_CHANGED";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Hotspot receiver");
        if (NetworkUtils.isHotspotEnabled(context)) {
            WifiHandler.setSetting(Config.HOTSPOT_SETTINGS, false);
        } else {
            WifiHandler.setSetting(Config.HOTSPOT_SETTINGS, true);
        }
    }
}

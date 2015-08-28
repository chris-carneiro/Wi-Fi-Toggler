package net.opencurlybraces.android.projects.wifitoggler.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import net.opencurlybraces.android.projects.wifitoggler.Config;
import net.opencurlybraces.android.projects.wifitoggler.WifiToggler;
import net.opencurlybraces.android.projects.wifitoggler.util.NetworkUtils;
import net.opencurlybraces.android.projects.wifitoggler.util.NotifUtils;
import net.opencurlybraces.android.projects.wifitoggler.util.PrefUtils;


public class HotspotModeStateReceiver extends BroadcastReceiver {

    private static final String TAG = "Hotspot receiver";
    public static final String HOTSPOT_STATE_CHANGED_ACTION = "android.net.wifi" +
            ".WIFI_AP_STATE_CHANGED";


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Hotspot receiver");
        boolean warningNotificationsEnabled = PrefUtils.areWarningNotificationsEnabled(context);
        if (NetworkUtils.isHotspotEnabled(context)) {
            if (warningNotificationsEnabled) {
                if (PrefUtils.isWifiTogglerActive(context)) {
                    NetworkUtils.buildWarningNotification(context);
                }
            }
            WifiToggler.setSetting(Config.HOTSPOT_SETTINGS, false);
        } else {
            WifiToggler.setSetting(Config.HOTSPOT_SETTINGS, true);
            NetworkUtils.dismissNotification(context, NotifUtils.NOTIFICATION_ID_WARNING);
        }
    }
}

package net.opencurlybraces.android.projects.wifitoggler.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import net.opencurlybraces.android.projects.wifitoggler.Config;
import net.opencurlybraces.android.projects.wifitoggler.WifiToggler;
import net.opencurlybraces.android.projects.wifitoggler.service.WifiTogglerService;
import net.opencurlybraces.android.projects.wifitoggler.util.NetworkUtils;
import net.opencurlybraces.android.projects.wifitoggler.util.NotifUtils;
import net.opencurlybraces.android.projects.wifitoggler.util.PrefUtils;

/**
 * There's no broadcast event to which we could register regarding the <i>scan always available </i>
 * setting. As a consequence we poll the status of the latter using the alarm manager in the {@link
 * WifiTogglerService}. The polling is
 * scheduled every {@link Config#CHECK_SCAN_ALWAYS_AVAILABLE_REQUEST_INTERVAL}
 */
public class ScanAlwaysAvailableReceiver extends BroadcastReceiver {


    private static final String TAG = "ScanAvailableReceiver";
    public static final String CHECK_SCAN_ALWAYS_AVAILABLE_REQUEST_ACTION =
            "CHECK_SCAN_ALWAYS_AVAILABLE_REQUEST";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Check always scan available request received");
        boolean warningNotificationsEnabled = PrefUtils.areWarningNotificationsEnabled(context);

        if (NetworkUtils.isScanAlwaysAvailable(context)) {
            Log.d(TAG, "Scan always available correct");
            WifiToggler.setSetting(Config.SCAN_ALWAYS_AVAILABLE_SETTINGS, true);
            NotifUtils.dismissNotification(context, NotifUtils.NOTIFICATION_ID_WARNING);
        } else {
            if (warningNotificationsEnabled) {
                if (PrefUtils.isWifiTogglerActive(context)) {
                    NotifUtils.buildWarningNotification(context);
                }
            }
            Log.d(TAG, "Scan always available ko");
            WifiToggler.setSetting(Config.SCAN_ALWAYS_AVAILABLE_SETTINGS, false);
        }
    }
}

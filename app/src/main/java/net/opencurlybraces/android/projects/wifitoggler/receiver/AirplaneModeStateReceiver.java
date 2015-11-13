package net.opencurlybraces.android.projects.wifitoggler.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.util.Log;

import net.opencurlybraces.android.projects.wifitoggler.Config;
import net.opencurlybraces.android.projects.wifitoggler.WifiToggler;
import net.opencurlybraces.android.projects.wifitoggler.util.NetworkUtils;
import net.opencurlybraces.android.projects.wifitoggler.util.NotifUtils;
import net.opencurlybraces.android.projects.wifitoggler.util.PrefUtils;

/**
 * Created by chris on 03/07/15.
 */
public class AirplaneModeStateReceiver extends BroadcastReceiver {

    private static final String TAG = "AirplaneModeReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Airplane Receiver");
        boolean warningNotificationsEnabled = PrefUtils.areWarningNotificationsEnabled(context);


        boolean isAirplaneModeOn = NetworkUtils.isAirplaneModeEnabled(context);

        if (isAirplaneModeOn) {
            setAirplaneSettingsCorrect(false);
            if (warningNotificationsEnabled) {
                if (PrefUtils.isWifiTogglerActive(context)) {
                    NotifUtils.buildWarningNotification(context);
                }
            }
        } else {
            setAirplaneSettingsCorrect(true);
            NotifUtils.dismissNotification(context, NotifUtils.NOTIFICATION_ID_WARNING);
            triggerScanAlwaysAvailableSettingCheck(context);
        }
    }

    /**
     * When Airplane mode is on, {@link WifiManager#isScanAlwaysAvailable()} returns false.
     * This call realigns the {@link WifiToggler#mObservableSystemSettings} cache by triggering a
     * system check.
     * @param context
     */
    private void triggerScanAlwaysAvailableSettingCheck(Context context) {
        context.sendBroadcast(new Intent(ScanAlwaysAvailableReceiver
                .CHECK_SCAN_ALWAYS_AVAILABLE_REQUEST_ACTION));
    }

    private void setAirplaneSettingsCorrect(boolean isCorrect) {
        WifiToggler.setSetting(Config.AIRPLANE_SETTINGS, isCorrect);
    }


}

package net.opencurlybraces.android.projects.wifihandler.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import net.opencurlybraces.android.projects.wifihandler.Config;
import net.opencurlybraces.android.projects.wifihandler.WifiHandler;
import net.opencurlybraces.android.projects.wifihandler.util.NetworkUtils;
import net.opencurlybraces.android.projects.wifihandler.util.PrefUtils;

/**
 * Created by chris on 03/07/15.
 */
public class AirplaneModeStateReceiver extends BroadcastReceiver {
    private static final String TAG = "AirplaneModeReceiver";

    public static final String EXTRAS_AIRPLANE_MODE_STATE = "state";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Airplane Receiver");
        boolean warningNotificationsEnabled = PrefUtils.areWarningNotificationsEnabled(context);


        boolean isAirplaneModeOn = intent.getBooleanExtra(EXTRAS_AIRPLANE_MODE_STATE, false);

        if (isAirplaneModeOn) {
            setAirplaneSettingsCorrect(false);
            if (warningNotificationsEnabled) {
                if (PrefUtils.isWifiHandlerActive(context)) {
                    NetworkUtils.buildAirplaneNotification(context);
                }
            }
        } else {
            setAirplaneSettingsCorrect(true);
            NetworkUtils.dismissNotification(context, Config.NOTIFICATION_ID_AIRPLANE_MODE);
        }
    }

    private void setAirplaneSettingsCorrect(boolean isCorrect) {
        WifiHandler.setSetting(Config.AIRPLANE_SETTINGS, isCorrect);
    }


}

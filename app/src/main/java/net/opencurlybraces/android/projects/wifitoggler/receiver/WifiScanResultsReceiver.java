package net.opencurlybraces.android.projects.wifitoggler.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

import net.opencurlybraces.android.projects.wifitoggler.Config;
import net.opencurlybraces.android.projects.wifitoggler.util.NetworkUtils;
import net.opencurlybraces.android.projects.wifitoggler.util.PrefUtils;
import net.opencurlybraces.android.projects.wifitoggler.receiver.util.WifiAdapterActivationHandler;

/**
 * {@link BroadcastReceiver} filtered on {@link android.net.wifi .WifiManager#SCAN_RESULTS_AVAILABLE_ACTION}
 * system action.
 *
 * @author Chris Carneiro
 */
public class WifiScanResultsReceiver extends BroadcastReceiver {
    private static final String TAG = "WifiScanResultsReceiver";


    @Override
    public void onReceive(Context context, Intent intent) {

        boolean locationPermissionGranted = !Config.RUNNING_POST_LOLLIPOP || (ContextCompat
                .checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);


        boolean notConnected = !NetworkUtils.isWifiConnected(context) && locationPermissionGranted;

        if (notConnected) {
            whenMatchFoundEnableWifiAdapter(context);
        } else {

            boolean signalStrengthBelowThreshold = NetworkUtils.getSignalStrength(context) <
                    PrefUtils.getWifiSignalStrengthThreshold(context);

            if (signalStrengthBelowThreshold) {
                NetworkUtils.disableWifiAdapter(context);
            }
        }

    }

    private void whenMatchFoundEnableWifiAdapter(Context context) {
        new WifiAdapterActivationHandler(context).execute();
    }

}

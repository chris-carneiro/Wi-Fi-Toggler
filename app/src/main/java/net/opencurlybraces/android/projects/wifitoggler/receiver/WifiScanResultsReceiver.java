package net.opencurlybraces.android.projects.wifitoggler.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import net.opencurlybraces.android.projects.wifitoggler.Config;
import net.opencurlybraces.android.projects.wifitoggler.data.model.Wifi;
import net.opencurlybraces.android.projects.wifitoggler.util.DeletedSavedWifiHandlerTask;
import net.opencurlybraces.android.projects.wifitoggler.util.NetworkUtils;
import net.opencurlybraces.android.projects.wifitoggler.util.PrefUtils;
import net.opencurlybraces.android.projects.wifitoggler.util.SavedWifiDBUtils;

import java.util.List;

/**
 * {@link BroadcastReceiver} filtered on {@link android.net.wifi
 * .WifiManager#SCAN_RESULTS_AVAILABLE_ACTION}
 * system action.
 *
 * @author Chris Carneiro
 */
public class WifiScanResultsReceiver extends BroadcastReceiver {
    private static final String TAG = "WifiScanResultsReceiver";


    @Override
    public void onReceive(Context context, Intent intent) {
        boolean locationPermissionGranted = !Config.RUNNING_MARSHMALLOW || (ContextCompat
                .checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);

        Log.d(TAG, "onReceive locationPermissionGranted=" + locationPermissionGranted);
        if (!NetworkUtils.isWifiConnected(context) && locationPermissionGranted) {
            new ScanResultAsyncHandler(context).execute();
        } else {

            int signalStrength = NetworkUtils.getSignalStrength(context);
            Log.d(TAG, "already connected");
            if (signalStrength < PrefUtils.getWifiSignalStrengthThreshold
                    (context)) {
                NetworkUtils.disableWifiAdapter(context);
            }
        }

    }


    private static class ScanResultAsyncHandler extends DeletedSavedWifiHandlerTask {

        private final Context mContext;

        public ScanResultAsyncHandler(Context context) {
            super(context);
            mContext = context;
        }


        @Override
        protected Boolean doInBackground(Object... params) {
            //            android.os.Debug.waitForDebugger(); THIS IS EVIL
            List<Wifi> wifisFromDB = getSavedWifisFromDB();
            List<ScanResult> availableWifis = NetworkUtils.getAvailableWifi(mContext);
            Log.d(TAG, "available Wifis count=" + (availableWifis != null ? availableWifis.size()
                    : 0));
            if (availableWifis == null) {
                return false;
            }
            boolean enableWifiAdapter = SavedWifiDBUtils.areThereAutoToggleSavedWifiInRange
                    (mContext, availableWifis, wifisFromDB);

            handleWifiActivation(enableWifiAdapter);
            removeUserUnwantedSavedWifi(wifisFromDB);
            return enableWifiAdapter;
        }

        @Override
        protected void onPostExecute(Object enableWifi) {
            Log.d(TAG, "enableWifi=" + enableWifi);
            super.onPostExecute(enableWifi);
        }

        private void handleWifiActivation(Boolean enableWifi) {
            Log.d(TAG, "handleWifiActivation");
            if (enableWifi) {
                NetworkUtils.enableWifiAdapter(mContext);
            } // else here in the future maybe schedule adapter deactivation as an open wifi
            // workaround
        }
    }
}

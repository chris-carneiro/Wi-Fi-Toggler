package net.opencurlybraces.android.projects.wifitoggler.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import net.opencurlybraces.android.projects.wifitoggler.Config;
import net.opencurlybraces.android.projects.wifitoggler.data.model.Wifi;
import net.opencurlybraces.android.projects.wifitoggler.util.DeletedSavedWifiSweepingTask;
import net.opencurlybraces.android.projects.wifitoggler.util.NetworkUtils;
import net.opencurlybraces.android.projects.wifitoggler.util.PrefUtils;
import net.opencurlybraces.android.projects.wifitoggler.util.SavedWifiDBUtils;
import net.opencurlybraces.android.projects.wifitoggler.util.WifiDeactivationHandler;

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

        if (!NetworkUtils.isWifiConnected(context) && locationPermissionGranted) {

            new ScanResultAsyncHandler(context).execute();
        } else {

            int signalStrength = NetworkUtils.getSignalStrength(context);
            Log.d(TAG, "Already connected");
            if (signalStrength < PrefUtils.getWifiSignalStrengthThreshold
                    (context)) {
                Log.d(TAG, "Signal Strength below threshold, disabling adapter");
                NetworkUtils.disableWifiAdapter(context);
            }
        }

    }


    private static class ScanResultAsyncHandler extends DeletedSavedWifiSweepingTask {
        private static final String TAG = "ScanResultAsyncHandler";
        private final Context mContext;
        private WifiDeactivationHandler mScheduleDisableWifi = null;

        public ScanResultAsyncHandler(Context context) {
            super(context);
            mScheduleDisableWifi = new WifiDeactivationHandler(context);
            mContext = context;
        }


        @Override
        protected Object doInBackground(Object... params) {
            //            android.os.Debug.waitForDebugger(); THIS IS EVIL
            List<Wifi> wifisFromDB = getSavedWifisFromDB();
            List<ScanResult> availableWifis = NetworkUtils.getNearbyWifi(mContext);

            Log.d(TAG, "available Wifis count=" + (availableWifis != null ? availableWifis.size()
                    : 0));
            if (availableWifis == null) {
                return false;
            }
            boolean enableWifiAdapter = SavedWifiDBUtils.areThereAutoToggleSavedWifiInRange
                    (mContext, availableWifis, wifisFromDB);

            handleWifiActivation(enableWifiAdapter);
            return enableWifiAdapter;
        }

        @Override
        protected void onPostExecute(Object enableWifi) {
            Log.d(TAG, "onPostExecute enableWifi=" + enableWifi);
            super.onPostExecute(enableWifi);
            // Fix issue https://github.com/chris-carneiro/Wi-Fi-Toggler/issues/2
            if (!(Boolean) enableWifi) {
                if (NetworkUtils.isWifiEnabled(mContext) && !PrefUtils.isWifiDisableWifiScheduled
                        (mContext)) {
                    scheduleDisableWifi();
                }
            }
        }

        private void handleWifiActivation(Boolean enableWifi) {
            Log.d(TAG, "handleWifiActivation");
            if (enableWifi) {
                NetworkUtils.enableWifiAdapter(mContext);
            }
        }

        private void scheduleDisableWifi() {
            Log.d(TAG, "scheduleDisableWifi=" + mScheduleDisableWifi.obtainMessage(Config
                    .WHAT_SCHEDULE_DISABLE_ADAPTER));
            mScheduleDisableWifi.sendMessageDelayed(Message.obtain(mScheduleDisableWifi,
                            Config.WHAT_SCHEDULE_DISABLE_ADAPTER),
                    Config.INTERVAL_NINETY_SECONDS);
            PrefUtils.setDisableWifiScheduled(mContext, true);
        }
    }
}

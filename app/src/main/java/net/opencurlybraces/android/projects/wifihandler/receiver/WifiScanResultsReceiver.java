package net.opencurlybraces.android.projects.wifihandler.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;

import net.opencurlybraces.android.projects.wifihandler.util.WifiUtils;

import java.util.List;

/**
 * {@link BroadcastReceiver} filtered on {@link android.net.wifi
 * .WifiManager#SCAN_RESULTS_AVAILABLE_ACTION}
 * system action.
 *
 * @author Chris Carneiro
 */
public class WifiScanResultsReceiver extends BroadcastReceiver {
    private static final String TAG = WifiScanResultsReceiver.class.getSimpleName();
    WifiManager mWifiManager = null;
    private static final int SIGNAL_STRENGTH_THRESHOLD = -80;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (mWifiManager == null) {
            mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        }
        Log.d(TAG, "SCAN_RESULTS received");
        new ScanResultAsyncHandler(mWifiManager).execute();
    }


    /**
     * First event on First application launch: - getConfiguredNetworks - insert configured
     * networks
     * <p>TODO onconnected : - Update known networks Database - unregister on scanresult receiver
     * <p/>
     * TODO on disconnected : - register scanresultreceiver if no known networks in range, disable
     * wifi adapter.</p> see http://stackoverflow.com/a/6362468/2445061
     *
     * So with this algorithm no need to check for available networks and their signal strengh
     *, which is a huge battery drainer, since on disconnection it means that the network is not
     * in range anymore.
     * TODO add a receiver for WIFI_AP_STATE_CHANGED set in sharedPreferences the state of the
     * hotspot ap(active or not), at app first launch if active: ask user to disable hotspot.
     */
    private static class ScanResultAsyncHandler extends AsyncTask<Void, Void, Void> {

        private WifiManager mWifiManager = null;

        private ScanResultAsyncHandler(WifiManager wifiManager) {
            mWifiManager = wifiManager;
        }

        @Override
        protected Void doInBackground(Void... params) {
            handleScanResults();
            return null;
        }

        private void handleScanResults() {
            try {
                List<ScanResult> availableWifiNetworks = mWifiManager
                        .getScanResults();
                // TODO replace this call with a database query
                List<WifiConfiguration> configuredWifiNetworks = WifiUtils.getConfiguredWifis
                        (mWifiManager);

                if (configuredWifiNetworks == null || availableWifiNetworks == null) {
                    return;
                }
                for (ScanResult wifiNetwork : availableWifiNetworks) {
                    for (WifiConfiguration wifiConfig : configuredWifiNetworks) {
                        String quoteStripped = wifiConfig.SSID.replace("\"", "");
                        if (quoteStripped.equals(wifiNetwork.SSID)) {
                            Log.d(TAG, "Signal Strength=" + wifiNetwork.level + " mSSID=" +
                                    wifiNetwork
                                            .SSID);
                            if (wifiNetwork.level > SIGNAL_STRENGTH_THRESHOLD) {
                                mWifiManager.setWifiEnabled(true);
                            } else {
                                mWifiManager.setWifiEnabled(false);
                            }
                        }
                    }
                }
            } catch (InterruptedException e) {
                Log.d(TAG, "Could not retrieve user's configured networks");
            }
        }


    }
}

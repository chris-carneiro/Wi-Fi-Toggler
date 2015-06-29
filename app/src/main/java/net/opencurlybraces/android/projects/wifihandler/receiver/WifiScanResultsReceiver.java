package net.opencurlybraces.android.projects.wifihandler.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;

import net.opencurlybraces.android.projects.wifihandler.data.table.ConfiguredWifi;

import java.util.ArrayList;
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
    ConnectivityManager mConnectivityManager = null;
    private static final int SIGNAL_STRENGTH_THRESHOLD = -80;

    @Override
    public void onReceive(Context context, Intent intent) {

        if (mConnectivityManager == null) {
            mConnectivityManager = (ConnectivityManager) context.getSystemService(Context
                    .CONNECTIVITY_SERVICE);
        }

        NetworkInfo wifiInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (!wifiInfo.isConnectedOrConnecting()) {
            new ScanResultAsyncHandler(context).execute();
        }
        Log.d(TAG, "SCAN_RESULTS received");


    }


    /**
     * First event on First application launch: - getConfiguredNetworks - insert configured networks
     * <p>TODO onconnected : - Update known networks Database - unregister on scanresult receiver
     * <p/> TODO on disconnected : - register scanresultreceiver if no known networks in range,
     * disable wifi adapter.</p> see http://stackoverflow.com/a/6362468/2445061
     * <p/>
     * So with this algorithm no need to check for available networks and their signal strengh ,
     * which is a huge battery drainer, since on disconnection it means that the network is not in
     * range anymore. TODO add a receiver for WIFI_AP_STATE_CHANGED set in sharedPreferences the
     * state of the hotspot ap(active or not), at app first launch if active: ask user to disable
     * hotspot.
     */
    private static class ScanResultAsyncHandler extends AsyncTask<Void, Void, Void> {

        private WifiManager mWifiManager = null;
        private final Context mContext;

        private static final String[] PROJECTION = new String[]{ConfiguredWifi.SSID};

        private ScanResultAsyncHandler(final Context context) {
            mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            mContext = context;
        }

        @Override
        protected Void doInBackground(Void... params) {
            handleScanResults();
            return null;
        }

        private void handleScanResults() {

            List<ScanResult> availableWifiNetworks = mWifiManager
                    .getScanResults();

            List<String> savedSSIDs = querySavedSSID();
            if (savedSSIDs == null || availableWifiNetworks == null) {
                return;
            }
            for (ScanResult wifiNetwork : availableWifiNetworks) {
                for (String savedWifi : savedSSIDs) {
                    if (savedWifi.equals(wifiNetwork.SSID)) {
                        Log.d(TAG, "Signal Strength=" + wifiNetwork.level + " mSSID=" +
                                wifiNetwork
                                        .SSID);
                        if (wifiNetwork.level > SIGNAL_STRENGTH_THRESHOLD) {
                            mWifiManager.setWifiEnabled(true);
                            return;
                        } else {
                            mWifiManager.setWifiEnabled(false);
                        }
                    }
                }
            }
        }

        private List<String> querySavedSSID() {
            List<String> savedSSIDs = null;
            Cursor cursor = null;
            try {
                cursor = mContext.getContentResolver().query(ConfiguredWifi.CONTENT_URI
                        , PROJECTION, null, null, null);
                if (cursor != null) {
                    int index = cursor.getColumnIndexOrThrow(ConfiguredWifi.SSID);
                    savedSSIDs = new ArrayList<>(cursor.getCount());
                    String ssid = null;
                    while (cursor.moveToNext()) {
                        ssid = cursor.getString(index);
                        savedSSIDs.add(ssid);
                    }
                    cursor.close();
                }
            } catch (IllegalArgumentException e) {
                if (cursor != null)
                    cursor.close();

                e.printStackTrace();
            }
            return savedSSIDs;
        }


    }
}

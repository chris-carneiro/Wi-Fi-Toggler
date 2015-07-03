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

import net.opencurlybraces.android.projects.wifihandler.data.table.SavedWifi;

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
    NetworkInfo mWifiInfo = null;
    private static final int SIGNAL_STRENGTH_THRESHOLD = -80;

    @Override
    public void onReceive(Context context, Intent intent) {

        lazyInit(context);

        if (!mWifiInfo.isConnectedOrConnecting()) {
            Log.d(TAG, "SCAN_RESULTS received");
            new ScanResultAsyncHandler(context).execute();
        }
    }

    private void lazyInit(final Context context) {
        if (mConnectivityManager == null)
            mConnectivityManager = (ConnectivityManager) context.getSystemService(Context
                    .CONNECTIVITY_SERVICE);

        if (mWifiInfo == null)
            mWifiInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
    }

    /**
     * TODO add a receiver for WIFI_AP_STATE_CHANGED set in sharedPreferences the state of the
     * hotspot ap(active or not), at app first launch if active: ask user to disable hotspot.
     */
    private static class ScanResultAsyncHandler extends AsyncTask<Void, Void, Void> {

        private WifiManager mWifiManager = null;
        private final Context mContext;

        private static final String[] PROJECTION = new String[]{SavedWifi.SSID};

        private ScanResultAsyncHandler(final Context context) {
            mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            mContext = context;
        }

        @Override
        protected Void doInBackground(Void... params) {
            handleScanResults(mWifiManager.getScanResults());
            return null;
        }

        private void handleScanResults(List<ScanResult> availableWifiNetworks) {
            List<String> savedSSIDsFromDb = getSavedSSIDsFromDB();

            if (savedSSIDsFromDb == null || availableWifiNetworks == null) {
                return;
            }
            for (ScanResult wifiNetwork : availableWifiNetworks) {
                for (String savedWifi : savedSSIDsFromDb) {
                    if (savedWifi.equals(wifiNetwork.SSID)) {
                        Log.d(TAG, "Signal Strength=" + wifiNetwork.level + " mSSID=" +
                                wifiNetwork
                                        .SSID);
                        if (wifiNetwork.level > SIGNAL_STRENGTH_THRESHOLD) {

                            mWifiManager.setWifiEnabled(true);
                            return;
                        }
                    }
                }
            }
        }

        private List<String> getSavedSSIDsFromDB() {
            List<String> savedSSIDs = null;
            Cursor cursor = null;
            try {
                cursor = mContext.getContentResolver().query(SavedWifi.CONTENT_URI
                        , PROJECTION, null, null, null);
                if (cursor != null) {
                    int index = cursor.getColumnIndexOrThrow(SavedWifi.SSID);
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
            } finally {
                if (cursor != null) cursor.close();
            }
            return savedSSIDs;
        }

    }
}

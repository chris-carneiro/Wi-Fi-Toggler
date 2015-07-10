package net.opencurlybraces.android.projects.wifihandler.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;

import net.opencurlybraces.android.projects.wifihandler.data.table.SavedWifi;
import net.opencurlybraces.android.projects.wifihandler.util.NetworkUtils;

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
    private static final int SIGNAL_STRENGTH_THRESHOLD = -80;

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG, "Wifi Connected=" + NetworkUtils.isWifiConnected(context));
        if (!NetworkUtils.isWifiConnected(context)) {
            Log.d(TAG, "SCAN_RESULTS received");
            new ScanResultAsyncHandler(context).execute();
        } else {
            Log.d(TAG, "already connected");

        }


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
            List<String> ssiDsFromDB = getSavedSSIDsFromDB();

            handleScanResults(mWifiManager.getScanResults(), ssiDsFromDB);

            removeUserUnwantedSavedWifi(ssiDsFromDB);
            return null;
        }

        /**
         * Compare the list of saved wifi from db with the system user's saved wifis and remove from
         * db the ones that aren't in the system anymore
         *
         * @param ssiDsFromDB
         */
        private void removeUserUnwantedSavedWifi(List<String> ssiDsFromDB) {
            List<WifiConfiguration> savedWifis = mWifiManager.getConfiguredNetworks();
            if (savedWifis == null) return;
            List<String> userSsids = extractSsidListFromSavedWifi(savedWifis);
            //TODO add robustness

            for (String ssidDb : ssiDsFromDB) {
                if (!userSsids.contains(ssidDb)) {
                    mContext.getContentResolver().delete(SavedWifi.CONTENT_URI, SavedWifi.SSID +
                            "=?", new String[]{ssidDb});
                }
            }
        }

        private void handleScanResults(List<ScanResult> availableWifiNetworks, List<String>
                savedSSIDsFromDb) {

            if (savedSSIDsFromDb == null || availableWifiNetworks == null) {
                return;
            }
            for (ScanResult wifiNetwork : availableWifiNetworks) {
                for (String savedWifi : savedSSIDsFromDb) {
                    if (savedWifi.equals(wifiNetwork.SSID)) {
                        Log.d(TAG, "Signal Strength=" + wifiNetwork.level + " mSSID=" +
                                wifiNetwork
                                        .SSID);
                        if (wifiNetwork.level >= SIGNAL_STRENGTH_THRESHOLD) {
                            mWifiManager.setWifiEnabled(true);
                            Log.d(TAG, "Saved wifi in range, enabling wifi adapter");
                            return;
                        } else {
                            mWifiManager.setWifiEnabled(false);
                        }
                    }
                }
            }
        }

        private List<String> extractSsidListFromSavedWifi(List<WifiConfiguration> savedWifis) {
            if (savedWifis == null || savedWifis.size() == 0) return null;
            List<String> ssids = new ArrayList<>(savedWifis.size());
            for (WifiConfiguration wifi : savedWifis) {
                ssids.add(wifi.SSID.replace("\"", ""));
            }

            return ssids;
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

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

import net.opencurlybraces.android.projects.wifihandler.Config;
import net.opencurlybraces.android.projects.wifihandler.data.table.SavedWifi;
import net.opencurlybraces.android.projects.wifihandler.util.NetworkUtils;
import net.opencurlybraces.android.projects.wifihandler.util.PrefUtils;

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
    private static final String TAG = "WifiScanResultsReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG, "Wifi Connected=" + NetworkUtils.isWifiConnected(context));
        if (!NetworkUtils.isWifiConnected(context)) {

            Log.d(TAG, "SCAN_RESULTS received");
            new ScanResultAsyncHandler(context).execute();
        } else {
            Log.d(TAG, "already connected checking wifi strength");
            int signalStrength = NetworkUtils.getSignalStrength(context);

            if (signalStrength < PrefUtils.getWifiSignalStrengthThreshold
                    (context)) {
                NetworkUtils.disableWifiAdapter(context);
            }
        }

    }



    /**
     * TODO add a receiver for WIFI_AP_STATE_CHANGED set in sharedPreferences the state of the
     * hotspot ap(active or not), at app first launch if active: ask user to disable hotspot.
     */
    private static class ScanResultAsyncHandler extends AsyncTask<Void, Void, Boolean> {
        private final Context mContext;

        private static final String[] PROJECTION = new String[]{SavedWifi.SSID};

        private ScanResultAsyncHandler(final Context context) {
            mContext = context;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            //            android.os.Debug.waitForDebugger(); THIS IS EVIL
            List<String> ssiDsFromDB = getSavedSSIDsFromDB();
            List<ScanResult> availableWifis = NetworkUtils.getAvailableWifi(mContext);
            if (availableWifis == null) {
                return false;
            }
            boolean enableWifiAdapter = savedWifiInRange(availableWifis, ssiDsFromDB);

            removeUserUnwantedSavedWifi(ssiDsFromDB);
            return enableWifiAdapter;
        }

        @Override
        protected void onPostExecute(Boolean enableWifi) {
            Log.d(TAG, "enableWifi=" + enableWifi);
            if (enableWifi) {
                NetworkUtils.enableWifiAdapter(mContext);
            } else {
                if (NetworkUtils.isWifiEnabled(mContext)) {

                    NetworkUtils.disableWifiAdapter(mContext);
                }
            }
        }

        /**
         * Compare the list of saved wifi from db with the system user's saved wifis and remove from
         * db the ones that aren't in the system anymore
         *
         * @param ssiDsFromDB
         */
        private void removeUserUnwantedSavedWifi(List<String> ssiDsFromDB) {
            List<WifiConfiguration> savedWifis = NetworkUtils.getSavedWifiSync(mContext);
            if (savedWifis == null) {
                return;
            }
            List<String> userSsids = extractSsidListFromSavedWifi(savedWifis);
            //TODO add robustness

            for (String ssidDb : ssiDsFromDB) {
                if (!userSsids.contains(ssidDb)) {
                    mContext.getContentResolver().delete(SavedWifi.CONTENT_URI, SavedWifi.SSID +
                            "=?", new String[]{ssidDb});
                }
            }
        }

        private boolean savedWifiInRange(List<ScanResult> availableWifiNetworks, List<String>
                savedSSIDsFromDb) {
            Log.d(TAG, "savedWifiInRange availableWifiNetworks=" + (availableWifiNetworks !=
                    null) +
                    " savedSSIDsFromDb=" + (savedSSIDsFromDb != null));
            if (savedSSIDsFromDb == null || availableWifiNetworks == null) {
                return false;
            }
            for (ScanResult wifiNetwork : availableWifiNetworks) {
                for (String savedWifi : savedSSIDsFromDb) {
                    if (savedWifi.equals(wifiNetwork.SSID)) {
                        Log.d(TAG, "Signal Strength=" + wifiNetwork.level + " mSSID=" +
                                wifiNetwork
                                        .SSID);
                        Log.d(TAG, "Wifi Signal strength level=" + WifiManager.calculateSignalLevel
                                (wifiNetwork.level, Config.WIFI_SIGNAL_STRENGTHLEVELS) + " " +
                                "Threshold=" + PrefUtils
                                .getWifiSignalStrengthThreshold
                                        (mContext));
                        int signalStrength = WifiManager.calculateSignalLevel
                                (wifiNetwork.level, Config.WIFI_SIGNAL_STRENGTHLEVELS);
                        if (signalStrength >= PrefUtils.getWifiSignalStrengthThreshold
                                (mContext)) {
                            return true;
                        }
                    }
                }
            }
            return false;
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

                    while (cursor.moveToNext()) {
                        String ssid = cursor.getString(index);
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

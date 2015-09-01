package net.opencurlybraces.android.projects.wifitoggler.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;

import net.opencurlybraces.android.projects.wifitoggler.Config;
import net.opencurlybraces.android.projects.wifitoggler.data.model.Wifi;
import net.opencurlybraces.android.projects.wifitoggler.data.table.SavedWifi;
import net.opencurlybraces.android.projects.wifitoggler.util.NetworkUtils;
import net.opencurlybraces.android.projects.wifitoggler.util.PrefUtils;

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

        if (!NetworkUtils.isWifiConnected(context)) {
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

    private static class ScanResultAsyncHandler extends AsyncTask<Void, Void, Boolean> {
        private final Context mContext;

        private static final String[] PROJECTION = new String[]{SavedWifi.SSID, SavedWifi
                .AUTO_TOGGLE};

        private ScanResultAsyncHandler(final Context context) {
            mContext = context;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            //            android.os.Debug.waitForDebugger(); THIS IS EVIL
            List<Wifi> wifisFromDB = getSavedWifisFromDB();
            List<ScanResult> availableWifis = NetworkUtils.getAvailableWifi(mContext);
            if (availableWifis == null) {
                return false;
            }
            boolean enableWifiAdapter = areThereActiveAutoToggleSavedWifiInRange(availableWifis,
                    wifisFromDB);

            handleWifiActivation(enableWifiAdapter);
            removeUserUnwantedSavedWifi(wifisFromDB);
            return enableWifiAdapter;
        }

        @Override
        protected void onPostExecute(Boolean enableWifi) {
            Log.d(TAG, "enableWifi=" + enableWifi);
        }

        private void handleWifiActivation(Boolean enableWifi) {
            Log.d(TAG, "handleWifiActivation");
            if (enableWifi) {
                NetworkUtils.enableWifiAdapter(mContext);
            }
        }

        /**
         * Compare the list of saved wifi from db with the system user's saved wifis and remove from
         * db the ones that aren't in the system anymore
         *
         * @param wifisFromDB
         */
        private void removeUserUnwantedSavedWifi(List<Wifi> wifisFromDB) {
            Log.d(TAG, "removeUserUnwantedSavedWifi");
            List<WifiConfiguration> savedWifis = NetworkUtils.getSavedWifiSync(mContext);
            if (savedWifis == null) {
                return;
            }
            List<String> savedSSIDs = extractSSIDListFromSavedWifi(savedWifis);

            for (Wifi wifiDb : wifisFromDB) {
                if (!savedSSIDs.contains(wifiDb.ssid)) {
                    deleteSSIDFromDb(wifiDb.ssid);
                }
            }
        }

        private void deleteSSIDFromDb(String ssidDb) {
            mContext.getContentResolver().delete(SavedWifi.CONTENT_URI, SavedWifi.SSID +
                    "=?", new String[]{ssidDb});
        }

        private boolean areThereActiveAutoToggleSavedWifiInRange(List<ScanResult>
                                                                         availableWifiNetworks,
                                                                 List<Wifi>
                                                                         savedWifisFromDb) {
            Log.d(TAG, "areThereActiveAutoToggleSavedWifiInRange availableWifiNetworks=" +
                    (availableWifiNetworks !=
                            null) +
                    " savedWifisFromDb=" + (savedWifisFromDb != null));
            if (savedWifisFromDb == null || availableWifiNetworks == null) {
                return false;
            }
            for (ScanResult wifiNetwork : availableWifiNetworks) {
                for (Wifi savedWifi : savedWifisFromDb) {
                    if (savedWifi.ssid.equals(wifiNetwork.SSID) && savedWifi.isAutoToggle) {
                        int signalStrength = WifiManager.calculateSignalLevel
                                (wifiNetwork.level, Config.WIFI_SIGNAL_STRENGTH_LEVELS);
                        Log.d(TAG, "signalStrength=" + signalStrength + " preferenceThreshold=" +
                                PrefUtils
                                        .getWifiSignalStrengthThreshold
                                                (mContext));
                        if (signalStrength >= PrefUtils.getWifiSignalStrengthThreshold
                                (mContext)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        private List<String> extractSSIDListFromSavedWifi(List<WifiConfiguration> savedWifis) {
            if (savedWifis == null || savedWifis.size() == 0) return null;

            List<String> ssids = new ArrayList<>(savedWifis.size());
            for (WifiConfiguration wifi : savedWifis) {
                ssids.add(wifi.SSID.replace("\"", ""));
            }

            return ssids;
        }

        private List<Wifi> getSavedWifisFromDB() {
            List<Wifi> savedWifis = null;
            Cursor cursor = null;
            try {
                cursor = mContext.getContentResolver().query(SavedWifi.CONTENT_URI
                        , PROJECTION, null, null, null);
                if (cursor != null) {
                    savedWifis = new ArrayList<>(cursor.getCount());
                    while (cursor.moveToNext()) {
                        Wifi savedWifi = Wifi.buildForCursor(cursor);
                        savedWifis.add(savedWifi);
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
            return savedWifis;
        }
    }
}

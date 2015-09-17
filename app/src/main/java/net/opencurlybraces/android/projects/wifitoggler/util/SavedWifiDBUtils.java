package net.opencurlybraces.android.projects.wifitoggler.util;

import android.content.Context;
import android.database.Cursor;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import net.opencurlybraces.android.projects.wifitoggler.Config;
import net.opencurlybraces.android.projects.wifitoggler.data.model.Wifi;
import net.opencurlybraces.android.projects.wifitoggler.data.table.SavedWifi;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chris on 02/09/15.
 */
public class SavedWifiDBUtils {

    private static final String TAG = "SavedWifiDBUtils";

    public static void deleteSSIDFromDb(final Context context, String ssidDb) {
        int deletedWifis = context.getContentResolver().delete(SavedWifi.CONTENT_URI, SavedWifi
                .SSID +
                "=?", new String[]{ssidDb});
        Log.d(TAG, "deletedWifis=" + deletedWifis);
    }

    public static List<String> extractSSIDListFromSavedWifi(List<WifiConfiguration> savedWifis) {
        if (savedWifis == null || savedWifis.size() == 0) return null;

        List<String> ssids = new ArrayList<>(savedWifis.size());
        for (WifiConfiguration wifi : savedWifis) {
            ssids.add(wifi.SSID.replace("\"", ""));
        }

        return ssids;
    }

    public static List<Wifi> getSavedWifisFromDB(final Context context, final String[] projection) {
        List<Wifi> savedWifis = null;
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(SavedWifi.CONTENT_URI
                    , projection, null, null, null);
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

    public static boolean areThereAutoToggleSavedWifiInRange(final Context context, List<ScanResult>
            availableWifiNetworks, List<Wifi> savedWifisFromDb) {
        Log.d(TAG, "areThereAutoToggleSavedWifiInRange availableWifiNetworks=" +
                (availableWifiNetworks !=
                        null ? availableWifiNetworks.size() : null) +
                " savedWifisFromDb=" + (savedWifisFromDb != null ? savedWifisFromDb.size() : null));
        if (savedWifisFromDb == null || availableWifiNetworks == null) {
            return false;
        }
        for (ScanResult wifiNetwork : availableWifiNetworks) {
            for (Wifi savedWifi : savedWifisFromDb) {
                if (savedWifi.getSsid().equals(wifiNetwork.SSID) && savedWifi.isAutoToggle()) {
                    int signalStrength = WifiManager.calculateSignalLevel
                            (wifiNetwork.level, Config.WIFI_SIGNAL_STRENGTH_LEVELS);
                    Log.d(TAG, "signalStrength=" + signalStrength + " preferenceThreshold=" +
                            PrefUtils
                                    .getWifiSignalStrengthThreshold
                                            (context));
                    if (signalStrength >= PrefUtils.getWifiSignalStrengthThreshold
                            (context)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}

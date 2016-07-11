package net.opencurlybraces.android.projects.wifitoggler.util;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.os.AsyncTask;
import android.util.Log;

import net.opencurlybraces.android.projects.wifitoggler.Config;
import net.opencurlybraces.android.projects.wifitoggler.data.model.Wifi;
import net.opencurlybraces.android.projects.wifitoggler.data.table.SavedWifi;

import java.util.ArrayList;
import java.util.List;

/**
 * Created to delete the saved wifis(from DB) that the user deliberately removed from the saved
 * networks Created by chris on 02/09/15.
 */
public class DeletedSavedWifiSweepingTask extends AsyncTask<Object, Object, Object> {

    private static final String TAG = "DeletedSavedWifiTask";


    private final Context mContext;

    public DeletedSavedWifiSweepingTask(final Context context) {
        mContext = context;
    }

    public static List<String> extractSSIDListFromUserWifi(List<WifiConfiguration> savedWifis) {
        if (savedWifis == null || savedWifis.size() == 0) return null;

        List<String> ssids = new ArrayList<>(savedWifis.size());
        for (WifiConfiguration wifi : savedWifis) {
            ssids.add(wifi.SSID.replace("\"", ""));
        }

        return ssids;
    }

    @Override
    protected Object doInBackground(Object... params) {
        List<Wifi> wifisFromDB = Wifi.getUserWifiFromDB(mContext, Config.WIFI);

        removeWifiDeletedByUserFromLocalDB(wifisFromDB);
        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
    }


    /**
     * Compare the list of saved wifi from db with the system user's saved wifis and remove from db
     * the ones that aren't in the system anymore
     *
     * @param wifisFromDB
     */
    private void removeWifiDeletedByUserFromLocalDB(List<Wifi> wifisFromDB) {
        Log.d(TAG, "removeWifiDeletedByUserFromLocalDB");
        List<WifiConfiguration> userWifis = NetworkUtils.getUserWifiFromSystemSync(mContext);
        Log.d(TAG, "System Saved Wifi=" + (userWifis != null ? userWifis.size() : null));
        if (userWifis == null) {
            return;
        }
        List<String> ssids = extractSSIDListFromUserWifi(userWifis);

        for (Wifi wifiDb : wifisFromDB) {
            boolean userDeletedNetworkFromSystem = !ssids.contains(wifiDb.getSsid());
            if (userDeletedNetworkFromSystem) {
                deleteSSIDFromDb(mContext, wifiDb.getSsid());
            }
        }
    }

    private void deleteSSIDFromDb(final Context context, String ssid) {
        int deletedWifis = context.getContentResolver().delete(SavedWifi.CONTENT_URI, SavedWifi
                .SSID + "=?", new String[]{ssid});
        Log.d(TAG, "deletedWifis=" + deletedWifis);
    }
}

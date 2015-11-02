package net.opencurlybraces.android.projects.wifitoggler.util;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.os.AsyncTask;
import android.util.Log;

import net.opencurlybraces.android.projects.wifitoggler.data.model.Wifi;
import net.opencurlybraces.android.projects.wifitoggler.data.table.SavedWifi;

import java.util.List;

/**
 * Created to delete the saved wifis(from DB) that the user deliberately removed from the saved
 * networks
 * Created by chris on 02/09/15.
 */
public class DeletedSavedWifiSweepingTask extends AsyncTask<Object, Object, Object> {
    private static final String TAG = "DeletedSavedWifiTask";
    private static final String[] PROJECTION = new String[]{SavedWifi._ID, SavedWifi.SSID, SavedWifi
            .AUTO_TOGGLE};

    private final Context mContext;


    public DeletedSavedWifiSweepingTask(final Context context) {
        mContext = context;
    }

    @Override
    protected Object doInBackground(Object... params) {
        List<Wifi> wifisFromDB = getSavedWifisFromDB();

        removeUserUnwantedSavedWifi(wifisFromDB);
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
    private void removeUserUnwantedSavedWifi(List<Wifi> wifisFromDB) {
        Log.d(TAG, "removeUserUnwantedSavedWifi");
        List<WifiConfiguration> savedWifis = NetworkUtils.getSavedWifiSync(mContext);
        Log.d(TAG, "System Saved Wifi=" + (savedWifis != null ? savedWifis.size() : null));
        if (savedWifis == null) {
            return;
        }
        List<String> savedSSIDs = SavedWifiDBUtils.extractSSIDListFromSavedWifi(savedWifis);

        for (Wifi wifiDb : wifisFromDB) {
            if (!savedSSIDs.contains(wifiDb.getSsid())) {
                SavedWifiDBUtils.deleteSSIDFromDb(mContext, wifiDb.getSsid());
            }
        }
    }

    protected List<Wifi> getSavedWifisFromDB() {
        return SavedWifiDBUtils.getSavedWifisFromDB(mContext, PROJECTION);
    }


}

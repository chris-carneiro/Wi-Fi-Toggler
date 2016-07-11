package net.opencurlybraces.android.projects.wifitoggler.receiver.util;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.os.AsyncTask;
import android.util.Log;

import net.opencurlybraces.android.projects.wifitoggler.Config;
import net.opencurlybraces.android.projects.wifitoggler.data.model.Wifi;
import net.opencurlybraces.android.projects.wifitoggler.util.NetworkUtils;
import net.opencurlybraces.android.projects.wifitoggler.util.PrefUtils;

import java.util.List;

/**
 * Created by chris on 11/07/16.
 */
public class WifiAdapterActivationHandler extends AsyncTask<Object, Object, Object> {

    private static final String TAG = "WifiActivationHandler";
    private final Context mContext;


    public WifiAdapterActivationHandler(Context context) {
        mContext = context;
    }

    @Override
    protected Object doInBackground(Object... params) {
        //            android.os.Debug.waitForDebugger(); THIS IS EVIL
        List<Wifi> userWifi = Wifi.getUserWifiAutoToggleEnabledFromDB(mContext, Config.WIFI);
        Log.d(TAG, "doInBackground: userWifi count=" + (userWifi != null ? userWifi.size() : null));
        List<ScanResult> nearbyNetworks = NetworkUtils.getNearbyWifi(mContext);
        if (nearbyNetworks == null) {
            return false;
        }
        int signalThreshold = PrefUtils.getWifiSignalStrengthThreshold(mContext);
        boolean matchFound = Wifi.nearbyNetworkMatchUserWifi(signalThreshold, nearbyNetworks,
                userWifi);

        if (matchFound) {
            NetworkUtils.enableWifiAdapter(mContext);
        }
        return matchFound;
    }

    @Override
    protected void onPostExecute(Object matchFound) {
        Log.d(TAG, "onPostExecute enableWifi=" + matchFound);
        super.onPostExecute(matchFound);
        // Fix issue https://github.com/chris-carneiro/Wi-Fi-Toggler/issues/2
        boolean noMatchFound = !(Boolean) matchFound;
        boolean wifiEnabled = NetworkUtils.isWifiEnabled(mContext);
        boolean notAlreadyScheduled = !PrefUtils.isWifiDisableWifiScheduled(mContext);

        if (noMatchFound && wifiEnabled && notAlreadyScheduled) {
            makeSureWifiIsDisabled();
        }
    }

    private void makeSureWifiIsDisabled() {
        int delay = PrefUtils.getWifiDeactivationDelay(mContext);
        NetworkUtils.scheduleDisableWifi(mContext, delay);
    }

}

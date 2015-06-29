package net.opencurlybraces.android.projects.wifihandler.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;
import android.util.Log;

/**
 * Created by chris on 15/06/15.
 */
public class WifiStateReceiver extends BroadcastReceiver {

    private static final String TAG = "WifiStateReceiver";
    WifiManager mWifiManager = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "wifi state changed");
        if (mWifiManager == null) {
            mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        }

        SupplicantState state = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);

        //        boolean isConnected = intent.getBooleanExtra(WifiManager
        // .EXTRA_SUPPLICANT_CONNECTED, false);
        Log.d(TAG, "state " + state.toString());
        switch (state) {
            case COMPLETED:
                Log.d(TAG, "Wifi Connected");
                break;

            case DISCONNECTED:
                Log.d(TAG, "Wifi DisConnected");
                break;
        }

        //        if (isConnected) {
        //
        //
        //        } else {
        //
        //        }
    }
}

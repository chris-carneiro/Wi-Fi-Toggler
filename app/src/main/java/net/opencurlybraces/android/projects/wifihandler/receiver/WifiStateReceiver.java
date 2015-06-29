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
        Log.d(TAG, "wifi state changed action=" + intent.getAction());
        if (mWifiManager == null) {
            mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        }

        //TODO asyncTask to handle datebase updates
        switch (intent.getAction()) {
            case WifiManager.WIFI_STATE_CHANGED_ACTION:
                int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
                switch (wifiState) {
                    case WifiManager.WIFI_STATE_DISABLED:

                        break;
                    case WifiManager.WIFI_STATE_ENABLED:
                        Log.d(TAG, "wifiState = " + wifiState);
                        break;

                }
                break;
            case WifiManager.SUPPLICANT_STATE_CHANGED_ACTION:
                SupplicantState state = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);

                Log.d(TAG, "state " + state.toString());
                switch (state) {
                    case COMPLETED:
                        Log.d(TAG, "Wifi Connected");
                        // TODO update SavedWifi state in DB
                        break;

                    case DISCONNECTED:
                        Log.d(TAG, "Wifi DisConnected");
                        // TODO update SavedWifi state in DB
                        break;
                }
                break;
        }


    }
}

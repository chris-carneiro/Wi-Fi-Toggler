package net.opencurlybraces.android.projects.wifihandler.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import net.opencurlybraces.android.projects.wifihandler.service.WifiHandlerService;
import net.opencurlybraces.android.projects.wifihandler.util.NetworkUtils;
import net.opencurlybraces.android.projects.wifihandler.util.PrefUtils;

/**
 * Created by chris on 30/06/15.
 */
public class WifiSupplicantStateReceiver extends BroadcastReceiver {
    private static final String TAG = "WifiSupplicantReceiver";

    public static final String EXTRA_CURRENT_SSID = "net.opencurlybraces.android" +
            ".projects.wifihandler.receiver.current_ssid";

    @Override
    public void onReceive(Context context, Intent intent) {


        if (PrefUtils.isWifiHandlerActive(context)) {
            SupplicantState state = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
            Intent updateSavedWifiState = buildIntentForWifiState(context, state);
            if (updateSavedWifiState == null || updateSavedWifiState.getAction() == null)
                return;

            context.startService(updateSavedWifiState);
        } else {
            Log.d(TAG, "WifiHandler inactive ignoring SupplicantStateChanged events");
        }

    }

    private static Intent buildIntentForWifiState(final Context context,
                                                  SupplicantState
                                                          state) {
        Intent updateSavedWifiState = new Intent(context,
                WifiHandlerService.class);

        switch (state) {
            case COMPLETED:
                Log.d(TAG, "COMPLETED supplicant state received=");
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context
                        .WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();

                String strippedSSID = wifiInfo.getSSID().replace("\"", "");
                updateSavedWifiState.putExtra(EXTRA_CURRENT_SSID,
                        strippedSSID);

                updateSavedWifiState.putExtra(WifiStateReceiver.EXTRA_SAVED_WIFI_NEW_STATE,
                        NetworkUtils.WifiAdapterStatus.CONNECTED);

                updateSavedWifiState.setAction(WifiHandlerService
                        .ACTION_HANDLE_SAVED_WIFI_UPDATE_CONNECT);

                break;
            case DISCONNECTED:
                Log.d(TAG, "DISCONNECTED supplicant state received=");
                updateSavedWifiState.putExtra(WifiStateReceiver.EXTRA_SAVED_WIFI_NEW_STATE,
                        NetworkUtils.WifiAdapterStatus.DISCONNECTED);

                updateSavedWifiState.setAction(WifiHandlerService
                        .ACTION_HANDLE_SAVED_WIFI_UPDATE_DISCONNECT);

                break;
            default:
                // Ignore other wifi states
                Log.d(TAG, "Supplicant state received and ignored=" + state);
                break;
        }
        return updateSavedWifiState;
    }
}

package net.opencurlybraces.android.projects.wifihandler.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import net.opencurlybraces.android.projects.wifihandler.service.WifiHandlerService;
import net.opencurlybraces.android.projects.wifihandler.util.NetworkUtils;
import net.opencurlybraces.android.projects.wifihandler.util.PrefUtils;

/**
 * Created by chris on 30/06/15.
 */
public class WifiConnectionStateReceiver extends BroadcastReceiver {
    private static final String TAG = "WifiSupplicantReceiver";

    public static final String EXTRA_CURRENT_SSID = "net.opencurlybraces.android" +
            ".projects.wifihandler.receiver.current_ssid";
    public static final String EXTRA_SAVED_WIFI_NEW_STATE = "net.opencurlybraces.android" +
            ".projects.wifihandler.receiver.saved_wifi_state";

    @Override
    public void onReceive(Context context, Intent intent) {


        if (PrefUtils.isWifiHandlerActive(context)) {
            //            SupplicantState state = intent.getParcelableExtra(WifiManager
            // .EXTRA_NEW_STATE);
            Intent updateSavedWifiState = buildIntentForWifiConnectionState(context);
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

                updateSavedWifiState.putExtra(EXTRA_SAVED_WIFI_NEW_STATE,
                        NetworkUtils.WifiAdapterStatus.CONNECTED);

                updateSavedWifiState.setAction(WifiHandlerService
                        .ACTION_HANDLE_SAVED_WIFI_UPDATE_CONNECT);

                break;
            case DISCONNECTED:
                Log.d(TAG, "DISCONNECTED supplicant state received=");
                updateSavedWifiState.putExtra(EXTRA_SAVED_WIFI_NEW_STATE,
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


    private Intent buildIntentForWifiConnectionState(final Context context) {
        ConnectivityManager connection = (ConnectivityManager) context.getSystemService(Context
                .CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connection.getActiveNetworkInfo();
        Intent updateSavedWifiState = new Intent(context,
                WifiHandlerService.class);
        if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            NetworkInfo.DetailedState state = networkInfo.getDetailedState();

            Log.d(TAG, "state=" + state);
            if (NetworkInfo.DetailedState.CONNECTED == state) {
                String strippedSSID = NetworkUtils.getCurrentSSID(context).replace("\"", "");
                //                String ssid = NetworkUtils.getCurrentSSID(context);
                Log.d(TAG, "Wifi connected=" + strippedSSID);
                updateSavedWifiState.putExtra(EXTRA_CURRENT_SSID, strippedSSID
                );

                updateSavedWifiState.putExtra(WifiConnectionStateReceiver.EXTRA_SAVED_WIFI_NEW_STATE,
                        NetworkUtils.WifiAdapterStatus.CONNECTED);

                updateSavedWifiState.setAction(WifiHandlerService
                        .ACTION_HANDLE_SAVED_WIFI_UPDATE_CONNECT);
                return updateSavedWifiState;
            } else {
                updateSavedWifiState.putExtra(WifiConnectionStateReceiver.EXTRA_SAVED_WIFI_NEW_STATE,
                        NetworkUtils.WifiAdapterStatus.DISCONNECTED);

                updateSavedWifiState.setAction(WifiHandlerService
                        .ACTION_HANDLE_SAVED_WIFI_UPDATE_DISCONNECT);
                Log.d(TAG, "not connected");
                return updateSavedWifiState;
            }
        }
        return null;
    }
}

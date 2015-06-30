package net.opencurlybraces.android.projects.wifihandler.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import net.opencurlybraces.android.projects.wifihandler.service.ContentIntentService;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by chris on 30/06/15.
 */
public class WifiSupplicantStateReceiver extends BroadcastReceiver {
    private static final String TAG = "WifiSupplicantReceiver";

    public static final String EXTRA_CURRENT_SSID = "net.opencurlybraces.android" +
            ".projects.wifihandler.receiver.current_ssid";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "WIFI_SUPPLICANT_CHANGED_ACTION Received");
        SupplicantState state = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
        Intent updateSavedWifiState = buildIntentForState(context, state).get();
        if (updateSavedWifiState == null || updateSavedWifiState.getAction() == null)
            return;

        context.startService(updateSavedWifiState);

    }

    private AtomicReference<Intent> buildIntentForState(final Context context, SupplicantState
            state) {
        AtomicReference<Intent> updateSavedWifiState = new AtomicReference<>(new Intent(context,
                ContentIntentService.class));

        switch (state) {
            case COMPLETED:
                Log.d(TAG, "COMPLETED supplicant state received=");
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context
                        .WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();

                String strippedSSID = wifiInfo.getSSID().replace("\"", "");
                updateSavedWifiState.get().putExtra(EXTRA_CURRENT_SSID,
                        strippedSSID);

                updateSavedWifiState.get().putExtra(WifiStateReceiver.EXTRA_SAVED_WIFI_NEW_STATE,
                        WifiConfiguration.Status.CURRENT);

                updateSavedWifiState.get().setAction(ContentIntentService
                        .ACTION_HANDLE_SAVED_WIFI_UPDATE_CONNECT);
                break;
            case DISCONNECTED:
                Log.d(TAG, "DISCONNECTED supplicant state received=");
                updateSavedWifiState.get().putExtra(WifiStateReceiver.EXTRA_SAVED_WIFI_NEW_STATE,
                        WifiConfiguration
                                .Status.DISABLED);
                updateSavedWifiState.get().setAction(ContentIntentService
                        .ACTION_HANDLE_SAVED_WIFI_UPDATE_DISCONNECT);

                break;
            default:
                Log.d(TAG, "Supplicant state received and ignored=" + state);
                break;
        }
        return updateSavedWifiState;
    }
}

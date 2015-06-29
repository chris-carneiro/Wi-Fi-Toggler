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

/**
 * Created by chris on 15/06/15.
 */
public class WifiStateReceiver extends BroadcastReceiver {

    private static final String TAG = "WifiStateReceiver";
    WifiManager mWifiManager = null;

    public static final String EXTRA_CURRENT_SSID = "net.opencurlybraces.android" +
            ".projects.wifihandler.receiver.current_ssid";

    public static final String EXTRA_SAVED_WIFI_NEW_STATE = "net.opencurlybraces.android" +
            ".projects.wifihandler.receiver.saved_wifi_new_state";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "wifi state changed action=" + intent.getAction());
        if (mWifiManager == null) {
            mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        }


        //        Intent registerScanResultReceiver = new Intent(context, WifiHandlerService
        //                .class);
        //        registerScanResultReceiver.setAction(WifiHandlerService
        //                .ACTION_REGISTER_SCAN_RESULT_RECEIVER);
        //
        //        Intent unregisterScanResultReceiver = new Intent(context, WifiHandlerService
        //                .class);
        //        unregisterScanResultReceiver.setAction(WifiHandlerService
        //                .ACTION_UNREGISTER_SCAN_RESULT_RECEIVER);

        Intent updateSavedWifi = new Intent(context, ContentIntentService
                .class);
        updateSavedWifi.setAction(ContentIntentService
                .ACTION_HANDLE_SAVED_WIFI_UPDATE);
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        //TODO figure out how to get an actual ssid before adapter is disabled or another solution..
        String strippedSSID = wifiInfo.getSSID().replace("\"", "");
        updateSavedWifi.putExtra(EXTRA_CURRENT_SSID, strippedSSID);

        switch (intent.getAction()) {
            case WifiManager.WIFI_STATE_CHANGED_ACTION:
                int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
                switch (wifiState) {
                    case WifiManager.WIFI_STATE_DISABLED:
                        //                        context.startService(registerScanResultReceiver);
                        updateSavedWifi.putExtra(EXTRA_SAVED_WIFI_NEW_STATE, WifiConfiguration
                                .Status.DISABLED);
                        context.startService(updateSavedWifi);
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
                        updateSavedWifi.putExtra(EXTRA_SAVED_WIFI_NEW_STATE, WifiConfiguration
                                .Status.CURRENT);
                        context.startService(updateSavedWifi);
                        break;
                    case DISCONNECTED:
                        Log.d(TAG, "Wifi Disconnected");
                        updateSavedWifi.putExtra(EXTRA_SAVED_WIFI_NEW_STATE, WifiConfiguration
                                .Status.DISABLED);
                        context.startService(updateSavedWifi);
                        break;
                }

                break;
        }


    }
}

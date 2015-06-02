package net.opencurlybraces.android.projects.wifihandler.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Parcelable;
import android.util.Log;

import net.opencurlybraces.android.projects.wifihandler.service.WifiEventService;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link BroadcastReceiver} filtered on {@link android.net.wifi
 * .WifiManager#SCAN_RESULTS_AVAILABLE_ACTION}
 * system action.
 *
 * @author Chris Carneiro
 */
public class WifiScanResultsReceiver extends BroadcastReceiver {
    private static final String TAG = WifiScanResultsReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "SCAN_RESULTS received");

        Intent handleScanResults = new Intent(context, WifiEventService.class);
        handleScanResults.setAction(WifiEventService.ACTION_HANDLE_WIFI_SCAN_RESULTS);
        context.startService(handleScanResults);
    }
}

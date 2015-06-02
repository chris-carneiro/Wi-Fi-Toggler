package net.opencurlybraces.android.projects.wifihandler.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.List;

/**
 * Service that handles wifi events (enabling/disabling ...) and then sends data results when needed
 * to a view controller. <BR/> Created by chris on 01/06/15.
 */
public class WifiEventService extends IntentService {

    private static final String TAG = WifiEventService.class.getSimpleName();
    public static final String ACTION_HANDLE_WIFI_SCAN_RESULTS = "net.opencurlybraces.android" +
            ".projects.wifihandler.service.ACTION_HANDLE_WIFI_SCAN_RESULTS";
    private WifiManager mWifiManager;

    //default constructor
    public WifiEventService() {
        super("WifiEventService");
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public WifiEventService(String name) {
        super(name);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        if (mWifiManager == null) {
            mWifiManager = (WifiManager) getSystemService(Context
                    .WIFI_SERVICE);
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "intent action " + intent.getAction());
        switch (intent.getAction()) {
            case ACTION_HANDLE_WIFI_SCAN_RESULTS:
                handleScanResults();
                break;
        }
    }

    private void handleScanResults() {
        boolean activateWifiAdapter = matchConfiguredWifiWithScanResults();
    }

    /**
     * Method stub will be used to compare configured networks (from DB) with the scan result
     *
     * @return boolean the result should be used to decide whether to activate the wifi. //TODO
     * implement in a util class ?
     */
    private boolean matchConfiguredWifiWithScanResults() {
        Log.d(TAG, "ScanResults=" + getWifiScanResults());
        return false;
    }


    private List<android.net.wifi.ScanResult> getWifiScanResults() {
        return mWifiManager.getScanResults();
    }
}

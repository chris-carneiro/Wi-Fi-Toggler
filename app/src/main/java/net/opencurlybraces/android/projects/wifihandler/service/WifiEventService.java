package net.opencurlybraces.android.projects.wifihandler.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import net.opencurlybraces.android.projects.wifihandler.data.provider.WifiHandlerContract;
import net.opencurlybraces.android.projects.wifihandler.data.table.ConfiguredWifi;
import net.opencurlybraces.android.projects.wifihandler.util.WifiUtils;

import java.util.List;

/**
 * Service that handles wifi events (enabling/disabling ...) and then sends data results when needed
 * to a view controller. <BR/> Created by chris on 01/06/15.
 */
public class WifiEventService extends IntentService {

    private static final String TAG = WifiEventService.class.getSimpleName();
    private static final String SERVICE_ACTION_PREFIX = "net.opencurlybraces.android" +
            ".projects.wifihandler.service.";
    public static final String ACTION_HANDLE_WIFI_SCAN_RESULTS = SERVICE_ACTION_PREFIX +
            "ACTION_HANDLE_WIFI_SCAN_RESULTS";
    public static final String ACTION_HANDLE_USER_WIFI_INSERT = SERVICE_ACTION_PREFIX +
            "ACTION_HANDLE_USER_WIFI_INSERT";

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
            case ACTION_HANDLE_USER_WIFI_INSERT:
                handleUserWifiInsert();
                break;

        }
    }

    private void handleScanResults() {
        boolean activateWifiAdapter = matchConfiguredWifiWithScanResults();
    }

    private void handleUserWifiInsert() {
        Log.d(TAG, "handleUserWifiInsert");
        List<WifiConfiguration> configuredWifis = WifiUtils.getConfiguredWifis(mWifiManager);
        List<ContentValues> values = ConfiguredWifi.buildValuesUsingConfigurations(configuredWifis);
        insertValues(values);
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

    private void insertValues(List<ContentValues> contents) {
        Log.d(TAG, "Content URI=" + ConfiguredWifi.CONTENT_URI);
        for (ContentValues values : contents) {
            getContentResolver().insert(ConfiguredWifi.CONTENT_URI, values);
        }
    }

}

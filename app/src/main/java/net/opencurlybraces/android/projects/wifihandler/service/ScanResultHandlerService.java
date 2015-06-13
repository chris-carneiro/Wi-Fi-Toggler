package net.opencurlybraces.android.projects.wifihandler.service;

import android.app.IntentService;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import net.opencurlybraces.android.projects.wifihandler.util.WifiUtils;

import java.util.List;

/**
 * Created by chris on 13/06/15.
 */
public class ScanResultHandlerService extends IntentService {

    private static final String TAG = "ScanResultHandler";

    public static final String ACTION_HANDLE_WIFI_SCAN_RESULTS = "net.opencurlybraces.android" +
            ".projects.wifihandler.service.action.ACTION_HANDLE_PAUSE_WIFI_HANDLER";


    private static final int SIGNAL_STRENGH_THRESHOLD = -80;
    private WifiManager mWifiManager = null;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public ScanResultHandlerService(String name) {
        super(name);
    }

    public ScanResultHandlerService() {
        super("ScanResultHandlerService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mWifiManager = (WifiManager) getSystemService(WIFI_SERVICE);

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        switch (intent.getAction()) {
            case ACTION_HANDLE_WIFI_SCAN_RESULTS:
                handleScanResults();
                break;
        }
    }


    private void handleScanResults() {

        try {
            List<ScanResult> availableWifiNetworks = mWifiManager
                    .getScanResults();
            List<WifiConfiguration> configuredWifiNetworks = WifiUtils.getConfiguredWifis
                    (mWifiManager);

            if (configuredWifiNetworks == null || availableWifiNetworks == null) {
                return;
            }
            for (ScanResult wifiNetwork : availableWifiNetworks) {
                for (WifiConfiguration wifiConfig : configuredWifiNetworks) {
                    String quoteStripped = wifiConfig.SSID.replace("\"", "");
                    if (quoteStripped.equals(wifiNetwork.SSID)) {
                        Log.d(TAG, "Signal Strength=" + wifiNetwork.level + " SSID=" + wifiNetwork
                                .SSID);
                        if (wifiNetwork.level > SIGNAL_STRENGH_THRESHOLD) {
                            mWifiManager.setWifiEnabled(true);
                        } else {
                            mWifiManager.setWifiEnabled(false);
                        }
                    }
                }
            }
        } catch (InterruptedException e) {
            Log.d(TAG, "Could not retrieve user's configured networks");
            return;
        }
    }
}

package net.opencurlybraces.android.projects.wifihandler.util;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.List;

/**
 * Wifi related action helper class Created by chris on 08/06/15.
 */
public class WifiUtils {

    private WifiUtils() {
    }

    public static boolean isWiFiEnabled(final Context context) {
        final WifiManager wifiManager =
                (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return wifiManager.isWifiEnabled();
    }

    /**
     * Helper method to request from the system, user's configured Wifis. Should be executed on a
     * worker thread. <BR/> As wifi must be enabled to get the configured networks, the wifi adapter
     * is enabled (Uses {@link android.net.wifi.WifiManager.WifiLock}) and disabled right away in
     * order to always get user's configured Wifis (if any)
     *
     * @param wifiManager
     * @return a List of {@link WifiConfiguration} or null if the user doesn't have any pre
     * configured networks
     */
    @Nullable
    public static List<WifiConfiguration> getConfiguredWifis(final WifiManager
                                                                     wifiManager) {
        WifiManager.WifiLock wifiLock = wifiManager.createWifiLock(WifiManager
                .WIFI_MODE_SCAN_ONLY, null);
        List<WifiConfiguration> configuredWifis = null;

        if (wifiManager.setWifiEnabled(true)) {
            wifiLock.acquire();
            do {
                configuredWifis = wifiManager.getConfiguredNetworks();
            } while (configuredWifis == null);

            wifiManager.setWifiEnabled(false);
            wifiLock.release();
        } else {
            Log.d("WifiConfigurationLoader", "could not enable wifi");
        }
        return configuredWifis;
    }

    public interface UserWifiConfigurationLoadedListener {

        void onUserWifiConfigurationLoaded (List<WifiConfiguration> userWifiConfigurations);
    }


}



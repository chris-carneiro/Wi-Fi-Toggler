package net.opencurlybraces.android.projects.wifihandler.util;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.support.annotation.Nullable;

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
     * is enabled and disabled right away in order to always get user's configured Wifis (if any)
     *
     * @param wifiManager
     * @return a List of {@link WifiConfiguration} or null if the user doesn't have any pre
     * configured networks
     */
    @Nullable
    public static List<WifiConfiguration> getConfiguredWifis(final WifiManager
                                                                     wifiManager) {
        List<WifiConfiguration> configuredWifis = null;

        if (wifiManager.setWifiEnabled(true)) {
            configuredWifis = wifiManager.getConfiguredNetworks();
            wifiManager.setWifiEnabled(false);
        }
        return configuredWifis;
    }

}


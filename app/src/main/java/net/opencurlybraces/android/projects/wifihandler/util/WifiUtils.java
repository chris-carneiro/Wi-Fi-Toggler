package net.opencurlybraces.android.projects.wifihandler.util;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.support.annotation.Nullable;
import android.util.Log;

import net.opencurlybraces.android.projects.wifihandler.data.model.UserWifi;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Wifi related action helper class Created by chris on 08/06/15.
 */
public class WifiUtils {

    private WifiUtils() {
    }

    private static final String TAG = "WifiUtils";

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
                                                                     wifiManager) throws
            InterruptedException {

        Log.d(TAG, "getConfiguredWifis");
        List<WifiConfiguration> configuredWifis = null;

        if (wifiManager.isWifiEnabled()) {
            configuredWifis = wifiManager.getConfiguredNetworks();
            return configuredWifis;
        } else {
            if (isHotspotOn(wifiManager)) {
                disableHotspot(wifiManager);
            }
            if (wifiManager.setWifiEnabled(true)) {
                WifiManager.WifiLock wifiLock = wifiManager.createWifiLock(WifiManager
                        .WIFI_MODE_SCAN_ONLY, null);
                wifiLock.acquire();
                int attempts = 0;

                do {
                    Log.d("WifiUtils", "getNetworks");
                    configuredWifis = wifiManager.getConfiguredNetworks();
                    attempts++;
                } while (configuredWifis == null && attempts < 100);

                wifiManager.setWifiEnabled(false);
                wifiLock.release();
            }

        }
        return configuredWifis;
    }


    /**
     * Check whether wifi hotspot on or off This is a workaround and should be used with caution as
     * it uses reflection to access private methods. There's no guarantee this will work.
     *
     * @param wifiManager
     * @return true hotspot active, false otherwise
     */
    private static boolean isHotspotOn(final WifiManager
                                               wifiManager) {
        try {
            Method method = wifiManager.getClass().getDeclaredMethod("isWifiApEnabled");
            method.setAccessible(true);
            return (Boolean) method.invoke(wifiManager);
        } catch (Throwable ignored) {}
        return false;
    }

    /**
     * Disable portable Wifi Hotspot This is a workaround and should be used with caution as it uses
     * reflection to access private methods. There's no guarantee this will work.
     *
     * @param wifiManager
     * @return
     */
    private static boolean disableHotspot(final WifiManager
                                                  wifiManager) {
        try {
            // if WiFi is on, turn it off
            if (isHotspotOn(wifiManager)) {
                wifiManager.setWifiEnabled(false);
            }
            Method method = wifiManager.getClass().getMethod("setWifiApEnabled",
                    WifiConfiguration.class, boolean.class);
            method.invoke(wifiManager, null, false);
            Thread.sleep(200);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public interface UserWifiConfigurationLoadedListener {

        void onUserWifiConfigurationLoaded(List<UserWifi> userWifiConfigurations);
    }


}



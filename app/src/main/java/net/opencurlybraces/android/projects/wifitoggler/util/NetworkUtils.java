package net.opencurlybraces.android.projects.wifitoggler.util;

import android.content.Context;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import net.opencurlybraces.android.projects.wifitoggler.Config;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Wifi related action helper class Created by chris on 08/06/15.
 */
public class NetworkUtils {

    private NetworkUtils() {
    }

    /**
     * Gathers the wifi adapter's states to use in receivers' #onReceive() callback.
     */
    public static class WifiAdapterStatus {
        private WifiAdapterStatus() {
        }

        public static final int CONNECTED = 0;
        public static final int DISABLED = 1;
        public static final int DISCONNECTED = 2;
    }

    private static final String TAG = "NetworkUtils";

    /**
     * Check whether wifi hotspot on or off This is a workaround and should be used with caution as
     * it uses reflection to access private methods. There's no guarantee this will work.
     *
     * @param context
     * @return true hotspot active, false otherwise
     */
    public static boolean isHotspotEnabled(final Context
                                                   context) {
        try {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context
                    .WIFI_SERVICE);
            Method method = wifiManager.getClass().getDeclaredMethod("isWifiApEnabled");
            method.setAccessible(true);
            return (Boolean) method.invoke(wifiManager);
        } catch (Throwable ignored) {}
        return false;
    }

    /**
     * <p>Check whether the wifi adapter has an IP address associated and therefore if it is
     * connected to an AP.</p> <b>This method was created because for some reason {@link
     * NetworkInfo#isConnected()} / {@link NetworkInfo#isConnectedOrConnecting()} both return false
     * sometimes even if the wifi adapter is connected.</b> <p>In addition we can't use the {@link
     * android.net.wifi.SupplicantState#COMPLETED} state since it would not work with NONE/WEP wifi
     * security based networks.</p><p> Thus, the idea is to assume that when the wifi adapter has a
     * valid IP address, it is therefore connected and should be more reliable for the
     * aforementioned cases.</p> <b>Beware though, it is NOT reliable to check if internet is
     * available.</b>
     *
     * @param context
     * @return
     */
    public static boolean isWifiConnected(final Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        Log.d(TAG, "isWifiConnected=" + (wifiInfo.getIpAddress() != 0));
        return wifiInfo.getIpAddress() != 0;
    }

    public static boolean isWifiEnabled(final Context context) {
        Log.d(TAG, "isWifiEnabled");
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return wifiManager.isWifiEnabled();
    }

    public static boolean isScanAlwaysAvailable(final Context context) {
        Log.d(TAG, "isScanAlwaysAvailable");
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return wifiManager.isScanAlwaysAvailable();
    }

    public static void disableWifiAdapter(final Context context) {
        Log.d(TAG, "disableWifiAdapter");
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(false);
    }

    public static void enableWifiAdapter(final Context context) {
        Log.d(TAG, "enableWifiAdapter");
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);
    }

    public static List<WifiConfiguration> getSavedWifiSync(final Context context) {
        Log.d(TAG, "getSavedWifiSync");
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return wifiManager.getConfiguredNetworks();
    }

    public static List<ScanResult> getNearbyWifi(final Context context) {
        Log.d(TAG, "getNearbyWifi");
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return wifiManager.getScanResults();
    }


    public static String getCurrentSsid(final Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String currentSsid = wifiInfo.getSSID();
        Log.d(TAG, "currentSsid=" + currentSsid);
        return (!TextUtils.isEmpty(currentSsid) ? currentSsid.replace("\"", "") : null);
    }


    /**
     * Gets the state of Airplane Mode.
     *
     * @param context
     * @return true if enabled.
     */

    public static boolean isAirplaneModeEnabled(Context context) {
        return Settings.Global.getInt(context.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0;

    }


    public static int getSignalStrength(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        int rssi = wifiManager.getConnectionInfo().getRssi();
        int signalStrength = WifiManager.calculateSignalLevel
                (rssi, Config.WIFI_SIGNAL_STRENGTH_LEVELS);
        Log.d(TAG, "wifi Strength=" + signalStrength + " threshold=" + PrefUtils
                .getWifiSignalStrengthThreshold
                        (context) + " rssi=" + rssi);
        return signalStrength;
    }


    public static void scheduleDisableWifi(final Context context, long delay) {
        WifiDeactivationHandler scheduledDisableWifi = new WifiDeactivationHandler(context);
        Log.d(TAG, "scheduleDisableWifi=" + scheduledDisableWifi.obtainMessage(Config
                .WHAT_SCHEDULE_DISABLE_ADAPTER));

        scheduledDisableWifi.sendMessageDelayed(Message.obtain(scheduledDisableWifi,
                        Config.WHAT_SCHEDULE_DISABLE_ADAPTER),
                delay);
        PrefUtils.setDisableWifiScheduled(context, true);
    }
}



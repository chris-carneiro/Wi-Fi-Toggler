package net.opencurlybraces.android.projects.wifihandler.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by chris on 12/06/15.
 */
public class PrefUtils {

    private static final String PREF_PREFIX = "net.opencurlybraces.android.projects.wifihandler" +
            ".prefs.";

    private static final String PREF_WIFI_HANDLER_ACTIVE = PREF_PREFIX + "wifi_handler_active";

    private static final String PREF_WIFI_CONNECTED = PREF_PREFIX + "wifi_connected";

    private static final String PREF_AIRPLANE_MODE_ON = PREF_PREFIX + "airplane_mode_on";

    private PrefUtils() {
    }

    public static void setWifiHandlerActive(final Context context, boolean active) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_WIFI_HANDLER_ACTIVE, active).apply();
    }

    public static boolean isWifiHandlerActive(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_WIFI_HANDLER_ACTIVE, false);
    }

    public static void setWifiConnected(final Context context, boolean active) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_WIFI_CONNECTED, active).apply();
    }

    /**
     * For some reason NetworkInfo.isConnected() returns false even if the wifi ap is actually
     * associated and connected. <p>This method returns the value set by the {@link
     * #setWifiConnected} </p>
     *
     * @param context
     * @return
     */
    public static boolean isWifiConnected(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_WIFI_CONNECTED, false);
    }

    public static void setAirplaneModeOn(final Context context, boolean on) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_AIRPLANE_MODE_ON, on).apply();
    }

    public static boolean isAirplaneModeOn(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_AIRPLANE_MODE_ON, false);
    }

}

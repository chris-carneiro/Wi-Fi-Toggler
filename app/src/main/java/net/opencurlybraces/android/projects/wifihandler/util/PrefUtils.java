package net.opencurlybraces.android.projects.wifihandler.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by chris on 12/06/15.
 */
public class PrefUtils {

    private static final String TAG = "PrefUtils";

    private static final String PREF_PREFIX = "net.opencurlybraces.android.projects.wifihandler" +
            ".prefs.";

    private static final String PREF_WIFI_HANDLER_ACTIVE = PREF_PREFIX + "wifi_handler_active";

    private static final String PREF_WIFI_CONNECTED = PREF_PREFIX + "wifi_connected";

    private static final String PREF_AIRPLANE_MODE_ON = PREF_PREFIX + "airplane_mode_on";

    public static final String PREF_RUN_AT_STARTUP = PREF_PREFIX + "run_at_startup";

    public static final String PREF_WARNING_NOTIFICATIONS = PREF_PREFIX + "warning_notifications";

    public static final String PREF_SIGNAL_STRENGTH_THRESHOLD = PREF_PREFIX +
            "signal_quality_threshold";

    private PrefUtils() {
    }

    public static void setWifiHandlerActive(final Context context, boolean active) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_WIFI_HANDLER_ACTIVE, active).apply();
    }

    public static boolean isWifiHandlerActive(final Context context) {
        Log.d(TAG, "isWifiHandlerActive");
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_WIFI_HANDLER_ACTIVE, false);
    }

    public static void setAirplaneModeOn(final Context context, boolean on) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_AIRPLANE_MODE_ON, on).apply();
    }

    public static boolean isAirplaneModeOn(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_AIRPLANE_MODE_ON, false);
    }

    public static boolean isRunAtStartupEnabled(final Context context) {
        Log.d(TAG, "isRunAtStartupEnabled");
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_RUN_AT_STARTUP, true);
    }

    public static boolean areWarningNotificationsEnabled(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_WARNING_NOTIFICATIONS, true);
    }

    public static int getWifiSignalStrengthThreshold(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return Integer.parseInt(sp.getString(PREF_SIGNAL_STRENGTH_THRESHOLD, "2"));
    }

}

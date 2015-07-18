package net.opencurlybraces.android.projects.wifihandler.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import net.opencurlybraces.android.projects.wifihandler.Config;

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
            "signal_strength_threshold";

    public static final String PREF_SAVED_WIFI_INSERTED = PREF_PREFIX + "saved_wifi_completed";

    public static final String PREF_SCAN_ALWAYS_AVAILABLE_ENABLED = PREF_PREFIX +
            "scan_always_available";

    public static final String PREF_ALL_SETTINGS_CORRECT_AT_FIRST_LAUNCH = PREF_PREFIX +
            "settings_correct_at_first_launch";

    private PrefUtils() {
    }

    public static void setWifiHandlerActive(final Context context, boolean active) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_WIFI_HANDLER_ACTIVE, active).apply();
    }

    public static boolean isWifiHandlerActive(final Context context) {
        Log.d(TAG, "isWifiHandlerActive");
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_WIFI_HANDLER_ACTIVE, true);
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


    public static void markSettingsCorrectAtFirstLaunch(final Context context) {
        Log.d(TAG, "setSettingsCorrectAtFirstLaunch=");
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_ALL_SETTINGS_CORRECT_AT_FIRST_LAUNCH, true).apply();
    }

    public static boolean wereSettingsCorrectAtFirstLaunch(final Context context) {
        Log.d(TAG, "wereSettingsCorrectAtFirstLaunch=");
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_ALL_SETTINGS_CORRECT_AT_FIRST_LAUNCH, false);
    }

    /**
     * Gets wifi signal strength threshold the user defined in app settings ²
     *
     * @param context
     * @return int
     */
    public static int getWifiSignalStrengthThreshold(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return Integer.parseInt(sp.getString(PREF_SIGNAL_STRENGTH_THRESHOLD, Config
                .DEFAULT_SIGNAL_STRENGTH_THRESHOLD));
    }

    public static void setSavedWifiInsertComplete(final Context context, boolean completed) {
        Log.d(TAG, "setSavedWifiInsertComplete=" + completed);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_SAVED_WIFI_INSERTED, completed).apply();
    }

    public static boolean isSavedWifiInsertComplete(final Context context) {
        Log.d(TAG, "isSavedWifiInsertComplete");
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_SAVED_WIFI_INSERTED, false);
    }

    public static boolean hasScanAlwaysAvailableBeenEnabled(final Context context) {
        Log.d(TAG, "hasScanAlwaysAvailableBeenEnableed");
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_SCAN_ALWAYS_AVAILABLE_ENABLED, false);
    }

    public static void setScanAlwaysAvailableBeenEnabled(final Context context, boolean enabled) {
        Log.d(TAG, "setScanAlwaysAvailableBeenEnabled=" + enabled);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_SCAN_ALWAYS_AVAILABLE_ENABLED, enabled).apply();
    }

}

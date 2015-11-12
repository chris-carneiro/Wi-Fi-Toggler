package net.opencurlybraces.android.projects.wifitoggler.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import net.opencurlybraces.android.projects.wifitoggler.Config;
import net.opencurlybraces.android.projects.wifitoggler.ui.PreferencesActivity;

/**
 * Created by chris on 12/06/15.
 */
public class PrefUtils {

    private static final String TAG = "PrefUtils";

    private static final String PREF_PREFIX = "net.opencurlybraces.android.projects.wifitoggler" +
            ".prefs.";
    private static final String PREF_WIFI_HANDLER_ACTIVE = PREF_PREFIX + "wifi_handler_active";
    public static final String PREF_RUN_AT_STARTUP = "run_at_startup";
    public static final String PREF_WARNING_NOTIFICATIONS = "warning_notifications";
    public static final String PREF_AUTO_TOGGLE_DEFAULT_VALUE_FOR_NEW_WIFI =
            "auto_toggle_default_value_for_new_wifi";

    public static final String PREF_SIGNAL_STRENGTH_THRESHOLD = "signal_strength_threshold";
    public static final String PREF_SAVED_WIFI_INSERTED = PREF_PREFIX + "saved_wifi_completed";
    public static final String PREF_ALL_SETTINGS_CORRECT_AT_FIRST_LAUNCH = PREF_PREFIX +
            "settings_correct_at_first_launch";
    public static final String PREF_DISABLE_WIFI_SCHEDULED = "disable_wifi_scheduled";

    private PrefUtils() {
    }

    public static void setWifiTogglerActive(final Context context, boolean active) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_WIFI_HANDLER_ACTIVE, active).apply();
    }

    public static boolean isWifiTogglerActive(final Context context) {
        Log.d(TAG, "isWifiTogglerActive");
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_WIFI_HANDLER_ACTIVE, true);
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

    public static boolean isAutoToggleModeAlwaysAskOnNewWifi(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return (sp.getString(PREF_AUTO_TOGGLE_DEFAULT_VALUE_FOR_NEW_WIFI, Config
                .DEFAULT_AUTO_TOGGLE_VALUE).equals(PreferencesActivity.AUTO_TOGGLE_ALWAYS_ASK));
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

    /**
     * Gets wifi auto toggle value for wifis the user connects to for the first time.
     *
     * @param context
     * @return String
     */
    public static String getAutoToggleModeOnNewWifi(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        return sp.getString(PREF_AUTO_TOGGLE_DEFAULT_VALUE_FOR_NEW_WIFI, Config
                .DEFAULT_AUTO_TOGGLE_VALUE);
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


    public static void setDisableWifiScheduled(final Context context, boolean isScheduled) {
        Log.d(TAG, "setDisableWifiScheduled isScheduled=" + isScheduled);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_DISABLE_WIFI_SCHEDULED, isScheduled).apply();
    }

    public static boolean isWifiDisableWifiScheduled(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        Log.d(TAG, "isWifiDisableWifiScheduled=" + sp.getBoolean(PREF_DISABLE_WIFI_SCHEDULED,
                false));
        return sp.getBoolean(PREF_DISABLE_WIFI_SCHEDULED, false);
    }

}

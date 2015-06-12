package net.opencurlybraces.android.projects.wifihandler.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by chris on 12/06/15.
 */
public class PrefUtils {


    private static final String PREF_WIFI_HANDLER_ACTIVE = "wifi_handler_active";

    private PrefUtils() {
    }

    public static void setWifiHandlerActive(final Context context, boolean active) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_WIFI_HANDLER_ACTIVE, active).commit();
    }

    public static boolean isWifiHandlerActive(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_WIFI_HANDLER_ACTIVE, false);
    }

}

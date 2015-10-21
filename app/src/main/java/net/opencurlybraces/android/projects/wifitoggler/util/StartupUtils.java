package net.opencurlybraces.android.projects.wifitoggler.util;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import net.opencurlybraces.android.projects.wifitoggler.Config;
import net.opencurlybraces.android.projects.wifitoggler.WifiToggler;

/**
 * Created by chris on 09/06/15.
 */
public class StartupUtils {

    private static final String TAG = "StartupUtils";

    private StartupUtils() {
    }

    /**
     * Distinguishes different kinds of app starts:
     * Based on @schnatterer work @see SO: http://stackoverflow.com/a/17786560/2445061
     */

    /**
     * First start ever
     */
    public static final int FIRST_TIME = 0;
    /**
     * First start in this version
     */
    public static final int FIRST_TIME_FOR_VERSION = 1;
    /**
     * Normal app start
     */
    public static final int NORMAL = 2;


    /**
     * The app version code (not the version name!) that was used on the last start of the app.
     */
    private static final String LAST_APP_VERSION = "last_app_version";

    /**
     * Finds out started for the first time (ever or in the current version).<br/> <br/> Note: This
     * method is <b>not idempotent</b> only the first call will determine the proper result. Any
     * subsequent calls will only return {@link #NORMAL} until the app is started again. So you
     * might want to consider caching the result!
     *
     * @return the type of app start
     */
    public static int appStartMode(final Context context) {
        PackageInfo pInfo;
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        int appStart = NORMAL;
        try {
            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            int lastVersionCode = sharedPreferences
                    .getInt(LAST_APP_VERSION, -1);
            int currentVersionCode = pInfo.versionCode;
            appStart = checkAppStart(currentVersionCode, lastVersionCode);
            // Update version in preferences
            sharedPreferences.edit()
                    .putInt(LAST_APP_VERSION, currentVersionCode).apply();
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG,
                    "Unable to determine current app version from package manager. Defensively " +
                            "assuming normal app start.");
        }
        return appStart;
    }

    public static int checkAppStart(int currentVersionCode, int lastVersionCode) {
        if (lastVersionCode == -1) {
            return FIRST_TIME;
        } else if (lastVersionCode < currentVersionCode) {
            return FIRST_TIME_FOR_VERSION;
        } else if (lastVersionCode > currentVersionCode) {
            Log.w(TAG, "Current version code (" + currentVersionCode
                    + ") is less then the one recognized on last startup ("
                    + lastVersionCode
                    + "). Defensively assuming normal app start.");
            return NORMAL;
        } else {
            return NORMAL;
        }
    }

    public static void startStrictMode() {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectAll()   // or .detectAll() for all detectable problems
                .penaltyLog()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .penaltyDeath()
                .build());
    }


    public static void doSystemSettingsPreCheck(Context context) {
        Log.d(TAG, " doSystemSettingsPreCheck");
        WifiToggler.setSetting(Config.SCAN_ALWAYS_AVAILABLE_SETTINGS, NetworkUtils
                .isScanAlwaysAvailable(context));

        WifiToggler.setSetting(Config.AIRPLANE_SETTINGS, !NetworkUtils
                .isAirplaneModeEnabled(context));

        WifiToggler.setSetting(Config.HOTSPOT_SETTINGS, !NetworkUtils
                .isHotspotEnabled(context));

        WifiToggler.setSetting(Config.STARTUP_CHECK_WIFI_SETTINGS, NetworkUtils
                .isWifiEnabled(context));

        if (Config.RUNNING_MARSHMALLOW) {
            WifiToggler.setSetting(Config.CHECK_LOCATION_PERMISSION_SETTINGS, ContextCompat
                    .checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED);
        }
    }

}

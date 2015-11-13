package net.opencurlybraces.android.projects.wifitoggler;

import android.app.AlarmManager;
import android.os.Build;

import net.opencurlybraces.android.projects.wifitoggler.ui.PreferencesActivity;

/**
 * Created by chris on 03/07/15.
 */
public class Config {

    private Config() {
    }

    public static final boolean DEBUG_MODE = false;
    public static final boolean RUNNING_MARSHMALLOW = Build.VERSION.SDK_INT >= Build
            .VERSION_CODES.M;

    /**
     * Default Preferences
     */
    public static final int WIFI_SIGNAL_STRENGTH_LEVELS = 11;
    public static final String DEFAULT_SIGNAL_STRENGTH_THRESHOLD = "4"; // fair
    public static final String DEFAULT_DEACTIVATION_DELAY = "180000"; // 3 Minutes
    public static final String DEFAULT_AUTO_TOGGLE_VALUE = PreferencesActivity
            .AUTO_TOGGLE_ACTIVE_BY_DEFAULT;
    public static final int SYSTEM_SETTINGS_CHECKS = (RUNNING_MARSHMALLOW ? 5 : 4);

    /**
     * Settings cache keys
     */
    public static final String SCAN_ALWAYS_AVAILABLE_SETTINGS = "SCAN_ALWAYS_AVAILABLE_SETTINGS";
    public static final String STARTUP_CHECK_WIFI_SETTINGS = "STARTUP_CHECK_WIFI_SETTINGS";
    public static final String AIRPLANE_SETTINGS = "AIRPLANE_SETTINGS";
    public static final String HOTSPOT_SETTINGS = "HOTSPOT_SETTINGS";
    public static final String CHECK_LOCATION_PERMISSION_SETTINGS =
            "CHECK_LOCATION_PERMISSION_SETTINGS";

    public static final String UNKNOWN_SSID = "<unknown ssid>";

    /**
     * Schedule delays
     */
    public static final long CHECK_SCAN_ALWAYS_AVAILABLE_REQUEST_INTERVAL = (DEBUG_MODE ? 1000 *
            60 :
            AlarmManager
                    .INTERVAL_HALF_DAY);
    public static final long DELAY_CHECK_HALF_SECOND = 500;
    public static final long DELAY_FIVE_SECONDS = 1000 * 5;
    public static final long DELAY_TEN_SECONDS = 1000 * 10;

    /**
     * Async queries tokens
     */
    public static final int TOKEN_QUERY = 1;
    public static final int TOKEN_INSERT = 2;
    public static final int TOKEN_UPDATE = 3;
    public static final int TOKEN_INSERT_BATCH = 5;


    /**
     * handler scheduled actions ids
     **/
    public static final int WHAT_REPEAT_CHECK_SCAN_ALWAYS = 2;
    public static final int WHAT_CHECK_SCAN_ALWAYS_AVAILABLE = 3;
    public static final int WHAT_SCHEDULE_DISABLE_ADAPTER = 4;
    public static final int WHAT_AUTO_HIDE = 5;

    /**
     * Android M Permission Requests codes
     */
    public static final int M_LOCATION_REQUEST_CODE = 101;
}

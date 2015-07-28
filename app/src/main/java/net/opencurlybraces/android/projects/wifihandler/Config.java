package net.opencurlybraces.android.projects.wifihandler;

import android.app.AlarmManager;

/**
 * Created by chris on 03/07/15.
 */
public class Config {

    private Config() {
    }

    public static final boolean DEBUG_MODE = false;

    public static final int NOTIFICATION_ID_WARNING = 101;
    public static final int NOTIFICATION_ID_WIFI_HANDLER_STATE = 100;

    public static final int WIFI_SIGNAL_STRENGTH_LEVELS = 11;

    public static final String DEFAULT_SIGNAL_STRENGTH_THRESHOLD = "4"; // fair

    public static final int SYSTEM_SETTINGS_CHECKS = 4;

    public static final String SCAN_ALWAYS_AVAILABLE_SETTINGS =
            "SCAN_ALWAYS_AVAILABLE_SETTINGS";
    public static final String STARTUP_CHECK_WIFI_SETTINGS = "STARTUP_CHECK_WIFI_SETTINGS";
    public static final String AIRPLANE_SETTINGS =
            "AIRPLANE_SETTINGS";
    public static final String HOTSPOT_SETTINGS =
            "HOTSPOT_SETTINGS";


    /**
     * Every hour
     */
    public static final long CHECK_SCAN_ALWAYS_AVAILABLE_REQUEST_INTERVAL = (DEBUG_MODE ? 1000 *
            60 :
            AlarmManager
                    .INTERVAL_HOUR);

    public static final long INTERVAL_CHECK_HALF_SECOND = 500;
}

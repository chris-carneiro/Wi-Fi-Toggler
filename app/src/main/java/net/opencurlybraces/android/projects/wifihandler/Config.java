package net.opencurlybraces.android.projects.wifihandler;

import android.app.AlarmManager;

/**
 * Created by chris on 03/07/15.
 */
public class Config {

    private Config() {
    }

    public static final boolean DEBUG_MODE = true;

    public static final int NOTIFICATION_ID_AIRPLANE_MODE = 101;
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
     * Every half day
     */
//    public static final long CHECK_SCAN_ALWAYS_AVAILABLE_REQUEST_INTERVAL = AlarmManager
//            .INTERVAL_HALF_DAY;

        public static final long CHECK_SCAN_ALWAYS_AVAILABLE_REQUEST_INTERVAL = 1000 * 60;

    //    public static final long BLITZ_CHECK_SCAN_ALWAYS_AVAILABLE_INTERVAL = 1000;
}

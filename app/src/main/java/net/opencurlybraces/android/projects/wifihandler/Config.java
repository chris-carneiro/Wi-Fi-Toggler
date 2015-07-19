package net.opencurlybraces.android.projects.wifihandler;

/**
 * Created by chris on 03/07/15.
 */
public class Config {

    private Config() {
    }

    public static final boolean DEBUG_MODE = true;

    public static final int NOTIFICATION_ID_AIRPLANE_MODE = 101;
    public static final int NOTIFICATION_ID_WIFI_HANDLER_STATE = 100;

    public static final int WIFI_SIGNAL_STRENGTHLEVELS = 11;

    public static final String DEFAULT_SIGNAL_STRENGTH_THRESHOLD = "4"; // fair

    public static final int STARTUP_SETTINGS_CHECKS = 4;

    public static final String STARTUP_CHECK_SCAN_ALWAYS_AVAILABLE_SETTINGS =
            "STARTUP_CHECK_SCAN_ALWAYS_AVAILABLE_SETTINGS";

    public static final String STARTUP_CHECK_WIFI_SETTINGS = "STARTUP_CHECK_WIFI_SETTINGS";
    public static final String STARTUP_CHECK_AIRPLANE_SETTINGS =
            "STARTUP_CHECK_AIRPLANE_SETTINGS";
    public static final String STARTUP_CHECK_HOTSTOP_SETTINGS =
            "STARTUP_CHECK_HOTSTOP_SETTINGS";

}

package net.opencurlybraces.android.projects.wifihandler.io;

/**
 * Created by chris on 03/07/15.
 */
public class WifiLockedOffException extends Exception {

    public static final int AIRPLANE_MODE_ON = 100;
    public static final int HOTSPOT_AP_ON = 101;


    private final int mReason;

    public WifiLockedOffException(int reason) {
        mReason = reason;
    }

    public int getReason() {
        return mReason;
    }

}

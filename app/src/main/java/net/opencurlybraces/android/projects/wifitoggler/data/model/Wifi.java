package net.opencurlybraces.android.projects.wifitoggler.data.model;

import android.database.Cursor;

import net.opencurlybraces.android.projects.wifitoggler.data.table.SavedWifi;

/**
 * {@link net.opencurlybraces.android.projects.wifitoggler.data.table.SavedWifi} Java Model Object
 * Created by chris on 01/09/15.
 */
public class Wifi {
    public String ssid;
    public int status;
    public boolean isAutoToggle;

    public static Wifi buildForCursor(final Cursor cursor) {
        int indexSsid = cursor.getColumnIndexOrThrow(SavedWifi.SSID);
        int indexIsAutoToggle = cursor.getColumnIndexOrThrow(SavedWifi.AUTO_TOGGLE);

        Wifi wifi = new Wifi();
        wifi.ssid = cursor.getString(indexSsid);
        wifi.isAutoToggle = cursor.getInt(indexIsAutoToggle) > 0;
        return wifi;
    }
}

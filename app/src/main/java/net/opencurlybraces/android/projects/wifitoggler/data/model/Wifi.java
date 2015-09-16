package net.opencurlybraces.android.projects.wifitoggler.data.model;

import android.database.Cursor;

import net.opencurlybraces.android.projects.wifitoggler.data.table.SavedWifi;

/**
 * {@link net.opencurlybraces.android.projects.wifitoggler.data.table.SavedWifi} Java Model Object
 * Created by chris on 01/09/15.
 */
public class Wifi {
    public int _id;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Wifi wifi = (Wifi) o;
        return _id == wifi._id;
    }

    @Override
    public int hashCode() {
        return _id;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Wifi{");
        sb.append("_id=").append(_id);
        sb.append(", ssid='").append(ssid).append('\'');
        sb.append(", status=").append(status);
        sb.append(", isAutoToggle=").append(isAutoToggle);
        sb.append('}');
        return sb.toString();
    }
}

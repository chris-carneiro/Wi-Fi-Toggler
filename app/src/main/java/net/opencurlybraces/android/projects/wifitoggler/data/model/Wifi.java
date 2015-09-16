package net.opencurlybraces.android.projects.wifitoggler.data.model;

import android.database.Cursor;

import net.opencurlybraces.android.projects.wifitoggler.data.table.SavedWifi;

/**
 * {@link net.opencurlybraces.android.projects.wifitoggler.data.table.SavedWifi} Java Model Object
 * Created by chris on 01/09/15.
 */
public class Wifi {
    private long _id;
    private String ssid;
    private int status;
    private boolean isAutoToggle;

    public long get_id() {
        return _id;
    }

    public boolean isAutoToggle() {
        return isAutoToggle;
    }

    public int getStatus() {
        return status;
    }

    public String getSsid() {
        return ssid;
    }

    private Wifi(WifiBuilder builder) {
        this._id = builder._id;
        this.ssid = builder.ssid;
        this.status = builder.status;
        this.isAutoToggle = builder.isAutoToggle;
    }

    public static Wifi buildForCursor(final Cursor cursor) {
        int indexId = cursor.getColumnIndexOrThrow(SavedWifi._ID);
        int indexSsid = cursor.getColumnIndexOrThrow(SavedWifi.SSID);
        int indexIsAutoToggle = cursor.getColumnIndexOrThrow(SavedWifi.AUTO_TOGGLE);

        long id = cursor.getInt(indexId);
        String ssid = cursor.getString(indexSsid);
        boolean isAutoToggle = cursor.getInt(indexIsAutoToggle) > 0;

        Wifi wifi = new Wifi.WifiBuilder().id(id).ssid(ssid).autoToggle(isAutoToggle).build();

        return wifi;
    }

    public static class WifiBuilder {

        private long _id;
        private String ssid;
        private int status;
        private boolean isAutoToggle;

        public WifiBuilder id(long id) {
            this._id = id;
            return this;
        }

        public WifiBuilder ssid(String ssid) {
            this.ssid = ssid;
            return this;
        }

        public WifiBuilder status(int status) {
            this.status = status;
            return this;
        }

        public WifiBuilder autoToggle(boolean isAutoToggle) {
            this.isAutoToggle = isAutoToggle;
            return this;
        }

        public Wifi build() {
            return new Wifi(this);
        }
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
        return (int) _id;
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

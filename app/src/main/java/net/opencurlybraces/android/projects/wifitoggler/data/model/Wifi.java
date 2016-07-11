package net.opencurlybraces.android.projects.wifitoggler.data.model;

import android.content.Context;
import android.database.Cursor;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import net.opencurlybraces.android.projects.wifitoggler.Config;
import net.opencurlybraces.android.projects.wifitoggler.data.table.SavedWifi;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link net.opencurlybraces.android.projects.wifitoggler.data.table.SavedWifi} Java Model Object
 * Created by chris on 01/09/15.
 */
public class Wifi {

    private static final String TAG = "Wifi";
    private static final String ENABLED_WIFI = "1";


    private long _id;
    private String ssid;
    private int status;
    private boolean isAutoToggle;


    /**
     * TODO should be done outside model to remove dependency to android framework (cursor and
     * context), and data should be passed in param
     **/
    private static List<Wifi> getUserWifiFromDB(final Context context, final
    String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        List<Wifi> userWifis = null;
        Cursor userWifiInDB = null;
        try {

            userWifiInDB = context.getContentResolver().query(SavedWifi.CONTENT_URI
                    , projection, selection, selectionArgs
                    , sortOrder);

            if (userWifiInDB != null) {
                userWifis = new ArrayList<>(userWifiInDB.getCount());
                while (userWifiInDB.moveToNext()) {

                    Wifi userWifi = buildWifiFromData(userWifiInDB);
                    userWifis.add(userWifi);
                }
                userWifiInDB.close();
            }
        } catch (IllegalArgumentException e) {
            if (userWifiInDB != null)
                userWifiInDB.close();
            e.printStackTrace();
        } finally {
            if (userWifiInDB != null) userWifiInDB.close();
        }
        return userWifis;
    }

    public static List<Wifi> getUserWifiAutoToggleEnabledFromDB(final Context context, final
    String[] projection) {

        return getUserWifiFromDB(context, projection, SavedWifi.whereAutoToggle, new
                String[]{ENABLED_WIFI},
                null);
    }

    public static List<Wifi> getUserWifiFromDB(final Context context, final
    String[] projection) {
        return getUserWifiFromDB(context, projection, null, null, null);
    }


    public static boolean nearbyNetworkMatchUserWifi(int signalThreshold, List<ScanResult>
            nearbyNetworks, List<Wifi> userWifis) {

        if (userWifis == null || nearbyNetworks == null) {
            return false;
        }
        for (ScanResult nearbyNetwork : nearbyNetworks) {

            for (Wifi userWifi : userWifis) {
                boolean matchFound = userWifi.getSsid().equals(nearbyNetwork.SSID);
                if (matchFound && userWifi.isAutoToggle()) {
                    Log.d(TAG, "nearbyNetworkMatchUserWifi: userWifi.getSsid()=" + userWifi
                            .getSsid());
                    Log.d(TAG, "nearbyNetworkMatchUserWifi: nearbyNetwork.SSID=" + nearbyNetwork
                            .SSID);
                    int signalStrength = WifiManager.calculateSignalLevel(nearbyNetwork.level,
                            Config.WIFI_SIGNAL_STRENGTH_LEVELS);
                    boolean nearbySignalIsStrongEnough = signalStrength >= signalThreshold;

                    if (nearbySignalIsStrongEnough) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

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

    public static Wifi buildWifiFromData(final Cursor cursor) {
        int indexId = cursor.getColumnIndexOrThrow(SavedWifi._ID);
        int indexSsid = cursor.getColumnIndexOrThrow(SavedWifi.SSID);
        int indexIsAutoToggle = cursor.getColumnIndexOrThrow(SavedWifi.AUTO_TOGGLE);

        long id = cursor.getInt(indexId);
        String ssid = cursor.getString(indexSsid);
        boolean isAutoToggle = cursor.getInt(indexIsAutoToggle) > 0;

        Wifi wifi = new Wifi.WifiBuilder().id(id).ssid(ssid).autoToggle(isAutoToggle).build();

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
}

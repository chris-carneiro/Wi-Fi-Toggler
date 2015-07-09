package net.opencurlybraces.android.projects.wifihandler.util;

import android.net.wifi.WifiConfiguration;

import java.util.Comparator;

/**
 * Created by chris on 06/07/15.
 */
public class WifiComparator implements Comparator<WifiConfiguration> {
    @Override
    public int compare(WifiConfiguration lhs, WifiConfiguration rhs) {
        return lhs.SSID.compareToIgnoreCase(rhs.SSID);
    }
}

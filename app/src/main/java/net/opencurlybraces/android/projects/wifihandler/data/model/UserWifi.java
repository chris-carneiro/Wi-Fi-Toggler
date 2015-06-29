package net.opencurlybraces.android.projects.wifihandler.data.model;

/**
 * Created by chris on 13/06/15.
 */
public class UserWifi {
    public String mSSID;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(mSSID.replace("\"", ""));
        if (mConnected) {
            sb.append("\n");
            sb.append("Connected");
        }
        return sb.toString();
    }

    public boolean mConnected;
}

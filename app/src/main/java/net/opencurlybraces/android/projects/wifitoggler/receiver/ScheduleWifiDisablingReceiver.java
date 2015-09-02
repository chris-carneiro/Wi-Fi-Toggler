package net.opencurlybraces.android.projects.wifitoggler.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import net.opencurlybraces.android.projects.wifitoggler.util.NetworkUtils;

/**
 * Created by chris on 02/09/15.
 */
public class ScheduleWifiDisablingReceiver extends BroadcastReceiver {

    private static final String TAG = "ScheduleDisableWifi";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Scheduled disabling wifi event received");
        NetworkUtils.disableWifiAdapter(context);
    }
}

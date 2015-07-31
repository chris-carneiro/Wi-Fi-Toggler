package net.opencurlybraces.android.projects.wifitoggler.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import net.opencurlybraces.android.projects.wifitoggler.service.WifiTogglerService;
import net.opencurlybraces.android.projects.wifitoggler.util.PrefUtils;

/**
 * Created by chris on 12/07/15.
 */
public class BootCompletedReceiver extends BroadcastReceiver {
    private static final String TAG = "BootCompletedReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Boot Completed event received");
        if (PrefUtils.isRunAtStartupEnabled(context)) {
            Intent runAtStartup = new Intent(context, WifiTogglerService.class);
            runAtStartup.setAction(WifiTogglerService
                    .ACTION_HANDLE_ACTIVATE_WIFI_HANDLER);
            context.startService(runAtStartup);
        }
    }
}

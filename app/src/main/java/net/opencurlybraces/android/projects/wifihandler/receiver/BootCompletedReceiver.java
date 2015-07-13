package net.opencurlybraces.android.projects.wifihandler.receiver;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import net.opencurlybraces.android.projects.wifihandler.service.WifiHandlerService;
import net.opencurlybraces.android.projects.wifihandler.util.PrefUtils;

/**
 * Created by chris on 12/07/15.
 */
public class BootCompletedReceiver extends BroadcastReceiver {
    private static final String TAG = "BootCompletedReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Boot Completed event received");
        if (PrefUtils.isRunAtStartupEnabled(context)) {
            Intent runWifiHandlerAtStartup = new Intent(context, WifiHandlerService.class);
            runWifiHandlerAtStartup.setAction(WifiHandlerService
                    .ACTION_HANDLE_ACTIVATE_WIFI_HANDLER);
            context.startService(runWifiHandlerAtStartup);
        }
    }
}

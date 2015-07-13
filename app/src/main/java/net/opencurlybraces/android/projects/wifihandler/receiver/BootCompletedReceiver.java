package net.opencurlybraces.android.projects.wifihandler.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import net.opencurlybraces.android.projects.wifihandler.service.WifiHandlerService;

/**
 * Created by chris on 12/07/15.
 */
public class BootCompletedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent runWifiHandlerAtStartup = new Intent(context, WifiHandlerService.class);
        runWifiHandlerAtStartup.setAction(WifiHandlerService
                .ACTION_HANDLE_ACTIVATE_WIFI_HANDLER);
        context.startService(runWifiHandlerAtStartup);
    }
}

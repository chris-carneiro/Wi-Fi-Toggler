package net.opencurlybraces.android.projects.wifitoggler.util;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.lang.ref.WeakReference;

/**
 * Created by chris on 22/10/15.
 */
public class WifiDeactivationHandler extends Handler {
    private static final String TAG = "WifiDeactivationHandler";
    private final WeakReference<Context> mHost;

    public WifiDeactivationHandler(Context host) {
        mHost = new WeakReference<>(host);
    }

    @Override
    public void handleMessage(Message msg) {
        Context host = mHost.get();
        if (host != null) {
            if (PrefUtils.isWifiDisableWifiScheduled(host)) {
                Log.d(TAG, "Wifi scheduled deactivation in progress...");
                NetworkUtils.disableWifiAdapter(host);
            } else {
                Log.d(TAG, "Wifi scheduled Deactivation has likely been aborted");
            }
        }
    }
}


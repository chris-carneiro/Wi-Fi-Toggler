package net.opencurlybraces.android.projects.wifihandler.util;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import net.opencurlybraces.android.projects.wifihandler.receiver.ScanAlwaysAvailableReceiver;

import java.lang.ref.WeakReference;

/**
 * Used to send periodically a {@link
 * ScanAlwaysAvailableReceiver#CHECK_SCAN_ALWAYS_AVAILABLE_REQUEST_ACTION}.
 * The reason I use a {@link Handler} and not a simple {@link AlarmManager} is to prevent {@link
 * android.os.DeadObjectException} from happening if the user kills the process. {@link
 * android.os.DeadObjectException} was happening each time a broadcast was received, when {@link
 * net.opencurlybraces.android.projects.wifihandler.service.WifiHandlerService} was running but not
 * WifiHandler's process Created by chris on 28/07/15.
 */
public final class CheckPassiveScanHandler extends Handler {
    private final WeakReference<Context> mHost;
    private final int mWhat;
    private final long mDelay;

    public CheckPassiveScanHandler(Context host, int what, long delay) {
        mHost = new WeakReference<>(host);
        mWhat = what;
        mDelay = delay;
    }

    @Override
    public void handleMessage(Message msg) {
        Context host = mHost.get();
        if (host != null) {
            host.sendBroadcast(new Intent(ScanAlwaysAvailableReceiver
                    .CHECK_SCAN_ALWAYS_AVAILABLE_REQUEST_ACTION));
            sendMessageDelayed(Message.obtain(this, mWhat), mDelay);
        }
    }
}

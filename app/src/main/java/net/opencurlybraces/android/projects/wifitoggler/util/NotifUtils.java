package net.opencurlybraces.android.projects.wifitoggler.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import net.opencurlybraces.android.projects.wifitoggler.R;
import net.opencurlybraces.android.projects.wifitoggler.receiver.WifiConnectionStateReceiver;
import net.opencurlybraces.android.projects.wifitoggler.service.WifiTogglerService;

/**
 * Created by chris on 28/08/15.
 */
public class NotifUtils {

    private static final String TAG = "NotifUtils";

    public static final int NOTIFICATION_ID_WIFI_HANDLER_STATE = 100;
    public static final int NOTIFICATION_ID_WARNING = 101;
    public static final int NOTIFICATION_ID_SET_AUTO_TOGGLE_STATE = 102;


    private NotifUtils() {
    }


    public static void buildSetAutoToggleChooserNotification(final Context context, String
            insertedWifi) {

        NotificationManager notifManager = (NotificationManager) context.getSystemService(Context
                .NOTIFICATION_SERVICE);

        Resources res = context.getResources();
        String tickerContent = res.getString(R.string
                .activate_auto_toggle_for_new_wifi_notification_content);
        String formattedTicker = String.format(tickerContent, insertedWifi);

        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(context)
                .setContentTitle(res.getString(R.string.app_name))
                .setContentText(formattedTicker)
                .setTicker(formattedTicker)
                .setSmallIcon(R.drawable.ic_autotoggle_state)
                .setColor(res.getColor(R.color.material_orange_400));



        notifBuilder.addAction(0, res.getString(R.string.positive_answer_action)
                , createSetAutoToggleStateIntent(context, insertedWifi, true));

        notifBuilder.addAction(0, res.getString(R.string.negative_answer_action)
                , createSetAutoToggleStateIntent(context,insertedWifi, false));

        Notification notification = notifBuilder.build();
        notifManager.notify(NOTIFICATION_ID_SET_AUTO_TOGGLE_STATE, notification);
    }

    //TODO refactor
    private static PendingIntent createSetAutoToggleStateIntent(Context context, String ssid,
                                                                boolean isAutoToggle) {
        Log.d(TAG, "createSetAutoToggleStateIntent");
        Intent setAutoToggle = new Intent(context, WifiTogglerService.class);

        setAutoToggle.putExtra(WifiConnectionStateReceiver
                .EXTRA_CURRENT_SSID, ssid);
        if (isAutoToggle) {
            setAutoToggle.setAction(WifiTogglerService.ACTION_HANDLE_NOTIFICATION_ACTION_AUTO_TOGGLE_ON);
        } else {
            setAutoToggle.setAction(WifiTogglerService.ACTION_HANDLE_NOTIFICATION_ACTION_AUTO_TOGGLE_OFF);
        }
        return PendingIntent.getService(context, 0, setAutoToggle,
                PendingIntent.FLAG_ONE_SHOT);
    }
    //TODO refactor
//    private static PendingIntent createSetAutoToggleStateOffIntent(Context context, String ssid) {
//        Log.d(TAG, "createSetAutoToggleStateOffIntent");
//        Intent setAutoToggle = new Intent(WifiTogglerService
//                .ACTION_HANDLE_NOTIFICATION_ACTION_SET_AUTO_TOGGLE,
//                null, context, WifiTogglerService.class);
//        setAutoToggle.putExtra(WifiConnectionStateReceiver
//                .EXTRA_CURRENT_SSID, ssid);
//        return PendingIntent.getService(context, 0, setAutoToggle,
//                PendingIntent.FLAG_UPDATE_CURRENT);
//    }
}

package net.opencurlybraces.android.projects.wifitoggler.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v4.app.NotificationCompat;

import net.opencurlybraces.android.projects.wifitoggler.R;
import net.opencurlybraces.android.projects.wifitoggler.service.WifiTogglerService;

/**
 * Created by chris on 28/08/15.
 */
public class NotifUtils {


    public static final String EXTRA_SET_AUTO_TOGGLE_STATE = "net.opencurlybraces.android" +
            ".projects" +
            ".wifitoggler.notifcation.set_auto_toggle_state";

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

        //        Intent notificationIntent = new Intent(this, SavedWifiListActivity.class);
        //        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
        //                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        //        PendingIntent intent = PendingIntent.getActivity(context, 0,
        //                notificationIntent, 0);
        String tickerContent = res.getString(R.string.activate_auto_toggle_for_new_wifi_notification_content);
       String formattedTicker= String.format(tickerContent, insertedWifi);

        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(context)
                .setContentTitle(res.getString(R.string.app_name))
                .setContentText(formattedTicker)
                .setTicker(formattedTicker)
                .setSmallIcon(R.drawable.ic_autotoggle_state)
                .setColor(res.getColor(R.color.material_orange_400))
                .setAutoCancel(true);


        notifBuilder.addAction(0, res.getString(R.string.negative_answer_action)
                , createSetAutoToggleStateOffIntent(context));
        notifBuilder.addAction(0, res.getString(R.string.positive_answer_action)
                , createSetAutoToggleStateOnIntent(context));

        Notification notification = notifBuilder.build();
        notifManager.notify(NOTIFICATION_ID_SET_AUTO_TOGGLE_STATE, notification);
        //        startForeground(Config.NOTIFICATION_ID_WIFI_HANDLER_STATE, notification);

    }

    private static PendingIntent createSetAutoToggleStateOnIntent(Context context) {
        Intent setAutoToggle = new Intent(WifiTogglerService
                .ACTION_HANDLE_NOTIFICATION_ACTION_SET_AUTO_TOGGLE,
                null, context, WifiTogglerService.class);
        setAutoToggle.putExtra(EXTRA_SET_AUTO_TOGGLE_STATE, true);
        return PendingIntent.getService(context, 0, setAutoToggle,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static PendingIntent createSetAutoToggleStateOffIntent(Context context) {
        Intent setAutoToggle = new Intent(WifiTogglerService
                .ACTION_HANDLE_NOTIFICATION_ACTION_SET_AUTO_TOGGLE,
                null, context, WifiTogglerService.class);
        setAutoToggle.putExtra(EXTRA_SET_AUTO_TOGGLE_STATE, false);

        return PendingIntent.getService(context, 0, setAutoToggle,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }
}

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
import net.opencurlybraces.android.projects.wifitoggler.ui.SavedWifiListActivity;
import net.opencurlybraces.android.projects.wifitoggler.ui.SystemSettingsCheckActivity;

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
                , createSetAutoToggleStateIntent(context, insertedWifi, false));

        Notification notification = notifBuilder.build();
        notifManager.notify(NOTIFICATION_ID_SET_AUTO_TOGGLE_STATE, notification);
    }

    private static PendingIntent createSetAutoToggleStateIntent(Context context, String ssid,
                                                                boolean isAutoToggle) {
        Log.d(TAG, "createSetAutoToggleStateIntent");
        Intent setAutoToggle = new Intent(context, WifiTogglerService.class);

        setAutoToggle.putExtra(WifiConnectionStateReceiver
                .EXTRA_CURRENT_SSID, ssid);
        if (isAutoToggle) {
            setAutoToggle.setAction(WifiTogglerService
                    .ACTION_HANDLE_NOTIFICATION_ACTION_AUTO_TOGGLE_ON);
        } else {
            setAutoToggle.setAction(WifiTogglerService
                    .ACTION_HANDLE_NOTIFICATION_ACTION_AUTO_TOGGLE_OFF);
        }
        return PendingIntent.getService(context, 0, setAutoToggle,
                PendingIntent.FLAG_ONE_SHOT);
    }


    public static void dismissNotification(final Context context, int notificationId) {
        Log.d(TAG, "dismissNotification notificationID=" + notificationId);
        NotificationManager notifManager = (NotificationManager) context.getSystemService(Context
                .NOTIFICATION_SERVICE);

        notifManager.cancel(notificationId);
    }

    public static void buildWarningNotification(final Context context) {
        NotificationManager notifManager = (NotificationManager) context.getSystemService(Context
                .NOTIFICATION_SERVICE);
        Resources res = context.getResources();
        Intent notificationIntent = new Intent(context, SystemSettingsCheckActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent intent = PendingIntent.getActivity(context, 0,
                notificationIntent, 0);

        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(context)
                .setContentTitle(res.getString(R.string
                        .system_settings_warning_notification_context_title))
                .setContentText(res.getString(R.string
                        .system_settings_warning_notification_ticker))
                .setTicker(res.getString(R.string.system_settings_warning_notification_ticker))
                .setSmallIcon(R.drawable.notif_icon)
                .setColor(context.getResources().getColor(R.color.material_orange_400))
                .setContentIntent(intent);

        Notification notification = notifBuilder.build();
        notifManager.notify(NotifUtils.NOTIFICATION_ID_WARNING, notification);
    }

    public static void buildWifiTogglerPausedNotification(final Context context) {
        NotificationManager notifManager = (NotificationManager) context.getSystemService(Context
                .NOTIFICATION_SERVICE);
        Resources res = context.getResources();
        Intent notificationIntent = new Intent(context, SavedWifiListActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent intent = PendingIntent.getActivity(context, 0,
                notificationIntent, 0);

        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(context)
                .setContentTitle(res.getString(R.string.app_name))
                .setContentText(res.getString(R.string
                        .paused_wifi_toggler_notification_context_title))
                .setTicker(res.getString(R.string.pause_notification_ticker_content))
                .setSmallIcon(R.drawable.notif_icon)
                .setColor(res.getColor(R.color.material_orange_400))
                .setContentIntent(intent);

        notifBuilder.addAction(0, res.getString(R.string.enable_action_title)
                , createActivateWifiTogglerIntent(context));
        Notification notification = notifBuilder.build();
        notifManager.notify(NotifUtils.NOTIFICATION_ID_WIFI_HANDLER_STATE, notification);

    }

    public static Notification buildWifiTogglerRunningNotification(final Context context) {
        Resources res = context.getResources();

        Intent notificationIntent = new Intent(context, SavedWifiListActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent intent = PendingIntent.getActivity(context, 0,
                notificationIntent, 0);

        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(context)
                .setContentTitle(res.getString(R.string.app_name))
                .setContentText(res.getString(R.string
                        .active_wifi_toggler_notification_context_title))
                .setTicker(res.getString(R.string.enable_notification_ticker_content))
                .setSmallIcon(R.drawable.notif_icon)
                .setColor(res.getColor(R.color.material_teal_400))
                .setContentIntent(intent);


        notifBuilder.addAction(0, res.getString(R.string.pause_action_title)
                , createPauseWifiTogglerIntent(context));

        Notification notification = notifBuilder.build();
        return notification;
    }

    private static PendingIntent createPauseWifiTogglerIntent(final Context context) {
        Intent pauseIntent = new Intent(WifiTogglerService.ACTION_HANDLE_NOTIFICATION_ACTION_PAUSE,
                null, context, WifiTogglerService.class);

        return PendingIntent.getService(context, 0, pauseIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static PendingIntent createActivateWifiTogglerIntent(final Context context) {
        Intent activateIntent = new Intent(WifiTogglerService
                .ACTION_HANDLE_NOTIFICATION_ACTION_ACTIVATE,
                null, context, WifiTogglerService.class);

        return PendingIntent.getService(context, 0, activateIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }
}

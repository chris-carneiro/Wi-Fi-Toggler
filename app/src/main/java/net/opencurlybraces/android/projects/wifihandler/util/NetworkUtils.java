package net.opencurlybraces.android.projects.wifihandler.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import net.opencurlybraces.android.projects.wifihandler.Config;
import net.opencurlybraces.android.projects.wifihandler.R;
import net.opencurlybraces.android.projects.wifihandler.SavedWifiListActivity;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

/**
 * Wifi related action helper class Created by chris on 08/06/15.
 */
public class NetworkUtils {

    private NetworkUtils() {
    }

    private static final String TAG = "NetworkUtils";

    /**
     * Helper method to request from the system, user's configured Wifis. Should be executed on a
     * worker thread. <BR/> As wifi must be enabled to get the configured networks, the wifi adapter
     * is enabled (Uses {@link android.net.wifi.WifiManager.WifiLock}) and disabled right away in
     * order to always get user's configured Wifis (if any).<p>Note that, the list is sorted in
     * alphabetical order according to SSIDs</p>
     *
     * @param wifiManager
     * @param listener
     */
    public static void getConfiguredWifis(@NonNull final WifiManager
                                                  wifiManager,
                                          @NonNull final
                                          SavedWifiConfigurationListener
                                                  listener) {

        new AsyncTask<Void, Void, List<WifiConfiguration>>() {

            @Override
            protected List<WifiConfiguration> doInBackground(Void... params) {
                List<WifiConfiguration> configuredWifis = null;

                if (!wifiManager.isWifiEnabled()) {
                    //            if (isHotspotOn(wifiManager)) {
                    //                disableHotspot(wifiManager);
                    //            }
                    if (wifiManager.setWifiEnabled(true)) {
                        WifiManager.WifiLock wifiLock = wifiManager.createWifiLock(WifiManager
                                .WIFI_MODE_SCAN_ONLY, null);
                        wifiLock.acquire();
                        int attempts = 0;

                        do {
                            Log.d("NetworkUtils", "getNetworks");
                            configuredWifis = wifiManager.getConfiguredNetworks();
                            attempts++;
                        } while (configuredWifis == null && attempts < 100);

                        wifiManager.setWifiEnabled(false);
                        wifiLock.release();
                    }

                } else {
                    configuredWifis = wifiManager.getConfiguredNetworks();
                }
                Collections.sort(configuredWifis, new WifiComparator());

                return configuredWifis;
            }

            @Override
            protected void onPostExecute(List<WifiConfiguration> savedWifis) {
                listener.onSavedWifiLoaded(savedWifis);
            }
        }.execute();
    }


    /**
     * Check whether wifi hotspot on or off This is a workaround and should be used with caution as
     * it uses reflection to access private methods. There's no guarantee this will work.
     *
     * @param context
     * @return true hotspot active, false otherwise
     */
    public static boolean isHotspotOn(final Context
                                              context) {
        try {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context
                    .WIFI_SERVICE);
            Method method = wifiManager.getClass().getDeclaredMethod("isWifiApEnabled");
            method.setAccessible(true);
            return (Boolean) method.invoke(wifiManager);
        } catch (Throwable ignored) {}
        return false;
    }

    /**
     * <p>Check whether the wifi adapter has an IP address associated and therefore if it is
     * connected to an AP.</p> <b>This method was created because for some reason {@link
     * NetworkInfo#isConnected()} / {@link NetworkInfo#isConnectedOrConnecting()} both return false
     * sometimes even if the wifi adapter is connected.</b> <p>In addition we can't use the {@link
     * android.net.wifi.SupplicantState#COMPLETED} state since it would not work with NONE/WEP wifi
     * security based networks.</p><p> Thus, the idea is to assume that when the wifi adapter has a
     * valid IP address, it is therefore connected and should be more reliable for the
     * aforementioned cases.</p> <b>Beware though, it is NOT reliable to check if internet is
     * available.</b>
     *
     * @param context
     * @return
     */
    public static boolean isWifiConnected(final Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo.getIpAddress() != 0;
    }

    public static void buildAirplaneNotification(final Context context) {
        NotificationManager notifManager = (NotificationManager) context.getSystemService(Context
                .NOTIFICATION_SERVICE);
        Resources res = context.getResources();
        Intent notificationIntent = new Intent(context, SavedWifiListActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent intent = PendingIntent.getActivity(context, 0,
                notificationIntent, 0);

        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(context)
                .setContentTitle(res.getString(R.string.airplane_mode_notification_context_title))
                .setContentText(res.getString(R.string
                        .airplane_mode_notification_ticker))
                .setTicker(res.getString(R.string.airplane_mode_notification_ticker))
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentIntent(intent);

        Notification notification = notifBuilder.build();
        notifManager.notify(Config.NOTIFICATION_ID_AIRPLANE_MODE, notification);
    }


    public static void dismissNotification(final Context context, int notificationId) {
        NotificationManager notifManager = (NotificationManager) context.getSystemService(Context
                .NOTIFICATION_SERVICE);

        notifManager.cancel(notificationId);
    }

    /**
     * Gets the state of Airplane Mode.
     *
     * @param context
     * @return true if enabled.
     */

    public static boolean isAirplaneModeOn(Context context) {
        return Settings.Global.getInt(context.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0;

    }

    public interface SavedWifiConfigurationListener {
        void onSavedWifiLoaded(List<WifiConfiguration> savedWifis);
    }

}



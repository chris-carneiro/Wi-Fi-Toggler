package net.opencurlybraces.android.projects.wifihandler.service;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.RemoteException;
import android.util.Log;

import net.opencurlybraces.android.projects.wifihandler.data.provider.WifiHandlerContract;
import net.opencurlybraces.android.projects.wifihandler.data.table.ConfiguredWifi;
import net.opencurlybraces.android.projects.wifihandler.util.WifiUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chris on 29/06/15.
 */
public class ContentIntentService extends IntentService {

    public static final String TAG = "ContentIntentService";

    private static final String SERVICE_ACTION_PREFIX = "net.opencurlybraces.android" +
            ".projects.wifihandler.service.";

    public static final String ACTION_HANDLE_SAVED_WIFI_INSERT = SERVICE_ACTION_PREFIX +
            "ACTION_HANDLE_SAVED_WIFI_INSERT";

    public static final String ACTION_HANDLE_SAVED_WIFI_UPDATE = SERVICE_ACTION_PREFIX +
            "ACTION_HANDLE_SAVED_WIFI_UPDATE";

    private WifiManager mWifiManager;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public ContentIntentService(String name) {
        super(name);
    }

    public ContentIntentService() {
        super("ContentIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (mWifiManager == null) {
            mWifiManager = (WifiManager) getSystemService(Context
                    .WIFI_SERVICE);
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        switch (intent.getAction()) {
            case ACTION_HANDLE_SAVED_WIFI_INSERT:
                handleUserWifiInsert();
                break;
            case ACTION_HANDLE_SAVED_WIFI_UPDATE:

                break;

        }
    }

    private void handleUserWifiInsert() {
        Log.d(TAG, "handleUserWifiInsert");
        List<WifiConfiguration> configuredWifis = null;
        try {
            configuredWifis = WifiUtils.getConfiguredWifis(mWifiManager);
            List<ContentProviderOperation> batch = ConfiguredWifi.buildBatch(configuredWifis);
            insertBatch(batch);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "Nothing to build");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void insertBatch(List<ContentProviderOperation> batch) {
        Log.d(TAG, "Authority URI=" + WifiHandlerContract.AUTHORITY);
        if (batch == null) return;

        try {
            ContentProviderResult[] results = getContentResolver().applyBatch(WifiHandlerContract
                            .AUTHORITY,
                    (ArrayList<ContentProviderOperation>) batch);
            Log.d(TAG, "ContentProviderResult=" + (results != null ? results.length : 0));
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }

}

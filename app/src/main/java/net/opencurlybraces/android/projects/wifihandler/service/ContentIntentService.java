package net.opencurlybraces.android.projects.wifihandler.service;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import net.opencurlybraces.android.projects.wifihandler.data.provider.WifiHandlerContract;
import net.opencurlybraces.android.projects.wifihandler.data.table.SavedWifi;
import net.opencurlybraces.android.projects.wifihandler.receiver.WifiStateReceiver;
import net.opencurlybraces.android.projects.wifihandler.receiver.WifiSupplicantStateReceiver;
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

    public static final String ACTION_HANDLE_SAVED_WIFI_UPDATE_CONNECT = SERVICE_ACTION_PREFIX +
            "ACTION_HANDLE_SAVED_WIFI_UPDATE_CONNECT";

    public static final String ACTION_HANDLE_SAVED_WIFI_UPDATE_DISCONNECT = SERVICE_ACTION_PREFIX +
            "ACTION_HANDLE_SAVED_WIFI_UPDATE_DISCONNECT";

    private static final String[] PROJECTION = new String[]{SavedWifi._ID, SavedWifi
            .SSID, SavedWifi.STATUS};

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
        Log.d(TAG, "Intent action=" + (intent != null ? intent.getAction() : null));
        if (intent == null) return;

        switch (intent.getAction()) {
            case ACTION_HANDLE_SAVED_WIFI_INSERT:
                handleUserWifiInsert();
                break;
            case ACTION_HANDLE_SAVED_WIFI_UPDATE_CONNECT:
                String ssid = intent.getStringExtra(WifiSupplicantStateReceiver.EXTRA_CURRENT_SSID);
                int newState = intent.getIntExtra(WifiStateReceiver.EXTRA_SAVED_WIFI_NEW_STATE, -1);
                updateSavedWifiStatus(ssid, newState);
                break;
            case ACTION_HANDLE_SAVED_WIFI_UPDATE_DISCONNECT:
                int state = intent.getIntExtra(WifiStateReceiver.EXTRA_SAVED_WIFI_NEW_STATE, -1);
                updateSavedWifiStatus(state);
                mWifiManager.setWifiEnabled(false);
                break;
        }
    }

    private void handleUserWifiInsert() {
        Log.d(TAG, "handleUserWifiInsert");
        List<WifiConfiguration> configuredWifis = null;
        try {
            configuredWifis = WifiUtils.getConfiguredWifis(mWifiManager);
            List<ContentProviderOperation> batch = SavedWifi.buildBatch(configuredWifis);
            insertBatch(batch);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "Nothing to build");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void insertBatch(List<ContentProviderOperation> batch) {
        if (batch == null) return;

        try {
            ContentProviderResult[] results = getContentResolver().applyBatch(WifiHandlerContract
                            .AUTHORITY,
                    (ArrayList<ContentProviderOperation>) batch);
        } catch (RemoteException | OperationApplicationException e) {
            e.printStackTrace();
        }
    }


    private void updateSavedWifiStatus(String ssid, int newState) {

        String rowId = getSavedIdForSSID(ssid);
        Uri uri = SavedWifi.buildConfiguredWifiUri(rowId);
        Log.d(TAG, "Connect action row id=" + rowId + " new state=" + newState);
        ContentValues value = new ContentValues(1);
        value.put(SavedWifi.STATUS, newState);
        getContentResolver().update(uri, value, SavedWifi
                ._ID + "=?", new String[]{rowId});

    }

    private void updateSavedWifiStatus(int newState) {
        String rowId = getConnectedWifiRowId();
        Log.d(TAG, "Disconnect action row id=" + rowId + " new state=" + newState);
        if(TextUtils.isEmpty(rowId)) return; //TODO remove this ugly workaround
        Uri uri = SavedWifi.buildConfiguredWifiUri(rowId);

        ContentValues value = new ContentValues(1);
        value.put(SavedWifi.STATUS, newState);
        getContentResolver().update(uri, value, SavedWifi
                ._ID + "=?", new String[]{rowId});
    }

    //TODO use query builder decrease redundancy
    private String getConnectedWifiRowId() {

        Cursor c = getContentResolver().query(SavedWifi.CONTENT_URI, PROJECTION,
                SavedWifi.STATUS + "=?",
                new String[]{String.valueOf(WifiConfiguration.Status.CURRENT)}, null);
        String rowId = null;
        if (c != null) {
            if (c.getCount() > 1) {
                return null;
            }
            if (c.moveToFirst()) {
                rowId = c.getString(0);
                c.close();
            }
        }
        return rowId;
    }

    private String getSavedIdForSSID(String savedSSID) {
        Cursor c = getContentResolver().query(SavedWifi.CONTENT_URI, PROJECTION,
                SavedWifi.SSID + "=?",
                new String[]{savedSSID}, null);
        String rowId = null;
        if (c != null) {
            if (c.getCount() > 1) {
                return null;
            }
            if (c.moveToFirst()) {
                rowId = c.getString(0);
                c.close();
            }
        }
        return rowId;
    }


}

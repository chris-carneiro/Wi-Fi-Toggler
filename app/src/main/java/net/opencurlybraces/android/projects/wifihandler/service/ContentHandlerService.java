package net.opencurlybraces.android.projects.wifihandler.service;

import android.app.Service;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.util.Log;

import net.opencurlybraces.android.projects.wifihandler.data.DataAsyncQueryHandler;
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
public class ContentHandlerService extends Service implements
        DataAsyncQueryHandler.AsyncQueryListener {

    public static final String TAG = "ContentHandlerService";

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

    private DataAsyncQueryHandler mDataAsyncQueryHandler = null;

    private static final int TOKEN_QUERY = 1;
    private static final int TOKEN_UPDATE = 3;
    private static final int TOKEN_INSERT_BATCH = 5;


    @Override
    public void onCreate() {
        super.onCreate();
        if (mWifiManager == null) {
            mWifiManager = (WifiManager) getSystemService(Context
                    .WIFI_SERVICE);
        }

        if (mDataAsyncQueryHandler == null) {
            mDataAsyncQueryHandler = new DataAsyncQueryHandler(getContentResolver(), this);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_NOT_STICKY;
        Log.d(TAG, "Intent Not null startId=" + startId);
        switch (intent.getAction()) {
            case ACTION_HANDLE_SAVED_WIFI_INSERT:
                handleUserWifiInsert();
                break;
            case ACTION_HANDLE_SAVED_WIFI_UPDATE_CONNECT:
                String ssid = intent.getStringExtra(WifiSupplicantStateReceiver.EXTRA_CURRENT_SSID);
                int newState = intent.getIntExtra(WifiStateReceiver.EXTRA_SAVED_WIFI_NEW_STATE, -1);

                startQuery(newState, SavedWifi.SSID + "=?", new String[]{ssid});
                break;
            case ACTION_HANDLE_SAVED_WIFI_UPDATE_DISCONNECT:
                int stateDisconnect = intent.getIntExtra(WifiStateReceiver
                        .EXTRA_SAVED_WIFI_NEW_STATE, -1);

                startQuery(stateDisconnect, SavedWifi.STATUS + "=?", new String[]{String.valueOf
                        (WifiConfiguration.Status.CURRENT)});

                break;
        }
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void handleUserWifiInsert() {
        Log.d(TAG, "handleUserWifiInsert");
        List<WifiConfiguration> configuredWifis = null;
        try {
            configuredWifis = WifiUtils.getConfiguredWifis(mWifiManager);
            List<ContentProviderOperation> batch = SavedWifi.buildBatch(configuredWifis);
            insertBatchAsync((ArrayList<ContentProviderOperation>) batch);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "Nothing to build");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void insertBatchAsync(ArrayList<ContentProviderOperation> batch) {
        if (batch == null) return;
        mDataAsyncQueryHandler.startInsertBatch(TOKEN_INSERT_BATCH, null, WifiHandlerContract
                .AUTHORITY, batch);

    }

    @Override
    public void onInsertBatchComplete(int token, Object cookie, ContentProviderResult[] results) {
        Log.d(TAG, "Async Batch Insert complete, stopping service");
        stopSelf();
    }

    @Override
    public void onQueryComplete(int token, Object cookie, Cursor cursor) {
        Log.d(TAG, "Query Complete token=" + token + " cookie=" + cookie);

        if (cursor == null || cursor.getCount() <= 0) return;

        String rowId = getRowIdFromCursor(cursor);

        Uri uri = SavedWifi.buildConfiguredWifiUri(rowId);

        ContentValues value = new ContentValues(1);
        value.put(SavedWifi.STATUS, (int) cookie);

        mDataAsyncQueryHandler.startUpdate(TOKEN_UPDATE, cookie, uri, value, SavedWifi
                ._ID + "=?", new String[]{rowId});
    }

    @Override
    public void onUpdateComplete(int token, int wifiState, int result) {
        Log.d(TAG, "Async Update complete, stopping service, wifiState=" + wifiState);

        if (wifiState == WifiConfiguration.Status.DISABLED) {
            disableWifiAdapter();
        }

    }

    private void disableWifiAdapter() {
        mWifiManager.setWifiEnabled(false);
    }

    /**
     * Issue an async Query given the selection params. Note that wifiState here is used to pass
     * wifi state value to the #onQueryComplete callback
     *
     * @param wifiState
     * @param where
     * @param whereArgs
     */
    private void startQuery(int wifiState, String where, String[] whereArgs) {
        mDataAsyncQueryHandler.startQuery(TOKEN_QUERY, wifiState, SavedWifi.CONTENT_URI, PROJECTION,
                where,
                whereArgs, null);
    }

    private String getRowIdFromCursor(final Cursor cursor) {
        String rowId = null;
        if (cursor != null) {
            if (cursor.getCount() > 1) {
                return null;
            }
            if (cursor.moveToFirst()) {
                rowId = cursor.getString(0);
                cursor.close();
            }
        }
        return rowId;
    }
}

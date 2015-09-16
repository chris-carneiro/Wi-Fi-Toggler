package net.opencurlybraces.android.projects.wifitoggler.data.provider;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import net.opencurlybraces.android.projects.wifitoggler.data.table.SavedWifi;

import java.util.ArrayList;


/**
 * Provides access to the WifiToggler data. Created by chris on 03/06/15.
 */
public class WifiTogglerProvider extends ContentProvider {

    private static final String TAG = "WifiTogglerProvider";

    private static final int CONFIGURED_WIFIS = 1;
    private static final int CONFIGURED_WIFI_ID = 2;
    private static final UriMatcher sURIMatcher;

    private WifiTogglerDatabase mWifiTogglerDatabase;

    static {
        sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        sURIMatcher.addURI(WifiTogglerContract.AUTHORITY, SavedWifi
                        .PATH_CONFIGURED_WIFIS,
                CONFIGURED_WIFIS);
        sURIMatcher.addURI(WifiTogglerContract.AUTHORITY, SavedWifi
                        .PATH_CONFIGURED_WIFIS +
                        "/#",
                CONFIGURED_WIFI_ID);
    }

    @Override
    public boolean onCreate() {
        mWifiTogglerDatabase = new WifiTogglerDatabase(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        SavedWifi.checkColumns(projection);

        queryBuilder.setTables(SavedWifi.TABLE);

        switch (sURIMatcher.match(uri)) {

            case CONFIGURED_WIFIS:
                break;
            case CONFIGURED_WIFI_ID:
                // adding the ID to the original query
                queryBuilder.appendWhere(SavedWifi._ID + "="
                        + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        String orderBy;

        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = "status ASC, is_auto_toggle DESC, ssid COLLATE NOCASE ASC ";
        } else {
            orderBy = sortOrder;
        }

        SQLiteDatabase db = mWifiTogglerDatabase.getReadableDatabase();

        Cursor cursor = queryBuilder.query(
                db,            // The database to query
                projection,    // The columns to return from the query
                selection,     // The columns for the where clause
                selectionArgs, // The values for the where clause
                null,          // don't group the rows
                null,          // don't filter by row groups
                orderBy        // The sort order
        );

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        /**
         * Chooses the MIME type based on the incoming URI pattern
         */
        switch (sURIMatcher.match(uri)) {
            // If the pattern is for notes or live folders, returns the general content type.
            case CONFIGURED_WIFIS:
                return SavedWifi.CONTENT_TYPE;
            case CONFIGURED_WIFI_ID:
                return SavedWifi.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase database = mWifiTogglerDatabase.getWritableDatabase();
        long rowId;
        int result = sURIMatcher.match(uri);
        //        Log.d(TAG, "Match result=" + result);
        switch (result) {
            case CONFIGURED_WIFIS:
                rowId = database.insert(SavedWifi.TABLE, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        // If the insert succeeded, the row ID > 0.
        if (rowId > 0) {
            Uri noteUri = SavedWifi.buildConfiguredWifiUri(String.valueOf(rowId));

            getContext().getContentResolver().notifyChange(uri, null);
            return noteUri;
        }

        throw new SQLException("Failed to insert row into " + uri);

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase sqlDB = mWifiTogglerDatabase.getWritableDatabase();
        int deletedRows = 0;
        switch (sURIMatcher.match(uri)) {
            case CONFIGURED_WIFIS:
                deletedRows = sqlDB.delete(SavedWifi.TABLE, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        if (deletedRows > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return deletedRows;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase sqlDB = mWifiTogglerDatabase.getWritableDatabase();
        int updatedRows = 0;
        switch (sURIMatcher.match(uri)) {
            case CONFIGURED_WIFIS:
                updatedRows = sqlDB.update(SavedWifi.TABLE,
                        values,
                        selection,
                        selectionArgs);
                break;
            case CONFIGURED_WIFI_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    updatedRows = sqlDB.update(SavedWifi.TABLE,
                            values,
                            SavedWifi._ID + "=" + id,
                            null);
                } else {
                    updatedRows = sqlDB.update(SavedWifi.TABLE,
                            values,
                            SavedWifi._ID + "=" + id
                                    + " AND "
                                    + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        Log.d(TAG, "updatedRow=" + updatedRows);
        return updatedRows;
    }

    @Override
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {

        final SQLiteDatabase database = mWifiTogglerDatabase.getWritableDatabase();
        database.beginTransaction();
        try {
            final int numOperations = operations.size();
            final ContentProviderResult[] results = new ContentProviderResult[numOperations];
            for (int i = 0; i < numOperations; i++) {
                results[i] = operations.get(i).apply(this, results, i);
            }
            database.setTransactionSuccessful();
            return results;
        } finally {
            database.endTransaction();
        }
    }
}

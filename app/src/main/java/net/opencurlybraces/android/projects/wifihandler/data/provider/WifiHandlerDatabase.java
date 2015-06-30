package net.opencurlybraces.android.projects.wifihandler.data.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import net.opencurlybraces.android.projects.wifihandler.data.table.SavedWifi;

/**
 * Created by chris on 03/06/15.
 */
public class WifiHandlerDatabase extends SQLiteOpenHelper {

    private static final String TAG = "WifiHandlerDatabase";

    private static final String DATABASE_NAME = "wifihandler.db";

    private static final int DATABASE_VERSION = 1;

    private final Context mContext;

    public WifiHandlerDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        SavedWifi.createTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        SavedWifi.upgrade(db, oldVersion, newVersion);
    }
}

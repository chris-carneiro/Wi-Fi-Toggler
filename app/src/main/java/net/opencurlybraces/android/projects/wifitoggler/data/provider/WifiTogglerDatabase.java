package net.opencurlybraces.android.projects.wifitoggler.data.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import net.opencurlybraces.android.projects.wifitoggler.data.table.SavedWifi;

/**
 * Created by chris on 03/06/15.
 */
public class WifiTogglerDatabase extends SQLiteOpenHelper {

    private static final String TAG = "WifiTogglerDatabase";
    private static final String DATABASE_NAME = "wifitoggler.db";
    private static final int DATABASE_VERSION = 3;

    private final Context mContext;

    public WifiTogglerDatabase(Context context) {
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

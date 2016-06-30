package net.opencurlybraces.android.projects.wifitoggler.ui;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;

import net.opencurlybraces.android.projects.wifitoggler.R;
import net.opencurlybraces.android.projects.wifitoggler.data.table.SavedWifi;
import net.opencurlybraces.android.projects.wifitoggler.util.SnackBarUndoActionDataHandler;

/**
 * Created by chris on 28/09/15.
 */
public class SavedDisabledWifiListActivity extends SavedWifiListActivityAbstract {

    private static final String TAG = "SavedDisabledWifiList";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disabled_saved_wifi_list);
        bindListView();
        bindViews();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {SavedWifi._ID, SavedWifi.SSID, SavedWifi.STATUS, SavedWifi
                .AUTO_TOGGLE};
        CursorLoader cursorLoader = new CursorLoader(this,
                SavedWifi.CONTENT_URI, projection, SavedWifi.whereAutoToggle, new String[]{"0"},
                null);
        return cursorLoader;
    }

    @Override
    protected void handleSnackBar(int reverseSortedPosition) {
        Cursor cursor = (Cursor) mSavedWifiCursorAdapter.getItem(reverseSortedPosition);
        String ssid = cursor.getString(cursor.getColumnIndexOrThrow(SavedWifi.SSID));

        String confirmationMessage = formatSnackBarMessage(ssid,R.string
                .wifi_enabled_confirmation_bottom_overlay_content );

        SnackBarUndoActionDataHandler.UndoData undoData = prepareSnackBarUndoDataObject
                (reverseSortedPosition, false);
        SnackBarUndoActionDataHandler snackBarUndoHelper = new SnackBarUndoActionDataHandler(this, undoData);

        showUndoSnackBar(confirmationMessage,snackBarUndoHelper);
    }

    @Override
    public void onClick(View v) {}

    @Override
    protected void bindViews() {
        mEmptyView.setText(getString(R.string.wifi_list_info_no_disabled_known_wifi));
    }


    @Override
    public void onConnected(Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }
}

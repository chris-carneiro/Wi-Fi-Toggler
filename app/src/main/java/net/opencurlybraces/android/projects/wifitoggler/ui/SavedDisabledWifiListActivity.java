package net.opencurlybraces.android.projects.wifitoggler.ui;

import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;

import net.opencurlybraces.android.projects.wifitoggler.R;
import net.opencurlybraces.android.projects.wifitoggler.data.table.SavedWifi;

/**
 * Created by chris on 28/09/15.
 */
public class SavedDisabledWifiListActivity extends SavedWifiListActivityAbstract {

    private static final String TAG = "SavedDisabledWifiList";


    private TextView mUndoButton = null;

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
    public void onDismiss(ListView listView, int[] reverseSortedPositions) {
        super.onDismiss(listView, reverseSortedPositions);

        for (int position : reverseSortedPositions) {

            long itemId = mSavedWifiCursorAdapter.getItemId(position);
           ContentValues cv = buildContentValuesForUpdate(position);
            updateAutoToggleValue(itemId, cv);
            displayConfirmationBannerWithUndo(position, R.string
                    .wifi_enabled_confirmation_bottom_overlay_content);
            mSavedWifiCursorAdapter
                    .notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.undo_action_wifi_button:
                handleUndoAction();
                break;
        }
    }

    @Override
    protected void bindViews() {
        mDismissConfirmationText = (TextView) findViewById(R.id.tv_confirmation_message_wifi);
        mDismissConfirmationBanner = (RelativeLayout) findViewById(R.id
                .saved_wifi_confirmation_banner);
        mEmptyView.setText(getString(R.string.wifi_list_info_no_disabled_known_wifi));
        mUndoButton = (TextView) findViewById(R.id.undo_action_wifi_button);
        mUndoButton.setOnClickListener(this);
    }


    @Override
    protected void setListAdapter() {
        mWifiTogglerWifiList.setAdapter(mSavedWifiCursorAdapter);
    }


    @Override
    protected void handleUndoAction() {
        hideBanner();
        ContentValues cv = new ContentValues();
        cv.put(SavedWifi.AUTO_TOGGLE, false);
        updateAutoToggleValue(mSavedWifiCursorAdapter.getItemIdToUndo(),cv);
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

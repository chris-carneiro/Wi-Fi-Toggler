package net.opencurlybraces.android.projects.wifitoggler.ui;

import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.opencurlybraces.android.projects.wifitoggler.Config;
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
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {SavedWifi._ID, SavedWifi.SSID, SavedWifi.STATUS, SavedWifi
                .AUTO_TOGGLE};
        CursorLoader cursorLoader = new CursorLoader(this,
                SavedWifi.CONTENT_URI, projection, SavedWifi.whereAutoToggle, new String[]{"0"},
                null);
        return cursorLoader;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public void onDismiss(ListView listView, int[] reverseSortedPositions) {
        super.onDismiss(listView, reverseSortedPositions);

        for (int position : reverseSortedPositions) {

            long itemId =  mSavedWifiCursorAdapter.getItemId(position);
            updateAutoToggleValue(itemId, position);
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
    public void handleUndoAction() {
        ContentValues cv = new ContentValues();
        cv.put(SavedWifi.AUTO_TOGGLE, false);
        mDataAsyncQueryHandler.startUpdate(Config.TOKEN_UPDATE, null, SavedWifi
                .CONTENT_URI, cv, SavedWifi
                .whereID, new String[]{String.valueOf(mSavedWifiCursorAdapter.getItemIdToUndo()
        )});
        mAutoHideHandler.removeMessages(WHAT_AUTO_HIDE);
        mAutoHideHandler.sendMessage(Message.obtain(mAutoHideHandler));
    }
}

package net.opencurlybraces.android.projects.wifitoggler.ui;

import android.app.LoaderManager;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import net.opencurlybraces.android.projects.wifitoggler.Config;
import net.opencurlybraces.android.projects.wifitoggler.R;
import net.opencurlybraces.android.projects.wifitoggler.data.DataAsyncQueryHandler;
import net.opencurlybraces.android.projects.wifitoggler.data.model.Wifi;
import net.opencurlybraces.android.projects.wifitoggler.data.provider.WifiTogglerContract;
import net.opencurlybraces.android.projects.wifitoggler.data.table.SavedWifi;
import net.opencurlybraces.android.projects.wifitoggler.ui.SwipeDismissListViewTouchListener
        .DismissCallbacks;
import net.opencurlybraces.android.projects.wifitoggler.util.StartupUtils;

import java.util.ArrayList;
import java.util.List;


public abstract class SavedWifiBaseListActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        DataAsyncQueryHandler.AsyncQueryListener, DismissCallbacks {

    private static final String TAG = "BaseListActivity";

    private static final int TOKEN_UPDATE_BATCH = 6;

    private static final String[] PROJECTION_SSID_AUTO_TOGGLE = new String[]{SavedWifi._ID,
            SavedWifi
                    .SSID, SavedWifi.AUTO_TOGGLE};

    protected static final String INSTANCE_KEY_CHECKED_ITEMS = "checkedItems";
    protected static final String INSTANCE_KEY_LIST_CHOICE_MODE = "listChoiceMode";
    protected static final String INSTANCE_KEY_IS_ACTION_MODE = "isActionMode";
    protected static final String INSTANCE_KEY_FIRST_VISIBLE_POSITION = "firstVisiblePosition";
    protected static final String INSTANCE_KEY_OFFSET_FROM_TOP = "offsetFromTop";


    protected SavedWifiListAdapter mSavedWifiCursorAdapter = null;
    protected DataAsyncQueryHandler mDataAsyncQueryHandler = null;

    protected ListView mWifiTogglerWifiList = null;
    protected TextView mEmptyView = null;

    private Bundle mSavedInstanceState = null;

    private SwipeDismissListViewTouchListener mTouchListener = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Config.DEBUG_MODE) {
            StartupUtils.startStrictMode();
        }
        super.onCreate(savedInstanceState);

        mDataAsyncQueryHandler = new DataAsyncQueryHandler(getContentResolver(), this);
        initCursorLoader();
        mSavedWifiCursorAdapter = (SavedWifiListAdapter) initCursorAdapter();

    }

    @Override
    protected void onResume() {
        super.onResume();
        setListAdapter();
        restoreListViewState();


    }

    protected void bindListView() {
        mWifiTogglerWifiList = (ListView) findViewById(android.R.id.list);
        mEmptyView = (TextView) findViewById(android.R.id.empty);
    }


    protected abstract void setListAdapter();

    protected ArrayList<ContentProviderOperation> udpateBatchWifiToggleState(List<Wifi> wifis) {
        ArrayList<ContentProviderOperation> operations = (ArrayList<ContentProviderOperation>)
                SavedWifi.buildBatchUpdateAutoToggle
                        (wifis);
        return operations;

    }

    protected void startBatchUpdate(ArrayList<ContentProviderOperation> operations) {
        mDataAsyncQueryHandler.startBatchOperations(TOKEN_UPDATE_BATCH, null, WifiTogglerContract
                .AUTHORITY, operations);
    }

    protected void displaySettingsActivity() {
        Intent preferencesIntent = new Intent(this, SettingsActivity.class);
        startActivity(preferencesIntent);
    }

    protected void initCursorLoader() {
        getLoaderManager().initLoader(0, null, this);
    }


    protected CursorAdapter initCursorAdapter() {
        Log.d(TAG, "initCursorAdapter");
        if (mSavedWifiCursorAdapter == null) {
            mSavedWifiCursorAdapter = new SavedWifiListAdapter(this, null, 0);
        }
        return mSavedWifiCursorAdapter;
    }

    protected void handleSavedWifiListLoading(boolean isChecked) {
        Log.d(TAG, "handleSavedWifiListLoading=" + isChecked);
        if (isChecked) {
            mWifiTogglerWifiList.setAdapter(mSavedWifiCursorAdapter);
        } else {
            mWifiTogglerWifiList.setAdapter(null);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mSavedWifiCursorAdapter.isActionMode()) {
            outState.putInt(INSTANCE_KEY_LIST_CHOICE_MODE, mWifiTogglerWifiList.getChoiceMode());
            outState.putIntegerArrayList(INSTANCE_KEY_CHECKED_ITEMS, mSavedWifiCursorAdapter
                    .getSelectedItems());
            outState.putBoolean(INSTANCE_KEY_IS_ACTION_MODE, mSavedWifiCursorAdapter.isActionMode
                    ());

        }
        int firstVisiblePosition = mWifiTogglerWifiList.getFirstVisiblePosition();
        View v = mWifiTogglerWifiList.getChildAt(0);
        int top = (v == null) ? 0 : (v.getTop() - mWifiTogglerWifiList.getPaddingTop());

        outState.putInt(INSTANCE_KEY_FIRST_VISIBLE_POSITION, firstVisiblePosition);
        outState.putInt(INSTANCE_KEY_OFFSET_FROM_TOP, top);
        mSavedInstanceState = outState; // Needed on Pause/Resume to keep selected items' state
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "onRestoreInstanceState");
        super.onRestoreInstanceState(savedInstanceState);
        mSavedInstanceState = savedInstanceState;
    }

    protected void restoreListViewState() {
        Log.d(TAG, "restoreListViewState ");

        if (mSavedInstanceState != null) {

            //            int choiceMode = mSavedInstanceState.getInt(INSTANCE_KEY_LIST_CHOICE_MODE,
            //                    mWifiTogglerWifiList
            //                            .getChoiceMode());
            //            boolean wasInActionModeOnPause = mSavedInstanceState.getBoolean
            //                    (INSTANCE_KEY_IS_ACTION_MODE);
            //
            //            if (wasInActionModeOnPause) {
            //                /**
            //                 *  Needed on screen rotation only as listview is apparently reset
            // and so is the
            //                 *  choice mode but not when app is put in background.
            //                 *  setChoiceMode() destroys the action mode, and we obviously do
            // want that..
            //                 */
            //                if (mWifiTogglerWifiList.getChoiceMode() != AbsListView
            //                        .CHOICE_MODE_MULTIPLE_MODAL) {
            //                    mWifiTogglerWifiList.setChoiceMode(choiceMode);
            //                }
            //                ArrayList<Integer> checkedItems = mSavedInstanceState
            // .getIntegerArrayList
            //                        (INSTANCE_KEY_CHECKED_ITEMS);
            //                setCachedItemsChecked(checkedItems);
            //            }

            int firstVisiblePosition = mSavedInstanceState.getInt
                    (INSTANCE_KEY_FIRST_VISIBLE_POSITION);
            int offsetTop = mSavedInstanceState.getInt(INSTANCE_KEY_OFFSET_FROM_TOP);
            // restore index and position
            mWifiTogglerWifiList.setSelectionFromTop(firstVisiblePosition, offsetTop);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            displaySettingsActivity();
            return true;
        }

        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_saved_wifi_list, menu);
        return true;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mSavedWifiCursorAdapter.swapCursor(data);

        if (mTouchListener == null) {
            mTouchListener =
                    new SwipeDismissListViewTouchListener(
                            mWifiTogglerWifiList,
                            this);
        }
        mWifiTogglerWifiList.setOnTouchListener(mTouchListener);
        // Setting this scroll listener is required to ensure that during ListView scrolling,
        // we don't look for swipes.
        mWifiTogglerWifiList.setOnScrollListener(mTouchListener.makeScrollListener());
        mTouchListener.resetDeletedViews();

        mWifiTogglerWifiList.setEmptyView(mEmptyView);

    }


    public ContentValues buildContentValuesForUpdate(Cursor cursor) {
        boolean isAutoToggle = (cursor.getInt(cursor
                .getColumnIndexOrThrow
                        (SavedWifi.AUTO_TOGGLE)) > 0);
        ContentValues cv = new ContentValues();
        cv.put(SavedWifi.AUTO_TOGGLE, !isAutoToggle);
        return cv;
    }

    public void updateAutoToggleValue(long itemId, ContentValues cv) {
        mDataAsyncQueryHandler.startUpdate(Config.TOKEN_UPDATE,
                null,
                SavedWifi.CONTENT_URI,
                cv,
                SavedWifi.whereID, new String[]{String.valueOf
                        (itemId)});
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mSavedWifiCursorAdapter.swapCursor(null);
    }

    @Override
    public void onUpdateComplete(int token, Object cookie, int result) {
        Log.d(TAG, "udpated rows count" + result);
    }

    @Override
    public void onBatchInsertComplete(int token, Object cookie, ContentProviderResult[] results) {
    }

    @Override
    public void onInsertComplete(int token, Object cookie, Uri uri) {
    }

    @Override
    public void onBatchUpdateComplete(int token, Object cookie, ContentProviderResult[] results) {
        if (Config.DEBUG_MODE) {
            queryWifiData();
        }
    }

    private void queryWifiData() {
        mDataAsyncQueryHandler.startQuery(1, null, SavedWifi.CONTENT_URI,
                PROJECTION_SSID_AUTO_TOGGLE,
                null, null, null);
    }

    @Override
    public void onQueryComplete(int token, Object cookie, Cursor cursor) {
        if (Config.DEBUG_MODE) {
            logCursorData(cursor);
        }

    }

    private void logCursorData(final Cursor cursor) {
        try {
            Log.d(TAG, "Wifi count=" + cursor.getCount());
            while (cursor.moveToNext()) {
                Wifi wifi = Wifi.buildForCursor(cursor);
                Log.d(TAG, "Ssid=" + wifi.getSsid() + " id=" + wifi.get_id() + " isAutoToggle=" +
                        wifi.isAutoToggle());
            }
            cursor.close();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @Override
    public boolean canDismiss(int position) {
        return true;
    }

    @Override
    public void onDismiss(ListView listView, int[] reverseSortedPositions) {
        for (int position : reverseSortedPositions) {
            long itemId = mSavedWifiCursorAdapter.getItemId(position);
            Cursor cursor = (Cursor) mSavedWifiCursorAdapter.getItem
                    (position);
            ContentValues cv = buildContentValuesForUpdate(cursor);
            updateAutoToggleValue(itemId, cv);
        }
        mSavedWifiCursorAdapter
                .notifyDataSetChanged();
    }
}

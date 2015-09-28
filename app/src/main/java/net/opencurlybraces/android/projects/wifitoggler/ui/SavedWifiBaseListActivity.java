package net.opencurlybraces.android.projects.wifitoggler.ui;

import android.app.LoaderManager;
import android.content.ContentProviderOperation;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import net.opencurlybraces.android.projects.wifitoggler.Config;
import net.opencurlybraces.android.projects.wifitoggler.R;
import net.opencurlybraces.android.projects.wifitoggler.data.DataAsyncQueryHandler;
import net.opencurlybraces.android.projects.wifitoggler.data.model.Wifi;
import net.opencurlybraces.android.projects.wifitoggler.data.provider.WifiTogglerContract;
import net.opencurlybraces.android.projects.wifitoggler.data.table.SavedWifi;
import net.opencurlybraces.android.projects.wifitoggler.util.StartupUtils;

import java.util.ArrayList;
import java.util.List;


public abstract class SavedWifiBaseListActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        DataAsyncQueryHandler.AsyncQueryListener {

    private static final String TAG = "BaseListActivity";

    private static final int TOKEN_UPDATE_BATCH = 6;

    protected static final String INSTANCE_KEY_CHECKED_ITEMS = "checkedItems";
    protected static final String INSTANCE_KEY_LIST_CHOICE_MODE = "listChoiceMode";
    protected static final String INSTANCE_KEY_IS_ACTION_MODE = "isActionMode";
    protected static final String INSTANCE_KEY_FIRST_VISIBLE_POSITION = "firstVisiblePosition";
    protected static final String INSTANCE_KEY_OFFSET_FROM_TOP = "offsetFromTop";


    protected SavedWifiListAdapter mSavedWifiCursorAdapter = null;
    protected DataAsyncQueryHandler mDataAsyncQueryHandler = null;

    protected ListView mWifiTogglerWifiList = null;
    protected TextView mEmptyView = null;

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
        mWifiTogglerWifiList.setEmptyView(mEmptyView);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mSavedWifiCursorAdapter.swapCursor(null);
    }

}

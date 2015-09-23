package net.opencurlybraces.android.projects.wifitoggler.ui;

import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import net.opencurlybraces.android.projects.wifitoggler.Config;
import net.opencurlybraces.android.projects.wifitoggler.R;
import net.opencurlybraces.android.projects.wifitoggler.WifiToggler;
import net.opencurlybraces.android.projects.wifitoggler.data.DataAsyncQueryHandler;
import net.opencurlybraces.android.projects.wifitoggler.data.model.Wifi;
import net.opencurlybraces.android.projects.wifitoggler.data.provider.WifiTogglerContract;
import net.opencurlybraces.android.projects.wifitoggler.data.table.SavedWifi;
import net.opencurlybraces.android.projects.wifitoggler.service.WifiTogglerService;
import net.opencurlybraces.android.projects.wifitoggler.util.PrefUtils;
import net.opencurlybraces.android.projects.wifitoggler.util.StartupUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class SavedWifiListActivity extends AppCompatActivity implements
        CompoundButton.OnCheckedChangeListener,
        LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener, Observer,
        DataAsyncQueryHandler.AsyncQueryListener {

    private static final String TAG = "SavedWifiList";
    private static final int TOKEN_UPDATE_BATCH = 6;
    private static final String INSTANCE_KEY_CHECKED_ITEMS = "checkedItems";
    private static final String INSTANCE_KEY_LIST_CHOICE_MODE = "listChoiceMode";
    private static final String INSTANCE_KEY_IS_ACTION_MODE = "isActionMode";
    private static final String INSTANCE_KEY_FIRST_VISIBLE_POSITION = "firstVisiblePosition";
    private static final String INSTANCE_KEY_OFFSET_FROM_TOP = "offsetFromTop";

    private static final String[] PROJECTION_SSID_AUTO_TOGGLE = new String[]{SavedWifi._ID,
            SavedWifi
                    .SSID, SavedWifi.AUTO_TOGGLE};

    private TextView mWifiTogglerSwitchLabel = null;
    private Switch mWifiTogglerActivationSwitch = null;
    private ListView mWifiTogglerWifiList = null;
    private TextView mEmptyView = null;
    private RelativeLayout mBanner = null;
    private TextView mBannerContent = null;

    private SavedWifiListAdapter mSavedWifiCursorAdapter = null;
    private ActionModeHelper mActionModeCallback = null;
    private DataAsyncQueryHandler mDataAsyncQueryHandler = null;
    private Bundle mSavedInstanceState = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Config.DEBUG_MODE) {
            StartupUtils.startStrictMode();
        }
        super.onCreate(savedInstanceState);
        Log.d(TAG, "OnCreate");
        setContentView(R.layout.activity_saved_wifi_list);
        mDataAsyncQueryHandler = new DataAsyncQueryHandler(getContentResolver(), this);
        bindViews();
        initCursorLoader();
        mSavedWifiCursorAdapter = (SavedWifiListAdapter) initCursorAdapter();
    }

    @Override
    protected void onStart() {
        super.onStart();
        WifiToggler.registerSettingObserver(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        WifiToggler.unRegisterSettingObserver(this);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();

        startupCheck();
        handleNotification(mWifiTogglerActivationSwitch.isChecked());
        setListAdapterAccordingToSwitchState();
        registerReceivers();
        handleBannerDisplay();
        doSystemSettingsCheck();
        setOnItemLongClickListener();
        restoreListViewState();
    }

    private void setOnItemLongClickListener() {
        if (mActionModeCallback == null) {
            mActionModeCallback = new ActionModeHelper(this,
                    mWifiTogglerWifiList);
        }
        mWifiTogglerWifiList.setOnItemLongClickListener(mActionModeCallback);
    }

    private void restoreListViewState() {
        Log.d(TAG, "restoreListViewState ");

        if (mSavedInstanceState != null) {

            int choiceMode = mSavedInstanceState.getInt(INSTANCE_KEY_LIST_CHOICE_MODE,
                    mWifiTogglerWifiList
                            .getChoiceMode());
            boolean wasInActionModeOnPause = mSavedInstanceState.getBoolean
                    (INSTANCE_KEY_IS_ACTION_MODE);
            if (wasInActionModeOnPause) {
                /**
                 *  Needed on screen rotation only as listview is apparently reset and so is the
                 *  choice mode but not when app is put in background.
                 *  setChoiceMode() destroys the action mode, and we obviously do want that..
                 */
                if (mWifiTogglerWifiList.getChoiceMode() != AbsListView
                        .CHOICE_MODE_MULTIPLE_MODAL) {
                    mWifiTogglerWifiList.setChoiceMode(choiceMode);
                }
                ArrayList<Integer> checkedItems = mSavedInstanceState.getIntegerArrayList
                        (INSTANCE_KEY_CHECKED_ITEMS);
                setCachedItemsChecked(checkedItems);
            }

            int firstVisiblePosition = mSavedInstanceState.getInt
                    (INSTANCE_KEY_FIRST_VISIBLE_POSITION);
            int offsetTop = mSavedInstanceState.getInt(INSTANCE_KEY_OFFSET_FROM_TOP);
            // restore index and position
            mWifiTogglerWifiList.setSelectionFromTop(firstVisiblePosition, offsetTop);
        }
    }

    private void setCachedItemsChecked(ArrayList<Integer> checkedItems) {
        Log.d(TAG, "setCachedItemsChecked checkedItems=" + checkedItems);
        if (checkedItems == null || checkedItems.isEmpty()) {
            Log.d(TAG, "No Items to check");
            return;
        }
        for (int i = 0; i < checkedItems.size(); i++) {
            int position = checkedItems.get(i);
            mWifiTogglerWifiList.setItemChecked(position, true);
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

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceivers();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_configured_wifi_list, menu);
        return true;
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
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.d(TAG, "onCheckedChanged");
        switch (buttonView.getId()) {
            case R.id.wifi_toggler_activation_switch:
                handleSwitchLabelValue(isChecked);
                handleContentViewMessage(isChecked);
                handleSavedWifiListLoading(isChecked);
                handleNotification(isChecked);
                break;
        }

    }

    private void handleContentViewMessage(boolean isChecked) {
        if (isChecked) {
            if (mSavedWifiCursorAdapter.getCount() < 1) {
                mEmptyView.setText(R.string.wifi_list_info_no_known_wifi);
            }
        } else {
            mEmptyView.setText(R.string.wifi_list_info_when_wifi_toggler_off);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {SavedWifi._ID, SavedWifi.SSID, SavedWifi.STATUS, SavedWifi
                .AUTO_TOGGLE};
        CursorLoader cursorLoader = new CursorLoader(this,
                SavedWifi.CONTENT_URI, projection, null, null, null);
        return cursorLoader;
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

    private void initCursorLoader() {
        getLoaderManager().initLoader(0, null, this);
    }

    private void bindViews() {
        mWifiTogglerSwitchLabel = (TextView) findViewById(R.id.wifi_toggler_switch_label);
        mWifiTogglerActivationSwitch = (Switch) findViewById(R.id.wifi_toggler_activation_switch);
        mWifiTogglerWifiList = (ListView) findViewById(android.R.id.list);
        mEmptyView = (TextView) findViewById(android.R.id.empty);
        mWifiTogglerActivationSwitch.setOnCheckedChangeListener(this);
        mBannerContent = (TextView) findViewById(R.id.wifi_toggler_message_banner_content);
        mBannerContent.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        mBannerContent.setSingleLine(true);
        mBannerContent.setSelected(true);
        mBanner = (RelativeLayout) findViewById(R.id.wifi_toggler_message_banner);
        mBanner.setOnClickListener(this);
    }


    private CursorAdapter initCursorAdapter() {
        Log.d(TAG, "initCursorAdapter");
        if (mSavedWifiCursorAdapter == null) {
            mSavedWifiCursorAdapter = new SavedWifiListAdapter(this, null, 0);
        }
        return mSavedWifiCursorAdapter;
    }

    private void startupCheck() {
        int startupMode = StartupUtils.appStartMode(this);

        switch (startupMode) {
            case StartupUtils.FIRST_TIME:
            case StartupUtils.FIRST_TIME_FOR_VERSION:
                handleFirstLaunch();
                break;
            case StartupUtils.NORMAL:
                handleNormalStartup();
                break;
        }
    }


    private void handleFirstLaunch() {
        Log.d(TAG, "handleFirstLaunch");
        if (WifiToggler.hasWrongSettingsForFirstLaunch()) {
            launchStartupCheckActivity();
        } else {
            PrefUtils.markSettingsCorrectAtFirstLaunch(this);
            loadSavedWifiIntoDatabase();
        }
    }

    private void unregisterReceivers() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mNotificationActionsReceiver);
    }

    private void loadSavedWifiIntoDatabase() {
        Intent insertSavedWifi = new Intent(this, WifiTogglerService.class);
        insertSavedWifi.setAction(WifiTogglerService.ACTION_HANDLE_SAVED_WIFI_INSERT);
        startService(insertSavedWifi);
    }

    private void handleNormalStartup() {
        Log.d(TAG, "handleNormalStartup");
        if (!PrefUtils.wereSettingsCorrectAtFirstLaunch(this) && WifiToggler
                .hasWrongSettingsForFirstLaunch()) {
            launchStartupCheckActivity();
        } else {
            if (!PrefUtils.isSavedWifiInsertComplete(this)) {
                loadSavedWifiIntoDatabase();
            }
        }
    }

    private void launchStartupCheckActivity() {
        Log.d(TAG, "launchStartupCheckActivity");
        Intent startupCheck = new Intent(this, StartupSettingsCheckActivity.class);
        startActivity(startupCheck);
    }

    private void launchSystemSettingsCheckActivity() {
        Log.d(TAG, "launchSystemSettingsCheckActivity");
        Intent settingsCheck = new Intent(this, SystemSettingsCheckActivity.class);
        startActivity(settingsCheck);
    }

    private void handleNotification(boolean isChecked) {
        Log.d(TAG, "handleNotification=" + isChecked);
        if (isChecked) {
            buildWifiTogglerStateNotification(WifiTogglerService
                    .ACTION_HANDLE_ACTIVATE_WIFI_HANDLER);
        } else {
            buildWifiTogglerStateNotification(WifiTogglerService
                    .ACTION_HANDLE_PAUSE_WIFI_HANDLER);
        }
    }

    private void buildWifiTogglerStateNotification(String actionHandleActivateWifiHandler) {
        Log.d(TAG, "buildWifiTogglerStateNotification");
        Intent startForegroundNotificationIntent = new Intent(this, WifiTogglerService.class);
        startForegroundNotificationIntent.setAction(actionHandleActivateWifiHandler);
        startService(startForegroundNotificationIntent);
    }

    private void handleSwitchLabelValue(boolean isChecked) {
        Log.d(TAG, "handleSwitchLabelValue=" + isChecked);
        if (isChecked) {
            String on = getString(R.string.on_wifi_toggler_switch_label_value);
            mWifiTogglerSwitchLabel.setText(on);
        } else {
            String off = getString(R.string.off_wifi_toggler_switch_label_value);
            mWifiTogglerSwitchLabel.setText(off);
        }
    }

    private void handleSavedWifiListLoading(boolean isChecked) {
        Log.d(TAG, "handleSavedWifiListLoading=" + isChecked);
        if (isChecked) {
            mWifiTogglerWifiList.setAdapter(mSavedWifiCursorAdapter);
        } else {
            mWifiTogglerWifiList.setAdapter(null);
        }
    }

    private void displaySettingsActivity() {
        Intent preferencesIntent = new Intent(this, SettingsActivity.class);
        startActivity(preferencesIntent);
    }

    private void setListAdapterAccordingToSwitchState() {
        Log.d(TAG, "setListAdapterAccordingToSwitchState");
        if (PrefUtils.isWifiTogglerActive(this)) {
            mWifiTogglerActivationSwitch.setChecked(true);
            mWifiTogglerWifiList.setAdapter(mSavedWifiCursorAdapter);
        } else {
            mWifiTogglerActivationSwitch.setChecked(false);
        }
    }

    private void registerReceivers() {
        Log.d(TAG, "registerReceivers");
        registerNotificationReceiver();
    }

    private void handleBannerDisplay() {
        Log.d(TAG, "handleBannerDisplay");
        if (WifiToggler.hasWrongSettingsForAutoToggle()) {
            mBanner.setVisibility(View.VISIBLE);
        } else {
            mBanner.setVisibility(View.GONE);
        }
    }

    private void registerNotificationReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiTogglerService.ACTION_HANDLE_NOTIFICATION_ACTION_ACTIVATE);
        intentFilter.addAction(WifiTogglerService.ACTION_HANDLE_NOTIFICATION_ACTION_PAUSE);
        LocalBroadcastManager.getInstance(this).registerReceiver(mNotificationActionsReceiver,
                intentFilter);
    }

    private BroadcastReceiver mNotificationActionsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "mNotificationActionsReceiver Intent action=" + intent.getAction());
            if (WifiTogglerService.ACTION_HANDLE_NOTIFICATION_ACTION_ACTIVATE.equals(intent
                    .getAction())) {
                mWifiTogglerActivationSwitch.setChecked(true);
            } else if (WifiTogglerService.ACTION_HANDLE_NOTIFICATION_ACTION_PAUSE.equals(intent
                    .getAction())) {
                mWifiTogglerActivationSwitch.setChecked(false);
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.wifi_toggler_message_banner:
                launchSystemSettingsCheckActivity();
                break;
        }
    }

    @Override
    public void update(Observable observable, Object data) {
        handleBannerDisplay();
    }

    // TODO move to util class
    private void doSystemSettingsCheck() {
        Intent checkSettings = new Intent(this, WifiTogglerService.class);
        checkSettings.setAction(WifiTogglerService.ACTION_STARTUP_SETTINGS_PRECHECK);
        startService(checkSettings);
    }

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


    @Override
    public void onBatchInsertComplete(int token, Object cookie, ContentProviderResult[] results) {
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
    public void onUpdateComplete(int token, Object cookie, int result) {

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
}

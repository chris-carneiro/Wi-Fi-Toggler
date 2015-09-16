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
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
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
        LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener, Observer, AbsListView
        .OnItemLongClickListener, AbsListView.OnItemClickListener,
        AbsListView.MultiChoiceModeListener, ActionMode.Callback,
        DataAsyncQueryHandler.AsyncQueryListener {

    private static final String TAG = "SavedWifiList";

    private static final int TOKEN_UPDATE_BATCH = 6;

    private TextView mWifiTogglerSwitchLabel = null;
    private Switch mWifiTogglerActivationSwitch = null;
    private ListView mWifiTogglerWifiList = null;
    private TextView mEmptyView = null;
    private RelativeLayout mBanner = null;
    private TextView mBannerContent = null;
    private SavedWifiListAdapter mSavedWifiCursorAdapter = null;
    private ActionMode mActionMode = null;
    private DataAsyncQueryHandler mDataAsyncQueryHandler = null;
    //    private final ArrayList<Wifi> mCheckedItems = new ArrayList();

    private final SparseBooleanArray mCheckedItemsSpecs = new SparseBooleanArray();

    private static final String[] PROJECTION_SSID_AUTO_TOGGLE = new String[]{SavedWifi._ID,
            SavedWifi
                    .SSID, SavedWifi.AUTO_TOGGLE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Config.DEBUG_MODE) {
            StartupUtils.startStrictMode();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_wifi_list);
        mDataAsyncQueryHandler = new DataAsyncQueryHandler(getContentResolver(), this);
        bindViews();
        initCursorLoader();
        mSavedWifiCursorAdapter = (SavedWifiListAdapter) initCursorAdapter();
        mWifiTogglerWifiList.setOnItemLongClickListener(this);
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
        //TODO handle actionMode restore

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
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "onConfigurationChanged");
        boolean isChecked = PrefUtils.isWifiTogglerActive(this);
        handleSavedWifiListLoading(isChecked);
        //TODO remove workaround until actionmode restore is implemented
        if (mActionMode != null) {
            mActionMode.finish();
            mActionMode = null;
            mCheckedItemsSpecs.clear();
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

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        Cursor wifiCursor = mSavedWifiCursorAdapter.getCursor();
        if (checked) {
            wifiCursor.moveToPosition(position);
            boolean isAutoToggle = wifiCursor.getInt(wifiCursor.getColumnIndexOrThrow(SavedWifi
                    .AUTO_TOGGLE)) > 0;
            mCheckedItemsSpecs.put((int) id, !isAutoToggle);
        } else {
            mCheckedItemsSpecs.delete((int) id);
        }
        mActionMode.setTitle(mCheckedItemsSpecs.size() + "");
        Log.d(TAG, "mCheckedItemsSpecs=" + mCheckedItemsSpecs);
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mActionMode = mode;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_saved_wifi_action_mode, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_reverse_auto_toggle:
                ArrayList<Wifi> wifiToUpdate = prepareWifiBatchUpdate();
                udpateBatchWifiToggleState(wifiToUpdate);
                break;
        }

        mActionMode.finish();
        return true;
    }

    @NonNull
    private ArrayList<Wifi> prepareWifiBatchUpdate() {
        ArrayList<Wifi> wifiToUpdate = new ArrayList<>(mCheckedItemsSpecs.size());
        for (int i = 0; i < mCheckedItemsSpecs.size(); i++) {
            Wifi wifi = new Wifi.WifiBuilder().id(mCheckedItemsSpecs.keyAt(i)).autoToggle
                    (mCheckedItemsSpecs.valueAt(i)).build();
            wifiToUpdate.add(wifi);
        }
        return wifiToUpdate;
    }

    private void udpateBatchWifiToggleState(List<Wifi> wifis) {
        ArrayList<ContentProviderOperation> operations = (ArrayList<ContentProviderOperation>)
                SavedWifi.buildBatchUpdateAutoToggle
                        (wifis);

        mDataAsyncQueryHandler.startBatchOperations(TOKEN_UPDATE_BATCH, null, WifiTogglerContract
                .AUTHORITY, operations);
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        resetCheckedItemsCache();
        mActionMode = null;
    }

    private void resetCheckedItemsCache() {
        mCheckedItemsSpecs.clear();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "onItemLONGClick position=" + position);
        mWifiTogglerWifiList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mWifiTogglerWifiList.setMultiChoiceModeListener(this);
        mWifiTogglerWifiList.setOnItemClickListener(this);
        mWifiTogglerWifiList.setItemChecked(position, true);
        return true;
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

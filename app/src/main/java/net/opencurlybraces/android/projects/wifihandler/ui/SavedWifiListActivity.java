package net.opencurlybraces.android.projects.wifihandler.ui;

import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import net.opencurlybraces.android.projects.wifihandler.Config;
import net.opencurlybraces.android.projects.wifihandler.R;
import net.opencurlybraces.android.projects.wifihandler.WifiHandler;
import net.opencurlybraces.android.projects.wifihandler.data.table.SavedWifi;
import net.opencurlybraces.android.projects.wifihandler.service.WifiHandlerService;
import net.opencurlybraces.android.projects.wifihandler.util.PrefUtils;
import net.opencurlybraces.android.projects.wifihandler.util.StartupUtils;

import java.util.Observable;
import java.util.Observer;

public class SavedWifiListActivity extends AppCompatActivity implements
        CompoundButton.OnCheckedChangeListener,
        LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener, Observer {

    private static final String TAG = "SavedWifiList";

    private TextView mWifiHandlerSwitchLabel = null;
    private Switch mWifiHandlerActivationSwitch = null;
    private ListView mWifiHandlerWifiList = null;
    private TextView mEmptyView = null;
    private RelativeLayout mBanner = null;
    private CursorAdapter mSavedWifiCursorAdapter = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Config.DEBUG_MODE) {
            StartupUtils.startStrictMode();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_wifi_list);

        bindViews();
        initCursorLoader();
        mSavedWifiCursorAdapter = initCursorAdapter();

    }

    @Override
    protected void onStart() {
        super.onStart();
        WifiHandler.registerSettingObserver(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        WifiHandler.unRegisterSettingObserver(this);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();

        startupCheck();
        //        handleNormalStartup();
        handleNotification(mWifiHandlerActivationSwitch.isChecked());
        setListAdapterAccordingToSwitchState();
        registerReceivers();
        handleBannerDisplay();
        doSystemSettingsCheck();
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
            case R.id.wifi_handler_activation_switch:
                handleSwitchLabelValue(isChecked);
                handleSavedWifiListLoading(isChecked);
                handleNotification(isChecked);
                break;
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "onConfigurationChanged");
        boolean isChecked = PrefUtils.isWifiHandlerActive(this);
        handleSavedWifiListLoading(isChecked);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {SavedWifi._ID, SavedWifi.SSID, SavedWifi.STATUS};
        CursorLoader cursorLoader = new CursorLoader(this,
                SavedWifi.CONTENT_URI, projection, null, null, null);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mSavedWifiCursorAdapter.swapCursor(data);
        mWifiHandlerWifiList.setEmptyView(mEmptyView);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mSavedWifiCursorAdapter.swapCursor(null);
    }

    private void initCursorLoader() {
        getLoaderManager().initLoader(0, null, this);
    }

    private void bindViews() {
        mWifiHandlerSwitchLabel = (TextView) findViewById(R.id.wifi_handler_switch_label);
        mWifiHandlerActivationSwitch = (Switch) findViewById(R.id.wifi_handler_activation_switch);
        mWifiHandlerWifiList = (ListView) findViewById(android.R.id.list);
        mEmptyView = (TextView) findViewById(android.R.id.empty);
        mWifiHandlerActivationSwitch.setOnCheckedChangeListener(this);
        mBanner = (RelativeLayout) findViewById(R.id.wifi_handler_message_banner);
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
        //        handleNotification(mWifiHandlerActivationSwitch.isChecked());
    }


    private void handleFirstLaunch() {
        Log.d(TAG, "handleFirstLaunch");
        if (WifiHandler.hasWrongSettingsForFirstLaunch()) {
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
        Intent insertSavedWifi = new Intent(this, WifiHandlerService.class);
        insertSavedWifi.setAction(WifiHandlerService.ACTION_HANDLE_SAVED_WIFI_INSERT);
        startService(insertSavedWifi);
    }

    private void handleNormalStartup() {
        Log.d(TAG, "handleNormalStartup");
        if (!PrefUtils.wereSettingsCorrectAtFirstLaunch(this) && WifiHandler
                .hasWrongSettingsForFirstLaunch()) {
            //            mWifiHandlerActivationSwitch.setChecked(false);
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
            buildWifiHandlerStateNotification(WifiHandlerService
                    .ACTION_HANDLE_ACTIVATE_WIFI_HANDLER);
        } else {
            buildWifiHandlerStateNotification(WifiHandlerService
                    .ACTION_HANDLE_PAUSE_WIFI_HANDLER);
        }
    }

    private void buildWifiHandlerStateNotification(String actionHandleActivateWifiHandler) {
        Log.d(TAG, "buildWifiHandlerStateNotification");
        Intent startForegroundNotificationIntent = new Intent(this, WifiHandlerService.class);
        startForegroundNotificationIntent.setAction(actionHandleActivateWifiHandler);
        startService(startForegroundNotificationIntent);
    }

    private void handleSwitchLabelValue(boolean isChecked) {
        Log.d(TAG, "handleSwitchLabelValue=" + isChecked);
        if (isChecked) {
            String on = getString(R.string.on_wifi_handler_switch_label_value);
            mWifiHandlerSwitchLabel.setText(on);
        } else {
            String off = getString(R.string.off_wifi_handler_switch_label_value);
            mWifiHandlerSwitchLabel.setText(off);
        }
    }

    private void handleSavedWifiListLoading(boolean isChecked) {
        Log.d(TAG, "handleSavedWifiListLoading=" + isChecked);
        if (isChecked) {
            mWifiHandlerWifiList.setAdapter(mSavedWifiCursorAdapter);
        } else {
            mWifiHandlerWifiList.setAdapter(null);
        }
    }

    private void displaySettingsActivity() {
        Intent preferencesIntent = new Intent(this, SettingsActivity.class);
        startActivity(preferencesIntent);
    }

    private void setListAdapterAccordingToSwitchState() {
        Log.d(TAG, "setListAdapterAccordingToSwitchState");
        if (PrefUtils.isWifiHandlerActive(this)) {
            mWifiHandlerActivationSwitch.setChecked(true);
            mWifiHandlerWifiList.setAdapter(mSavedWifiCursorAdapter);
        } else {
            mWifiHandlerActivationSwitch.setChecked(false);
        }
    }

    private void registerReceivers() {
        Log.d(TAG, "registerReceivers");
        registerNotificationReceiver();
    }

    private void handleBannerDisplay() {
        Log.d(TAG, "handleBannerDisplay");
        if (WifiHandler.hasWrongSettingsForAutoToggle()) {
            mBanner.setVisibility(View.VISIBLE);
        } else {
            mBanner.setVisibility(View.GONE);
        }
    }

    private void registerNotificationReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiHandlerService.ACTION_HANDLE_NOTIFICATION_ACTION_ACTIVATE);
        intentFilter.addAction(WifiHandlerService.ACTION_HANDLE_NOTIFICATION_ACTION_PAUSE);
        LocalBroadcastManager.getInstance(this).registerReceiver(mNotificationActionsReceiver,
                intentFilter);
    }

    private BroadcastReceiver mNotificationActionsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "mNotificationActionsReceiver Intent action=" + intent.getAction());
            if (WifiHandlerService.ACTION_HANDLE_NOTIFICATION_ACTION_ACTIVATE.equals(intent
                    .getAction())) {
                mWifiHandlerActivationSwitch.setChecked(true);
            } else if (WifiHandlerService.ACTION_HANDLE_NOTIFICATION_ACTION_PAUSE.equals(intent
                    .getAction())) {
                mWifiHandlerActivationSwitch.setChecked(false);
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.wifi_handler_message_banner:
                launchSystemSettingsCheckActivity();
                break;
        }
    }

    @Override
    public void update(Observable observable, Object data) {
        handleBannerDisplay();
    }


    private void doSystemSettingsCheck() {
        Intent checkSettings = new Intent(this, WifiHandlerService.class);
        checkSettings.setAction(WifiHandlerService.ACTION_STARTUP_SETTINGS_PRECHECK);
        startService(checkSettings);
    }
}

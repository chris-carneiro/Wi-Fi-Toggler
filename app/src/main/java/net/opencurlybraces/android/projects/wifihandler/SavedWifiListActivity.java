package net.opencurlybraces.android.projects.wifihandler;

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
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import net.opencurlybraces.android.projects.wifihandler.data.table.SavedWifi;
import net.opencurlybraces.android.projects.wifihandler.service.ContentIntentService;
import net.opencurlybraces.android.projects.wifihandler.service.WifiHandlerService;
import net.opencurlybraces.android.projects.wifihandler.util.PrefUtils;
import net.opencurlybraces.android.projects.wifihandler.util.StartupUtils;


public class SavedWifiListActivity extends AppCompatActivity implements
        CompoundButton.OnCheckedChangeListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "SavedWifiList";

    private TextView mWifiHandlerSwitchLabel = null;
    private Switch mWifiHandlerActivationSwitch = null;
    private ListView mWifiHandlerWifiList = null;
    private TextView mEmptyView = null;

    private CursorAdapter mSavedWifiCursorAdapter = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configured_wifi_list);

        mWifiHandlerSwitchLabel = (TextView) findViewById(R.id.wifi_handler_switch_label);
        mWifiHandlerActivationSwitch = (Switch) findViewById(R.id.wifi_handler_activation_switch);
        mWifiHandlerWifiList = (ListView) findViewById(android.R.id.list);
        mEmptyView = (TextView) findViewById(android.R.id.empty);
        mWifiHandlerActivationSwitch.setOnCheckedChangeListener(this);

        startupCheck();

    }


    private BroadcastReceiver mNotificationActionsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Intent=" + intent.getAction());
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
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume isactive=" + PrefUtils.isWifiHandlerActive(this));
        initLoader();
        mSavedWifiCursorAdapter = initAdapter();
        if (PrefUtils.isWifiHandlerActive(this)) {
            mWifiHandlerActivationSwitch.setChecked(true);
            mWifiHandlerWifiList.setAdapter(mSavedWifiCursorAdapter);
        } else {
            mWifiHandlerActivationSwitch.setChecked(false);
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiHandlerService.ACTION_HANDLE_NOTIFICATION_ACTION_ACTIVATE);
        intentFilter.addAction(WifiHandlerService.ACTION_HANDLE_NOTIFICATION_ACTION_PAUSE);

        LocalBroadcastManager.getInstance(this).registerReceiver(mNotificationActionsReceiver,
                intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mNotificationActionsReceiver);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
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


    private void handleNotification(boolean isChecked) {
        if (isChecked) {
            Intent startForegroundNotificationIntent = new Intent(this, WifiHandlerService.class);
            startForegroundNotificationIntent.setAction(WifiHandlerService
                    .ACTION_HANDLE_ACTIVATE_WIFI_HANDLER);
            startService(startForegroundNotificationIntent);
        } else {
            Intent dismissableNotificationIntent = new Intent(this, WifiHandlerService.class);
            dismissableNotificationIntent.setAction(WifiHandlerService
                    .ACTION_HANDLE_PAUSE_WIFI_HANDLER);
            startService(dismissableNotificationIntent);
        }

    }

    private void handleSwitchLabelValue(boolean isChecked) {
        if (isChecked) {
            String on = getString(R.string.on_wifi_handler_switch_label_value);
            mWifiHandlerSwitchLabel.setText(on);
        } else {
            String off = getString(R.string.off_wifi_handler_switch_label_value);
            mWifiHandlerSwitchLabel.setText(off);
        }
    }


    private void handleSavedWifiListLoading(boolean isChecked) {
        if (isChecked) {
            mWifiHandlerWifiList.setAdapter(mSavedWifiCursorAdapter);
        } else {
            mWifiHandlerWifiList.setAdapter(null);
        }
    }


    private void loadSavedWifiIntoDatabase() {
        Intent handleSavedWifiInsert = new Intent(this, ContentIntentService.class);
        handleSavedWifiInsert.setAction(ContentIntentService.ACTION_HANDLE_SAVED_WIFI_INSERT);
        this.startService(handleSavedWifiInsert);
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

    private void initLoader() {
        getLoaderManager().initLoader(0, null, this);
    }

    private CursorAdapter initAdapter() {
        Log.d(TAG, "initAdapter");
        // Must include the _id column for the adapter to work
        String[] from = new String[]{SavedWifi.SSID, SavedWifi.STATUS};
        // Fields on the UI to which we map
        int[] to = new int[]{R.id.configured_wifi_ssid, R.id.configured_wifi_state};

        if (mSavedWifiCursorAdapter == null) {
            mSavedWifiCursorAdapter = new SavedWifiListAdapter(this, null, 0);
        }
        return mSavedWifiCursorAdapter;
    }


    private void startupCheck() {
        int startupMode = StartupUtils.appStartMode(this);
        switch (startupMode) {
            case StartupUtils.FIRST_TIME:
                Log.d(TAG, "Startup mode: FIRST_TIME");
            case StartupUtils.FIRST_TIME_FOR_VERSION:
                Log.d(TAG, "Startup mode: FIRST_TIME_FOR_VERSION");
                loadSavedWifiIntoDatabase();
                break;
            case StartupUtils.NORMAL:
                Log.d(TAG, "Startup mode: NORMAL");
                break;
        }
    }


}

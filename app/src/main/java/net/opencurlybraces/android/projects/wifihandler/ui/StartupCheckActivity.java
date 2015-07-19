package net.opencurlybraces.android.projects.wifihandler.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import net.opencurlybraces.android.projects.wifihandler.Config;
import net.opencurlybraces.android.projects.wifihandler.R;
import net.opencurlybraces.android.projects.wifihandler.service.WifiHandlerService;
import net.opencurlybraces.android.projects.wifihandler.util.NetworkUtils;
import net.opencurlybraces.android.projects.wifihandler.util.PrefUtils;

import java.util.concurrent.ConcurrentHashMap;


public class StartupCheckActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "StartupCheckActivity";

    public static final String HOTSPOT_STATE_CHANGE_ACTION = "android.net.wifi" +
            ".WIFI_AP_STATE_CHANGED";

    private static final String TETHER_SETTINGS_ACTION = "com.android.settings.TetherSettings";
    private static final String TETHER_SETTINGS_CLASSNAME = "com.android.settings";

    RelativeLayout mScanCheckLayout = null;
    RelativeLayout mWifiCheckLayout = null;
    RelativeLayout mAirplaneCheckLayout = null;
    RelativeLayout mHotspotCheckLayout = null;
    Button mContinueButton = null;

    private ConcurrentHashMap<String, Boolean> mSettingsStateCache = new ConcurrentHashMap<>(Config
            .STARTUP_SETTINGS_CHECKS);
    private ImageView wifiNextIcon;
    private ImageView airplaneNextIcon;
    private ImageView hotspotNextIcon;
    private ImageView mScanNextIcon;
    private static final int REQUEST_CODE_SCAN_ALWAYS_AVAILABLE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup_check);
        mScanCheckLayout = (RelativeLayout) findViewById(R.id
                .startup_check_scan_always_available_layout);
        mWifiCheckLayout = (RelativeLayout) findViewById(R.id
                .startup_check_wifi_settings_layout);
        mAirplaneCheckLayout = (RelativeLayout) findViewById(R.id
                .startup_check_airplane_settings_layout);
        mHotspotCheckLayout = (RelativeLayout) findViewById(R.id
                .startup_check_hotspot_settings_layout);
        mContinueButton = (Button) findViewById(R.id.startup_check_settings_continue_button);

        wifiNextIcon = (ImageView) mWifiCheckLayout.findViewById(R.id
                .startup_check_wifi_settings_next_ic);
        airplaneNextIcon = (ImageView) mAirplaneCheckLayout.findViewById(R.id
                .startup_check_airplane_settings_next_ic);
        hotspotNextIcon = (ImageView) mHotspotCheckLayout.findViewById(R.id
                .startup_check_hotspot_settings_next_ic);
        mScanNextIcon = (ImageView) mScanCheckLayout.findViewById(R.id
                .startup_check_scan_always_available_next_ic);
    }


    @Override
    protected void onResume() {
        super.onResume();

        setLayoutAccordingToSettings();
        registerReceivers();

    }

    private void setContinueButtonListenerAccordingToSettings() {
        if (mSettingsStateCache.containsValue(false)) {
            mContinueButton.setOnClickListener(null);
            mContinueButton.setEnabled(false);
        } else {
            mContinueButton.setOnClickListener(this);
            mContinueButton.setEnabled(true);
        }
    }

    private void setLayoutAccordingToSettings() {
        setScanLayoutAccordingToSettings();
        setWifiLayoutAccordingToSettings();
        setAirplaneLayoutAccordingToSettings();
        setHotspotLayoutAccordingToSettings();
        setContinueButtonListenerAccordingToSettings();
    }

    private void registerReceivers() {
        registerAirplaneReceiver();
        registerWifiStateReceiver();
        registerHotspotStateReceiver();
        registerFinishActivityReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceivers();
    }

    private void unregisterReceivers() {
        unregisterReceiver(mAirplaneModeReceiver);
        unregisterReceiver(mWifiStateReceiver);
        unregisterReceiver(mHotspotStateReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mFinishStartupCheckReceiver);
    }

    private void registerAirplaneReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        registerReceiver(mAirplaneModeReceiver, intentFilter);
    }

    private void registerWifiStateReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(mWifiStateReceiver, intentFilter);
    }

    private void registerHotspotStateReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(HOTSPOT_STATE_CHANGE_ACTION);
        registerReceiver(mHotspotStateReceiver, intentFilter);
    }

    private void registerFinishActivityReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiHandlerService.ACTION_FINISH_STARTUP_CHECK_ACTIVITY);
        LocalBroadcastManager.getInstance(this).registerReceiver(mFinishStartupCheckReceiver,
                intentFilter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_startup_check, menu);
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

    private void setWifiLayoutAccordingToSettings() {
        Log.d(TAG, "setWifiLayoutAccordingToSettings");

        if (!NetworkUtils.isWifiEnabled(this)) {

            displayCheckWifiSettingsLayout();

            cacheSettingsState(Config.STARTUP_CHECK_WIFI_SETTINGS, false);

        } else {
            displayWifiSettingsCorrectLayout();
            cacheSettingsState(Config.STARTUP_CHECK_WIFI_SETTINGS, true);
        }

        setContinueButtonListenerAccordingToSettings();
    }

    private void displayWifiSettingsCorrectLayout() {
        Log.d(TAG, "displayWifiSettingsCorrectLayout");

        mWifiCheckLayout.setBackgroundResource(R.drawable
                .stroke_rectangle_shape_teal_lighter);
        wifiNextIcon.setVisibility(View.GONE);
        mWifiCheckLayout.setOnClickListener(null);
    }

    private void displayCheckWifiSettingsLayout() {
        Log.d(TAG, "displayCheckWifiSettingsLayout");
        mWifiCheckLayout.setBackgroundResource(R.drawable
                .startup_check_settings_textview_warning_selector);
        wifiNextIcon.setVisibility(View.VISIBLE);
        mWifiCheckLayout.setOnClickListener(this);
    }

    private void setAirplaneLayoutAccordingToSettings() {
        Log.d(TAG, "setAirplaneLayoutAccordingToSettings");

        if (NetworkUtils.isAirplaneModeEnabled(this)) {
            displayCheckAirplaneSettingsLayout();
            cacheSettingsState(Config.STARTUP_CHECK_AIRPLANE_SETTINGS, false);

        } else {
            displayAirplaneSettingsCorrectLayout();
            cacheSettingsState(Config.STARTUP_CHECK_AIRPLANE_SETTINGS, true);
        }

        setContinueButtonListenerAccordingToSettings();
    }

    private void displayAirplaneSettingsCorrectLayout() {
        Log.d(TAG, "displayAirplaneSettingsCorrectLayout");
        mAirplaneCheckLayout.setBackgroundResource(R.drawable
                .stroke_rectangle_shape_teal_lighter);
        airplaneNextIcon.setVisibility(View.GONE);
        mAirplaneCheckLayout.setOnClickListener(null);
    }

    private void displayCheckAirplaneSettingsLayout() {
        Log.d(TAG, "displayCheckAirplaneSettingsLayout");
        mAirplaneCheckLayout.setBackgroundResource(R.drawable
                .startup_check_settings_textview_warning_selector);
        airplaneNextIcon.setVisibility(View.VISIBLE);
        mAirplaneCheckLayout.setOnClickListener(this);
    }

    private void setHotspotLayoutAccordingToSettings() {
        Log.d(TAG, "setHotspotLayoutAccordingToSettings");
        if (NetworkUtils.isHotspotEnabled(this)) {
            displayCheckHotspotSettingsLayout();

            cacheSettingsState(Config.STARTUP_CHECK_HOTSTOP_SETTINGS, false);
        } else {
            displayHotspotSettingsCorrectLayout();
            cacheSettingsState(Config.STARTUP_CHECK_HOTSTOP_SETTINGS, true);
        }

        setContinueButtonListenerAccordingToSettings();
    }

    private void displayHotspotSettingsCorrectLayout() {
        Log.d(TAG, "displayHotspotSettingsCorrectLayout");
        mHotspotCheckLayout.setBackgroundResource(R.drawable
                .stroke_rectangle_shape_teal_lighter);
        hotspotNextIcon.setVisibility(View.GONE);
        mHotspotCheckLayout.setOnClickListener(null);
    }

    private void displayCheckHotspotSettingsLayout() {
        Log.d(TAG, "displayCheckHotspotSettingsLayout");
        mHotspotCheckLayout.setBackgroundResource(R.drawable
                .startup_check_settings_textview_warning_selector);
        hotspotNextIcon.setVisibility(View.VISIBLE);
        mHotspotCheckLayout.setOnClickListener(this);
    }

    private void setScanLayoutAccordingToSettings() {
        Log.d(TAG, "setScanLayoutAccordingToSettings");

        if (NetworkUtils.isScanAlwaysAvailable(this)) {

            displayScanSettingsCorrectLayout();
            cacheSettingsState(Config.STARTUP_CHECK_SCAN_ALWAYS_AVAILABLE_SETTINGS, true);
        } else {
            displayCheckScanSettingsLayout();
            cacheSettingsState(Config.STARTUP_CHECK_SCAN_ALWAYS_AVAILABLE_SETTINGS,
                    false);
        }

        setContinueButtonListenerAccordingToSettings();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CODE_SCAN_ALWAYS_AVAILABLE:
                Log.d(TAG, "resultCode" + resultCode);

                if (resultCode == RESULT_OK) {
                    displayScanSettingsCorrectLayout();
                } else {
                    displayCheckScanSettingsLayout();
                }

                cacheSettingsState(Config.STARTUP_CHECK_SCAN_ALWAYS_AVAILABLE_SETTINGS,
                        resultCode == RESULT_OK);

                PrefUtils.setScanAlwaysAvailableBeenEnabled(this, resultCode == RESULT_OK);
                setContinueButtonListenerAccordingToSettings();
                break;
        }
    }

    private void cacheSettingsState(String key, boolean correct) {
        Log.d(TAG, "cacheSettingsState");
        mSettingsStateCache.put(key, correct);
    }

    private void displayCheckScanSettingsLayout() {
        Log.d(TAG, "displayCheckScanSettingsLayout");

        mScanCheckLayout.setBackgroundResource(R.drawable
                .startup_check_settings_textview_warning_selector);
        mScanNextIcon.setVisibility(View.VISIBLE);
        mScanCheckLayout.setOnClickListener(this);
    }

    private void displayScanSettingsCorrectLayout() {
        Log.d(TAG, "displayScanSettingsCorrectLayout");

        mScanCheckLayout.setBackgroundResource(R.drawable
                .stroke_rectangle_shape_teal_lighter);
        mScanNextIcon.setVisibility(View.GONE);
        mScanCheckLayout.setOnClickListener(null);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.startup_check_scan_always_available_layout:
                Intent enableScanAvailable = new Intent(WifiManager
                        .ACTION_REQUEST_SCAN_ALWAYS_AVAILABLE);
                startActivityForResult(enableScanAvailable, REQUEST_CODE_SCAN_ALWAYS_AVAILABLE);
                break;
            case R.id.startup_check_wifi_settings_layout:
                Intent enableWifi = new Intent(Settings.ACTION_WIFI_SETTINGS);
                startActivity(enableWifi);
                break;
            case R.id.startup_check_airplane_settings_layout:
                Intent disableAirplane = new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS);
                startActivity(disableAirplane);
                break;
            case R.id.startup_check_hotspot_settings_layout:
                Intent tetherSettings = new Intent();
                tetherSettings.setClassName(TETHER_SETTINGS_CLASSNAME, TETHER_SETTINGS_ACTION);
                startActivity(tetherSettings);
                break;
            case R.id.startup_check_settings_continue_button:
                loadSavedWifiIntoDatabase();
                PrefUtils.markSettingsCorrectAtFirstLaunch(this);
                //                finish(); //Calls onActivityResult of SavedWIfiListActivity
                break;
        }
    }

    private BroadcastReceiver mAirplaneModeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setAirplaneLayoutAccordingToSettings();
        }
    };

    private BroadcastReceiver mWifiStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setWifiLayoutAccordingToSettings();
        }
    };


    private BroadcastReceiver mHotspotStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setHotspotLayoutAccordingToSettings();
        }
    };


    private BroadcastReceiver mFinishStartupCheckReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Intent=" + intent.getAction());
            if (WifiHandlerService.ACTION_FINISH_STARTUP_CHECK_ACTIVITY.equals(intent
                    .getAction())) {
                StartupCheckActivity.this.finish();
            }
        }
    };

    private void loadSavedWifiIntoDatabase() {
        Intent handleSavedWifiInsert = new Intent(this, WifiHandlerService.class);
        handleSavedWifiInsert.setAction(WifiHandlerService.ACTION_HANDLE_SAVED_WIFI_INSERT);
        startService(handleSavedWifiInsert);
    }
}

package net.opencurlybraces.android.projects.wifihandler.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
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

import java.util.HashMap;


public class StartupCheckActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "StartupCheckActivity";

    public static final String HOTSPOT_STATE_CHANGE_ACTION = "android.net.wifi" +
            ".WIFI_AP_STATE_CHANGED";


    RelativeLayout mScanCheckLayout = null;
    RelativeLayout mWifiCheckLayout = null;
    RelativeLayout mAirplaneCheckLayout = null;
    RelativeLayout mHotspotCheckLayout = null;
    Button mContinueButton = null;

    private HashMap<String, Boolean> mSettingsStateCorrect = new HashMap<>(Config
            .STARTUP_SETTINGS_CHECKS);


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

    }


    @Override
    protected void onResume() {
        super.onResume();

        setLayoutAccordingToSettings();
        registerReceivers();
        setContinueButtonListenerAccordingToSettings();
    }

    private void setContinueButtonListenerAccordingToSettings() {
        if (mSettingsStateCorrect.containsValue(false)) {
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
    }

    private void registerReceivers() {
        registerAirplaneReceiver();
        registerWifiStateReceiver();
        registerHotspotStateReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mAirplaneModeReceiver);
        unregisterReceiver(mWifiStateReceiver);
        unregisterReceiver(mHotspotStateReceiver);
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
        final ImageView nextIcon = (ImageView) mWifiCheckLayout.findViewById(R.id
                .startup_check_wifi_settings_next_ic);
        if (!NetworkUtils.isWifiEnabled(this)) {
            mWifiCheckLayout.setBackgroundResource(R.drawable
                    .startup_check_settings_textview_warning_selector);
            nextIcon.setVisibility(View.VISIBLE);
            mWifiCheckLayout.setOnClickListener(this);
            mSettingsStateCorrect.put(Config.STARTUP_CHECK_WIFI_SETTINGS, false);
            return;
        }
        mWifiCheckLayout.setBackgroundResource(R.drawable
                .stroke_rectangle_shape_teal_lighter);
        nextIcon.setVisibility(View.GONE);
        mWifiCheckLayout.setOnClickListener(null);
        mSettingsStateCorrect.put(Config.STARTUP_CHECK_WIFI_SETTINGS, true);

        setContinueButtonListenerAccordingToSettings();
    }

    private void setAirplaneLayoutAccordingToSettings() {
        final ImageView nextIcon = (ImageView) mAirplaneCheckLayout.findViewById(R.id
                .startup_check_airplane_settings_next_ic);
        if (NetworkUtils.isAirplaneModeOn(this)) {
            mAirplaneCheckLayout.setBackgroundResource(R.drawable
                    .startup_check_settings_textview_warning_selector);
            nextIcon.setVisibility(View.VISIBLE);
            mAirplaneCheckLayout.setOnClickListener(this);
            mSettingsStateCorrect.put(Config.STARTUP_CHECK_AIRPLANE_SETTINGS, false);
            return;
        }
        mAirplaneCheckLayout.setBackgroundResource(R.drawable
                .stroke_rectangle_shape_teal_lighter);
        nextIcon.setVisibility(View.GONE);
        mAirplaneCheckLayout.setOnClickListener(null);
        mSettingsStateCorrect.put(Config.STARTUP_CHECK_AIRPLANE_SETTINGS, true);

        setContinueButtonListenerAccordingToSettings();
    }

    private void setHotspotLayoutAccordingToSettings() {
        final ImageView nextIcon = (ImageView) mHotspotCheckLayout.findViewById(R.id
                .startup_check_hotspot_settings_next_ic);
        if (NetworkUtils.isHotspotOn(this)) {
            mHotspotCheckLayout.setBackgroundResource(R.drawable
                    .startup_check_settings_textview_warning_selector);
            nextIcon.setVisibility(View.VISIBLE);
            mHotspotCheckLayout.setOnClickListener(this);
            mSettingsStateCorrect.put(Config.STARTUP_CHECK_HOTSTOP_SETTINGS, false);
            return;
        }
        mHotspotCheckLayout.setBackgroundResource(R.drawable
                .stroke_rectangle_shape_teal_lighter);
        nextIcon.setVisibility(View.GONE);
        mHotspotCheckLayout.setOnClickListener(null);
        mSettingsStateCorrect.put(Config.STARTUP_CHECK_HOTSTOP_SETTINGS, true);

        setContinueButtonListenerAccordingToSettings();
    }

    private void setScanLayoutAccordingToSettings() {
        final ImageView nextIcon = (ImageView) mScanCheckLayout.findViewById(R.id
                .startup_check_scan_always_available_next_ic);
        if (!NetworkUtils.isScanAlwaysAvailable(this)) {
            mScanCheckLayout.setBackgroundResource(R.drawable
                    .startup_check_settings_textview_warning_selector);
            nextIcon.setVisibility(View.VISIBLE);
            mScanCheckLayout.setOnClickListener(this);
            mSettingsStateCorrect.put(Config.STARTUP_CHECK_SCAN_ALWAYS_AVAILABLE_SETTINGS,
                    false);
            return;
        }
        mScanCheckLayout.setBackgroundResource(R.drawable
                .stroke_rectangle_shape_teal_lighter);
        nextIcon.setVisibility(View.GONE);
        mScanCheckLayout.setOnClickListener(null);
        mSettingsStateCorrect.put(Config.STARTUP_CHECK_SCAN_ALWAYS_AVAILABLE_SETTINGS, true);

        setContinueButtonListenerAccordingToSettings();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 1:
                Log.d(TAG, "resultCode" + resultCode);
                final ImageView nextIcon = (ImageView) mScanCheckLayout.findViewById(R.id
                        .startup_check_scan_always_available_next_ic);
                if (resultCode == RESULT_OK) {
                    mScanCheckLayout.setBackgroundResource(R.drawable
                            .stroke_rectangle_shape_teal_lighter);
                    nextIcon.setVisibility(View.GONE);
                    mScanCheckLayout.setOnClickListener(null);
                    mSettingsStateCorrect.put(Config
                            .STARTUP_CHECK_SCAN_ALWAYS_AVAILABLE_SETTINGS, true);


                } else {
                    mScanCheckLayout.setBackgroundResource(R.drawable
                            .startup_check_settings_textview_warning_selector);
                    nextIcon.setVisibility(View.VISIBLE);
                    mScanCheckLayout.setOnClickListener(this);
                    mSettingsStateCorrect.put(Config
                            .STARTUP_CHECK_SCAN_ALWAYS_AVAILABLE_SETTINGS, false);
                }
                PrefUtils.setScanAlwaysAvailableBeenEnabled(this, resultCode == RESULT_OK);
                setContinueButtonListenerAccordingToSettings();
                break;
        }
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);
        Log.d(TAG, "onActivityReenter");

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.startup_check_scan_always_available_layout:
                Log.d(TAG, "check your scan settings");
                Intent enableScanAvailable = new Intent(WifiManager
                        .ACTION_REQUEST_SCAN_ALWAYS_AVAILABLE);
                startActivityForResult(enableScanAvailable, 1);
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
                Intent disableHotspot = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                startActivity(disableHotspot);
                break;
            case R.id.startup_check_settings_continue_button:
                loadSavedWifiIntoDatabase();
                Intent startWifiHandler = new Intent(this, SavedWifiListActivity.class);
                startActivity(startWifiHandler);

                break;
        }
    }

    private BroadcastReceiver mAirplaneModeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //            boolean isAirplaneModeOn = intent.getBooleanExtra
            // (AirplaneModeStateReceiver
            //                    .EXTRAS_AIRPLANE_MODE_STATE, false);
            //
            //            final ImageView nextIcon = (ImageView) mAirplaneCheckLayout
            // .findViewById(R.id
            //                    .startup_check_airplane_settings_next_ic);
            //
            //            if (isAirplaneModeOn) {
            //                if (!NetworkUtils.isScanAlwaysAvailable(context)) {
            //                    mAirplaneCheckLayout.setBackgroundResource(R.drawable
            //                            .startup_check_settings_textview_warning_selector);
            //                    nextIcon.setVisibility(View.VISIBLE);
            //                    mAirplaneCheckLayout.setOnClickListener(StartupCheckActivity
            // .this);
            //                }
            //            } else {
            //                mAirplaneCheckLayout.setBackgroundResource(R.drawable
            //                        .stroke_rectangle_shape_teal_lighter);
            //                nextIcon.setVisibility(View.GONE);
            //                mAirplaneCheckLayout.setOnClickListener(null);
            //
            //            }
            setAirplaneLayoutAccordingToSettings();
        }
    };

    private BroadcastReceiver mWifiStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //            final ImageView nextIcon = (ImageView) mWifiCheckLayout.findViewById(R.id
            //                    .startup_check_wifi_settings_next_ic);

            //            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            //                int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
            //
            //                if (WifiManager.WIFI_STATE_DISABLED == wifiState) {
            //
            //                    if (!NetworkUtils.isWifiEnabled(context)) {
            //                        mWifiCheckLayout.setBackgroundResource(R.drawable
            //                                .startup_check_settings_textview_warning_selector);
            //                        nextIcon.setVisibility(View.VISIBLE);
            //                        mWifiCheckLayout.setOnClickListener(StartupCheckActivity
            // .this);
            //
            //                    }
            //                } else if (WifiManager.WIFI_STATE_ENABLED == wifiState) {
            //                    mWifiCheckLayout.setBackgroundResource(R.drawable
            //                            .stroke_rectangle_shape_teal_lighter);
            //                    nextIcon.setVisibility(View.GONE);
            //                    mWifiCheckLayout.setOnClickListener(null);
            //                }
            //            }
            setWifiLayoutAccordingToSettings();
        }
    };


    private BroadcastReceiver mHotspotStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            //            final ImageView nextIcon = (ImageView) mHotspotCheckLayout.findViewById
            // (R.id
            //                    .startup_check_airplane_settings_next_ic);
            //            if (NetworkUtils.isHotspotOn(context)) {
            //                mHotspotCheckLayout.setBackgroundResource(R.drawable
            //                        .startup_check_settings_textview_warning_selector);
            //                nextIcon.setVisibility(View.VISIBLE);
            //                mHotspotCheckLayout.setOnClickListener(StartupCheckActivity.this);
            //
            //            } else {
            //                mHotspotCheckLayout.setBackgroundResource(R.drawable
            //                        .stroke_rectangle_shape_teal_lighter);
            //                nextIcon.setVisibility(View.GONE);
            //                mHotspotCheckLayout.setOnClickListener(null);
            //            }
            setHotspotLayoutAccordingToSettings();
        }
    };


    private void loadSavedWifiIntoDatabase() {
        Intent handleSavedWifiInsert = new Intent(this, WifiHandlerService.class);
        handleSavedWifiInsert.setAction(WifiHandlerService.ACTION_HANDLE_SAVED_WIFI_INSERT);
        startService(handleSavedWifiInsert);
    }
}

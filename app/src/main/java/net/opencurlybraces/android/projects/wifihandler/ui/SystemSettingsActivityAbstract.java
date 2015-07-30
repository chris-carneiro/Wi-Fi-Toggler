package net.opencurlybraces.android.projects.wifihandler.ui;

import android.content.Intent;
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
import net.opencurlybraces.android.projects.wifihandler.WifiHandler;
import net.opencurlybraces.android.projects.wifihandler.service.WifiHandlerService;
import net.opencurlybraces.android.projects.wifihandler.util.CheckPassiveScanHandler;
import net.opencurlybraces.android.projects.wifihandler.util.StartupUtils;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by chris on 24/07/15.
 */
public abstract class SystemSettingsActivityAbstract extends AppCompatActivity implements View
        .OnClickListener, Observer {

    private static final String TAG = "SystemSettingsAbstract";

    protected static final int TICK_WHAT = 2;

    protected RelativeLayout mScanCheckLayout = null;
    protected RelativeLayout mAirplaneCheckLayout = null;
    protected RelativeLayout mHotspotCheckLayout = null;
    protected RelativeLayout mWifiCheckLayout = null;

    protected ImageView mWifiNextIcon;
    protected ImageView mAirplaneNextIcon;
    protected ImageView mHotspotNextIcon;
    protected ImageView mScanNextIcon;

    protected Button mContinueButton = null;

    private static final int REQUEST_CODE_SCAN_ALWAYS_AVAILABLE = 1;
    private static final String TETHER_SETTINGS_ACTION = "com.android.settings.TetherSettings";
    private static final String TETHER_SETTINGS_CLASSNAME = "com.android.settings";

    protected final CheckPassiveScanHandler mCheckPassiveHandler = new CheckPassiveScanHandler
            (this, 2, Config.INTERVAL_CHECK_HALF_SECOND);

    protected abstract void onContinueClicked();

    protected abstract void setLayoutAccordingToSettings();

    /**
     * Called each time a setting is changed
     */
    protected abstract void checkContinueButtonListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Config.DEBUG_MODE) {
            StartupUtils.startStrictMode();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_settings_check);

        bindViews();
    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        WifiHandler.registerSettingObserver(this);
        startRepeatingCheck();
//        doSystemSettingsPreCheck();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStart");
        WifiHandler.unRegisterSettingObserver(this);
        stopRepeatingCheck();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        setLayoutAccordingToSettings();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CODE_SCAN_ALWAYS_AVAILABLE:
                Log.d(TAG, "resultCode" + resultCode);
                boolean isCorrect = resultCode == RESULT_OK;
                if (isCorrect) {
                    displaySettingsCorrectLayout(mScanCheckLayout, mScanNextIcon);
                } else {
                    displayCheckSettingsLayout(mScanCheckLayout, mScanNextIcon);
                }
                WifiHandler.setSetting(Config.SCAN_ALWAYS_AVAILABLE_SETTINGS, isCorrect);

                checkContinueButtonListener();
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.startup_check_scan_always_available_layout:
                /** airplane mode disables scan always available feature **/
                if (!WifiHandler.isCorrectSetting(Config.AIRPLANE_SETTINGS)) {
                    Intent disableAirplane = new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS);
                    startActivity(disableAirplane);
                } else {
                    Intent enableScanAvailable = new Intent(WifiManager
                            .ACTION_REQUEST_SCAN_ALWAYS_AVAILABLE);
                    startActivityForResult(enableScanAvailable, REQUEST_CODE_SCAN_ALWAYS_AVAILABLE);
                }
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
            case R.id.system_settings_check_continue_button:
                onContinueClicked();
                stopRepeatingCheck();
                break;
        }

    }

    protected void displaySettingsCorrectLayout(final RelativeLayout layout, final ImageView icon) {
        Log.d(TAG, "displaySettingsCorrectLayout");
        layout.setBackgroundResource(R.drawable
                .stroke_rectangle_shape_teal_lighter);
        icon.setVisibility(View.GONE);
        layout.setOnClickListener(null);
    }

    protected void displayCheckSettingsLayout(final RelativeLayout layout, final ImageView icon) {
        Log.d(TAG, "displayCheckSettingsLayout");
        layout.setBackgroundResource(R.drawable
                .startup_check_settings_textview_warning_selector);
        icon.setVisibility(View.VISIBLE);
        layout.setOnClickListener(this);
    }

    protected void setScanLayoutAccordingToSettings() {
        Log.d(TAG, "setScanLayoutAccordingToSettings");

        if (WifiHandler.isCorrectSetting(Config.SCAN_ALWAYS_AVAILABLE_SETTINGS)) {
            displaySettingsCorrectLayout(mScanCheckLayout, mScanNextIcon);
        } else {
            displayCheckSettingsLayout(mScanCheckLayout, mScanNextIcon);
        }

//        checkContinueButtonListener();
    }

    protected void setAirplaneLayoutAccordingToSettings() {
        Log.d(TAG, "setAirplaneLayoutAccordingToSettings");

        if (!WifiHandler.isCorrectSetting(Config.AIRPLANE_SETTINGS)) {
            displayCheckSettingsLayout(mAirplaneCheckLayout, mAirplaneNextIcon);
        } else {
            displaySettingsCorrectLayout(mAirplaneCheckLayout, mAirplaneNextIcon);
        }

//        checkContinueButtonListener();
    }

    protected void setHotspotLayoutAccordingToSettings() {
        Log.d(TAG, "setHotspotLayoutAccordingToSettings");
        if (!WifiHandler.isCorrectSetting(Config.HOTSPOT_SETTINGS)) {
            displayCheckSettingsLayout(mHotspotCheckLayout, mHotspotNextIcon);
        } else {
            displaySettingsCorrectLayout(mHotspotCheckLayout, mHotspotNextIcon);
        }

//        checkContinueButtonListener();
    }

    protected void setWifiLayoutAccordingToSettings() {
        Log.d(TAG, "setWifiLayoutAccordingToSettings");

        if (!WifiHandler.isCorrectSetting(Config.STARTUP_CHECK_WIFI_SETTINGS)) {
            displayCheckSettingsLayout(mWifiCheckLayout, mWifiNextIcon);
        } else {
            displaySettingsCorrectLayout(mWifiCheckLayout, mWifiNextIcon);
        }

//        checkContinueButtonListener();
    }

    private void bindViews() {
        mScanCheckLayout = (RelativeLayout) findViewById(R.id
                .startup_check_scan_always_available_layout);
        mAirplaneCheckLayout = (RelativeLayout) findViewById(R.id
                .startup_check_airplane_settings_layout);
        mHotspotCheckLayout = (RelativeLayout) findViewById(R.id
                .startup_check_hotspot_settings_layout);
        mWifiCheckLayout = (RelativeLayout) findViewById(R.id
                .startup_check_wifi_settings_layout);

        mContinueButton = (Button) findViewById(R.id.system_settings_check_continue_button);

        mAirplaneNextIcon = (ImageView) mAirplaneCheckLayout.findViewById(R.id
                .startup_check_airplane_settings_next_ic);
        mHotspotNextIcon = (ImageView) mHotspotCheckLayout.findViewById(R.id
                .startup_check_hotspot_settings_next_ic);
        mScanNextIcon = (ImageView) mScanCheckLayout.findViewById(R.id
                .startup_check_scan_always_available_next_ic);
        mWifiNextIcon = (ImageView) mWifiCheckLayout.findViewById(R.id
                .startup_check_wifi_settings_next_ic);

    }



    /**
     * Simulates a system broadcast to check accurately the scan always available setting
     */
    protected abstract void startRepeatingCheck();

    protected void stopRepeatingCheck() {
        mCheckPassiveHandler.removeMessages(TICK_WHAT);
    }

    @Override
    public void update(Observable observable, Object data) {
        setLayoutAccordingToSettings();
    }
}

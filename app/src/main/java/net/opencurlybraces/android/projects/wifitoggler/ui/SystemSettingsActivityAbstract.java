package net.opencurlybraces.android.projects.wifitoggler.ui;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.android.gms.common.api.GoogleApiClient;

import net.opencurlybraces.android.projects.wifitoggler.Config;
import net.opencurlybraces.android.projects.wifitoggler.R;
import net.opencurlybraces.android.projects.wifitoggler.WifiToggler;
import net.opencurlybraces.android.projects.wifitoggler.util.CheckPassiveScanHandler;
import net.opencurlybraces.android.projects.wifitoggler.util.StartupUtils;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by chris on 24/07/15.
 */
public abstract class SystemSettingsActivityAbstract extends AppCompatActivity implements View
        .OnClickListener, Observer, OnRequestPermissionsResultCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "SystemSettingsAbstract";

    protected RelativeLayout mScanCheckLayout = null;
    protected RelativeLayout mAirplaneCheckLayout = null;
    protected RelativeLayout mHotspotCheckLayout = null;
    protected RelativeLayout mWifiCheckLayout = null;
    protected RelativeLayout mLocationCheckLayout = null;


    protected ImageView mWifiNextIcon;
    protected ImageView mAirplaneNextIcon;
    protected ImageView mHotspotNextIcon;
    protected ImageView mScanNextIcon;
    protected ImageView mLocationNextIcon;


    protected Button mContinueButton = null;

    private static final int REQUEST_CODE_SCAN_ALWAYS_AVAILABLE = 1;
    private static final String TETHER_SETTINGS_ACTION = "com.android.settings.TetherSettings";
    private static final String TETHER_SETTINGS_CLASSNAME = "com.android.settings";

    protected final CheckPassiveScanHandler mCheckPassiveHandler = new CheckPassiveScanHandler
            (this, Config.WHAT_REPEAT_CHECK_SCAN_ALWAYS, Config.DELAY_CHECK_HALF_SECOND);

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
        WifiToggler.registerSettingObserver(this);
        startRepeatingCheck();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStart");
        WifiToggler.unRegisterSettingObserver(this);
        stopRepeatingCheck();
    }

    @TargetApi (Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[]
            grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == Config.M_LOCATION_REQUEST_CODE) {
            for (int i = 0, len = permissions.length; i < len; i++) {
                String permission = permissions[i];
                if (Manifest.permission.ACCESS_COARSE_LOCATION.equals(permission)) {

                    WifiToggler.setSetting(Config.CHECK_LOCATION_PERMISSION_SETTINGS,
                            grantResults[i] ==
                                    PackageManager.PERMISSION_GRANTED);
                }
            }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        setLayoutAccordingToSettings();

        if (Config.RUNNING_MARSHMALLOW) {
            handleLocationPermissionCheck();
        }
    }



    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
        boolean isCorrect = resultCode == RESULT_OK;

        switch (requestCode) {
            case REQUEST_CODE_SCAN_ALWAYS_AVAILABLE:
                Log.d(TAG, "resultCode" + resultCode);
                if (isCorrect) {
                    displaySettingsCorrectLayout(mScanCheckLayout, mScanNextIcon);
                } else {
                    displayCheckSettingsLayout(mScanCheckLayout, mScanNextIcon);
                }
                WifiToggler.setSetting(Config.SCAN_ALWAYS_AVAILABLE_SETTINGS, isCorrect);

                checkContinueButtonListener();
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.startup_check_location_permission_settings_layout:
                handleLocationPermissionRequest();
                break;
            case R.id.startup_check_scan_always_available_layout:

                handleScanSettingsDisplay();
                break;
            case R.id.startup_check_wifi_settings_layout:
                Intent enableWifi = new Intent(Settings.ACTION_WIFI_SETTINGS);
                startActivity(enableWifi);
                break;
            case R.id.startup_check_airplane_settings_layout:
                Intent disableAirplane = new Intent(Settings
                        .ACTION_AIRPLANE_MODE_SETTINGS);
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

    public void handleLocationPermissionRequest() {
        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_COARSE_LOCATION)) {
            Intent showExplanation = new Intent(this, LocationPermissionActivityAsDialog
                    .class);
            startActivity(showExplanation);

        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission
                    .ACCESS_COARSE_LOCATION}, Config.M_LOCATION_REQUEST_CODE);
        }
    }

    private void handleScanSettingsDisplay() {
        /** On some devices airplane mode disables scan always available feature. Wait for
         * feedback to workaround this issue
         *  **/
        if (!WifiToggler.isCorrectSetting(Config.AIRPLANE_SETTINGS)) {
            Intent disableAirplane = new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS);
            startActivity(disableAirplane);
        } else {
            Intent enableScanAvailable = new Intent(WifiManager
                    .ACTION_REQUEST_SCAN_ALWAYS_AVAILABLE);
            startActivityForResult(enableScanAvailable, REQUEST_CODE_SCAN_ALWAYS_AVAILABLE);
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

        if (WifiToggler.isCorrectSetting(Config.SCAN_ALWAYS_AVAILABLE_SETTINGS)) {
            displaySettingsCorrectLayout(mScanCheckLayout, mScanNextIcon);
        } else {
            displayCheckSettingsLayout(mScanCheckLayout, mScanNextIcon);
        }
    }

    protected void setAirplaneLayoutAccordingToSettings() {
        Log.d(TAG, "setAirplaneLayoutAccordingToSettings");

        if (!WifiToggler.isCorrectSetting(Config.AIRPLANE_SETTINGS)) {
            displayCheckSettingsLayout(mAirplaneCheckLayout, mAirplaneNextIcon);
        } else {
            displaySettingsCorrectLayout(mAirplaneCheckLayout, mAirplaneNextIcon);
        }
    }

    protected void setHotspotLayoutAccordingToSettings() {
        Log.d(TAG, "setHotspotLayoutAccordingToSettings");
        if (!WifiToggler.isCorrectSetting(Config.HOTSPOT_SETTINGS)) {
            displayCheckSettingsLayout(mHotspotCheckLayout, mHotspotNextIcon);
        } else {
            displaySettingsCorrectLayout(mHotspotCheckLayout, mHotspotNextIcon);
        }
    }

    protected void setWifiLayoutAccordingToSettings() {
        Log.d(TAG, "setWifiLayoutAccordingToSettings");

        if (!WifiToggler.isCorrectSetting(Config.STARTUP_CHECK_WIFI_SETTINGS)) {
            displayCheckSettingsLayout(mWifiCheckLayout, mWifiNextIcon);
        } else {
            displaySettingsCorrectLayout(mWifiCheckLayout, mWifiNextIcon);
        }
    }

    protected void setLocationPermissionLayoutAccordingToSettings() {
        Log.d(TAG, "setLocationPermissionLayoutAccordingToSettings");

        if (!WifiToggler.isCorrectSetting(Config.CHECK_LOCATION_PERMISSION_SETTINGS)) {
            displayCheckSettingsLayout(mLocationCheckLayout, mLocationNextIcon);
        } else {
            displaySettingsCorrectLayout(mLocationCheckLayout, mLocationNextIcon);
        }
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
        mContinueButton.setTextColor(getResources().getColor(android.R.color.white));

        mAirplaneNextIcon = (ImageView) mAirplaneCheckLayout.findViewById(R.id
                .startup_check_airplane_settings_next_ic);
        mHotspotNextIcon = (ImageView) mHotspotCheckLayout.findViewById(R.id
                .startup_check_hotspot_settings_next_ic);
        mScanNextIcon = (ImageView) mScanCheckLayout.findViewById(R.id
                .startup_check_scan_always_available_next_ic);
        mWifiNextIcon = (ImageView) mWifiCheckLayout.findViewById(R.id
                .startup_check_wifi_settings_next_ic);

        if (Config.RUNNING_MARSHMALLOW) {
            mLocationCheckLayout = (RelativeLayout) findViewById(R.id
                    .startup_check_location_permission_settings_layout);
            mLocationCheckLayout.setVisibility(View.VISIBLE);
            mLocationNextIcon = (ImageView) mLocationCheckLayout.findViewById(R.id
                    .startup_check_location_permission_settings_next_ic);
        }

    }


    private void handleLocationPermissionCheck() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission
                .ACCESS_COARSE_LOCATION) == PackageManager
                .PERMISSION_GRANTED) {
            WifiToggler.setSetting(Config.CHECK_LOCATION_PERMISSION_SETTINGS, true);
        }
    }

    /**
     * Simulates a system broadcast to check accurately the scan always available setting
     */
    protected abstract void startRepeatingCheck();

    protected void stopRepeatingCheck() {
        mCheckPassiveHandler.removeMessages(Config.WHAT_REPEAT_CHECK_SCAN_ALWAYS);
    }

    @Override
    public void update(Observable observable, Object data) {
        setLayoutAccordingToSettings();
    }
}

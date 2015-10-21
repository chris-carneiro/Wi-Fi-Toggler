package net.opencurlybraces.android.projects.wifitoggler.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;

import net.opencurlybraces.android.projects.wifitoggler.Config;
import net.opencurlybraces.android.projects.wifitoggler.WifiToggler;
import net.opencurlybraces.android.projects.wifitoggler.service.WifiTogglerService;
import net.opencurlybraces.android.projects.wifitoggler.util.NetworkUtils;
import net.opencurlybraces.android.projects.wifitoggler.util.PrefUtils;


public class StartupSettingsCheckActivity extends SystemSettingsActivityAbstract {

    private static final String TAG = "StartupSettingsCheck";

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void startRepeatingCheck() {
        mCheckPassiveHandler.sendMessageDelayed(Message.obtain(mCheckPassiveHandler, Config
                        .WHAT_REPEAT_CHECK_SCAN_ALWAYS),
                Config.INTERVAL_CHECK_HALF_SECOND);
    }

    @Override
    protected void checkContinueButtonListener() {
        if (WifiToggler.hasWrongSettingsForFirstLaunch()) {
            mContinueButton.setOnClickListener(null);
            mContinueButton.setEnabled(false);

        } else {
            mContinueButton.setOnClickListener(this);
            mContinueButton.setEnabled(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mWifiCheckLayout.setVisibility(View.VISIBLE);
        setLayoutAccordingToSettings();
        registerFinishActivityReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver
                (mFinishStartupCheckReceiver);
    }

    @Override
    protected void onContinueClicked() {
        if (!PrefUtils.isSavedWifiInsertComplete(this)) {
            loadSavedWifiIntoDatabase();
        }
        PrefUtils.markSettingsCorrectAtFirstLaunch(this);
    }

    @Override
    protected void setLayoutAccordingToSettings() {
        setAirplaneLayoutAccordingToSettings();
        setScanLayoutAccordingToSettings();
        setWifiLayoutAccordingToSettings();
        setHotspotLayoutAccordingToSettings();

        if (Config.RUNNING_MARSHMALLOW) {
            setLocationPermissionLayoutAccordingToSettings();
        }
        checkContinueButtonListener();
    }

    private void registerFinishActivityReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiTogglerService.ACTION_FINISH_STARTUP_CHECK_ACTIVITY);
        LocalBroadcastManager.getInstance(this).registerReceiver(mFinishStartupCheckReceiver,
                intentFilter);
    }

    private BroadcastReceiver mFinishStartupCheckReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Intent=" + intent.getAction());
            if (WifiTogglerService.ACTION_FINISH_STARTUP_CHECK_ACTIVITY.equals(intent
                    .getAction())) {
                if (NetworkUtils.isWifiConnected(context)) {
                    Intent handleSavedWifiUpdate = new Intent(context, WifiTogglerService.class);
                    handleSavedWifiUpdate.setAction(WifiTogglerService
                            .ACTION_HANDLE_SAVED_WIFI_UPDATE_CONNECT);
                    startService(handleSavedWifiUpdate);
                }
                StartupSettingsCheckActivity.this.finish();
            }
        }
    };

    // TODO move to util class
    private void loadSavedWifiIntoDatabase() {
        Intent handleSavedWifiInsert = new Intent(this, WifiTogglerService.class);
        handleSavedWifiInsert.setAction(WifiTogglerService.ACTION_HANDLE_SAVED_WIFI_INSERT);
        startService(handleSavedWifiInsert);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed");
    }
}

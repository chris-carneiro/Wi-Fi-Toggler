package net.opencurlybraces.android.projects.wifihandler.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;

import net.opencurlybraces.android.projects.wifihandler.Config;
import net.opencurlybraces.android.projects.wifihandler.WifiHandler;
import net.opencurlybraces.android.projects.wifihandler.service.WifiHandlerService;
import net.opencurlybraces.android.projects.wifihandler.util.PrefUtils;

import java.lang.ref.WeakReference;
import java.util.Observable;


public class StartupSettingsCheckActivity extends SystemSettingsActivityAbstract {

    private static final String TAG = "StartupSettingsCheck";

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void startRepeatingCheck() {
        mCheckPassiveHandler.sendMessageDelayed(Message.obtain(mCheckPassiveHandler, TICK_WHAT),
                Config.INTERVAL_CHECK_ONE_SECOND);
    }

    @Override
    protected void checkContinueButtonListener() {
        if (WifiHandler.hasWrongSettingsForFirstLaunch()) {
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
        loadSavedWifiIntoDatabase();
        PrefUtils.markSettingsCorrectAtFirstLaunch(this);
    }

    @Override
    protected void setLayoutAccordingToSettings() {
        setAirplaneLayoutAccordingToSettings();
        setScanLayoutAccordingToSettings();
        setWifiLayoutAccordingToSettings();
        setHotspotLayoutAccordingToSettings();
    }

    private void registerFinishActivityReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiHandlerService.ACTION_FINISH_STARTUP_CHECK_ACTIVITY);
        LocalBroadcastManager.getInstance(this).registerReceiver(mFinishStartupCheckReceiver,
                intentFilter);
    }

    private BroadcastReceiver mFinishStartupCheckReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Intent=" + intent.getAction());
            if (WifiHandlerService.ACTION_FINISH_STARTUP_CHECK_ACTIVITY.equals(intent
                    .getAction())) {
                StartupSettingsCheckActivity.this.finish();
            }
        }
    };

    private void loadSavedWifiIntoDatabase() {
        Intent handleSavedWifiInsert = new Intent(this, WifiHandlerService.class);
        handleSavedWifiInsert.setAction(WifiHandlerService.ACTION_HANDLE_SAVED_WIFI_INSERT);
        startService(handleSavedWifiInsert);
    }

    @Override
    public void update(Observable observable, Object data) {
        Log.d(TAG, "update");
        setLayoutAccordingToSettings();
    }


}

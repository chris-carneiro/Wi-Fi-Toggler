package net.opencurlybraces.android.projects.wifitoggler.ui;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;

import net.opencurlybraces.android.projects.wifitoggler.Config;
import net.opencurlybraces.android.projects.wifitoggler.WifiToggler;

/**
 * Created by chris on 27/07/15.
 */
public class SystemSettingsCheckActivity extends SystemSettingsActivityAbstract {
    private static final String TAG = "SystemSettingsCheck";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void startRepeatingCheck() {
        mCheckPassiveHandler.sendMessageDelayed(Message.obtain(mCheckPassiveHandler, Config
                        .WHAT_REPEAT_CHECK_SCAN_ALWAYS),
                Config.DELAY_CHECK_HALF_SECOND);
    }

    @Override
    protected void checkContinueButtonListener() {
        if (WifiToggler.hasWrongSettingsForAutoToggle()) {
            mContinueButton.setOnClickListener(null);
            mContinueButton.setEnabled(false);
        } else {
            mContinueButton.setOnClickListener(this);
            mContinueButton.setEnabled(true);
        }
    }

    @Override
    protected void onContinueClicked() {
        this.finish();
    }

    @Override
    protected void setLayoutAccordingToSettings() {
        setHotspotLayoutAccordingToSettings();
        setScanLayoutAccordingToSettings();
        setAirplaneLayoutAccordingToSettings();
        if (Config.RUNNING_POST_LOLLIPOP) {
            setLocationPermissionLayoutAccordingToSettings();
        }
        checkContinueButtonListener();
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

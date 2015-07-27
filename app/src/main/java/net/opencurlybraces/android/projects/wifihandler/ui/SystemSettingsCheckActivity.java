package net.opencurlybraces.android.projects.wifihandler.ui;

import android.os.Bundle;
import android.util.Log;

import net.opencurlybraces.android.projects.wifihandler.WifiHandler;

import java.util.Observable;

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
        setLayoutAccordingToSettings();
    }

    @Override
    protected void checkContinueButtonListener() {
        if (WifiHandler.hasWrongSettingsForAutoToggle()) {
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
    }

    @Override
    public void update(Observable observable, Object data) {
        Log.d(TAG, "update");
        setLayoutAccordingToSettings();
    }
}

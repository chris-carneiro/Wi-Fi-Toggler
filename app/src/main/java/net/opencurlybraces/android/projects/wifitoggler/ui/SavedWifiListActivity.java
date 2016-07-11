package net.opencurlybraces.android.projects.wifitoggler.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import net.opencurlybraces.android.projects.wifitoggler.Config;
import net.opencurlybraces.android.projects.wifitoggler.R;
import net.opencurlybraces.android.projects.wifitoggler.WifiToggler;
import net.opencurlybraces.android.projects.wifitoggler.service.WifiTogglerService;
import net.opencurlybraces.android.projects.wifitoggler.ui.fragments.BaseListFragment;
import net.opencurlybraces.android.projects.wifitoggler.ui.fragments.EnabledWifiListFragment;
import net.opencurlybraces.android.projects.wifitoggler.ui.fragments.InfoMessageFragment;
import net.opencurlybraces.android.projects.wifitoggler.util.NetworkUtils;
import net.opencurlybraces.android.projects.wifitoggler.util.PrefUtils;
import net.opencurlybraces.android.projects.wifitoggler.util.StartupUtils;

import java.util.Observable;
import java.util.Observer;

public class SavedWifiListActivity extends AppCompatActivity implements
        CompoundButton.OnCheckedChangeListener,
        View.OnClickListener, Observer, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<LocationSettingsResult> {

    private static final String TAG = "SavedWifiList";

    private int REQUEST_CHECK_SETTINGS = 1003;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1002;

    private TextView mWifiTogglerSwitchLabel = null;
    private Switch mWifiTogglerActivationSwitch = null;
    private RelativeLayout mBanner = null;

    private GoogleApiClient mGoogleApiClient = null;
    private LocationRequest mLocationRequest = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_wifi_list);

        if (savedInstanceState == null) {

            BaseListFragment.showFragment(EnabledWifiListFragment.newInstance(), this);
        }

        bindViews();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(false);

        handlePostLollipopRequirements();
    }

    @Override
    protected void onStart() {
        super.onStart();
        WifiToggler.registerSettingObserver(this);
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();

            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(mLocationRequest);

            PendingResult<LocationSettingsResult> mLocationSettingsResult = LocationServices
                    .SettingsApi.checkLocationSettings
                            (mGoogleApiClient, builder
                                    .build());

            mLocationSettingsResult.setResultCallback(this);

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        WifiToggler.unRegisterSettingObserver(this);
    }

    @Override
    protected void onResume() {
        /** TODO restore value
         onResume, retain instance or use fragment.isVisible() instead or maybe move this field in
         the fragment itself **/
        super.onResume();

        startupCheck();
        handleNotification(mWifiTogglerActivationSwitch.isChecked());
        handleBannerDisplay();
        doSystemSettingsCheck();
        registerReceivers();
        switchActivation();
        if (NetworkUtils.isWifiEnabled(this)) {
            WifiToggler.removeDeletedSavedWifiFromDB(this);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceivers();

        unRegisterPostLollipopRequiredListeners();
    }

    private void unRegisterPostLollipopRequiredListeners() {
        //Fix for issue #7
        if (mGoogleApiClient != null) {
            mGoogleApiClient.unregisterConnectionCallbacks(this);
            mGoogleApiClient.unregisterConnectionFailedListener(this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_saved_wifi_list, menu);
        return true;
    }

    @Override
    public void onResult(LocationSettingsResult locationSettingsResult) {
        final Status status = locationSettingsResult.getStatus();

        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                Log.d(TAG, "LocationSettingsStatusCodes.SUCCESS");
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                // Location settings are not satisfied. But could be fixed by showing
                // the user
                // a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    status.startResolutionForResult(
                            SavedWifiListActivity.this,
                            REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException e) {
                    // Ignore the error.
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                // Location settings are not satisfied. However, we have no way to
                // fix the
                // settings so we won't show the dialog.
                Log.d(TAG, "LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE");
                //TODO in the future warn the user
                break;
            default:
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.d(TAG, "onCheckedChanged");
        switch (buttonView.getId()) {
            case R.id.wifi_toggler_activation_switch:
                handleSwitchLabelValue(isChecked);
                handleNotification(isChecked);

                if (isChecked) {
                    BaseListFragment.showFragment(EnabledWifiListFragment.newInstance(), this);
                } else {
                    BaseListFragment.showFragment(InfoMessageFragment.newInstance(getString(R
                            .string.wifi_list_info_when_wifi_toggler_off)), this);
                }
                break;
            default:
                break;
        }

    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "View Id=" + v.getId());
        switch (v.getId()) {
            case R.id.wifi_toggler_message_banner:
                launchSystemSettingsCheckActivity();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            displayAppPreferencesActivity();
            return true;
        }

        return false;
    }


    private void bindViews() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mWifiTogglerSwitchLabel = (TextView) findViewById(R.id.wifi_toggler_switch_label);
        mWifiTogglerActivationSwitch = (Switch) findViewById(R.id.wifi_toggler_activation_switch);
        mWifiTogglerActivationSwitch.setOnCheckedChangeListener(this);
        TextView mBannerContent = (TextView) findViewById(R.id.wifi_toggler_message_banner_content);
        mBannerContent.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        mBannerContent.setSingleLine(true);
        mBannerContent.setSelected(true);
        mBanner = (RelativeLayout) findViewById(R.id.wifi_toggler_message_banner);
        mBanner.setOnClickListener(this);

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
            default:
                break;
        }
    }

    private void handleFirstLaunch() {
        Log.d(TAG, "handleFirstLaunch");
        if (WifiToggler.hasWrongSettingsForFirstLaunch()) {
            launchStartupCheckActivity();
        } else {
            PrefUtils.markSettingsCorrectAtFirstLaunch(this);
            if (!PrefUtils.isSavedWifiInsertComplete(this)) {
                loadSavedWifiIntoDatabase();
            }
        }
    }

    private void unregisterReceivers() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mNotificationActionsReceiver);
    }

    private void loadSavedWifiIntoDatabase() {
        Intent insertSavedWifi = new Intent(this, WifiTogglerService.class);
        insertSavedWifi.setAction(WifiTogglerService.ACTION_HANDLE_SAVED_WIFI_INSERT);
        startService(insertSavedWifi);
    }

    private void handleNormalStartup() {
        Log.d(TAG, "handleNormalStartup");
        if (!PrefUtils.wereSettingsCorrectAtFirstLaunch(this) && WifiToggler
                .hasWrongSettingsForFirstLaunch()) {
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
            buildWifiTogglerStateNotification(WifiTogglerService
                    .ACTION_HANDLE_ACTIVATE_WIFI_HANDLER);
        } else {
            buildWifiTogglerStateNotification(WifiTogglerService
                    .ACTION_HANDLE_PAUSE_WIFI_HANDLER);
        }
    }

    private void buildWifiTogglerStateNotification(String actionHandleActivateWifiHandler) {
        Log.d(TAG, "buildWifiTogglerStateNotification");
        Intent startForegroundNotificationIntent = new Intent(this, WifiTogglerService.class);
        startForegroundNotificationIntent.setAction(actionHandleActivateWifiHandler);
        startService(startForegroundNotificationIntent);
    }

    private void handleSwitchLabelValue(boolean isChecked) {
        Log.d(TAG, "handleSwitchLabelValue=" + isChecked);
        if (isChecked) {
            String on = getString(R.string.on_wifi_toggler_switch_label_value);
            mWifiTogglerSwitchLabel.setText(on);
        } else {
            String off = getString(R.string.off_wifi_toggler_switch_label_value);
            mWifiTogglerSwitchLabel.setText(off);
        }
    }


    private void switchActivation() {
        Log.d(TAG, "switchActivation");
        if (PrefUtils.isWifiTogglerActive(this)) {
            mWifiTogglerActivationSwitch.setChecked(true);
        } else {
            mWifiTogglerActivationSwitch.setChecked(false);
        }
    }

    private void registerReceivers() {
        Log.d(TAG, "registerReceivers");
        registerNotificationReceiver();
    }

    private void handleBannerDisplay() {
        Log.d(TAG, "handleBannerDisplay");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (WifiToggler.hasWrongSettingsForAutoToggle()) {
                    mBanner.setVisibility(View.VISIBLE);
                } else {
                    mBanner.setVisibility(View.GONE);
                }
            }
        });
    }

    private void registerNotificationReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiTogglerService.ACTION_HANDLE_NOTIFICATION_ACTION_ACTIVATE);
        intentFilter.addAction(WifiTogglerService.ACTION_HANDLE_NOTIFICATION_ACTION_PAUSE);
        LocalBroadcastManager.getInstance(this).registerReceiver(mNotificationActionsReceiver,
                intentFilter);
    }

    private BroadcastReceiver mNotificationActionsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "mNotificationActionsReceiver Intent action=" + intent.getAction());
            if (WifiTogglerService.ACTION_HANDLE_NOTIFICATION_ACTION_ACTIVATE.equals(intent
                    .getAction())) {
                mWifiTogglerActivationSwitch.setChecked(true);
            } else if (WifiTogglerService.ACTION_HANDLE_NOTIFICATION_ACTION_PAUSE.equals(intent
                    .getAction())) {
                mWifiTogglerActivationSwitch.setChecked(false);
            }
        }
    };

    @Override
    public void update(Observable observable, Object data) {
        handleBannerDisplay();
    } // can be
    // called back from a worker thread

    // TODO move to util class
    private void doSystemSettingsCheck() {
        Intent checkSettings = new Intent(this, WifiTogglerService.class);
        checkSettings.setAction(WifiTogglerService.ACTION_STARTUP_SETTINGS_PRECHECK);
        startService(checkSettings);
    }

    private void handlePostLollipopRequirements() {
        if (Config.RUNNING_POST_LOLLIPOP) {
            if (checkPlayServices()) {
                buildGoogleApiClient();
                buildLocationRequest();
            }
        }
    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                finish();
            }
            return false;
        }
        return true;
    }

    private void buildLocationRequest() {
        mLocationRequest = new LocationRequest()
                .setInterval(Config.DELAY_FIVE_SECONDS)
                .setFastestInterval(1000)
                .setPriority(LocationRequest.PRIORITY_LOW_POWER);
    }

    private void displayAppPreferencesActivity() {
        Intent intent = new Intent();
        intent.setClass(this, PreferencesActivity.class);
        startActivity(intent);
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}

package net.opencurlybraces.android.projects.wifihandler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import net.opencurlybraces.android.projects.wifihandler.service.WifiHandlerService;
import net.opencurlybraces.android.projects.wifihandler.util.PrefUtils;
import net.opencurlybraces.android.projects.wifihandler.util.WifiUtils;

import java.util.List;


public class ConfiguredWifiListActivity extends AppCompatActivity implements
        CompoundButton.OnCheckedChangeListener, WifiUtils.UserWifiConfigurationLoadedListener {

    private static final String TAG = "ConfiguredWifiList";

    private TextView mWifiHandlerSwitchLabel = null;
    private Switch mWifiHandlerActivationSwitch = null;
    private ListView mWifiHandlerWifiList = null;
    private TextView mEmptyView = null;
    private ArrayAdapter<WifiConfiguration> mConfiguredWifiAdapter = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configured_wifi_list);

        mWifiHandlerSwitchLabel = (TextView) findViewById(R.id.wifi_handler_switch_label);
        mWifiHandlerActivationSwitch = (Switch) findViewById(R.id.wifi_handler_activation_switch);
        mWifiHandlerWifiList = (ListView) findViewById(android.R.id.list);
        mEmptyView = (TextView) findViewById(android.R.id.empty);
        mWifiHandlerActivationSwitch.setOnCheckedChangeListener(this);

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
        if (PrefUtils.isWifiHandlerActive(this)) {
            mWifiHandlerActivationSwitch.setChecked(true);
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
                handleUserWifiListLoading(isChecked);
                handleNotification(isChecked);
                break;
        }

    }


    private void handleNotification(boolean isChecked) {
        if (isChecked) {
            Intent startForgroundNotificationIntent = new Intent(this, WifiHandlerService.class);
            startForgroundNotificationIntent.setAction(WifiHandlerService
                    .ACTION_HANDLE_ACTIVATE_WIFI_HANDLER);
            startService(startForgroundNotificationIntent);
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


    private void handleUserWifiListLoading(boolean isChecked) {
        if (isChecked) {
            loadWifiConfigurations();
        } else {
            mWifiHandlerWifiList.setAdapter(null);
        }
    }

    @Override
    public void onUserWifiConfigurationLoaded(List<WifiConfiguration> userWifiConfigurations) {
        if (userWifiConfigurations == null) {
            mWifiHandlerActivationSwitch.setChecked(false);
            askUserCheckHotspot();
            return;
        }
        setListViewData(userWifiConfigurations);
    }

    private void loadWifiConfigurations() {
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        new WifiConfigurationLoader(wifiManager, this).execute();
    }

    private void setListViewData(List<WifiConfiguration> userWifiConfigurations) {
        mConfiguredWifiAdapter = new ArrayAdapter<>(this, R.layout
                .configured_wifi_list_row, R.id.configured_wifi_ssid,
                userWifiConfigurations);
        mWifiHandlerWifiList.setAdapter(mConfiguredWifiAdapter);
        mWifiHandlerWifiList.setEmptyView(mEmptyView);
    }

    private void askUserCheckHotspot() {
        Toast.makeText(this, "WifiConfigurations could not be retrieved, please disable any " +
                "hotspot or tethering mode and retry", Toast
                .LENGTH_LONG).show();
    }

    private static class WifiConfigurationLoader extends AsyncTask<Void, Void,
            List<WifiConfiguration>> {

        private WifiManager mWifiManager;
        private WifiUtils.UserWifiConfigurationLoadedListener mListener;

        private WifiConfigurationLoader(WifiManager wifiManager, WifiUtils
                .UserWifiConfigurationLoadedListener listener) {
            mWifiManager = wifiManager;
            mListener = listener;
        }


        @Override
        protected List<WifiConfiguration> doInBackground(Void... params) {
            try {
                return WifiUtils.getConfiguredWifis(mWifiManager);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<WifiConfiguration> wifiConfigurations) {
            mListener.onUserWifiConfigurationLoaded(wifiConfigurations);
            mListener = null;
        }
    }
}

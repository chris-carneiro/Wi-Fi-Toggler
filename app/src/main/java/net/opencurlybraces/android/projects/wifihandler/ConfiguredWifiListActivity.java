package net.opencurlybraces.android.projects.wifihandler;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import net.opencurlybraces.android.projects.wifihandler.util.WifiUtils;

import java.util.List;


public class ConfiguredWifiListActivity extends AppCompatActivity implements
        CompoundButton.OnCheckedChangeListener, WifiUtils.UserWifiConfigurationLoadedListener {

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

    @Override
    protected void onResume() {
        super.onResume();
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
                break;
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
            WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
            new WifiConfigurationLoader(wifiManager, this).execute();
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
        mConfiguredWifiAdapter = new ArrayAdapter<>(this, R.layout
                .configured_wifi_list_row, R.id.configured_wifi_ssid,
                userWifiConfigurations);
        mWifiHandlerWifiList.setAdapter(mConfiguredWifiAdapter);
        mWifiHandlerWifiList.setEmptyView(mEmptyView);
    }

    private void askUserCheckHotspot() {
        Toast.makeText(this,"WifiConfigurations could not be retrieved, please disable any " +
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

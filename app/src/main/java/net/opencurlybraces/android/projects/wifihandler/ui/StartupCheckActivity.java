package net.opencurlybraces.android.projects.wifihandler.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import net.opencurlybraces.android.projects.wifihandler.R;
import net.opencurlybraces.android.projects.wifihandler.util.NetworkUtils;


public class StartupCheckActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "StartupCheckActivity";

    RelativeLayout mScanCheckLayout = null;
    RelativeLayout mWifiCheckLayout = null;
    RelativeLayout mAirplaneCheckLayout = null;
    RelativeLayout mHotspotCheckLayout = null;


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

    }


    @Override
    protected void onResume() {
        super.onResume();
        setScanLayoutAccordingToSettings();
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

    private void setScanLayoutAccordingToSettings() {
        final ImageView nextIcon = (ImageView) mScanCheckLayout.findViewById(R.id
                .startup_check_scan_always_available_next_ic);
        if (!NetworkUtils.isScanAlwaysAvailable(this)) {
            mScanCheckLayout.setBackgroundResource(R.drawable
                    .startup_check_settings_textview_warning_selector);
            nextIcon.setVisibility(View.VISIBLE);
            mScanCheckLayout.setOnClickListener(this);
            return;
        }
        mScanCheckLayout.setBackgroundResource(R.drawable
                .stroke_rectangle_shape_teal_lighter);
        nextIcon.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.startup_check_scan_always_available_layout:
                Log.d(TAG, "check your scan settings");
                break;
            case R.id.startup_check_wifi_settings_layout:
                break;
            case R.id.startup_check_airplane_settings_layout:
                break;
            case R.id.startup_check_hotspot_settings_layout:
                break;
            case R.id.startup_check_settings_continue_button:
                break;
        }
    }
}

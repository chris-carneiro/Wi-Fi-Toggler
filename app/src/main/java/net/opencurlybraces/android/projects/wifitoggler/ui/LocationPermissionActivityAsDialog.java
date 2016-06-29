package net.opencurlybraces.android.projects.wifitoggler.ui;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import net.opencurlybraces.android.projects.wifitoggler.Config;
import net.opencurlybraces.android.projects.wifitoggler.R;

/**
 * Activity used as a Custom Dialog. It was created to educate the user about the new privacy policy
 * that comes with android M and to explain why the location permission is needed. <BR/> Created by
 * chris on 19/10/15.
 */
public class LocationPermissionActivityAsDialog extends Activity implements View.OnClickListener {

    private TextView mPositiveButton = null;
    private TextView mNegativeButton = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_permission_dialog_layout);
        mPositiveButton = (TextView) findViewById(R.id.location_permission_positive_button);
        mNegativeButton = (TextView) findViewById(R.id.location_permission_negative_button);
        mPositiveButton.setOnClickListener(this);
        mNegativeButton.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.location_permission_positive_button:
                requestLocationPermission();
                finish();
                break;
            case R.id.location_permission_negative_button:
                finish();
                break;
            default:
                break;
        }
    }

    public void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission
                .ACCESS_COARSE_LOCATION}, Config.M_LOCATION_REQUEST_CODE);
    }
}

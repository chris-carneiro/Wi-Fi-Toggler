package net.opencurlybraces.android.projects.wifitoggler.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

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
                displayApplicationSettings();
                finish();
                break;
            case R.id.location_permission_negative_button:
                finish();
                break;
        }
    }

    public void displayApplicationSettings() {
        Intent intent = new Intent(android.provider.Settings
                .ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" +
                getPackageName()));
        startActivity(intent);
    }
}

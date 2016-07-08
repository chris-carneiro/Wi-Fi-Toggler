package net.opencurlybraces.android.projects.wifitoggler.ui;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import net.opencurlybraces.android.projects.wifitoggler.R;
import net.opencurlybraces.android.projects.wifitoggler.ui.fragments.PreferencesFragment;

/**
 * Created by chris on 08/07/16.
 */
public class PreferencesActivity extends AppCompatActivity {

    public static final String AUTO_TOGGLE_ACTIVE_BY_DEFAULT = "Active";
    public static final String AUTO_TOGGLE_DISABLED_BY_DEFAULT = "Disabled";
    public static final String AUTO_TOGGLE_ALWAYS_ASK = "Ask";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prefs_container);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            getFragmentManager().beginTransaction().replace(R.id.prefs_fragment_container, new
                    PreferencesFragment()).commit();

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);

    }
}

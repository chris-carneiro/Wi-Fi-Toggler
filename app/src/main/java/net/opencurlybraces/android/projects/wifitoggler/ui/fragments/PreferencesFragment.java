package net.opencurlybraces.android.projects.wifitoggler.ui.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import net.opencurlybraces.android.projects.wifitoggler.R;

/**
 * Created by chris on 08/07/16.
 */
public class PreferencesFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);
    }
}

package net.opencurlybraces.android.projects.wifitoggler.ui.fragments;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;

import net.opencurlybraces.android.projects.wifitoggler.R;
import net.opencurlybraces.android.projects.wifitoggler.util.NotifUtils;
import net.opencurlybraces.android.projects.wifitoggler.util.PrefUtils;

/**
 * Created by chris on 08/07/16.
 */
public class PreferencesFragment extends PreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "PreferencesFrag";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().setOnPreferenceChangeListener(null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setOnPreferenceChangeListeners();

        setPreferencesSummaries();
    }

    private void setPreferencesSummaries() {
        getPreferenceScreen().findPreference(PrefUtils.PREF_WIFI_DEACTIVATION_DELAY).setSummary
                (PrefUtils.getWifiDeactivationDelayStringValue(getActivity()));

        getPreferenceScreen().findPreference(PrefUtils.PREF_SIGNAL_STRENGTH_THRESHOLD).setSummary
                (PrefUtils.getWifiSignalStrengthThresholdStringValue(getActivity()));

        getPreferenceScreen().findPreference(PrefUtils
                .PREF_AUTO_TOGGLE_DEFAULT_VALUE_FOR_NEW_WIFI).setSummary(PrefUtils
                .getAutoToggleModeOnNewWifiStringValue(getActivity()));
    }

    private void setOnPreferenceChangeListeners() {
        getPreferenceScreen().findPreference(PrefUtils.PREF_WIFI_DEACTIVATION_DELAY)
                .setOnPreferenceChangeListener(this);
        getPreferenceScreen().findPreference(PrefUtils.PREF_SIGNAL_STRENGTH_THRESHOLD)
                .setOnPreferenceChangeListener(this);
        getPreferenceScreen().findPreference(PrefUtils.PREF_WARNING_NOTIFICATIONS)
                .setOnPreferenceChangeListener(this);
        getPreferenceScreen().findPreference(PrefUtils.PREF_AUTO_TOGGLE_DEFAULT_VALUE_FOR_NEW_WIFI)
                .setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Log.d(TAG, "onPreferenceChange: ");

        if (PrefUtils.PREF_WARNING_NOTIFICATIONS.equals(preference.getKey())) {
            if (!(Boolean) newValue) {
                NotifUtils.dismissNotification(preference.getContext(), NotifUtils
                        .NOTIFICATION_ID_WARNING);
            }
            return true;
        }

        String stringValue = newValue.toString();

        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;

            int index = listPreference.findIndexOfValue(stringValue);
            preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);

        }
        return true;
    }
}

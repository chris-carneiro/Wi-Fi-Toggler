package net.opencurlybraces.android.projects.wifihandler.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import net.opencurlybraces.android.projects.wifihandler.Config;
import net.opencurlybraces.android.projects.wifihandler.R;
import net.opencurlybraces.android.projects.wifihandler.util.NetworkUtils;
import net.opencurlybraces.android.projects.wifihandler.util.PrefUtils;
import net.opencurlybraces.android.projects.wifihandler.util.StartupUtils;

import java.util.List;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On handset devices,
 * settings are presented as a single list. On tablets, settings are split by category, with
 * category headers shown to the left of the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html"> Android Design:
 * Settings</a> for design guidelines and the <a href="http://developer.android
 * .com/guide/topics/ui/settings.html">Settings API Guide</a> for more information on developing a
 * Settings UI.
 */
public class SettingsActivity extends PreferenceActivity {
    /**
     * Determines whether to always show the simplified settings UI, where settings are presented in
     * a single list. When false, settings are shown as a master/detail two-pane view on tablets.
     * When true, a single pane is shown on tablets.
     */
    private static final boolean ALWAYS_SIMPLE_PREFS = false;
    private static final String TAG = "SettingsActivity";

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        if (Config.DEBUG_MODE) {
            StartupUtils.startStrictMode();
        }
        super.onCreate(savedInstanceState, persistentState);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        setUpToolbar();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            View toolbarDropShadow = findViewById(R.id.toolbar_shadow);
            toolbarDropShadow.setVisibility(View.VISIBLE);
        }

        setupSimplePreferencesScreen();
    }

    private void setUpToolbar() {
        LinearLayout root = (LinearLayout) findViewById(android.R.id.list).getParent()
                .getParent().getParent();
        AppBarLayout appBarLayout = (AppBarLayout) LayoutInflater.from(this).inflate(R.layout
                        .settings_toolbar,
                root,
                false);
        root.addView(appBarLayout, 0); // insert at top
        Toolbar bar = (Toolbar) appBarLayout.getChildAt(0);

        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * Shows the simplified settings UI if the device configuration if the device configuration
     * dictates that a simplified, single-pane UI should be shown.
     */
    private void setupSimplePreferencesScreen() {
        if (!isSimplePreferences(this)) {
            return;
        }

        // In the simplified UI, fragments are not used at all and we instead
        // use the older PreferenceActivity APIs.

        // Add container
        addPreferencesFromResource(R.xml.pref_container);


        // Add 'general' preferences.
        PreferenceCategory generalHeader = new PreferenceCategory(this);
        generalHeader.setTitle(R.string.pref_header_general);
        getPreferenceScreen().addPreference(generalHeader);
        addPreferencesFromResource(R.xml.pref_general);

        // Add 'notifications' preferences, and a corresponding header.
        PreferenceCategory notificationHeader = new PreferenceCategory(this);
        notificationHeader.setTitle(R.string.pref_header_notifications);
        getPreferenceScreen().addPreference(notificationHeader);
        addPreferencesFromResource(R.xml.pref_notification);

        bindPreferenceBooleanValue(findPreference(PrefUtils.PREF_RUN_AT_STARTUP));
        bindPreferenceSummaryToValue(findPreference(PrefUtils.PREF_SIGNAL_STRENGTH_THRESHOLD));
        bindPreferenceBooleanValue(findPreference(PrefUtils.PREF_WARNING_NOTIFICATIONS));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this) && !isSimplePreferences(this);
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For example, 10" tablets
     * are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Determines whether the simplified settings UI should be shown. This is true if this is forced
     * via {@link #ALWAYS_SIMPLE_PREFS}, or the device doesn't have newer APIs like {@link
     * PreferenceFragment}, or the device doesn't have an extra-large screen. In these cases, a
     * single-pane "simplified" settings UI should be shown.
     */
    private static boolean isSimplePreferences(Context context) {
        return ALWAYS_SIMPLE_PREFS
                || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
                || !isXLargeTablet(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi (Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        if (!isSimplePreferences(this)) {
            loadHeadersFromResource(R.xml.pref_headers, target);
        }
    }


    private static Preference.OnPreferenceChangeListener sOnCheckChangedListener =
            new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object value) {
                    Log.d(TAG, "onPreferenceChange=" + preference.getKey() + " value=" + value);
                    if (PrefUtils.PREF_WARNING_NOTIFICATIONS.equals(preference.getKey())) {
                        if (!(Boolean) value) {
                            NetworkUtils.dismissNotification(preference.getContext(), Config
                                    .NOTIFICATION_ID_AIRPLANE_MODE);
                        }
                    }

                    return true;
                }
            };


    /**
     * A preference value change listener that updates the preference's summary to reflect its new
     * value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener =
            new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object value) {
                    Log.d(TAG, "onPreferenceChange=" + preference.getKey() + " value=" + value);

                    String stringValue = value.toString();

                    if (preference instanceof ListPreference) {
                        // For list preferences, look up the correct
                        //                     display value in
                        // the preference's 'entries' list.
                        ListPreference listPreference = (ListPreference)
                                preference;
                        int index = listPreference.findIndexOfValue
                                (stringValue);

                        // Set the summary to reflect the new value.
                        preference.setSummary(
                                index >= 0
                                        ? listPreference.getEntries()[index]
                                        : null);

                    }
                    return true;
                }
            };

    /**
     * Binds a preference's summary to its value. More specifically, when the preference's value is
     * changed, its summary (line of text below the preference title) is updated to reflect the
     * value. The summary is also immediately updated upon calling this method. The exact display
     * format is dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    private static void bindPreferenceBooleanValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sOnCheckChangedListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getBoolean(preference.getKey(), true));
    }

    /**
     * This fragment shows general preferences only. It is used when the activity is showing a
     * two-pane settings UI.
     */
    @TargetApi (Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference(PrefUtils.PREF_RUN_AT_STARTUP));
            bindPreferenceSummaryToValue(findPreference(PrefUtils.PREF_SIGNAL_STRENGTH_THRESHOLD));
        }
    }

    /**
     * This fragment shows notification preferences only. It is used when the activity is showing a
     * two-pane settings UI.
     */
    @TargetApi (Build.VERSION_CODES.HONEYCOMB)
    public static class NotificationPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_notification);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference(PrefUtils.PREF_WARNING_NOTIFICATIONS));
        }
    }

}

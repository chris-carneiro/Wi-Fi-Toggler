package net.opencurlybraces.android.projects.wifitoggler.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import net.opencurlybraces.android.projects.wifitoggler.Config;
import net.opencurlybraces.android.projects.wifitoggler.R;
import net.opencurlybraces.android.projects.wifitoggler.util.NotifUtils;
import net.opencurlybraces.android.projects.wifitoggler.util.PrefUtils;
import net.opencurlybraces.android.projects.wifitoggler.util.StartupUtils;

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
public class PreferencesActivity extends PreferenceActivity {
    /**
     * Determines whether to always show the simplified settings UI, where settings are presented in
     * a single list. When false, settings are shown as a master/detail two-pane view on tablets.
     * When true, a single pane is shown on tablets.
     */
    private static final boolean ALWAYS_SIMPLE_PREFS = false;
    private static final String TAG = "PreferencesActivity";

    public static final String AUTO_TOGGLE_ACTIVE_BY_DEFAULT = "Active";
    public static final String AUTO_TOGGLE_DISABLED_BY_DEFAULT = "Disabled";
    public static final String AUTO_TOGGLE_ALWAYS_ASK = "Ask";


    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        if (Config.DEBUG_MODE) {
            StartupUtils.startStrictMode();
        }
        super.onCreate(savedInstanceState, persistentState);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
        bar.setTitle(R.string.title_activity_settings);
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

        addPreferencesFromResource(R.xml.pref_general);
        addPreferencesFromResource(R.xml.pref_notification);

        bindPreferenceSummaryToValue(findPreference(PrefUtils.PREF_WIFI_DEACTIVATION_DELAY));
        bindPreferenceBooleanValue(findPreference(PrefUtils.PREF_RUN_AT_STARTUP));
        bindPreferenceSummaryToValue(findPreference(PrefUtils.PREF_SIGNAL_STRENGTH_THRESHOLD));
        bindPreferenceBooleanValue(findPreference(PrefUtils.PREF_WARNING_NOTIFICATIONS));
        bindPreferenceSummaryToValue(findPreference(PrefUtils
                .PREF_AUTO_TOGGLE_DEFAULT_VALUE_FOR_NEW_WIFI));

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


    private static Preference.OnPreferenceChangeListener sOnCheckChangedListener =
            new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object value) {
                    Log.d(TAG, "onPreferenceBooleanChange=" + preference.getKey() + " value=" +
                            value);
                    Log.d(TAG, "AutoToggleDefaultvalue=" + PrefUtils
                            .getAutoToggleModeOnNewWifi
                                    (preference.getContext()));
                    if (PrefUtils.PREF_WARNING_NOTIFICATIONS.equals(preference.getKey())) {
                        if (!(Boolean) value) {
                            NotifUtils.dismissNotification(preference.getContext(), NotifUtils
                                    .NOTIFICATION_ID_WARNING);
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
                    Log.d(TAG, "onPreferenceSummaryChange=" + preference.getKey() + " value=" +
                            value);

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

}

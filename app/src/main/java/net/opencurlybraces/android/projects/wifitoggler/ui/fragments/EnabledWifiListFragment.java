package net.opencurlybraces.android.projects.wifitoggler.ui.fragments;


import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import net.opencurlybraces.android.projects.wifitoggler.R;
import net.opencurlybraces.android.projects.wifitoggler.data.table.SavedWifi;
import net.opencurlybraces.android.projects.wifitoggler.util.PrefUtils;
import net.opencurlybraces.android.projects.wifitoggler.util.SnackBarUndoActionDataHandler;

/**
 * Created by chris on 02/07/16.
 */
public class EnabledWifiListFragment extends BaseListFragment implements LoaderManager
        .LoaderCallbacks<Cursor> {

    public static final String TAG = "SavedWifiFragment";
    public static final int LOADER_ID = 0;
    private static final String ENABLED_WIFI = "1";

    public EnabledWifiListFragment() {
    }

    public static EnabledWifiListFragment newInstance() {

        Log.d(TAG, "newInstance: ");
        EnabledWifiListFragment fragment = new EnabledWifiListFragment();
        return fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated: ");
        super.onActivityCreated(savedInstanceState);
        getActivity().getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable
    Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        View rootView = inflater.inflate(R.layout.saved_wifi_list_fragment, container, false);
        return rootView;
    }


    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader: ");
        String[] projection = {SavedWifi._ID, SavedWifi.SSID, SavedWifi.STATUS, SavedWifi
                .AUTO_TOGGLE};
        CursorLoader cursorLoader = new CursorLoader(getActivity(),
                SavedWifi.CONTENT_URI, projection, SavedWifi.whereAutoToggle, new
                String[]{ENABLED_WIFI},
                null);
        Log.d(TAG, "onCreateLoader: autoToggleStateSelection=" + "0");
        return cursorLoader;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_display_wifi);
        boolean whenTogglerIsActive = PrefUtils.isWifiTogglerActive(getActivity());
        item.setVisible(whenTogglerIsActive);
        item.setTitle(R.string
                .action_disabled_wifis);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Log.d(TAG, "onOptionsItemSelected: ");
        if (id == R.id.action_display_wifi) {
            showFragment(DisabledWifiListFragment.newInstance(), getActivity());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void handleSnackBar(int dismissedObjectPosition) {
        Cursor cursor = (Cursor) mSavedWifiListAdapter.getItem(dismissedObjectPosition);
        String ssid = cursor.getString(cursor.getColumnIndexOrThrow(SavedWifi.SSID));

        String confirmationMessage = formatSnackBarMessage(ssid,R.string
                .wifi_disabled_confirmation_bottom_overlay_content );
        SnackBarUndoActionDataHandler.UndoData undoData = prepareSnackBarUndoDataObject
                (dismissedObjectPosition, true);
        SnackBarUndoActionDataHandler snackBarUndoHelper = new SnackBarUndoActionDataHandler(getActivity(),
                undoData);

        showUndoSnackBar(confirmationMessage,snackBarUndoHelper);
    }

}

package net.opencurlybraces.android.projects.wifitoggler.ui.fragments;


import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.opencurlybraces.android.projects.wifitoggler.R;
import net.opencurlybraces.android.projects.wifitoggler.data.table.SavedWifi;
import net.opencurlybraces.android.projects.wifitoggler.ui.SavedWifiListActivity;
import net.opencurlybraces.android.projects.wifitoggler.ui.SavedWifiListAdapter;

/**
 * Created by chris on 02/07/16.
 */
public class SavedWifiListFragment extends ListFragment implements LoaderManager
        .LoaderCallbacks<Cursor> {

    public static final String TAG = "SavedWifiFragment";

    private SavedWifiListAdapter mSavedWifiListAdapter = null;


    public SavedWifiListFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach: ");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated: ");
        super.onActivityCreated(savedInstanceState);
        mSavedWifiListAdapter = new SavedWifiListAdapter(getActivity(), null, 0);
        setListAdapter(mSavedWifiListAdapter);
        getActivity().getLoaderManager().initLoader(0, null, this);

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
        //        String autoToggleStateSelection = null;
        //        if (args != null && args.containsKey(SavedWifiListActivity
        // .WIFI_FRAGMENT_BUNDLE_KEY)) {
        //            autoToggleStateSelection = args.getString(SavedWifiListActivity
        //                    .WIFI_FRAGMENT_BUNDLE_KEY);
        //        }
        String[] projection = {SavedWifi._ID, SavedWifi.SSID, SavedWifi.STATUS, SavedWifi
                .AUTO_TOGGLE};
        CursorLoader cursorLoader = new CursorLoader(getActivity(),
                SavedWifi.CONTENT_URI, projection, SavedWifi.whereAutoToggle, new
                String[]{"1"},
                null);
        Log.d(TAG, "onCreateLoader: autoToggleStateSelection=" + "0");
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader loader, Cursor data) {
        mSavedWifiListAdapter.swapCursor(data);

    }

//    @Override
//    public void onLoadFinished(Loader loader, Object data) {
//        Log.d(TAG, "onLoadFinished: ");
//        mSavedWifiListAdapter.swapCursor((Cursor) data);
//    }

    @Override
    public void onLoaderReset(Loader loader) {
        mSavedWifiListAdapter.swapCursor(null);
    }


    public static SavedWifiListFragment newInstance(String param) {
        Log.d(TAG, "newInstance: ");
        SavedWifiListFragment fragment = new SavedWifiListFragment();
        Bundle args = new Bundle();
        args.putString(SavedWifiListActivity.WIFI_FRAGMENT_BUNDLE_KEY, param);
        fragment.setArguments(args);
        return fragment;
    }

}

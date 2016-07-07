package net.opencurlybraces.android.projects.wifitoggler.ui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import net.opencurlybraces.android.projects.wifitoggler.R;
import net.opencurlybraces.android.projects.wifitoggler.ui.SavedWifiListAdapter;

/**
 * Created by chris on 07/07/16.
 */
public abstract class BaseListFragment extends ListFragment implements LoaderManager
        .LoaderCallbacks<Cursor> {

    private static final String TAG = "BaseListFragment";
    protected SavedWifiListAdapter mSavedWifiListAdapter = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated: ");
        super.onActivityCreated(savedInstanceState);
        mSavedWifiListAdapter = new SavedWifiListAdapter(getActivity(), null, 0);
        setListAdapter(mSavedWifiListAdapter);
    }

    @Override
    public void onLoadFinished(Loader loader, Cursor data) {
        Log.d(TAG, "onLoadFinished: ");
        mSavedWifiListAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader loader) {
        mSavedWifiListAdapter.swapCursor(null);
    }


    public static void showFragment(Fragment fragmentToShow, final Activity host) {

        FragmentManager fragmentManager = host.getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(
                R.animator.card_flip_right_in,
                R.animator.card_flip_right_out,
                R.animator.card_flip_left_in,
                R.animator.card_flip_left_out);
        transaction.replace(R.id.wifi_list_fragment_container, fragmentToShow);
        transaction.commit();
    }
}

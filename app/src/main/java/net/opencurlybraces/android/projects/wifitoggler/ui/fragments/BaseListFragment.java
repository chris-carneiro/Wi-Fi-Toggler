package net.opencurlybraces.android.projects.wifitoggler.ui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import net.opencurlybraces.android.projects.wifitoggler.Config;
import net.opencurlybraces.android.projects.wifitoggler.R;
import net.opencurlybraces.android.projects.wifitoggler.data.DataAsyncQueryHandler;
import net.opencurlybraces.android.projects.wifitoggler.data.table.SavedWifi;
import net.opencurlybraces.android.projects.wifitoggler.ui.SavedWifiListAdapter;
import net.opencurlybraces.android.projects.wifitoggler.ui.SwipeDismissListViewTouchListener;
import net.opencurlybraces.android.projects.wifitoggler.util.SavedWifiDBUtils;
import net.opencurlybraces.android.projects.wifitoggler.util.SnackBarUndoActionDataHandler;

/**
 * Created by chris on 07/07/16.
 */
public abstract class BaseListFragment extends ListFragment implements LoaderManager
        .LoaderCallbacks<Cursor>, SwipeDismissListViewTouchListener.DismissCallbacks {

    private static final String TAG = "BaseListFragment";
    protected SavedWifiListAdapter mSavedWifiListAdapter = null;
    private SwipeDismissListViewTouchListener mTouchListener = null;
    private DataAsyncQueryHandler mDataAsyncQueryHandler;

    protected abstract void handleSnackBar(int dismissedObjectPosition);

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
        mDataAsyncQueryHandler = new DataAsyncQueryHandler(getActivity().getContentResolver(),
                mSavedWifiListAdapter);
    }

    @Override
    public void onLoadFinished(Loader loader, Cursor data) {
        Log.d(TAG, "onLoadFinished: ");
        mSavedWifiListAdapter.swapCursor(data);

        if (mTouchListener == null) {
            mTouchListener =
                    new SwipeDismissListViewTouchListener(
                            getListView(),
                            this);
        }
        getListView().setOnTouchListener(mTouchListener);
        // Setting this scroll listener is required to ensure that during ListView scrolling,
        // we don't look for swipes.
        getListView().setOnScrollListener(mTouchListener.makeScrollListener());
        mTouchListener.resetDeletedViews();
    }

    @Override
    public void onLoaderReset(Loader loader) {
        mSavedWifiListAdapter.swapCursor(null);
    }


    @Override
    public boolean canDismiss(int position) {
        return true;
    }

    @Override
    public void onDismiss(ListView listView, int[] reverseSortedPositions) {
        Cursor cursor = (Cursor) mSavedWifiListAdapter.getItem(reverseSortedPositions[0]);
        ContentValues cv = SavedWifiDBUtils.getReversedItemAutoToggleValue(cursor);

        int itemId = (int) mSavedWifiListAdapter.getItemId(reverseSortedPositions[0]);
        updateAutoToggleValueWithId(itemId, cv);

        mSavedWifiListAdapter.notifyDataSetChanged();
        handleSnackBar(reverseSortedPositions[0]);
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

    private void updateAutoToggleValueWithId(long itemId, ContentValues cv) {
        mDataAsyncQueryHandler.startUpdate(Config.TOKEN_UPDATE,
                itemId,
                SavedWifi.CONTENT_URI,
                cv,
                SavedWifi.whereID, new String[]{String.valueOf
                        (itemId)});
    }

    protected void showUndoSnackBar(String confirmationMessage, View.OnClickListener
            onClickListener) {

        Snackbar.make(getListView(), confirmationMessage,
                Snackbar.LENGTH_LONG).setAction(R.string.wifi_undo_action_text, onClickListener)
                .setActionTextColor(ContextCompat.getColor(getActivity(), android.R.color.white))
                .show();
    }

    @NonNull
    protected SnackBarUndoActionDataHandler.UndoData prepareSnackBarUndoDataObject(int reverseSortedPosition, boolean activateAutoToggle) {
        int itemId = (int) mSavedWifiListAdapter.getItemId(reverseSortedPosition);
        ContentValues cv = new ContentValues();
        cv.put(SavedWifi.AUTO_TOGGLE, activateAutoToggle);
        return new SnackBarUndoActionDataHandler.UndoData
                (itemId, cv);
    }

    protected String formatSnackBarMessage(String ssid, int resMessage) {
        String confirmation = getResources().getString(resMessage);
        return String.format(confirmation, ssid);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getListView().setOnTouchListener(null);
        getListView().setOnScrollListener(null);
    }
}

package net.opencurlybraces.android.projects.wifitoggler.ui;

import android.app.Activity;
import android.content.ContentProviderOperation;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import net.opencurlybraces.android.projects.wifitoggler.R;
import net.opencurlybraces.android.projects.wifitoggler.data.model.Wifi;
import net.opencurlybraces.android.projects.wifitoggler.data.table.SavedWifi;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by chris on 18/09/15.
 */
public class ActionModeHelper implements
        AdapterView.OnItemLongClickListener, AbsListView.OnItemClickListener,
        AbsListView.MultiChoiceModeListener {

    private static final String TAG = "ActionModeHelper";

    private final SavedWifiListActivity mWifiListActivity;
    private final ListView mWifiTogglerWifiList;
    private ActionMode mActionMode;
    private final SavedWifiListAdapter mSavedWifiCursorAdapter;

    private final SparseBooleanArray mSelectedItemsSpecs = new SparseBooleanArray();


    public ActionModeHelper(SavedWifiListActivity wifiListActivity, ListView savedWifiListView) {
        this.mWifiListActivity = wifiListActivity;
        this.mWifiTogglerWifiList = savedWifiListView;
        mSavedWifiCursorAdapter = (SavedWifiListAdapter) savedWifiListView.getAdapter();
        mWifiTogglerWifiList.setMultiChoiceModeListener(this);
    }


    private void setAutoToggleValueForSelectedItems(int position, int id, boolean selected) {
        if (selected) {
            Cursor wifiCursor = mSavedWifiCursorAdapter.getCursor();
            wifiCursor.moveToPosition(position);
            boolean isAutoToggle = (wifiCursor.getInt(wifiCursor.getColumnIndexOrThrow
                    (SavedWifi.AUTO_TOGGLE)) > 0);
            mSelectedItemsSpecs.put(id, !isAutoToggle);
        } else {
            mSelectedItemsSpecs.delete(id);
        }
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {

        Log.d(TAG, "onCreateActionMode");
        MenuInflater inflater = mWifiListActivity.getMenuInflater();
        inflater.inflate(R.menu.menu_saved_wifi_action_mode, menu);
        mSavedWifiCursorAdapter.setIsActionMode(true);
        mActionMode = mode;
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        Log.d(TAG, "onPrepareActionMode");
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

        switch (item.getItemId()) {
            case R.id.item_reverse_auto_toggle:

                updateItemsAsync();
                mActionMode.finish();
                break;
            case R.id.item_settings_select_all:
                selectAllItems();
                break;
        }

        return true;
    }

    private void selectAllItems() {
        int count = mSavedWifiCursorAdapter.selectAllItems();
        for (int i = 0; i < count; i++) {
            mWifiTogglerWifiList.setItemChecked(i, true);
        }
    }

    private void updateItemsAsync() {
        AsyncUpdate performAsyncUpdate = new AsyncUpdate(mWifiListActivity);
        performAsyncUpdate.pushSelectedItemSpecs(mSelectedItemsSpecs);
        performAsyncUpdate.execute();
    }


    @Override
    public void onDestroyActionMode(ActionMode mode) {
        Log.d(TAG, "OnDestroyActionMode");
        mActionMode = null;
        mSavedWifiCursorAdapter.setIsActionMode(false);
        mSavedWifiCursorAdapter.clearSelectedItems();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "onItemLONGClick position=" + position);

        mSavedWifiCursorAdapter.setIsActionMode(true);
        mWifiTogglerWifiList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mWifiTogglerWifiList.setItemChecked(position, true);
        return true;

    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "onItemClick");
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        Log.d(TAG, "onItemCheckedChanged");
        if (mActionMode == null) return;
        mSavedWifiCursorAdapter.setSelectedItem(position, checked);

        /**
         *   {@link super()#onItemCheckedStateChanged(ActionMode, int, long, boolean)} calls
         *   {@link ActionMode#finish()} when {@link
         *   ListView#getCheckedItemCount()} equals 0
         *
         *   For some reason, we can't rely on {@link  ListView#getCheckedItemCount()}.
         *   When in selection mode, if the app is paused and resumed, the checked item count is
         *   incremented by 1 and {@link ListView#getCheckedItemCount()} returns the wrong
         *   value whereas {@link ListView#getCheckedItemPositions()} size is right.
         *   As a consequence I decided to rely on my own cache and finish action mode manually.
         */

        if (mSavedWifiCursorAdapter.getSelectedItemCount() == 0) {
            mActionMode.finish();
            return;
        }
        mActionMode.setTitle(
                mSavedWifiCursorAdapter.getSelectedItemCount() + "");
        setAutoToggleValueForSelectedItems(position, (int) id, checked);
    }


    /**
     * Used to asynchronously update wifi items in DB.
     */
    private static class AsyncUpdate extends
            AsyncTask<Void, Void, ArrayList<ContentProviderOperation>> {

        private SparseBooleanArray mSelectedItemsSpecs = null;

        private final SavedWifiListActivity mWifiListActivity;

        public AsyncUpdate(Activity listActivity) {
            this.mWifiListActivity = (SavedWifiListActivity) new WeakReference<>(listActivity)
                    .get();
        }

        @Override
        protected ArrayList<ContentProviderOperation> doInBackground(Void... params) {
            if (mWifiListActivity == null) return null;
            ArrayList<Wifi> wifiToUpdate = prepareWifiBatchUpdate();

            return mWifiListActivity.udpateBatchWifiToggleState(wifiToUpdate);
        }

        @Override
        protected void onPostExecute(ArrayList<ContentProviderOperation> operations) {
            super.onPostExecute(operations);
            if (mWifiListActivity == null) return; //you never know...
            mWifiListActivity.startBatchUpdate(operations);
        }


        public void pushSelectedItemSpecs(SparseBooleanArray selectedItems) {
            this.mSelectedItemsSpecs = selectedItems;
        }

        private ArrayList<Wifi> prepareWifiBatchUpdate() {
            ArrayList<Wifi> wifiToUpdate = new ArrayList<>(mSelectedItemsSpecs.size());
            for (int i = 0; i < mSelectedItemsSpecs.size(); i++) {
                Wifi wifi = new Wifi.WifiBuilder().id(mSelectedItemsSpecs.keyAt(i)).autoToggle
                        (mSelectedItemsSpecs.valueAt(i)).build();
                wifiToUpdate.add(wifi);
            }
            return wifiToUpdate;
        }
    }

}

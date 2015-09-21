package net.opencurlybraces.android.projects.wifitoggler.ui;

import android.database.Cursor;
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
        Log.d(TAG, "ActionModeHelper constructor");
        this.mWifiListActivity = wifiListActivity;
        this.mWifiTogglerWifiList = savedWifiListView;
        mSavedWifiCursorAdapter = (SavedWifiListAdapter) savedWifiListView.getAdapter();
        Log.d(TAG, "mSavedWifiCursorAdapter=" + mSavedWifiCursorAdapter);
        mWifiTogglerWifiList.setMultiChoiceModeListener(this);
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
                ArrayList<Wifi> wifiToUpdate = prepareWifiBatchUpdate();
                mWifiListActivity.udpateBatchWifiToggleState(wifiToUpdate);
                break;
        }
        mActionMode.finish();
        return true;
    }


    @Override
    public void onDestroyActionMode(ActionMode mode) {
        Log.d(TAG, "OnDestroyActionMode");
        mActionMode = null;
        mSavedWifiCursorAdapter.setIsActionMode(false);
//        mSavedWifiCursorAdapter.clearSelectedItems();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "onItemLONGClick position=" + position + " mActionMode=" + mActionMode);

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
        Log.d(TAG, "onItemCheckedChanged actionMode=" + mode);
        if (mActionMode == null) return;
        mSavedWifiCursorAdapter.toggleSelectedItem(position);
        mActionMode.setTitle(
                mWifiTogglerWifiList.getCheckedItemCount() + "");
        setAutoToggleValueForSelectedItems(position, (int) id, checked);
    }

    public ActionMode getActionMode() {
        return mActionMode;
    }
}

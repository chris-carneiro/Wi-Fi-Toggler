package net.opencurlybraces.android.projects.wifitoggler.ui;

import android.app.LoaderManager;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentSender;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import net.opencurlybraces.android.projects.wifitoggler.Config;
import net.opencurlybraces.android.projects.wifitoggler.R;
import net.opencurlybraces.android.projects.wifitoggler.WifiToggler;
import net.opencurlybraces.android.projects.wifitoggler.data.DataAsyncQueryHandler;
import net.opencurlybraces.android.projects.wifitoggler.data.model.Wifi;
import net.opencurlybraces.android.projects.wifitoggler.data.table.SavedWifi;
import net.opencurlybraces.android.projects.wifitoggler.ui.SwipeDismissListViewTouchListener
        .DismissCallbacks;
import net.opencurlybraces.android.projects.wifitoggler.util.NetworkUtils;
import net.opencurlybraces.android.projects.wifitoggler.util.StartupUtils;

import java.lang.ref.WeakReference;


public abstract class SavedWifiListActivityAbstract extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        DataAsyncQueryHandler.AsyncQueryListener, DismissCallbacks, View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<LocationSettingsResult> {

    private static final String TAG = "BaseListActivity";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1002;
    private static final String[] PROJECTION_SSID_AUTO_TOGGLE = new String[]{SavedWifi._ID,
            SavedWifi
                    .SSID, SavedWifi.AUTO_TOGGLE};

    protected static final String INSTANCE_KEY_FIRST_VISIBLE_POSITION = "firstVisiblePosition";
    protected static final String INSTANCE_KEY_OFFSET_FROM_TOP = "offsetFromTop";

    protected SavedWifiListAdapter mSavedWifiCursorAdapter = null;
    protected DataAsyncQueryHandler mDataAsyncQueryHandler = null;
    protected ListView mWifiTogglerWifiList = null;
    protected TextView mEmptyView = null;
    protected RelativeLayout mDismissConfirmationBanner = null;
    protected TextView mDismissConfirmationText = null;
    protected ViewAutoHideHandler mAutoHideHandler = null;

    private int REQUEST_CHECK_SETTINGS = 1003;
    private Bundle mSavedInstanceState = null;
    private SwipeDismissListViewTouchListener mTouchListener = null;
    private GoogleApiClient mGoogleApiClient = null;
    private LocationRequest mLocationRequest = null;
    private PendingResult<LocationSettingsResult> mLocationSettingsResult = null;

    protected abstract void setListAdapter();
    protected abstract void handleUndoAction();
    protected abstract void bindViews();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Config.DEBUG_MODE) {
            StartupUtils.startStrictMode();
        }
        super.onCreate(savedInstanceState);

        mDataAsyncQueryHandler = new DataAsyncQueryHandler(getContentResolver(), this);
        initCursorLoader();
        mSavedWifiCursorAdapter = (SavedWifiListAdapter) initCursorAdapter();

        if (Config.RUNNING_MARSHMALLOW) {
            if (checkPlayServices()) {
                buildGoogleApiClient();
                buildLocationRequest();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();

            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(mLocationRequest);

            mLocationSettingsResult = LocationServices.SettingsApi.checkLocationSettings
                    (mGoogleApiClient, builder
                            .build());

            mLocationSettingsResult.setResultCallback(this);

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setListAdapter();
        restoreListViewState();
        if (NetworkUtils.isWifiEnabled(this)) {
            WifiToggler.removeDeletedSavedWifiFromDB(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWifiTogglerWifiList.setOnTouchListener(null);
        mWifiTogglerWifiList.setOnScrollListener(null);
    }

    @Override
    public void onResult(LocationSettingsResult locationSettingsResult) {
        final Status status = locationSettingsResult.getStatus();

        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                Log.d(TAG, "LocationSettingsStatusCodes.SUCCESS");
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                // Location settings are not satisfied. But could be fixed by showing
                // the user
                // a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    status.startResolutionForResult(
                            SavedWifiListActivityAbstract.this,
                            REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException e) {
                    // Ignore the error.
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                // Location settings are not satisfied. However, we have no way to
                // fix the
                // settings so we won't show the dialog.
                Log.d(TAG, "LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE");
                //TODO in the future warn the user
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        int firstVisiblePosition = mWifiTogglerWifiList.getFirstVisiblePosition();
        View v = mWifiTogglerWifiList.getChildAt(0);
        int top = (v == null) ? 0 : (v.getTop() - mWifiTogglerWifiList.getPaddingTop());

        outState.putInt(INSTANCE_KEY_FIRST_VISIBLE_POSITION, firstVisiblePosition);
        outState.putInt(INSTANCE_KEY_OFFSET_FROM_TOP, top);
        mSavedInstanceState = outState; // Needed on Pause/Resume to keep selected items' state
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "onRestoreInstanceState");
        super.onRestoreInstanceState(savedInstanceState);
        mSavedInstanceState = savedInstanceState;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            displaySettingsActivity();
            return true;
        }

        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_saved_wifi_list, menu);
        return true;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mSavedWifiCursorAdapter.swapCursor(data);

        if (mTouchListener == null) {
            mTouchListener =
                    new SwipeDismissListViewTouchListener(
                            mWifiTogglerWifiList,
                            this);
        }
        mWifiTogglerWifiList.setOnTouchListener(mTouchListener);
        // Setting this scroll listener is required to ensure that during ListView scrolling,
        // we don't look for swipes.
        mWifiTogglerWifiList.setOnScrollListener(mTouchListener.makeScrollListener());
        mTouchListener.resetDeletedViews();

        mWifiTogglerWifiList.setEmptyView(mEmptyView);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mSavedWifiCursorAdapter.swapCursor(null);
    }

    @Override
    public void onUpdateComplete(int token, Object cookie, int result) {
        if (cookie != null) {
            cacheItemIdForUndo((long) cookie);
        }
    }

    @Override
    public void onBatchInsertComplete(int token, Object cookie, ContentProviderResult[] results) {}

    @Override
    public void onInsertComplete(int token, Object cookie, Uri uri) {}

    @Override
    public void onBatchUpdateComplete(int token, Object cookie, ContentProviderResult[] results) {
        if (Config.DEBUG_MODE) {
            queryWifiData();
        }
    }

    @Override
    public void onQueryComplete(int token, Object cookie, Cursor cursor) {
        if (Config.DEBUG_MODE) {
            logCursorData(cursor);
        }
    }

    private void buildLocationRequest() {
        mLocationRequest = new LocationRequest()
                .setInterval(Config.DELAY_FIVE_SECONDS)
                .setFastestInterval(1000)
                .setPriority(LocationRequest.PRIORITY_LOW_POWER);
    }

    protected void bindListView() {
        mWifiTogglerWifiList = (ListView) findViewById(android.R.id.list);
        mEmptyView = (TextView) findViewById(android.R.id.empty);
    }

    protected void displaySettingsActivity() {
        Intent preferencesIntent = new Intent(this, PreferencesActivity.class);
        startActivity(preferencesIntent);
    }

    protected void initCursorLoader() {
        getLoaderManager().initLoader(0, null, this);
    }

    protected CursorAdapter initCursorAdapter() {
        Log.d(TAG, "initCursorAdapter");
        if (mSavedWifiCursorAdapter == null) {
            mSavedWifiCursorAdapter = new SavedWifiListAdapter(this, null, 0);
        }
        return mSavedWifiCursorAdapter;
    }

    protected void handleSavedWifiListLoading(boolean isChecked) {
        Log.d(TAG, "handleSavedWifiListLoading=" + isChecked);
        if (isChecked) {
            mWifiTogglerWifiList.setAdapter(mSavedWifiCursorAdapter);
        } else {
            mWifiTogglerWifiList.setAdapter(null);
        }
    }

    protected void restoreListViewState() {
        Log.d(TAG, "restoreListViewState ");

        if (mSavedInstanceState != null) {
            int firstVisiblePosition = mSavedInstanceState.getInt
                    (INSTANCE_KEY_FIRST_VISIBLE_POSITION);
            int offsetTop = mSavedInstanceState.getInt(INSTANCE_KEY_OFFSET_FROM_TOP);
            // restore ListView scroll position
            mWifiTogglerWifiList.setSelectionFromTop(firstVisiblePosition, offsetTop);
        }
    }

    public ContentValues buildContentValuesForUpdate(int position) {
        Cursor cursor = (Cursor) mSavedWifiCursorAdapter.getItem
                (position);
        boolean isAutoToggle = (cursor.getInt(cursor
                .getColumnIndexOrThrow
                        (SavedWifi.AUTO_TOGGLE)) > 0);
        ContentValues cv = new ContentValues();
        cv.put(SavedWifi.AUTO_TOGGLE, !isAutoToggle);
        return cv;
    }

    public void updateAutoToggleValue(long itemId, int position) {
        ContentValues cv = buildContentValuesForUpdate(position);

        mDataAsyncQueryHandler.startUpdate(Config.TOKEN_UPDATE,
                itemId,
                SavedWifi.CONTENT_URI,
                cv,
                SavedWifi.whereID, new String[]{String.valueOf
                        (itemId)});
    }

    private void queryWifiData() {
        mDataAsyncQueryHandler.startQuery(1, null, SavedWifi.CONTENT_URI,
                PROJECTION_SSID_AUTO_TOGGLE,
                null, null, null);
    }

    private void logCursorData(final Cursor cursor) {
        try {
            Log.d(TAG, "Wifi count=" + cursor.getCount());
            while (cursor.moveToNext()) {
                Wifi wifi = Wifi.buildForCursor(cursor);
                Log.d(TAG, "Ssid=" + wifi.getSsid() + " id=" + wifi.get_id() + " isAutoToggle=" +
                        wifi.isAutoToggle());
            }
            cursor.close();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @Override
    public boolean canDismiss(int position) {
        return true;
    }

    @Override
    public void onDismiss(ListView listView, int[] reverseSortedPositions) {
        Log.d(TAG, "onDismiss");

        mAutoHideHandler = new ViewAutoHideHandler(mDismissConfirmationBanner);
        mAutoHideHandler.sendMessageDelayed(Message.obtain(mAutoHideHandler,
                        Config.WHAT_AUTO_HIDE),
                Config.DELAY_FIVE_SECONDS);

    }


    public void cacheItemIdForUndo(long itemId) {
        mSavedWifiCursorAdapter.setItemIdToUndo(itemId);
    }

    public void displayConfirmationBannerWithUndo(int position, int messageResourceId) {
        Cursor cursor = (Cursor) mSavedWifiCursorAdapter.getItem
                (position);
        String confirmation = getResources().getString(messageResourceId);
        String ssid = cursor.getString(cursor.getColumnIndexOrThrow(SavedWifi.SSID));
        String confirmationMessage = String.format(confirmation, ssid);
        mDismissConfirmationText.setText(confirmationMessage);
        mDismissConfirmationBanner.animate().alpha(1.0f).setDuration(300);
    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                finish();
            }
            return false;
        }
        return true;
    }

    protected static class ViewAutoHideHandler extends Handler {
        private final WeakReference<RelativeLayout> mViewToHide;

        public ViewAutoHideHandler(RelativeLayout viewToHide) {
            mViewToHide = new WeakReference<>(viewToHide);
        }

        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "handleMessage");
            RelativeLayout viewToHide = mViewToHide.get();
            if (viewToHide != null) {
                Log.d(TAG, "Message received alpha =" + viewToHide.getAlpha());
                viewToHide.animate().alpha(0.0f).setDuration(300);
            }
        }
    }
}

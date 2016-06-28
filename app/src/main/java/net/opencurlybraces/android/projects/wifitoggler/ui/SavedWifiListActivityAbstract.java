package net.opencurlybraces.android.projects.wifitoggler.ui;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentSender;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
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
import net.opencurlybraces.android.projects.wifitoggler.data.table.SavedWifi;
import net.opencurlybraces.android.projects.wifitoggler.ui.SwipeDismissListViewTouchListener
        .DismissCallbacks;
import net.opencurlybraces.android.projects.wifitoggler.util.NetworkUtils;
import net.opencurlybraces.android.projects.wifitoggler.util.PrefUtils;
import net.opencurlybraces.android.projects.wifitoggler.util.SavedWifiDBUtils;
import net.opencurlybraces.android.projects.wifitoggler.util.StartupUtils;


public abstract class SavedWifiListActivityAbstract extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>, DismissCallbacks, View.OnClickListener,
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
    protected ListView mWifiTogglerWifiList = null;
    protected TextView mEmptyView = null;
    protected RelativeLayout mDismissConfirmationBanner = null;
    protected TextView mDismissConfirmationText = null;
    protected Handler mAutoHideHandler = null;

    private int REQUEST_CHECK_SETTINGS = 1003;
    private Bundle mSavedInstanceState = null;
    private SwipeDismissListViewTouchListener mTouchListener = null;
    private GoogleApiClient mGoogleApiClient = null;
    private LocationRequest mLocationRequest = null;
    private DataAsyncQueryHandler mDataAsyncQueryHandler = null;

    protected abstract void bindViews();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Config.DEBUG_MODE) {
            StartupUtils.startStrictMode();
        }
        super.onCreate(savedInstanceState);


        initCursorLoader();
        mSavedWifiCursorAdapter = (SavedWifiListAdapter) initCursorAdapter();
        mAutoHideHandler = new Handler();
        mDataAsyncQueryHandler = new DataAsyncQueryHandler(getContentResolver(), mSavedWifiCursorAdapter);
        handlePostLollipopRequirements();

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();

            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(mLocationRequest);

            PendingResult<LocationSettingsResult> mLocationSettingsResult = LocationServices
                    .SettingsApi.checkLocationSettings
                            (mGoogleApiClient, builder
                                    .build());

            mLocationSettingsResult.setResultCallback(this);

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setAdapter();
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

    private void setAdapter() {
        Log.d(TAG, "setAdapter");
        if (PrefUtils.isWifiTogglerActive(this)) {
            mWifiTogglerWifiList.setAdapter(mSavedWifiCursorAdapter);
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

    private void displaySettingsActivity() {
        Intent preferencesIntent = new Intent(this, PreferencesActivity.class);
        startActivity(preferencesIntent);
    }

    private void initCursorLoader() {
        getLoaderManager().initLoader(0, null, this);
    }

    private CursorAdapter initCursorAdapter() {
        Log.d(TAG, "initCursorAdapter");
        if (mSavedWifiCursorAdapter == null) {
            mSavedWifiCursorAdapter = new SavedWifiListAdapter(this, null, 0);
        }
        return mSavedWifiCursorAdapter;
    }


    private void restoreListViewState() {
        Log.d(TAG, "restoreListViewState ");

        if (mSavedInstanceState != null) {
            int firstVisiblePosition = mSavedInstanceState.getInt
                    (INSTANCE_KEY_FIRST_VISIBLE_POSITION);
            int offsetTop = mSavedInstanceState.getInt(INSTANCE_KEY_OFFSET_FROM_TOP);
            // restore ListView scroll position
            mWifiTogglerWifiList.setSelectionFromTop(firstVisiblePosition, offsetTop);
        }
    }

    protected void handleUndoAction(ContentValues cv) {
        hideBanner();
        updateAutoToggleValueWithId(mSavedWifiCursorAdapter.getItemIdToUndo(), cv);
    }

    private void updateAutoToggleValueWithId(long itemId, ContentValues cv) {
        mDataAsyncQueryHandler.startUpdate(Config.TOKEN_UPDATE,
                itemId,
                SavedWifi.CONTENT_URI,
                cv,
                SavedWifi.whereID, new String[]{String.valueOf
                        (itemId)});
    }


    @Override
    public boolean canDismiss(int position) {
        return true;
    }

    @Override
    public void onDismiss(ListView listView, int[] reverseSortedPositions) {
        Log.d(TAG, "onDismiss");
        scheduleUndoBannerAutoHide();

        Cursor cursor = (Cursor) mSavedWifiCursorAdapter.getItem(reverseSortedPositions[0]);
        ContentValues cv = SavedWifiDBUtils.getReversedItemAutoToggleValue(cursor);

        int itemId = (int) mSavedWifiCursorAdapter.getItemId(reverseSortedPositions[0]);
        updateAutoToggleValueWithId(itemId, cv);
        mSavedWifiCursorAdapter.notifyDataSetChanged();
    }

    private void scheduleUndoBannerAutoHide() {
        // Fix for issue #4
        mAutoHideHandler.removeCallbacks(mHideBannerRunnable);
        mAutoHideHandler.postDelayed(mHideBannerRunnable, Config.DELAY_FIVE_SECONDS);
    }

    protected void showUndoSnackBar(final Cursor cursor, int
            messageResourceId) {

        String confirmation = getResources().getString(messageResourceId);
        String ssid = cursor.getString(cursor.getColumnIndexOrThrow(SavedWifi.SSID));
        String confirmationMessage = String.format(confirmation, ssid);
        mDismissConfirmationText.setText(confirmationMessage);
        mDismissConfirmationBanner.animate().alpha(1.0f).setDuration(300);
    }

    private void buildGoogleApiClient() {
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

    /**
     * Created to fix Issue #4 as a simple {@link Handler#removeCallbacksAndMessages(Object)} or
     * {@link Handler#removeMessages(int)} wouldn't work to cancel the delayed scheduled message.
     */
    private Runnable mHideBannerRunnable = new Runnable() {
        public void run() {
            Log.d(TAG, "Runnable run");
            mDismissConfirmationBanner.animate().alpha(0.0f).setDuration(300);
        }
    };

    private void handlePostLollipopRequirements() {
        if (Config.RUNNING_MARSHMALLOW) {
            if (checkPlayServices()) {
                buildGoogleApiClient();
                buildLocationRequest();
            }
        }
    }

    private void hideBanner() {
        // hides undo banner immediately
        mAutoHideHandler.post(mHideBannerRunnable);
    }

}

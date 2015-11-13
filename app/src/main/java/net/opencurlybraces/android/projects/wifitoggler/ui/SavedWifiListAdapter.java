package net.opencurlybraces.android.projects.wifitoggler.ui;

import android.animation.Animator;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CursorAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.opencurlybraces.android.projects.wifitoggler.R;
import net.opencurlybraces.android.projects.wifitoggler.data.table.SavedWifi;
import net.opencurlybraces.android.projects.wifitoggler.util.NetworkUtils;

/**
 * Created by chris on 13/06/15.
 */
public class SavedWifiListAdapter extends CursorAdapter {
    private static final String TAG = "SavedWifiListAdapter";


    final LayoutInflater mLayoutInflater;
    protected long mItemIdUndo;

    public long getItemIdToUndo() {
        return mItemIdUndo;
    }
    public void setItemIdToUndo(long itemIdToUndo) {
        this.mItemIdUndo = itemIdToUndo;
    }

    public SavedWifiListAdapter(Context context, Cursor c, int flag) {
        super(context, c, flag);

        mLayoutInflater = LayoutInflater.from(context);
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = mLayoutInflater.inflate(R.layout.saved_wifi_list_row, parent, false);
        return bindViewTags(cursor, view);
    }


    @Override
    public void bindView(final View view, Context context, Cursor cursor) {
        TextView ssidView = (TextView) view.getTag(R.string.tag_key_ssid);
        TextView statusView = (TextView) view.getTag(R.string.tag_key_status);
        final RelativeLayout row = (RelativeLayout) view.getTag(R.string
                .tag_key_row);

        int ssidIndex = (int) view.getTag(R.string.tag_key_ssid_index);
        int statusIndex = (int) view.getTag(R.string.tag_key_status_index);
        int autoToggleIndex = (int) view.getTag(R.string.tag_key_row_index);

        String ssid = cursor.getString(ssidIndex);
        int status = cursor.getInt(statusIndex);
        int autoToggle = cursor.getInt(autoToggleIndex);
        boolean isAutoToggle = (autoToggle > 0);
        ssidView.setText(ssid);
        ssidView.setPadding(15, 0, 0, 0);

        String statusValue = toStringStatus(context, status);
        statusView.setText(statusValue);
        statusView.setPadding(15, 0, 0, 0);

        RelativeLayout.LayoutParams lp = createLayoutParamsWithStatus(status);
        ssidView.setLayoutParams(lp);
        setTextColor(context, ssidView, statusView, isAutoToggle);

        handleUndoItemAnimation(cursor, row);

    }

    //TOOD create animation builder
    public void handleUndoItemAnimation(Cursor cursor, final RelativeLayout row) {
        if (getItemId(cursor.getPosition()) == getItemIdToUndo()) {
            setItemIdToUndo(-1);// prevent animation onResume
            final ViewTreeObserver observer = row.getViewTreeObserver();

            observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

                @Override
                public void onGlobalLayout() {
                    if (observer.isAlive()) {
                        observer.removeOnGlobalLayoutListener(
                                this);
                    }
                    row.setTranslationX(row.getWidth());
                    row.setAlpha(0.0f);
                    row.animate().alpha(1f).translationX(0)
                            .setDuration(500).setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {
                        }
                    });
                }
            });
        }
    }

    private void setTextColor(Context context, TextView ssidView, TextView statusView, boolean
            isAutoToggle) {
        if (isAutoToggle) {
            ssidView.setTextColor(context.getResources().getColor(android.R.color
                    .black));
        } else {
            ssidView.setTextColor(context.getResources().getColor(R.color
                    .material_grey_400));
            statusView.setTextColor(context.getResources().getColor(R.color
                    .material_grey_400));
        }
    }

    private RelativeLayout.LayoutParams createLayoutParamsWithStatus(int status) {
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout
                .LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        if (status != NetworkUtils.WifiAdapterStatus.CONNECTED) {
            lp.addRule(RelativeLayout.CENTER_VERTICAL);
        }
        return lp;
    }

    private String toStringStatus(final Context context, int status) {
        String wifiStatus = "";
        switch (status) {
            case NetworkUtils.WifiAdapterStatus.CONNECTED:
                wifiStatus = context.getString(R.string.connected_saved_wifi_status);
                break;
        }
        return wifiStatus;
    }

    private View bindViewTags(final Cursor cursor, final View view) {

        RelativeLayout row = (RelativeLayout) view.findViewById(R.id.saved_wifi_row_layout);
        TextView ssid = (TextView) view.findViewById(R.id.saved_wifi_ssid);
        TextView status = (TextView) view.findViewById(R.id.saved_wifi_state);

        int ssidIndex = cursor.getColumnIndexOrThrow(SavedWifi.SSID);
        int statusIndex = cursor.getColumnIndexOrThrow(SavedWifi.STATUS);
        int autoToggleIndex = cursor.getColumnIndexOrThrow(SavedWifi.AUTO_TOGGLE);

        view.setTag(R.string.tag_key_ssid, ssid);
        view.setTag(R.string.tag_key_status, status);
        view.setTag(R.string.tag_key_row, row);
        view.setTag(R.string.tag_key_ssid_index, ssidIndex);
        view.setTag(R.string.tag_key_status_index, statusIndex);


        /**
         * the row's background changes according to autotoggle value
         */
        view.setTag(R.string.tag_key_row_index, autoToggleIndex);


        return view;
    }

}

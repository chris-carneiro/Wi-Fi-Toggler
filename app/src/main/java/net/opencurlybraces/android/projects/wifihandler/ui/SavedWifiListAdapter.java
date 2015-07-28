package net.opencurlybraces.android.projects.wifihandler.ui;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.opencurlybraces.android.projects.wifihandler.R;
import net.opencurlybraces.android.projects.wifihandler.data.table.SavedWifi;
import net.opencurlybraces.android.projects.wifihandler.util.NetworkUtils;

/**
 * Created by chris on 13/06/15.
 */
public class SavedWifiListAdapter extends CursorAdapter {
    private static final String TAG = "SavedWifiListAdapter";

    final LayoutInflater mLayoutInflater;

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
    public void bindView(View view, Context context, Cursor cursor) {
        Log.d(TAG, "bindView");

        TextView ssidView = (TextView) view.getTag(R.string.tag_key_ssid);
        TextView statusView = (TextView) view.getTag(R.string.tag_key_status);
        int ssidIndex = (int) view.getTag(R.string.tag_key_ssid_index);
        int statusIndex = (int) view.getTag(R.string.tag_key_status_index);

        String ssid = cursor.getString(ssidIndex);
        int status = cursor.getInt(statusIndex);

        ssidView.setText(ssid);
        ssidView.setPadding(15, 0, 0, 0);

        String statusValue = toStringStatus(context, status);
        statusView.setText(statusValue);
        statusView.setPadding(15, 0, 0, 0);

        RelativeLayout.LayoutParams lp = createLayoutParamsWithStatus(status);
        ssidView.setLayoutParams(lp);

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
        TextView ssid = (TextView) view.findViewById(R.id.saved_wifi_ssid);
        TextView status = (TextView) view.findViewById(R.id.saved_wifi_state);
        int ssidIndex = cursor.getColumnIndexOrThrow(SavedWifi.SSID);
        int statusIndex = cursor.getColumnIndexOrThrow(SavedWifi.STATUS);

        view.setTag(R.string.tag_key_ssid, ssid);
        view.setTag(R.string.tag_key_status, status);
        view.setTag(R.string.tag_key_ssid_index, ssidIndex);
        view.setTag(R.string.tag_key_status_index, statusIndex);
        return view;
    }
}

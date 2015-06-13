package net.opencurlybraces.android.projects.wifihandler;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import net.opencurlybraces.android.projects.wifihandler.data.model.UserWifi;

import java.util.List;

/**
 * Created by chris on 13/06/15.
 */
public class ConfiguredWifiListAdapter extends BaseAdapter {


    private final List<UserWifi> mUserWifis;

    public ConfiguredWifiListAdapter(@NonNull List<UserWifi> userWifis) {
        mUserWifis = userWifis;
    }

    static class ViewHolder {
        TextView ssid;
        TextView active;
    }

    @Override
    public int getCount() {
        return (mUserWifis != null ? mUserWifis.size() : 0);
    }

    @Override
    public UserWifi getItem(int position) {
        return (mUserWifis != null ? mUserWifis.get(position) : null);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = ((LayoutInflater) parent.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(R.layout.configured_wifi_list_row, parent, false);
            viewHolder = new ViewHolder();

            /**
             * Keep a reference of the widgets so that the adapter doesn't call
             * findViewById() each time getView() is called
             **/
            viewHolder.ssid = (TextView) convertView
                    .findViewById(R.id.configured_wifi_ssid);
            viewHolder.active = (TextView) convertView
                    .findViewById(R.id.configured_wifi_active_state);

            /** Set the reference of the viewHolder on the Item **/
            convertView.setTag(viewHolder);
        } else {
            /** Get the object's reference we set earlier on the convertView **/
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Resources res = convertView.getContext().getResources();

        UserWifi userWifi = getItem(position);
        viewHolder.ssid.setText(userWifi.SSID);

        String isActive = (userWifi.mActive ? "Connected" : "");
        if (userWifi.mActive) {
            viewHolder.active.setTextColor(res.getColor(android.R.color.holo_green_dark));
            viewHolder.active.setVisibility(View.VISIBLE);
        } else {
            viewHolder.active.setVisibility(View.GONE);
        }
        viewHolder.active.setText(isActive);

        return convertView;
    }
}

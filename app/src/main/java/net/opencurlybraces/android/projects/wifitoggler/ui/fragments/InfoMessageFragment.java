package net.opencurlybraces.android.projects.wifitoggler.ui.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.opencurlybraces.android.projects.wifitoggler.R;

/**
 * Created by chris on 05/07/16.
 */
public class InfoMessageFragment extends Fragment {

    public static final String INFO_MESSAGE_BUNDLE_KEY = "info_message_key";
    private static final String TAG = "InfoMessageFragment";

    public InfoMessageFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {

        View rootView = inflater.inflate(R.layout.info_message_fragment, container, false);
        setInfoMessage(rootView);
        return rootView;
    }

    private void setInfoMessage(View rootView) {
        TextView info = (TextView) rootView.findViewById(R.id.info_message_when_off);
        String infoMessage = getArguments().getString(INFO_MESSAGE_BUNDLE_KEY);
        info.setText(infoMessage);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public static Fragment newInstance(String messageContent) {
        Log.d(TAG, "newInstance: ");
        Fragment fragment = new InfoMessageFragment();
        Bundle args = new Bundle();
        args.putString(INFO_MESSAGE_BUNDLE_KEY, messageContent);
        fragment.setArguments(args);
        return fragment;
    }

}

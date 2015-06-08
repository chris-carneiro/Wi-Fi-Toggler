package net.opencurlybraces.android.projects.wifihandler.data.provider;

import android.net.Uri;

/**
 * Contract class for interacting with {@link WifiHandlerProvider}.  Created by chris on 03/06/15.
 */
public class WifiHandlerContract {

    public static final String AUTHORITY = "net.opencurlybraces.android.projects" +
            ".wifihandler.data.provider";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

}

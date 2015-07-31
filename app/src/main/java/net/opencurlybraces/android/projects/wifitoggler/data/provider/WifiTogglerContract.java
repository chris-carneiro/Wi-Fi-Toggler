package net.opencurlybraces.android.projects.wifitoggler.data.provider;

import android.net.Uri;

/**
 * Contract class for interacting with {@link WifiTogglerProvider}.  Created by chris on 03/06/15.
 */
public class WifiTogglerContract {

    public static final String AUTHORITY = "net.opencurlybraces.android.projects" +
            ".wifitoggler.data.provider";

    public static final String SCHEME = "content://";

    public static final Uri BASE_CONTENT_URI = Uri.parse(SCHEME + AUTHORITY);

}

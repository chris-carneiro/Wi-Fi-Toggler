package net.opencurlybraces.android.projects.wifitoggler.util;

import android.content.ContentValues;
import android.content.Context;
import android.view.View;

import net.opencurlybraces.android.projects.wifitoggler.Config;
import net.opencurlybraces.android.projects.wifitoggler.data.DataAsyncQueryHandler;
import net.opencurlybraces.android.projects.wifitoggler.data.table.SavedWifi;

/**
 * Created by chris on 30/06/16.
 */
public class SnackBarUndoActionDataHandler implements View.OnClickListener {

    private final UndoData mUndoData;
    private final DataAsyncQueryHandler mDataAsyncQueryHandler;

    public SnackBarUndoActionDataHandler(Context context, UndoData undoData) {
        mUndoData = undoData;
        mDataAsyncQueryHandler = new DataAsyncQueryHandler(context.getContentResolver(), null);
    }

    @Override
    public void onClick(View v) {
        updateAutoToggleValueWithId(mUndoData.getItemId(), mUndoData.getValues());
    }

    private void updateAutoToggleValueWithId(long itemId, ContentValues cv) {
        mDataAsyncQueryHandler.startUpdate(Config.TOKEN_UPDATE,
                itemId,
                SavedWifi.CONTENT_URI,
                cv,
                SavedWifi.whereID, new String[]{String.valueOf
                        (itemId)});
    }

    public static class UndoData {

        private final long mItemId;

        public ContentValues getValues() {
            return mValues;
        }

        public long getItemId() {
            return mItemId;
        }

        private final ContentValues mValues;

        public UndoData(long mItemId, ContentValues mValues) {
            this.mItemId = mItemId;
            this.mValues = mValues;
        }
    }
}

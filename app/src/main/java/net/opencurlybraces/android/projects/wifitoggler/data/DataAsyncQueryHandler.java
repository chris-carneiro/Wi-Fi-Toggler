package net.opencurlybraces.android.projects.wifitoggler.data;

import android.content.AsyncQueryHandler;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created to add {@code applyBatch()} as an async query
 * <p/>
 * {@inheritDoc}
 *
 * @see net.opencurlybraces.android.projects.wifitoggler.data.provider .WifiTogglerProvider
 * <p/>
 * Created by chris on 02/07/15.
 */
public class DataAsyncQueryHandler extends AsyncQueryHandler {

    private static final String TAG = "DataAsyncQuery";

    private WeakReference<AsyncQueryListener> mListener;
    private Handler mWorkerThreadHandler = null;
    private static final int EVENT_ARG_INSERT_BATCH = 5;
    private static final int EVENT_ARG_UPDATE_BATCH = 6;
    final WeakReference<ContentResolver> mResolver;
    private static Looper sLooper = null;

    public DataAsyncQueryHandler(ContentResolver cr, AsyncQueryListener listener) {
        super(cr);
        mResolver = new WeakReference<>(cr);
        setQueryListener(listener);

        synchronized (AsyncQueryHandler.class) {
            if (sLooper == null) {
                HandlerThread thread = new HandlerThread("DataAsyncWorker");
                thread.start();

                sLooper = thread.getLooper();
            }
        }
        mWorkerThreadHandler = new WorkerHandler(sLooper);
    }

    public final class WorkerHandlerArgs {
        public Handler handler;
        public String authority;
        public Object result;
        public Object cookie;
        public ArrayList<ContentProviderOperation> operations;
    }


    private class WorkerHandler extends AsyncQueryHandler.WorkerHandler {

        public WorkerHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {

            final ContentResolver resolver = mResolver.get();
            if (resolver == null) return;

            int token = msg.what;
            int event = msg.arg1;
            WorkerHandlerArgs args = (WorkerHandlerArgs) msg.obj;
            switch (event) {

                case EVENT_ARG_INSERT_BATCH:
                case EVENT_ARG_UPDATE_BATCH:
                    try {
                        args.result = resolver.applyBatch(args.authority,
                                args.operations);
                        Message reply = args.handler.obtainMessage(token);
                        reply.obj = args;
                        reply.arg1 = msg.arg1;
                        reply.sendToTarget();
                    } catch (RemoteException | OperationApplicationException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    //if not batch insert let the Super class handle other queries
                    super.handleMessage(msg);
            }

        }
    }


    public interface AsyncQueryListener {
        /**
         * Called when an asynchronous batch insert is completed.
         *
         * @param token   the token to identify the query, passed in from {@link
         *                #startBatchOperations}.
         * @param cookie  the cookie object passed in from {@link #startBatchOperations(int, Object,
         *                String, ArrayList)}.
         * @param results the results of the operations
         */
        void onBatchInsertComplete(int token, Object cookie, ContentProviderResult[]
                results);

        /**
         * Called when an asynchronous query is completed.
         *
         * @param token  the token to identify the query, passed in from {@link #startQuery}.
         * @param cookie the cookie object passed in from {@link #startQuery}.
         * @param cursor The cursor holding the results from the query.
         */
        void onQueryComplete(int token, Object cookie, Cursor cursor);


        /**
         * Called when an asynchronous update is completed.
         *
         * @param token  the token to identify the query, passed in from {@link #startUpdate(int,
         *               Object, Uri, ContentValues, String, String[])}.
         * @param cookie
         * @param result The number of updated rows
         */
        void onUpdateComplete(int token, Object cookie, int result);

        /**
         * Called when an asynchronous insert is completed.
         *
         * @param token  the token to identify the query, passed in from {@link #startInsert(int,
         *               Object, Uri, ContentValues)} (int, Object, Uri, String, String[])}.
         * @param cookie
         * @param uri    row inserted uri
         */
        void onInsertComplete(int token, Object cookie, Uri uri);

        /**
         * Called when an asynchronous batch update is completed.
         *
         * @param token   the token to identify the query, passed in from {@link
         *                #startBatchOperations}.
         * @param cookie  the cookie object passed in from {@link #startBatchOperations(int, Object,
         *                String, ArrayList)}.
         * @param results the results of the operations
         */
        void onBatchUpdateComplete(int token, Object cookie, ContentProviderResult[]
                results);

    }

    public void setQueryListener(AsyncQueryListener listener) {
        mListener = new WeakReference<>(listener);
    }

    protected void onInsertBatchComplete(int token, Object cookie, ContentProviderResult[]
            results) {
        final AsyncQueryListener listener = mListener.get();
        if (listener != null) {
            listener.onBatchInsertComplete(token, cookie, results);
        }
    }

    protected void onUpdateBatchComplete(int token, Object cookie, ContentProviderResult[]
            results) {
        final AsyncQueryListener listener = mListener.get();
        if (listener != null) {
            listener.onBatchUpdateComplete(token, cookie, results);
        }
    }

    @Override
    protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
        final AsyncQueryListener listener = mListener.get();
        if (listener != null) {
            listener.onQueryComplete(token, cookie, cursor);
        } else if (cursor != null) {
            cursor.close();
        }
    }

    @Override
    protected void onUpdateComplete(int token, Object cookie, int result) {
        final AsyncQueryListener listener = mListener.get();
        if (listener != null) {
            listener.onUpdateComplete(token, cookie, result);
        }
    }

    @Override
    protected void onInsertComplete(int token, Object cookie, Uri uri) {
        final AsyncQueryListener listener = mListener.get();
        if (listener != null) {
            listener.onInsertComplete(token, cookie, uri);
        }
    }

    /**
     * This method begins an asynchronous batch operations. When the batch operation is done {@link
     * #onInsertComplete(int, Object, Uri)} or {@link #onUpdateBatchComplete(int, Object,
     * ContentProviderResult[])} is called.
     *
     * @param token      token A token passed into {@link #onInsertComplete} to identify the insert
     *                   operation.
     * @param cookie     cookie An object that gets passed into {@link #onInsertBatchComplete}
     * @param authority  the authority of the ContentProvider to which this batch should be applied
     * @param operations the operations to apply
     */
    public void startBatchOperations(int token, Object cookie, String authority,
                                     ArrayList<ContentProviderOperation> operations) {
        // Use the token as what so cancelOperations works properly
        Message msg = mWorkerThreadHandler.obtainMessage(token);
        msg.arg1 = token;

        WorkerHandlerArgs args = new WorkerHandlerArgs();
        args.handler = this;
        args.authority = authority;
        args.cookie = cookie;
        args.operations = operations;
        msg.obj = args;

        mWorkerThreadHandler.sendMessage(msg);
    }

    @Override
    public void handleMessage(Message msg) {

        int token = msg.what;
        int event = msg.arg1;

        switch (event) {
            case EVENT_ARG_INSERT_BATCH:
                WorkerHandlerArgs args = (WorkerHandlerArgs) msg.obj;
                onInsertBatchComplete(token, args.cookie, (ContentProviderResult[]) args.result);
                break;
            case EVENT_ARG_UPDATE_BATCH:
                WorkerHandlerArgs argsUpdate = (WorkerHandlerArgs) msg.obj;
                onUpdateBatchComplete(token, argsUpdate.cookie, (ContentProviderResult[])
                        argsUpdate.result);
                break;
            default:
                super.handleMessage(msg);
        }

    }

    /**
     * Attempts to cancel insert batch that has not already started. Note that there is no guarantee
     * that the operation will be canceled. They still may result in a call to
     * on[Query/Insert/Update/Delete]Complete after this call has completed.
     */
    public void cancelInsertBatch() {
        mWorkerThreadHandler.removeMessages(EVENT_ARG_INSERT_BATCH);
    }

}

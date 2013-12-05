package ca.dragonflystudios.binder;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;

import ca.dragonflystudios.endu.Endu;
import ca.dragonflystudios.hap.DsContentProvider;

/**
 * Created by Jun Luo on 13-12-04.
 */

public class EnduBinder implements LoaderManager.LoaderCallbacks<Cursor> {
    public interface BindEnduCallback {
        public void bindEndu(Cursor cursor, int position);
    }

    // TODO: refactor the Endu stuff into something like a "Presenter" interface.
    // TODO: refactor the Collection stuff into something like a "Content"/"Model" interface.

    private static int sLoaderId = 1024;
    private static Object sLock = new Object();
    private static int getNextLoaderId() {
        synchronized(sLock) {
            return ++sLoaderId;
        }
    }

    final int mLoaderId;

    LoaderManager mLoaderManager;
    Context mContext;
    DsContentProvider.Collection mCollection;
    String mSelection;
    String[] mSelectionArgs;
    String mSortOrder;
    Endu mEndu;

    public EnduBinder(LoaderManager loaderManager, Context context, Endu endu, DsContentProvider.Collection collection, String selection, String[] selectionArgs, String sortOrder) {
        mLoaderManager = loaderManager;
        mContext = context;
        mEndu = endu;
        mCollection = collection;
        mSelection = selection;
        mSelectionArgs = selectionArgs;
        mSortOrder = sortOrder;

        mLoaderId = getNextLoaderId();
        mLoaderManager.initLoader(mLoaderId, null, this);
    }

    public int getLoaderId() {
        return mLoaderId;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        mCollection.requestSync(mContext, mSelection, mSelectionArgs, mSortOrder);
        return new CursorLoader(mContext, mCollection.getUri(), mCollection.columnNames, mSelection, mSelectionArgs, mSortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (mLoaderId == cursorLoader.getId()) {
            mEndu.updateCursor(cursor, 0);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        if (mLoaderId == cursorLoader.getId()) {
            mEndu.updateCursor(null, -1);
        }
    }
}

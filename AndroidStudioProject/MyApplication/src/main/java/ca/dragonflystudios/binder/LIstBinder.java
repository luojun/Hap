package ca.dragonflystudios.binder;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;

import ca.dragonflystudios.endu.Endu;
import ca.dragonflystudios.endu.ListEndu;
import ca.dragonflystudios.hap.DsContentProvider;

/**
 * Created by Jun Luo on 13-12-02.
 *
 * A generic ListBinder that is agnostic about the type of the UI -- AUI or GUI or whatever, so long
 * as the UI conforms to a model (generalization of the View Model in MVVM) of the UI.
 *
 */

// TODO: refactor the Endu stuff into something like a "Presenter" interface.
// TODO: refactor the Collection stuff into something like a "Content"/"Model" interface.

public class ListBinder implements LoaderManager.LoaderCallbacks<Cursor>{

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
    ListEndu mListEndu;

    public ListBinder(LoaderManager loaderManager, Context context, ListEndu listEndu, DsContentProvider.Collection collection, String selection, String[] selectionArgs, String sortOrder) {
        mLoaderManager = loaderManager;
        mContext = context;
        mListEndu = listEndu;
        mCollection = collection;
        mSelection = selection;
        mSelectionArgs = selectionArgs;
        mSortOrder = sortOrder;

        mLoaderId = getNextLoaderId();
        mLoaderManager.initLoader(mLoaderId, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        mCollection.requestSync(mContext, mSelection, mSelectionArgs, mSortOrder);
        return new CursorLoader(mContext, mCollection.getUri(), mCollection.columnNames, mSelection, mSelectionArgs, mSortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (mLoaderId == cursorLoader.getId()) {
            mListEndu.updateCursor(cursor);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        if (mLoaderId == cursorLoader.getId()) {
            mListEndu.updateCursor(null);
        }
    }

}

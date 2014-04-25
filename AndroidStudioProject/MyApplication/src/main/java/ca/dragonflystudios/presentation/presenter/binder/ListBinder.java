package ca.dragonflystudios.presentation.presenter.binder;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;

import ca.dragonflystudios.presentation.presenter.endu.ListEndu;
import ca.dragonflystudios.content.model.Collection;

/**
 * Created by Jun Luo on 13-12-02.
 * <p/>
 * A generic ListBinder that is agnostic about the type of the UI -- AUI or GUI or whatever, so long
 * as the UI conforms to a model (generalization of the View Model in MVVM) of the UI.
 */

// TODO: refactor the Endu stuff into something like a "Presenter" interface.
// TODO: refactor the Collection stuff into something like a "Content"/"Model" interface.

public class ListBinder extends EnduBinder implements LoaderManager.LoaderCallbacks<Cursor>
{
    ListEndu mListEndu;

    public ListBinder(LoaderManager loaderManager, Context context, ListEndu listEndu, Collection collection, String selection, String[] selectionArgs, String sortOrder)
    {
        super(loaderManager, context, listEndu, collection, selection, selectionArgs, sortOrder);
        mListEndu = listEndu;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor)
    {
        if (getLoaderId() == cursorLoader.getId()) {
            mListEndu.updateListCursor(cursor);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader)
    {
        if (getLoaderId() == cursorLoader.getId()) {
            mListEndu.updateListCursor(null);
        }
    }
}

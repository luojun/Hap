package ca.dragonflystudios.hap;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

import ca.dragonflystudios.content.model.Collection;
import ca.dragonflystudios.content.model.Model;
import ca.dragonflystudios.content.service.Service;
import ca.dragonflystudios.hap.content.C;

public class NavigableContent implements LoaderManager.LoaderCallbacks<Cursor>
{
    private final static int PROGRAMS_LOADER_ID = 1;
    private final static int PROGRAM_ITEMS_LOADER_ID = 2;

    public NavigableContent(Context context, LoaderManager loaderManager)
    {
        mContext = context;
        mCurrentLevel = Level.CATEGORY_LIST;
        mLoaderManager = loaderManager;
        mLoaderManager.initLoader(PROGRAMS_LOADER_ID, null, this);
    }

    private Context mContext;
    private LoaderManager mLoaderManager;

    private Cursor mProgramsCursor;
    private Cursor mCurrentProgramCursor;

    private void log(String tag)
    {
        Log.w(tag, "mProgramsCursor -- " + ((null == mProgramsCursor) ? "null" : "count : " + mProgramsCursor.getCount() + "; position: " + mProgramsCursor.getPosition()));
        Log.w(tag, "mCurrentProgramCursor -- " + ((null == mCurrentProgramCursor) ? "null" : "count : " + mCurrentProgramCursor.getCount() + "; position: " + mCurrentProgramCursor.getPosition()));
    }

    private boolean shiftCursor(Cursor cursor, int shift)
    {
        log("before shiftCursor with shift = " + shift);
        if (null == cursor)
            return false;

        boolean ret = cursor.move(shift);
        log("after shiftCursor with shift = " + shift);

        return ret;
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle)
    {
        switch (i) {
            case PROGRAMS_LOADER_ID:
                Collection collection = Model.getModelByAuthority(C.NPR_AUTHORITY).getCollectionByName(C.COLLECTION_NAME_PROGRAMS);
                requestSync(collection.getId(), null, null, null);
                return new CursorLoader(mContext, collection.getUri(), collection.itemFieldNames, null, null, null);
            case PROGRAM_ITEMS_LOADER_ID:
                String program_id = mProgramsCursor.getString(mProgramsCursor.getColumnIndex(C.field.id));
                collection = Model.getModelByAuthority(C.NPR_AUTHORITY).getCollectionByName(C.COLLECTION_NAME_STORIES);
                requestSync(collection.getId(), null, new String[] { program_id } , null);
                return new CursorLoader(mContext, collection.getUri(), collection.itemFieldNames, C.field.program_id + " = " + program_id, null, null);
            default:
                throw new RuntimeException("Invalid loader id: " + i);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor)
    {
        switch (cursorLoader.getId()) {
            case PROGRAMS_LOADER_ID:
                mProgramsCursor = cursor;
                if (null != mProgramsCursor && mProgramsCursor.getCount() > 0)
                    mProgramsCursor.moveToFirst();
                log("onLoadFinished with loaderId " + cursorLoader.getId());
                return;
            case PROGRAM_ITEMS_LOADER_ID:
                mCurrentProgramCursor = cursor;
                if (null != mCurrentProgramCursor && mCurrentProgramCursor.getCount() > 0)
                    mCurrentProgramCursor.moveToFirst();
                log("onLoadFinished with loaderId " + cursorLoader.getId());
                return;
            default:
                throw new RuntimeException("Invalid loader id: " + cursorLoader.getId());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader)
    {
        switch (cursorLoader.getId()) {
            case PROGRAMS_LOADER_ID:
                mProgramsCursor = null;
                log("onLoaderReset with loaderId " + cursorLoader.getId());
                return;
            case PROGRAM_ITEMS_LOADER_ID:
                mCurrentProgramCursor = null;
                log("onLoaderReset with loaderId " + cursorLoader.getId());
                return;
            default:
                throw new RuntimeException("Invalid loader id: " + cursorLoader.getId());
        }
    }

    public class Mp4
    {
        public String teaser;
        public String url;

        public Mp4(String teaser, String url)
        {
            this.teaser = teaser;
            this.url = url;
        }
    }

    private enum Level
    {
        CATEGORY_LIST, ITEM_LIST, ITEM
    }

    private Level mCurrentLevel;

    public Object getContent()
    {
        switch (mCurrentLevel) {
            case CATEGORY_LIST:
                if (null != mProgramsCursor && mProgramsCursor.getCount() > 0)
                    return mProgramsCursor.getString(mProgramsCursor.getColumnIndex(C.field.title)) + ". " + mProgramsCursor.getString(mProgramsCursor.getColumnIndex(C.field.description));
                else
                    return null;
            case ITEM_LIST:
                if (null != mCurrentProgramCursor && mCurrentProgramCursor.getCount() > 0)
                    return mCurrentProgramCursor.getString(mCurrentProgramCursor.getColumnIndex(C.field.title));
                else
                    return null;
            case ITEM:
                if (null != mCurrentProgramCursor && mCurrentProgramCursor.getCount() > 0) {
                    return new Mp4(mCurrentProgramCursor.getString(mCurrentProgramCursor.getColumnIndex(C.field.teaser)), mCurrentProgramCursor.getString(mCurrentProgramCursor.getColumnIndex(C.field.mp4)));
                } else
                    return null;
        }
        return null;
    }

    public String getContentDescription()
    {
        switch (mCurrentLevel) {
            case CATEGORY_LIST:
                if (null != mProgramsCursor && mProgramsCursor.getCount() > 0)
                    return "Programs. The current program is " + mProgramsCursor.getString(mProgramsCursor.getColumnIndex(C.field.title)) + ".";
                else
                    return null;
            case ITEM_LIST:
                if (null != mProgramsCursor && mProgramsCursor.getCount() > 0 && null != mCurrentProgramCursor && mCurrentProgramCursor.getCount() > 0)
                    return "Stories in " + mProgramsCursor.getString(mProgramsCursor.getColumnIndex(C.field.title)) + ". Current story: " + mCurrentProgramCursor.getString(mCurrentProgramCursor.getColumnIndex(C.field.title)) + ".";
                else
                    return null;
            case ITEM:
                return "Story.";
        }
        return null;
    }

    public boolean gotoParent()
    {
        switch (mCurrentLevel) {
            case CATEGORY_LIST:
                return false;
            case ITEM_LIST:
                mCurrentLevel = Level.CATEGORY_LIST;
                return true;
            case ITEM:
                mCurrentLevel = Level.ITEM_LIST;
                return true;
        }
        return false;
    }

    public boolean gotoChild()
    {
        switch (mCurrentLevel) {
            case CATEGORY_LIST:
                mCurrentLevel = Level.ITEM_LIST;
                if (null != mProgramsCursor && mProgramsCursor.getCount() > 0) {
                    mCurrentProgramCursor = null;
                    mLoaderManager.restartLoader(PROGRAM_ITEMS_LOADER_ID, null, NavigableContent.this);
                    return true;
                } else {
                    return false;
                }
            case ITEM_LIST:
                if (null == mCurrentProgramCursor)
                    return false;
                mCurrentLevel = Level.ITEM;
                return true;
            case ITEM:
                return false;
        }
        return false;
    }

    public boolean gotoPreviousSibling()
    {
        switch (mCurrentLevel) {
            case CATEGORY_LIST:
                return shiftCursor(mProgramsCursor, -1);
            case ITEM_LIST:
                return shiftCursor(mCurrentProgramCursor, -1);
            case ITEM:
                return shiftCursor(mCurrentProgramCursor, -1);
        }
        return false;
    }

    public boolean gotoNextSibling()
    {
        switch (mCurrentLevel) {
            case CATEGORY_LIST:
                return shiftCursor(mProgramsCursor, 1);
            case ITEM_LIST:
                return shiftCursor(mCurrentProgramCursor, 1);
            case ITEM:
                return shiftCursor(mCurrentProgramCursor, 1);
        }
        return false;
    }

    private void requestSync(int collectionId, String selection, String[] selectionArgs, String sortOrder)
    {
        final Intent intent = new Intent(mContext, Service.class);
        intent.putExtra(Service.KEY_COLLECTION_ID, collectionId);
        intent.putExtra(Service.KEY_SELECTION, selection);
        intent.putExtra(Service.KEY_SELECTION_ARGS, selectionArgs);
        intent.putExtra(Service.KEY_SORT_ORDER, sortOrder);
        mContext.startService(intent);
    }
}

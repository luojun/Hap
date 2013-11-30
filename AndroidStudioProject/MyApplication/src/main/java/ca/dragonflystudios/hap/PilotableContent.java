package ca.dragonflystudios.hap;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;

public class PilotableContent implements Pilotable, LoaderManager.LoaderCallbacks<Cursor> {

    private final static String NPR_AUTHORITY = "api.npr.org";
    private final static String COLLECTION_NAME_PROGRAMS = "programs";
    private final static String COLLECTION_NAME_PROGRAM_ITEMS = "program_items";
    private final static int PROGRAMS_LOADER_ID = 1;
    private final static int PROGRAM_ITEMS_LOADER_ID = 2;

    public PilotableContent(Context context, LoaderManager loaderManager) {
        mCurrentLevel = Level.CATEGORY_LIST;
        mContext = context;
        mLoaderManager = loaderManager;
        mLoaderManager.initLoader(PROGRAMS_LOADER_ID, null, this);
    }

    private Context mContext;
    private LoaderManager mLoaderManager;

    private Cursor mProgramsCursor;
    private Cursor mCurrentProgramCursor;

    private boolean shiftCursor(Cursor cursor, int shift) {
        if (null == cursor)
            return false;

        return cursor.move(shift);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        switch(i) {
            case PROGRAMS_LOADER_ID:
                DsContentProvider.Collection collection = DsContentProvider.Model.getModelByAuthority("api.npr.org").getCollectionByName("programs");
                return new CursorLoader(mContext, collection.getUri(), collection.columnNames, null, null, null);
            case PROGRAM_ITEMS_LOADER_ID:
                String program_id = mProgramsCursor.getString(mProgramsCursor.getColumnIndex("program_id"));
                collection = DsContentProvider.Model.getModelByAuthority("api.npr.org").getCollectionByName("program_items");
                return new CursorLoader(mContext, collection.getUri(), collection.columnNames, "program_id", new String[] { program_id }, null);
            default:
                throw new RuntimeException("Invalid loader id: " + i);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        switch (cursorLoader.getId()) {
            case PROGRAMS_LOADER_ID:
                mProgramsCursor = cursor;
                return;
            case PROGRAM_ITEMS_LOADER_ID:
                mCurrentProgramCursor = cursor;
                return;
            default:
                throw new RuntimeException("Invalid loader id: " + cursorLoader.getId());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        switch (cursorLoader.getId()) {
            case PROGRAMS_LOADER_ID:
                mProgramsCursor = null;
                return;
            case PROGRAM_ITEMS_LOADER_ID:
                mCurrentProgramCursor = null;
                return;
            default:
                throw new RuntimeException("Invalid loader id: " + cursorLoader.getId());
        }
    }

    private enum Level {
        CATEGORY_LIST, ITEM_LIST, ITEM
    }

    private Level mCurrentLevel;

    @Override
    public Object getContent() {
        switch (mCurrentLevel) {
            case CATEGORY_LIST:
                return mProgramsCursor.getString(mProgramsCursor.getColumnIndex("title")) + ". " + mProgramsCursor.getString(mProgramsCursor.getColumnIndex("description"));
            case ITEM_LIST:
                return mCurrentProgramCursor.getString(mCurrentProgramCursor.getColumnIndex("title"));
            case ITEM:
                return mCurrentProgramCursor.getString(mCurrentProgramCursor.getColumnIndex("teaser"));
        }
        return null;
    }

    @Override
    public String getContentDescription() {
        switch (mCurrentLevel) {
            case CATEGORY_LIST:
                return "Programs. The current program is " + mProgramsCursor.getString(mProgramsCursor.getColumnIndex("title")) + ".";
            case ITEM_LIST:
                return "Stories in " + mProgramsCursor.getString(mProgramsCursor.getColumnIndex("title")) + ". Current story: " + mCurrentProgramCursor.getString(mCurrentProgramCursor.getColumnIndex("title")) + ".";
            case ITEM:
                return "Story.";
        }
        return null;
    }

    @Override
    public boolean up() {
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

    @Override
    public boolean down() {
        switch (mCurrentLevel) {
            case CATEGORY_LIST:
                mCurrentLevel = Level.ITEM_LIST;
                mCurrentProgramCursor = null;
                mLoaderManager.restartLoader(PROGRAM_ITEMS_LOADER_ID, null, this);
                return true;
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

    @Override
    public boolean next() {
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

    @Override
    public boolean previous() {
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
}

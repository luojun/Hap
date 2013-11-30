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
        mContext = context;
        mLoaderManager = loaderManager;
        mLoaderManager.initLoader(PROGRAMS_LOADER_ID, null, this);
    }

    private Context mContext;
    private LoaderManager mLoaderManager;

    private Cursor mProgramsCursor;
    private Cursor mCurrentProgramCursor;

    // TODO:
    // (1) maintain a cursor for lineups
    // (2) maintain a cursor for current stories ... <= Note that this can be an arbitrary set of stories, so long as the query could express it
    // (3) use CursorLoaders to update these cursors
    // (4) automatically refresh content at a given level and part upon onLoadingFinish ...
    // (5) make it so that the app plays at least OK with loader reset etc.
    // ( ) apply program id argument
    // ( ) navigation hooks with cursor position change
    private final String[] mLineups = {"Afghanistan", "All things considered", "America", "Arts", "Automobile"};
    private final String[] mAfghanistanMeta = {
            "Story 1 in Afghanistan begins as follows.",
            "Story 2 in Afghanistan does not even have a beginning.",
            "Story 3 in Afghanistan will never end.",
            "Story 4 in Afghanistan is a ghost story.",
            "Story 5 in Afgahnistan has a spelling mistake."
    };
    private final String[] mAtcMeta = {
            "Story 1 in All Things Considered begins as follows.",
            "Story 2 in All Things Considered does not even have a beginning.",
            "Story 3 in All Things Considered considers all things."
    };
    private final String[] mAmericaMeta = {
            "Story 1 in America is a ghost story.",
            "Story 2 in America is about Canada."
    };
    private final String[] mArtsMeta = {
            "Story 1 in Arts has no spelling whatsoever."
    };
    private final String[] mAutomobileMeta = {
            "Story 1 in Automobile begins as follows.",
            "Story 2 in Automobile does not even have a beginning.",
            "Story 3 in Automobile will never end.",
            "Story 4 in Automobile is a ghost story.",
            "Story 5 in Automobile is in the driver's seat."
    };
    private final String[][] mItemMetas = {mAfghanistanMeta, mAtcMeta, mAmericaMeta, mArtsMeta, mAutomobileMeta};

    private final String[] mAfghanistanStories = {
            "Story 1 in Afghanistan goes on and on. Story 1 in Afghanistan goes on and on. Story 1 in Afghanistan goes on and on.",
            "Story 2 in Afghanistan goes on and on. Story 2 in Afghanistan goes on and on. Story 2 in Afghanistan goes on and on.",
            "Story 3 in Afghanistan goes on and on. Story 3 in Afghanistan goes on and on. Story 3 in Afghanistan goes on and on.",
            "Story 4 in Afghanistan goes on and on. Story 4 in Afghanistan goes on and on. Story 4 in Afghanistan goes on and on.",
            "Story 5 in Afghanistan goes on and on. Story 5 in Afghanistan goes on and on. Story 5 in Afghanistan goes on and on."
    };
    private final String[] mAtcStories = {
            "Story 1 in All Things Considered goes on and on. Story 1 in All Things Considered goes on and on. Story 1 in All Things Considered goes on and on.",
            "Story 2 in All Things Considered goes on and on. Story 2 in All Things Considered goes on and on. Story 2 in All Things Considered goes on and on.",
            "Story 3 in All Things Considered goes on and on. Story 3 in All Things Considered goes on and on. Story 3 in All Things Considered goes on and on."
    };
    private final String[] mAmericaStories = {
            "Story 1 in America goes on and on. Story 1 in America goes on and on. Story 1 in America goes on and on.",
            "Story 2 in America goes on and on. Story 2 in America goes on and on. Story 2 in America goes on and on."
    };
    private final String[] mArtsStories = {
            "Story 1 in Arts goes on and on. Story 1 in Arts goes on and on. Story 1 in Arts goes on and on."
    };
    private final String[] mAutomobileStories = {
            "Story 1 in Automobile goes on and on. Story 1 in Automobile goes on and on. Story 1 in Automobile goes on and on.",
            "Story 2 in Automobile goes on and on. Story 2 in Automobile goes on and on. Story 2 in Automobile goes on and on.",
            "Story 3 in Automobile goes on and on. Story 3 in Automobile goes on and on. Story 3 in Automobile goes on and on.",
            "Story 4 in Automobile goes on and on. Story 4 in Automobile goes on and on. Story 4 in Automobile goes on and on.",
            "Story 5 in Automobile goes on and on. Story 5 in Automobile goes on and on. Story 5 in Automobile goes on and on."
    };
    private final String[][] mStories = {mAfghanistanStories, mAtcStories, mAmericaStories, mArtsStories, mAutomobileStories};

    public PilotableContent() {
        mCurrentLevel = Level.CATEGORY_LIST;
        mCurrentCategoryIndex = 0;
        mCurrentItemIndex = 0;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        switch(i) {
            case PROGRAMS_LOADER_ID:
                DsContentProvider.Collection collection = DsContentProvider.Model.getModelByAuthority("api.npr.org").getCollectionByName("programs");
                return new CursorLoader(mContext, collection.getUri(), collection.columnNames, null, null, null);
            case PROGRAM_ITEMS_LOADER_ID:
                collection = DsContentProvider.Model.getModelByAuthority("api.npr.org").getCollectionByName("program_items");
                return new CursorLoader(mContext, collection.getUri(), collection.columnNames, null, null, null);
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
    private int mCurrentCategoryIndex;
    private int mCurrentItemIndex;

    @Override
    public Object getContent() {
        switch (mCurrentLevel) {
            case CATEGORY_LIST:
                return mLineups[mCurrentCategoryIndex];
            case ITEM_LIST:
                return mItemMetas[mCurrentCategoryIndex][mCurrentItemIndex];
            case ITEM:
                return mStories[mCurrentCategoryIndex][mCurrentItemIndex];
        }
        return null;
    }

    @Override
    public String getContentDescription() {
        switch (mCurrentLevel) {
            case CATEGORY_LIST:
                return "Categories. The current category is " + mLineups[mCurrentCategoryIndex] + ".";
            case ITEM_LIST:
                return "Stories under category " + mLineups[mCurrentCategoryIndex] + ". The current story is " + mItemMetas[mCurrentCategoryIndex][mCurrentItemIndex] + ".";
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
                mCurrentItemIndex = 0;
                mCurrentLevel = Level.ITEM_LIST;
                mLoaderManager.restartLoader(PROGRAM_ITEMS_LOADER_ID, null, this);
                return true;
            case ITEM_LIST:
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
                if (mCurrentCategoryIndex < mLineups.length - 1) {
                    mCurrentCategoryIndex++;
                    return true;
                } else
                    return false;
            case ITEM_LIST:
                if (mCurrentItemIndex < mItemMetas[mCurrentCategoryIndex].length - 1) {
                    mCurrentItemIndex++;
                    return true;
                } else
                    return false;
            case ITEM:
                if (mCurrentItemIndex < mStories[mCurrentCategoryIndex].length - 1) {
                    mCurrentItemIndex++;
                    return true;
                } else
                    return false;
        }
        return false;
    }

    @Override
    public boolean previous() {
        switch (mCurrentLevel) {
            case CATEGORY_LIST:
                if (mCurrentCategoryIndex > 0) {
                    mCurrentCategoryIndex--;
                    return true;
                } else
                    return false;
            case ITEM_LIST:
                if (mCurrentItemIndex > 0) {
                    mCurrentItemIndex--;
                    return true;
                } else
                    return false;
            case ITEM:
                if (mCurrentItemIndex > 0) {
                    mCurrentItemIndex--;
                    return true;
                } else
                    return false;
        }
        return false;
    }
}

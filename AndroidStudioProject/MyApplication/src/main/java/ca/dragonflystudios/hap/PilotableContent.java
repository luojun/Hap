package ca.dragonflystudios.hap;

public class PilotableContent implements Pilotable {

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

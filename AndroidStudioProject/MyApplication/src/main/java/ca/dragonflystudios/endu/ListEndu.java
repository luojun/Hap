package ca.dragonflystudios.endu;

import android.database.Cursor;

import java.util.ArrayList;

import ca.dragonflystudios.Player.ContentPlayer;

/**
 * Created by Jun Luo on 13-12-02.
 */

public class ListEndu extends Endu
{
    public ListEndu(ContentPlayer enduPlayer, ContentPlayer listItemPlayer)
    {
        super(enduPlayer);
        mListItemPlayer = listItemPlayer;
    }

    ContentPlayer mListItemPlayer;
    ArrayList<Endu> mEndus;

    private Cursor mCursor;
    private int mCurrentItemIndex = -1;

    public void updateListCursor(Cursor cursor)
    {
        mCursor = cursor;

        if (null == mCursor || mCursor.getCount() < 1) {
            mEndus = null;
            mCurrentItemIndex = -1;
            return;
        }

        int count = mCursor.getCount();
        mEndus = new ArrayList<Endu>(count);
        for (int i = 0; i < count; i++) {
            mEndus.add(i, new Endu(mListItemPlayer, mCursor, i));
        }
        setCurrentItem(0);
    }

    public void playCurrentItem()
    {
        mEndus.get(mCurrentItemIndex).onPlay();
    }

    public void setCurrentItem(int index)
    {
        mCurrentItemIndex = index;
        mCursor.moveToPosition(index);
        playItem(index);
    }

    public void playItem(int index)
    {
        mEndus.get(index).onPlay();
    }

    @Override
    public boolean up()
    {
        return false;
    }

    @Override
    public boolean down()
    {
        return onPlay();
    }

    @Override
    public boolean next()
    {
        return false;
    }

    @Override
    public boolean previous()
    {
        return false;
    }

}

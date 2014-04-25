package ca.dragonflystudios.presentation.presenter.endu;

import android.database.Cursor;

import ca.dragonflystudios.presentation.player.Playable;

/**
 * Created by Jun Luo on 13-12-02.
 * <p/>
 * This is meant to be a generic AUI (Auditory UI) class that corresponds to View in GUI. "Endu"
 * is so called because the word "view" is derived from the (old) French word for "seen" and
 * the French word for "heard" is "entendu".
 * <p/>
 * The corresponding class for Window will be called "Windah", where "ah" is the first two letters
 * of the word Ahre, for "ear". Etymologically, "Window" means "wind-eye".
 */

// TODO: Tap into the onDraw callbacks of view for framed refreshing? TAI!

public class Endu
{
    private Cursor mCursor;
    private int mPosition;

    private Playable mContentPlayer;

    public Endu(Playable player)
    {
        mContentPlayer = player;
    }

    public Endu(Playable player, Cursor cursor, int position)
    {
        mContentPlayer = player;
        mCursor = cursor;
        mPosition = position;
    }

    public boolean onPlay()
    {
        if (null == mCursor && 0 <= mPosition && mPosition < mCursor.getCount())
            return false;

        int savedPosition = mCursor.getPosition();
        if (savedPosition != mPosition)
            mCursor.moveToPosition(mPosition);

//        mContentPlayer.playContent(mCursor);

        if (savedPosition != mPosition)
            mCursor.moveToPosition(savedPosition);

        return true;
    }

    public void updateCursor(Cursor cursor, int position)
    {
        mCursor = cursor;
        mPosition = position;
    }

    public boolean up()
    {
        return false;
    }

    public boolean down()
    {
        return onPlay();
    }

    public boolean next()
    {
        return false;
    }

    public boolean previous()
    {
        return false;
    }

    public Object getContent()
    {
        return null;
    }

    public String getContentDescription()
    {
        return null;
    }
}

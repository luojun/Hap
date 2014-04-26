package ca.dragonflystudios.hap;

import android.app.LoaderManager;
import android.content.Context;

import ca.dragonflystudios.content.model.Collection;
import ca.dragonflystudios.content.model.Model;
import ca.dragonflystudios.presentation.presenter.binder.ListBinder;
import ca.dragonflystudios.presentation.presenter.endu.ListEndu;

/**
 * Created by Jun Luo on 13-12-04.
 */

public class NprNavigator
{
    // TODO: tracks history; allow going "home" etc.

    private static enum Level
    {
        CATEGORY_LIST, ITEM_LIST, ITEM
    }

    private Level mCurrentLevel;
    private Context mContext;
    private ListBinder mProgramsBinder;
    private ListEndu mProgramsEndu;
    private ListBinder mCurrentProgramBinder;
    private ListEndu mCurrentProgramEndu;

    public NprNavigator(Context context, LoaderManager loaderManager)
    {
        mCurrentLevel = Level.CATEGORY_LIST;
        mContext = context;
/*
        mProgramsEndu = new ListEndu(new Playable()
        {
            @Override
            public void playContent(Cursor cursor)
            {
                // play NPR overview
            }
        }, new Playable()
        {
            @Override
            public void playContent(Cursor cursor)
            {
                // play program overview
            }
        });
*/
        Collection collection = Model.getModelByAuthority("api.npr.org").getCollectionByName("programs");
        mProgramsBinder = new ListBinder(loaderManager, context, mProgramsEndu, collection, null, null, null);
        mCurrentProgramBinder = null;
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

    public boolean up()
    {
        return false;
    }

    public boolean down()
    {
        switch (mCurrentLevel) {
            case CATEGORY_LIST:
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

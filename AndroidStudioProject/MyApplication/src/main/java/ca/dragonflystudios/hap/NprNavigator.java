package ca.dragonflystudios.hap;

import android.app.LoaderManager;
import android.content.Context;
import android.database.Cursor;

import ca.dragonflystudios.ui.Pilotable;
import ca.dragonflystudios.ui.Playable;
import ca.dragonflystudios.presentation.presenter.binder.ListBinder;
import ca.dragonflystudios.content.model.Collection;
import ca.dragonflystudios.content.model.Model;
import ca.dragonflystudios.presentation.presenter.endu.ListEndu;
import ca.dragonflystudios.navigation.Pilotable;
import ca.dragonflystudios.presentation.player.Playable;

/**
 * Created by Jun Luo on 13-12-04.
 */

public class NprNavigator implements Pilotable
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

    @Override
    public boolean up()
    {
        return false;
    }

    @Override
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

    @Override
    public Object getContent()
    {
        return null;
    }

    @Override
    public String getContentDescription()
    {
        return null;
    }

}

package ca.dragonflystudios.hap;

import android.app.LoaderManager;
import android.content.Context;
import android.database.Cursor;

import ca.dragonflystudios.Player.ContentPlayer;
import ca.dragonflystudios.binder.ListBinder;
import ca.dragonflystudios.endu.ListEndu;

/**
 * Created by Jun Luo on 13-12-04.
 */

public class NprNavigator implements Pilotable
{
    // TODO: tracks history; allow going "home" etc.

    private final static String NPR_AUTHORITY = "api.npr.org";

    private static enum Level
    {
        CATEGORY_LIST, ITEM_LIST, ITEM
    }

    private final static String COLLECTION_NAME_PROGRAMS = "programs";
    private final static String COLLECTION_NAME_PROGRAM_ITEMS = "program_items";

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

        mProgramsEndu = new ListEndu(new ContentPlayer()
        {
            @Override
            public void playContent(Cursor cursor)
            {
                // play NPR overview
            }
        }, new ContentPlayer()
        {
            @Override
            public void playContent(Cursor cursor)
            {
                // play program overview
            }
        });

        DsContentProvider.Collection collection = DsContentProvider.Model.getModelByAuthority("api.npr.org").getCollectionByName("programs");
        mProgramsBinder = new ListBinder(loaderManager, context, mProgramsEndu, collection, null, null, null);
        mCurrentProgramBinder = null;

                        /*
                String program_id = mProgramsCursor.getString(mProgramsCursor.getColumnIndex("id"));
                collection = DsContentProvider.Model.getModelByAuthority("api.npr.org").getCollectionByName("program_items");
                collection.requestSync(mContext, "program_id = ?", new String[] { program_id }, null);
                return new CursorLoader(mContext, collection.getUri(), collection.columnNames, "program_id = " + program_id, null, null);
                 */

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

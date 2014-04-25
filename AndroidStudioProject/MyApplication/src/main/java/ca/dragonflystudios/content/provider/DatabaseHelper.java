package ca.dragonflystudios.content.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import ca.dragonflystudios.content.model.Collection;
import ca.dragonflystudios.content.model.Model;

/**
 * Created by jun on 2014-04-18.
 */

public class DatabaseHelper
{
    private static class DbOpenHelper extends SQLiteOpenHelper
    {
        private Model mModel;

        public DbOpenHelper(Context context, Model model)
        {
            super(context, model.authority + ".db", null, model.version);
            mModel = model;
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            for (Collection collection : mModel.getCollections())
                createTable(db, collection);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
            db.execSQL("DROP TABLE IF EXISTS _cache_stamps_");
            for (Collection collection : mModel.getCollections())
                dropTable(db, collection);

            onCreate(db);
        }
    }

    protected static SQLiteDatabase initializeDatabase(Context context, Model model)
    {
        DbOpenHelper helper = new DbOpenHelper(context, model);
        return helper.getWritableDatabase();
    }

    public static void createTable(SQLiteDatabase database, Collection collection)
    {
        database.execSQL(getTableCreationString(collection));
    }

    public static void dropTable(SQLiteDatabase database, Collection collection)
    {
        database.execSQL("DROP TABLE IF EXISTS " + collection.name);
    }

    private static String getTableCreationString(Collection collection)
    {
        StringBuilder sb = new StringBuilder("CREATE TABLE ");
        sb.append(collection.name);

        sb.append(" (");
        sb.append(Collection.KEY_PRIMARY);
        sb.append(" integer primary key");
        for (int i = 0; i < collection.itemFieldNames.length; i++) {
            sb.append(", ");
            sb.append(collection.itemFieldNames[i]);
            // TODO: everything is "text not null" for now
            sb.append(" text not null");
        }
        sb.append(");");

        Log.d(DatabaseHelper.class.getName(), sb.toString());
        return sb.toString();
    }
}


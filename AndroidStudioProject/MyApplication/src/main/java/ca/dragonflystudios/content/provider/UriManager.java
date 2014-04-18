package ca.dragonflystudios.content.provider;

import android.content.UriMatcher;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import java.util.HashMap;

import ca.dragonflystudios.content.model.Collection;
import ca.dragonflystudios.content.model.Model;

/**
 * Created by jun on 2014-04-18.
 */

public class UriManager
{
    public static class MCD
    {
        public Model model;
        public Collection collection;
        public SQLiteDatabase database;

        public MCD(Model model, Collection collection, SQLiteDatabase database)
        {
            this.model = model;
            this.collection = collection;
            this.database = database;
        }
    }

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static HashMap<Integer, SQLiteDatabase> sDatabaseRegistry = new HashMap<Integer, SQLiteDatabase>();

    public static void registerDatabaseForModel(SQLiteDatabase db, Model model)
    {
        sDatabaseRegistry.put(model.getIndex(), db);
    }

    public static void registerCollection(String authority, int modelIndex, String collectionName, int collectionIndex)
    {
        final int collectionId = modelIndex * Model.MAX_COLLECTIONS + collectionIndex;
        sUriMatcher.addURI(authority, collectionName, collectionId);
    }

    public static MCD resolve(Uri uri)
    {
        final int code = sUriMatcher.match(uri);
        if (-1 == code)
            throw new IllegalArgumentException("Cannot find database for unknown URI: " + uri);

        final int modelIndex = code / Model.MAX_COLLECTIONS;
        final Model model = Model.getModel(modelIndex);
        final Collection collection = model.getCollection(code % Model.MAX_COLLECTIONS);
        final SQLiteDatabase database = sDatabaseRegistry.get(model);

        return new MCD(model, collection, database);
    }
}

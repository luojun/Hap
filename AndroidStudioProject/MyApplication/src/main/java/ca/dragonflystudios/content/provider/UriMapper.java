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

public class UriMapper
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
        sDatabaseRegistry.put(model.getId(), db);
    }

    public static void registerCollection(String authority, String collectionName, int collectionId)
    {
        sUriMatcher.addURI(authority, collectionName, collectionId);
    }

    public static MCD resolve(Uri uri)
    {
        final int collectionId = sUriMatcher.match(uri);
        if (-1 == collectionId)
            throw new IllegalArgumentException("Cannot find database for unknown URI: " + uri);

        final int modelId = Model.cid2mid(collectionId);
        final Model model = Model.getModel(modelId);
        final Collection collection = Model.getCollection(collectionId);
        final SQLiteDatabase database = sDatabaseRegistry.get(modelId);

        return new MCD(model, collection, database);
    }

    public static String cid2url(int collectionId, String selection, String[] selectionArgs, String sortOrder)
    {
        // WAIL: selectionArgs and sortOrder ignored
        return Model.getCollection(collectionId).getUrl(selectionArgs);
    }

    public static Uri cid2uri(int collectionId)
    {
        return Model.getCollection(collectionId).getUri();
    }
}

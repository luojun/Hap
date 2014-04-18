package ca.dragonflystudios.content.provider;

/**
 * Created by jun on 2014-04-18.
 */

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import ca.dragonflystudios.content.model.Model;
import ca.dragonflystudios.content.model.ModelCollectionPair;
import ca.dragonflystudios.content.model.UriManager;

public class DssContentProvider extends ContentProvider {

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        ModelCollectionPair pair = UriManager.resolve(uri);
        int count = pair.model.database.delete(pair.collection.name, selection, selectionArgs);
        // getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        ModelCollectionPair pair = UriManager.resolve(uri);
        return "vnd.android.cursor.dir/vnd." + pair.model.authority;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        ModelCollectionPair pair = UriManager.resolve(uri);

        Cursor c = pair.model.database.rawQuery("SELECT * FROM " + pair.collection.name + " WHERE _id = '" + values.getAsString("_id") + "'", null);
        try {
            if (c.moveToFirst()) {
                return uri;
            }
        } finally {
            c.close();
        }

        long rowID = pair.model.database.insert(pair.collection.name, "", values);

        if (rowID > 0) {
            Uri _uri = ContentUris.withAppendedId(uri, rowID); // TODO: is this right?
            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        }

        throw new SQLException("Failed to insert");
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        ModelCollectionPair pair = UriManager.resolve(uri);
        String tableName = pair.collection.name;
        SQLiteDatabase db = pair.model.database;
        boolean inserted = false;

        db.beginTransaction();
        try {
            for (ContentValues v : values)
                if (null != v) {
                    long ret = db.insertWithOnConflict(tableName, null, v, SQLiteDatabase.CONFLICT_IGNORE);
                    // long ret = db.insert(tableName, null, v); // Use this version for debugging, because exception will be thrown (in contrast to the version above)
                    Log.d(getClass().getName(), "insert to " + tableName + " returns: " + ret);
                    if (-1 != ret)
                        inserted = true;
                }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        if (inserted)
            getContext().getContentResolver().notifyChange(uri, null);

        return values.length;
    }

    @Override
    public boolean onCreate() {
        for (Model model : Model.sModels)
            if (!model.initializeDb(getContext()))
                return false;
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.w(getClass().getName(), "query is being called with uri " + uri);
        final ModelCollectionPair pair = UriManager.resolve(uri);
        final int modelIndex = pair.model.getIndex();
        final int collectionIndex = pair.collection.getIndex();

        String tableName = pair.collection.name;
        SQLiteQueryBuilder qBuilder = new SQLiteQueryBuilder();
        qBuilder.setTables(tableName);

        Cursor c = qBuilder.query(pair.model.database, projection, selection, selectionArgs, null, null, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);

        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        ModelCollectionPair pair = UriManager.resolve(uri);
        int count = pair.model.database.updateWithOnConflict(pair.collection.name, values, selection, selectionArgs, SQLiteDatabase.CONFLICT_REPLACE);
        getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }
}

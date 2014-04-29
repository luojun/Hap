package ca.dragonflystudios.content.provider;

/**
 * Created by jun on 2014-04-18.
 */

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;

import ca.dragonflystudios.content.model.Model;

public class Provider extends ContentProvider
{
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs)
    {
        UriMapper.MCD mcd = UriMapper.resolve(uri);
        int count = mcd.database.delete(mcd.collection.name, selection, selectionArgs);
        return count;
    }

    @Override
    public String getType(Uri uri)
    {
        UriMapper.MCD mcd = UriMapper.resolve(uri);
        return "vnd.android.cursor.dir/vnd." + mcd.model.authority;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values)
    {
        // TODO: check the correctness of this implementation!

        UriMapper.MCD mcd = UriMapper.resolve(uri);

        Cursor c = mcd.database.rawQuery("SELECT * FROM " + mcd.collection.name + " WHERE _id = '" + values.getAsString("_id") + "'", null);
        try {
            if (c.moveToFirst()) {
                return uri;
            }
        } finally {
            c.close();
        }

        long rowID = mcd.database.insert(mcd.collection.name, "", values);

        if (rowID > 0) {
            Uri _uri = ContentUris.withAppendedId(uri, rowID); // TODO: is this right?
            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        }

        throw new SQLException("Failed to insert");
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values)
    {
        UriMapper.MCD mcd = UriMapper.resolve(uri);
        String tableName = mcd.collection.name;
        SQLiteDatabase db = mcd.database;
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
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)
    {
        final int size = operations.size();
        final ContentProviderResult[] results = new ContentProviderResult[size];

        if (size > 1) {
            final Uri uri = operations.get(0).getUri();
            UriMapper.MCD mcd = UriMapper.resolve(uri);
            SQLiteDatabase db = mcd.database;

            db.beginTransaction();
            try {
                for (int i = 0; i < size; i++)
                    results[i] = operations.get(i).apply(this, null, 0);
                db.setTransactionSuccessful();
                getContext().getContentResolver().notifyChange(uri, null);
            } catch (OperationApplicationException e) {
                e.printStackTrace();
            } finally {
                db.endTransaction();
            }
        }

        return results;
    }

    @Override
    public boolean onCreate()
    {
        for (Model model : Model.sModels) {
            SQLiteDatabase db = DatabaseHelper.initializeDatabase(getContext(), model);
            if (null == db)
                return false;
            else
                UriMapper.registerDatabaseForModel(db, model);
        }
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {
        final UriMapper.MCD mcd = UriMapper.resolve(uri);

        String tableName = mcd.collection.name;
        SQLiteQueryBuilder qBuilder = new SQLiteQueryBuilder();
        qBuilder.setTables(tableName);

        Cursor c = qBuilder.query(mcd.database, projection, selection, selectionArgs, null, null, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);

        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
    {
        UriMapper.MCD mcd = UriMapper.resolve(uri);
        int count = mcd.database.updateWithOnConflict(mcd.collection.name, values, selection, selectionArgs, SQLiteDatabase.CONFLICT_REPLACE);
        getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }
}

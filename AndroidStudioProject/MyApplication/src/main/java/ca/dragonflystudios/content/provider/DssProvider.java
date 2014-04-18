package ca.dragonflystudios.content.provider;

/**
 * Created by jun on 2014-04-18.
 */

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import ca.dragonflystudios.content.model.Model;
import ca.dragonflystudios.content.service.HttpService;
import ca.dragonflystudios.hap.DsSyncService;

public class DssProvider extends ContentProvider
{
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs)
    {
        UriManager.MCD mcd = UriManager.resolve(uri);
        int count = mcd.database.delete(mcd.collection.name, selection, selectionArgs);
        // getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri)
    {
        UriManager.MCD mcd = UriManager.resolve(uri);
        return "vnd.android.cursor.dir/vnd." + mcd.model.authority;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values)
    {
        UriManager.MCD mcd = UriManager.resolve(uri);

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
        UriManager.MCD mcd = UriManager.resolve(uri);
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
    public boolean onCreate()
    {
        for (Model model : Model.sModels) {
            SQLiteDatabase db = DatabaseHelper.initializeDatabase(getContext(), model);
            if (null == db)
                return false;
            else
                UriManager.registerDatabaseForModel(db, model);
        }
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {
        final UriManager.MCD mcd = UriManager.resolve(uri);

        String tableName = mcd.collection.name;
        SQLiteQueryBuilder qBuilder = new SQLiteQueryBuilder();
        qBuilder.setTables(tableName);

        Cursor c = qBuilder.query(mcd.database, projection, selection, selectionArgs, null, null, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);

        requestSync(mcd, selection, selectionArgs, sortOrder);
        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
    {
        UriManager.MCD mcd = UriManager.resolve(uri);
        int count = mcd.database.updateWithOnConflict(mcd.collection.name, values, selection, selectionArgs, SQLiteDatabase.CONFLICT_REPLACE);
        getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }

    private void requestSync(UriManager.MCD mcd, String selection, String[] selectionArgs, String sortOrder)
    {
        final Context context = getContext();
        final Intent intent = new Intent(context, HttpService.class);
        intent.putExtra(DsSyncService.KEY_MODEL_INDEX, mcd.model.getIndex());
        intent.putExtra(DsSyncService.KEY_COLLECTION_INDEX, mcd.collection.getIndex());
        intent.putExtra(DsSyncService.KEY_SELECTION, selection);
        intent.putExtra(DsSyncService.KEY_SELECTION_ARGS, selectionArgs);
        intent.putExtra(DsSyncService.KEY_SORT_ORDER, sortOrder);
        context.startService(intent);
    }
}

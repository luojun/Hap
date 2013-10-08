package ca.dragonflystudios.hap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class DsContentProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    public static class Model {
        public static ArrayList<Model> sModels = new ArrayList<Model>();
        public static Model getModel(int index) {
            return sModels.get(index);
        }

        public static final int MAX_COLLECTIONS = 1024;

        private static final AtomicInteger sNextModelIndex = new AtomicInteger(0);
        private static int getNextModelIndex() {
            return sNextModelIndex.getAndIncrement();
        }

        private final AtomicInteger mNextCollectionIndex;
        private int getNextCollectionIndex() {
            int index = mNextCollectionIndex.getAndIncrement();
            if (index >= MAX_COLLECTIONS)
                throw new RuntimeException("At the limit for the number of collections in model " + name + ". Cannot create more collections!");
            return index;
        }

        private int mModelIndex;

        public int getIndex() {
            return mModelIndex;
        }

        public String name;
        public String authority;
        public int version;
        public ArrayList<Collection> collections;
        public SQLiteDatabase database;

        public void addCollection(Collection collection) {
            int index = getNextCollectionIndex();
            collection.setModel(this, index);
            collections.add(index, collection);
        }

        public Collection getCollection(int index) {
            return collections.get(index);
        }

        public Model(String name, String authority, int version, Collection[] collections) {
            mModelIndex = getNextModelIndex();
            mNextCollectionIndex = new AtomicInteger(0);
            this.name = name;
            this.authority = authority;
            this.version = version;
            for (Collection collection : collections)
                addCollection(collection);
            sModels.add(mModelIndex, this);
        }

        public boolean initializeDb(Context context) {
            Model.ModelDbOpenHelper helper = new ModelDbOpenHelper(context, this);
            database = helper.getWritableDatabase();
            return (null != database);
        }

        private static class ModelDbOpenHelper extends SQLiteOpenHelper {
            private final Model mModel;

            public ModelDbOpenHelper(Context context, Model model) {
                super(context, model.authority + ".db", null, model.version);
                mModel = model;
            }

            @Override
            public void onCreate(SQLiteDatabase db) {
                for (Collection collection : mModel.collections)
                    collection.createTable(db);
            }

            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                // TODO: content model handles upgrade according to Web API versions
                for (Collection collection : mModel.collections)
                    collection.dropTable(db);

                onCreate(db);
            }
        }
    }

    public static class Collection {
        public static final String KEY_PRIMARY = "_id";

        private Model mModel;
        private int mIndex;
        public String name;
        public String url;
        public String[] contentsPath;
        public String[][] valuePaths;
        public String[] columnNames;


        public Model getModel() {
            return mModel;
        }

        public int getIndex() {
            return mIndex;
        }

        public Collection(String name, String url, String[] contentsPath, String[][] valuePaths, String[] columnNames) {
            this.name = name;
            this.url = url;
            this.contentsPath = contentsPath;
            this.valuePaths = valuePaths;
            this.columnNames = columnNames;
        }

        protected void setModel(Model model, int index) {
            mModel = model;
            mIndex = index;
            sUriMatcher.addURI(model.authority, this.name, model.getIndex() * Model.MAX_COLLECTIONS + index);
        }

        protected void createTable(SQLiteDatabase database) {
            database.execSQL(getTableCreationString());
        }

        private String getTableCreationString() {
            StringBuilder sb = new StringBuilder("CREATE TABLE ");
            sb.append(this.name);

            sb.append(" (");
            sb.append(KEY_PRIMARY);
            sb.append(" integer primary key");
            for (int i = 0; i < this.columnNames.length; i++) {
                sb.append(", ");
                sb.append(this.columnNames[i]);
                // TODO: everything is "text not null" for now
                sb.append(" text not null");
            }
            sb.append(");");

            Log.d(getClass().getName(), sb.toString());
            return sb.toString();
        }

        protected void dropTable(SQLiteDatabase database) {
            database.execSQL("DROP TABLE IF EXISTS " + this.name);
        }
    }

    private static class ModelCollectionPair {
        public Model model;
        public Collection collection;

        ModelCollectionPair (Model model, Collection collection) {
            this.model = model;
            this.collection = collection;
        }
    }

    private static ModelCollectionPair decodeUri(Uri uri) {
        final int code = sUriMatcher.match(uri);
        if (-1 == code)
            throw new IllegalArgumentException("Unknown URI " + uri);

        final Model model = Model.getModel(code / Model.MAX_COLLECTIONS);
        ModelCollectionPair pair = new ModelCollectionPair(model,  model.getCollection(code % Model.MAX_COLLECTIONS));
        return pair;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        ModelCollectionPair pair = decodeUri(uri);
        int count = pair.model.database.delete(pair.collection.name, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        ModelCollectionPair pair = decodeUri(uri);
        return "vnd.android.cursor.dir/vnd." + pair.model.authority;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        ModelCollectionPair pair = decodeUri(uri);

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
        ModelCollectionPair pair = decodeUri(uri);
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
        // Trigger downloading ...
        ModelCollectionPair pair = decodeUri(uri);
        String tableName = pair.collection.name;

        SQLiteQueryBuilder qBuilder = new SQLiteQueryBuilder();
        qBuilder.setTables(tableName);

        Cursor c = qBuilder.query(pair.model.database, projection, selection, selectionArgs, null, null, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);

        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        ModelCollectionPair pair = decodeUri(uri);
        int count = pair.model.database.updateWithOnConflict(pair.collection.name, values, selection, selectionArgs, SQLiteDatabase.CONFLICT_REPLACE);
        getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }
}

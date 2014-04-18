package ca.dragonflystudios.content.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import ca.dragonflystudios.content.model.Collection;
import ca.dragonflystudios.content.model.Model;

/**
 * Created by jun on 2014-04-18.
 */

public class DbHelper extends SQLiteOpenHelper {
    private final Model mModel;

    public DbHelper(Context context, Model model) {
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


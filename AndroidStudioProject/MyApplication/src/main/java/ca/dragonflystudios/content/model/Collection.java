package ca.dragonflystudios.content.model;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import ca.dragonflystudios.content.json.JsonPath;
import ca.dragonflystudios.hap.DsSyncService;

/**
 * Created by jun on 2014-04-18.
 */

public class Collection {

    static {
        Collection NprPrograms = new Collection("programs", new String[]{"id", "title", "description"},
                "http://api.npr.org/list?id=3004&output=JSON&numResults=20&apiKey=MDEyMzY0MjM5MDEzODEyOTAxOTFmYWE4ZA001", new JsonPath("item"),
                new JsonPath[]{ new JsonPath("id"), new JsonPath("title", "$text"), new  JsonPath("additionalInfo", "$text")});

        Collection NprProgramItems = new Collection("program_items", new String[]{"id", "program_id", "title", "teaser", "date", "mp4" },
                "http://api.npr.org/query?id=%s&fields=title,teaser,storyDate,audio&output=JSON&numResults=20&apiKey=MDEyMzY0MjM5MDEzODEyOTAxOTFmYWE4ZA001", new JsonPath("list", "story"),
                new JsonPath[]{ new JsonPath("id"), new JsonPath("show", 0, "program", "id"), new JsonPath("title", "$text"),
                        new JsonPath("teaser", "$text"), new JsonPath("storyDate", "$text"), new JsonPath("audio", 0, "format", "mp4", "$text")}) {
            @Override
            public String getUrl(String[] selectionArgs) {
                return String.format(url, selectionArgs);
            }
        };

        new Model("NPR News", "api.npr.org", 1, new Collection[] { NprPrograms, NprProgramItems});
    }

    public static final String KEY_PRIMARY = "_id";

    private Model mModel;
    private int mIndex;
    public String name;
    public String url;
    public JsonPath contentsPath;
    public JsonPath[] valuePaths;
    public String[] columnNames;


    public Model getModel() {
        return mModel;
    }

    public int getIndex() {
        return mIndex;
    }

    public Collection(String name, String[] columnNames, String url, JsonPath contentsPath, JsonPath[] valuePaths) {
        this.name = name;
        this.url = url;
        this.contentsPath = contentsPath;
        this.valuePaths = valuePaths;
        this.columnNames = columnNames;
    }

    public Uri getUri() {
        return Uri.parse("content://" + getModel().authority + "/" + name);
    }

    /* TODO: more sensible implementation of mapping selection to columnName and selectionArgs to query terms
     */
    // The default implementation ignores selectionArgs
    public String getUrl(String[] selectionArgs) {
        return url;
    }

    protected void setModel(Model model, int index) {
        mModel = model;
        mIndex = index;
        final int collectionId = model.getIndex() * Model.MAX_COLLECTIONS + index;
        UriManager.register(model.authority, this.name, collectionId);
    }

    public void createTable(SQLiteDatabase database) {
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

    public void dropTable(SQLiteDatabase database) {
        database.execSQL("DROP TABLE IF EXISTS " + this.name);
    }

    public void requestSync(Context context, String selection, String[] selectionArgs, String sortOrder) {
        final Intent intent = new Intent(context, DsSyncService.class);
        intent.putExtra(DsSyncService.KEY_MODEL_INDEX, getModel().getIndex());
        intent.putExtra(DsSyncService.KEY_COLLECTION_INDEX, getIndex());
        intent.putExtra(DsSyncService.KEY_SELECTION, selection);
        intent.putExtra(DsSyncService.KEY_SELECTION_ARGS, selectionArgs);
        intent.putExtra(DsSyncService.KEY_SORT_ORDER, sortOrder);
        context.startService(intent);
    }
}


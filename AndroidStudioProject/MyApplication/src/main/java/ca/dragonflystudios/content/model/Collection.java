package ca.dragonflystudios.content.model;

import android.net.Uri;

import ca.dragonflystudios.content.provider.UriManager;

/**
 * Created by jun on 2014-04-18.
 */

public class Collection
{
    public static final String KEY_PRIMARY = "_id";

    private Model mModel;
    private int mIndex;
    public String name;
    public String url;
    public ContentPath itemsPath;
    public ContentPath[] itemFieldPaths;
    public String[] itemFieldNames;


    public Model getModel()
    {
        return mModel;
    }

    public int getIndex()
    {
        return mIndex;
    }

    public Collection(String name, String[] itemFieldNames, String url, ContentPath itemsPath, ContentPath[] itemFieldPaths)
    {
        this.name = name;
        this.url = url;
        this.itemsPath = itemsPath;
        this.itemFieldPaths = itemFieldPaths;
        this.itemFieldNames = itemFieldNames;
    }

    public Uri getUri()
    {
        return Uri.parse("content://" + getModel().authority + "/" + name);
    }

    /* TODO: more sensible implementation of mapping selection to columnName and selectionArgs to query terms
     */
    // The default implementation ignores selectionArgs
    public String getUrl(String[] selectionArgs)
    {
        return url;
    }

    protected void setModel(Model model, int collectionIndex)
    {
        mModel = model;
        mIndex = collectionIndex;
        UriManager.registerCollection(model.authority, model.getIndex(), this.name, collectionIndex);
    }
}


package ca.dragonflystudios.content.model;

import android.net.Uri;

import ca.dragonflystudios.content.provider.UriMapper;
import ca.dragonflystudios.content.processor.ContentPath;

/**
 * Created by jun on 2014-04-18.
 */

public class Collection
{
    public static final String KEY_PRIMARY = "_id";

    private Model mModel;
    private int mId;
    public String name;
    public String url;
    public ContentPath itemsPath;
    public ContentPath[] itemFieldPaths;
    public String[] itemFieldNames;


    public Model getModel()
    {
        return mModel;
    }

    public int getId()
    {
        return mId;
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
    public String getUrl(String[] selectionArgs)
    {
        return String.format(url, selectionArgs);
    }

    protected void setModel(Model model, int collectionId)
    {
        mModel = model;
        mId = collectionId;
        UriMapper.registerCollection(model.authority, this.name, mId);
    }
}


package ca.dragonflystudios.content.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by jun on 2014-04-18.
 */

public class Model
{
    private static final Object sLock = new Object();

    public static final ArrayList<Model> sModels = new ArrayList<Model>();

    public static ArrayList<Model> getModels()
    {
        return sModels;
    }

    public static Model getModel(int id)
    {
        synchronized (sLock) {
            return sModels.get(id);
        }
    }

    public static Model getModelByAuthority(String authority)
    {
        synchronized (sLock) {
            for (Model model : sModels) {
                if (model.authority.equalsIgnoreCase(authority))
                    return model;
            }
        }
        return null;
    }

    public static final int MAX_COLLECTIONS = 1024;
    public static final int RESERVED_COLLECTIONS = 24;

    private static final AtomicInteger sNextModelId = new AtomicInteger(0);

    private static int getNextModelId()
    {
        return sNextModelId.getAndIncrement();
    }

    private final AtomicInteger mNextCollectionIndex;

    private int getNextCollectionIndex()
    {
        int index = mNextCollectionIndex.getAndIncrement();
        if (index >= MAX_COLLECTIONS - RESERVED_COLLECTIONS)
            throw new RuntimeException("At the limit for the number of collections in model " + name + ". Cannot create more collections!");
        return index;
    }

    private int mModelId;
    public int getId()
    {
        return mModelId;
    }
    public String name;
    public String authority;
    public int version;

    private HashMap<Integer, Collection> mCollections;

    public static int cid2mid(int cid)
    {
        return cid / MAX_COLLECTIONS;
    }

    private static int cr8cid(int modelId, int collectionIndex)
    {
        return modelId * MAX_COLLECTIONS + collectionIndex;
    }

    public static Collection getCollection(int id)
    {
        return getModel(cid2mid(id)).mCollections.get(id);
    }

    public java.util.Collection<Collection> getCollections()
    {
        return mCollections.values();
    }

    synchronized public Collection getCollectionByName(String name)
    {
        for (Collection collection : mCollections.values()) {
            if (collection.name.equals(name))
                return collection;
        }
        return null;
    }

    public Model(String name, String authority, int version, Collection[] collections)
    {
        mModelId = getNextModelId();
        mNextCollectionIndex = new AtomicInteger(0);
        this.name = name;
        this.authority = authority;
        this.version = version;
        mCollections = new HashMap<Integer, Collection>();
        for (Collection collection : collections) {
            int index = getNextCollectionIndex();
            int cid = cr8cid(mModelId, index);
            collection.setModel(this, cid);
            mCollections.put(cid, collection);
        }

        synchronized (sLock) {
            sModels.add(mModelId, this);
        }
    }
}

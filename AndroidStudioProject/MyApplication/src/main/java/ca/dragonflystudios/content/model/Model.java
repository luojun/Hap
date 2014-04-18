package ca.dragonflystudios.content.model;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by jun on 2014-04-18.
 */

public class Model {
    private static final Object sLock = new Object();

    public static ArrayList<Model> sModels = new ArrayList<Model>();

    public static Model getModel(int index) {
        synchronized(sLock) {
            return sModels.get(index);
        }
    }

    public static Model getModelByAuthority(String authority) {
        synchronized(sLock) {
            for (Model model : sModels) {
                if (model.authority.equalsIgnoreCase(authority))
                    return model;
            }
        }
        return null;
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

    synchronized public void addCollection(Collection collection) {
        int index = getNextCollectionIndex();
        collection.setModel(this, index);
        collections.add(index, collection);
    }

    synchronized public Collection getCollection(int index) {
        return collections.get(index);
    }

    synchronized public Collection getCollectionByName(String name) {
        for (Collection collection : collections) {
            if (collection.name.equals(name))
                return collection;
        }
        return null;
    }

    public Model(String name, String authority, int version, Collection[] collections) {
        mModelIndex = getNextModelIndex();
        mNextCollectionIndex = new AtomicInteger(0);
        this.name = name;
        this.authority = authority;
        this.version = version;
        this.collections = new ArrayList<Collection>();
        for (Collection collection : collections)
            addCollection(collection);

        synchronized (sLock) {
            sModels.add(mModelIndex, this);
        }
    }
}

package ca.dragonflystudios.content.model;

import android.content.UriMatcher;
import android.net.Uri;

/**
 * Created by jun on 2014-04-18.
 */

public class UriManager {
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    public static void register(String authority, String collectionName, int collectionId)
    {
        sUriMatcher.addURI(authority, collectionName, collectionId);
    }

    public static ModelCollectionPair resolve(Uri uri) {
        final int code = sUriMatcher.match(uri);
        if (-1 == code)
            throw new IllegalArgumentException("Unknown URI " + uri);

        final Model model = Model.getModel(code / Model.MAX_COLLECTIONS);
        ModelCollectionPair pair = new ModelCollectionPair(model, model.getCollection(code % Model.MAX_COLLECTIONS));
        return pair;
    }
}

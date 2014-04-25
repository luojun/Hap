package ca.dragonflystudios.content.processor;

import android.content.ContentValues;

/**
 * Created by jun on 2014-04-18.
 */

public interface ContentsExtractor
{
    /**
     * Extracts items of a collection from a structure, such as a JsonNode, into an array of ContentValues.
     *
     * @param base           a recursive structure, such as a JsonNode, that contains the items
     * @param itemsPath      path to where the items are located inside @param base
     * @param itemFieldPaths paths to the fields of an item that are to be extracted
     * @param itemFieldNames names of the fields of an item, in one-to-one correspondence with @param itemFieldPaths
     * @return extracted content items
     */
    public ContentValues[] extract(Object base, int collectionId);
}

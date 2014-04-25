package ca.dragonflystudios.content.processor.xml;

import android.content.ContentValues;

import java.io.InputStream;

import ca.dragonflystudios.content.processor.ContentsExtractor;
import ca.dragonflystudios.content.processor.StreamParser;

/**
 * Created by jun on 2014-04-19.
 */

// Placeholder class for future implementation, if need be.

public class XmlProcessor implements StreamParser, ContentsExtractor
{
    @Override
    public Object parse(InputStream stream)
    {
        // WAIL: this could be a simple wrapper of what's available in the Android library?
        return null;
    }

    @Override
    public ContentValues[] extract(Object base, int collectionId)
    {
        return null;
    }
}

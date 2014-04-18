package ca.dragonflystudios.content.processor;

import java.io.InputStream;

/**
 * Created by jun on 2014-04-18.
 */

public interface StreamParser
{
    public Object parse(InputStream stream);
}

package ca.dragonflystudios.content;

import java.io.InputStream;

/**
 * Created by jun on 2014-04-18.
 */

public interface StreamParser <T>
{
    public T parse(InputStream stream);
}

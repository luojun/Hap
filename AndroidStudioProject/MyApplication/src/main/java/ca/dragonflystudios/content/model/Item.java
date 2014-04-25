package ca.dragonflystudios.content.model;

/**
 * Created by jun on 2014-04-24.
 */

public class Item
{
    public Item(Object content, String description)
    {
        mContent = content;
        mDescription = description;
    }

    public Object getContent()
    {
        return mContent;
    }

    public String getDescription()
    {
        return mDescription;
    }

    private Object mContent;
    private String mDescription;
}

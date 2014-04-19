package ca.dragonflystudios.content.service.processor.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.MissingNode;

import ca.dragonflystudios.content.service.processor.ContentPath;

/**
 * Created by jun on 2014-04-18.
 */

public class JsonPath implements ContentPath
{
    public JsonPath(Object... objects)
    {
        mPath = objects;
    }

    private Object[] mPath;

    public int getLength()
    {
        return mPath.length;
    }

    public Object get(int position)
    {
        return mPath[position];
    }

    public static JsonNode nodeAtPath(JsonNode sourceNode, JsonPath path)
    {
        JsonNode currentNode = sourceNode;
        JsonNode nextNode = MissingNode.getInstance();

        for (int i = 0; i < path.getLength(); i++) {
            final Object step = path.get(i);
            if (step instanceof String)
                nextNode = currentNode.get((String) step);
            else if (step instanceof Integer)
                nextNode = currentNode.get((Integer) step);

            if (nextNode.isMissingNode())
                return nextNode;
            else
                currentNode = nextNode;
        }

        return nextNode;
    }
}

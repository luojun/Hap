package ca.dragonflystudios.content.processor.json;

import android.content.ContentValues;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.IOException;
import java.io.InputStream;

import ca.dragonflystudios.content.model.Collection;
import ca.dragonflystudios.content.model.Model;
import ca.dragonflystudios.content.processor.ContentsExtractor;
import ca.dragonflystudios.content.processor.StreamParser;

/**
 * Created by jun on 2014-04-18.
 */

public class JsonProcessor implements StreamParser, ContentsExtractor
{
    @Override
    public JsonNode parse(InputStream stream)
    {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = null;
        try {
            jsonNode = mapper.readValue(stream, JsonNode.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return jsonNode;
    }

    @Override
    public ContentValues[] extract(Object base, int collectionId)
    {
        final Collection collection = Model.getCollection(collectionId);
        final ArrayNode contentsNode = (ArrayNode) JsonPath.nodeAtPath((JsonNode) base, (JsonPath) collection.itemsPath);
        final ContentValues[] contents = new ContentValues[contentsNode.size()];

        for (int i = 0; i < contentsNode.size(); i++) {
            final JsonNode contentNode = contentsNode.get(i);
            if (contentNode.isMissingNode())
                continue;

            ContentValues row = new ContentValues();
            for (int j = 0; j < collection.itemFieldNames.length; j++) {
                JsonNode valueNode = JsonPath.nodeAtPath(contentNode, (JsonPath) collection.itemFieldPaths[j]);
                // TODO handle data types other than String
                row.put(collection.itemFieldNames[j], valueNode.asText());
            }
            contents[i] = row;
        }
        return contents;
    }
}

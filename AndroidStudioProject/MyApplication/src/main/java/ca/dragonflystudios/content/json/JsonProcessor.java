package ca.dragonflystudios.content.json;

import android.content.ContentValues;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.IOException;
import java.io.InputStream;

import ca.dragonflystudios.content.ContentPath;
import ca.dragonflystudios.content.ContentsExtractor;
import ca.dragonflystudios.content.StreamParser;

/**
 * Created by jun on 2014-04-18.
 */

public class JsonProcessor implements StreamParser<JsonNode>, ContentsExtractor<JsonNode>
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
    public ContentValues[] extract(JsonNode base, ContentPath itemsPath, ContentPath[] itemFieldPaths, String[] itemFieldNames) {
        final ArrayNode contentsNode = (ArrayNode) JsonPath.nodeAtPath(base, (JsonPath)itemsPath);
        final ContentValues[] contents = new ContentValues[contentsNode.size()];

        for (int i = 0; i < contentsNode.size(); i++) {
            final JsonNode contentNode = contentsNode.get(i);
            if (contentNode.isMissingNode())
                continue;

            ContentValues row = new ContentValues();
            for (int j = 0; j < itemFieldNames.length; j++) {
                JsonNode valueNode = JsonPath.nodeAtPath(contentNode, (JsonPath)itemFieldPaths[j]);
                // TODO handle data types other than String
                row.put(itemFieldNames[j], valueNode.asText());
            }
            contents[i] = row;
        }
        return contents;
    }
}

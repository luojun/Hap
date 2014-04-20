package ca.dragonflystudios.hap.content;

import ca.dragonflystudios.content.model.Collection;
import ca.dragonflystudios.content.model.Model;
import ca.dragonflystudios.content.service.processor.json.JsonPath;

/**
 * Created by jun on 2014-04-19.
 */
public class HapContent
{
    // Do app-specific static initalization in this app-specific class
    // Note that the constructors for Collection and Model self-register the instances created
    public static final Collection NprPrograms = new Collection("programs", new String[]{"id", "title", "description"},
                "http://api.npr.org/list?id=3004&output=JSON&numResults=20&apiKey=MDEyMzY0MjM5MDEzODEyOTAxOTFmYWE4ZA001", new JsonPath("item"),
                new JsonPath[]{new JsonPath("id"), new JsonPath("title", "$text"), new JsonPath("additionalInfo", "$text")});

    public static final Collection NprProgramItems = new Collection("program_items", new String[]{"id", "program_id", "title", "teaser", "date", "mp4"},
                "http://api.npr.org/query?id=%s&fields=title,teaser,storyDate,audio&output=JSON&numResults=20&apiKey=MDEyMzY0MjM5MDEzODEyOTAxOTFmYWE4ZA001", new JsonPath("list", "story"),
                new JsonPath[]{new JsonPath("id"), new JsonPath("show", 0, "program", "id"), new JsonPath("title", "$text"),
                        new JsonPath("teaser", "$text"), new JsonPath("storyDate", "$text"), new JsonPath("audio", 0, "format", "mp4", "$text")})
        {
            @Override
            public String getUrl(String[] selectionArgs)
            {
                return String.format(url, selectionArgs);
            }
        };

    public static final Model NprModel = new Model("NPR News", "api.npr.org", 1, new Collection[]{NprPrograms, NprProgramItems});
}

package ca.dragonflystudios.hap.content;

/**
 * Created by jun on 2014-04-26.
 */

public class C
{
    public final static String NPR_MODEL_NAME = "NPR Stories";
    public final static String NPR_AUTHORITY = "api.npr.org";
    public final static String COLLECTION_NAME_PROGRAMS = "programs";
    public final static String COLLECTION_NAME_STORIES = "stories";

    public final static class field
    {
        public final static String id = "id";
        public final static String title = "title";
        public final static String description = "description";
        public final static String program_id = "program_id";
        public final static String teaser = "teaser";
        public final static String date = "date";
        public final static String mp4 = "mp4";
    }

    public final static String[] programFields = { field.id, field.title, field.description };
    public final static String[] programItemFields = { field.id, field.program_id, field.title, field.teaser, field.date, field.mp4 };
}

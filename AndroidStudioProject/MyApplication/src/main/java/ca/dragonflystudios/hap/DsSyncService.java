package ca.dragonflystudios.hap;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.MissingNode;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DsSyncService extends IntentService {

    public DsSyncService() {
        super("Sync Service");
    }

    public static final String KEY_MODEL_INDEX = "ds_model_index";
    public static final String KEY_COLLECTION_INDEX = "ds_collection_index";
    public static final String KEY_SELECTION = "ds_selection";
    public static final String KEY_SELECTION_ARGS = "ds_selection_args";

    @Override
    protected void onHandleIntent(Intent intent) {
        final Bundle extras = intent.getExtras();
        final ContentResolver cr = getContentResolver();

        final int modelIndex = extras.getInt(KEY_MODEL_INDEX);
        final int collectionIndex = extras.getInt(KEY_COLLECTION_INDEX);
        final String selection = extras.getString(KEY_SELECTION);
        final String[] selectionArgs = extras.getStringArray(KEY_SELECTION_ARGS);

        final DsContentProvider.Collection collection = DsContentProvider.Model.getModel(modelIndex).getCollection(collectionIndex);
        final Uri uri = collection.getUri();
        final String url = collection.getUrl(selection, selectionArgs);

        final JsonPath contentsPath = collection.contentsPath;
        final String[] keyStrings = collection.columnNames;
        final JsonPath[] valuePaths = collection.valuePaths;

        final ContentValues[] contents = getContentsFromUrl(url, uri, contentsPath, keyStrings, valuePaths);
        cr.delete(uri, selection, selectionArgs);
        cr.bulkInsert(uri, contents);

        return;
    }

    public static ContentValues[] getContentsFromUrl(String url, Uri uri, JsonPath contentsPath, String[] keyStrings, JsonPath[] valuePaths) {
        final JsonNode baseNode = HttpHelper.getJsonAtUrl(url);
        final ArrayNode contentsNode = (ArrayNode) JsonPath.nodeAtPath(baseNode, contentsPath);
        final ContentValues[] contents = new ContentValues[contentsNode.size()];

        for (int i = 0; i < contentsNode.size(); i++) {
            final JsonNode contentNode = contentsNode.get(i);
            if (contentNode.isMissingNode())
                continue;

            ContentValues row = new ContentValues();
            for (int j = 0; j < keyStrings.length; j++) {
                JsonNode valueNode = JsonPath.nodeAtPath(contentNode, valuePaths[j]);
                // TODO handle data types other than String
                row.put(keyStrings[j], valueNode.asText());
            }
            contents[i] = row;
        }
        return contents;
    }

    public static class JsonPath {
        public JsonPath(Object... objects) {
            mPath = objects;
        }

        private Object[] mPath;

        public int getLength() {
            return mPath.length;
        }

        public Object get(int position) {
            return mPath[position];
        }

        public static JsonNode nodeAtPath(JsonNode sourceNode, JsonPath path) {
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

    public static class HttpHelper {

        public static int CONNECT_TIMEOUT = 10000;
        public static int READ_TIMEOUT = 10000;

        public static JsonNode getJsonAtUrl(String urlString) {
            if (BuildConfig.DEBUG)
                Log.d(HttpHelper.class.getName(), "getting resource @ " + urlString);

            HttpURLConnection connection = null;
            try {
                URL url = new URL(urlString);
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(CONNECT_TIMEOUT);
                connection.setReadTimeout(READ_TIMEOUT);
                connection.setRequestMethod("GET");
                connection.setDoInput(true);
                int response = connection.getResponseCode();
                if (BuildConfig.DEBUG)
                    Log.d(HttpHelper.class.getName(), "server responded with " + response);
                if (200 == response) {
                    InputStream stream = connection.getInputStream();
                    final ObjectMapper mapper = new ObjectMapper();
                    return mapper.readValue(stream, JsonNode.class);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                connection.disconnect();
            }

            return null;
        }
    }
}

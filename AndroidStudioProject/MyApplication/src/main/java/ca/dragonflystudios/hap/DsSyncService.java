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

    public static final String KEY_URI = "ds_uri";
    public static final String KEY_QUERY_ARGUMENTS = "ds_query_args";

    @Override
    protected void onHandleIntent(Intent intent) {
        final Bundle extras = intent.getExtras();
        final ContentResolver cr = getContentResolver();

        final Uri uri = Uri.parse(extras.getString(KEY_URI));
        final DsContentProvider.ModelCollectionPair mcp = DsContentProvider.decodeUri(uri);

        final String[] queryArgs = extras.getStringArray(KEY_QUERY_ARGUMENTS);
        final String queryUrl = String.format(mcp.collection.url, queryArgs);

        final JsonPath contentsPath = mcp.collection.contentsPath;
        final String[] keyStrings = mcp.collection.columnNames;
        final JsonPath[] valuePaths = mcp.collection.valuePaths;

        final ContentValues[] contents = getContentsFromUrl(queryUrl, uri, contentsPath, keyStrings, valuePaths);
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
                JsonNode valueNode = JsonPath.nodeAtPath(contentsNode, valuePaths[i]);
                // TODO handle data types other than String
                row.put(keyStrings[j], valueNode.asText());
            }
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

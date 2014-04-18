package ca.dragonflystudios.ContentService.Http;

import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import ca.dragonflystudios.content.StreamParser;
import ca.dragonflystudios.hap.BuildConfig;

// NOTE: consider Android specific HttpResponseCache: http://developer.android.com/reference/android/net/http/HttpResponseCache.html

/**
 * Created by jun on 2014-04-18.
 */
public class HttpHelper <T>
{
    public static int CONNECT_TIMEOUT = 10000;
    public static int READ_TIMEOUT = 10000;

    public HttpHelper(StreamParser<T> streamParser) {
        mStreamParser = streamParser;
    }

    protected T getContentAtUrl(String urlString, String etag, long ifModifiedSinceDate) {
        if (BuildConfig.DEBUG)
            Log.d(HttpHelper.class.getName(), "getting resource @ " + urlString);

        HttpURLConnection connection = null;
        T content = null;
        try {
            final URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            int response = connection.getResponseCode();
            if (BuildConfig.DEBUG)
                Log.d(HttpHelper.class.getName(), "server responded with:\n" + response);
            if (200 == response) {
                connection.getIfModifiedSince();
                content = (T)mStreamParser.parse(connection.getInputStream());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != connection)
                connection.disconnect();
        }

        return content;
    }

    private StreamParser mStreamParser;
}

package ca.dragonflystudios.content.service;

import android.util.Log;

import org.apache.http.HttpStatus;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import ca.dragonflystudios.content.processor.StreamParser;
import ca.dragonflystudios.hap.BuildConfig;

// NOTE: consider Android's HttpResponseCache: http://developer.android.com/reference/android/net/http/HttpResponseCache.html

/**
 * Created by jun on 2014-04-18.
 */
public class HttpHelper
{
    public static int CONNECT_TIMEOUT = 10000;
    public static int READ_TIMEOUT = 10000;

    protected static Service.Result request(Service.Request request, StreamParser parser)
    {

        HttpURLConnection connection = null;
        final Service.Result result = new Service.Result(0, null);

        try {
            final URL url = new URL(request.url);
            connection = (HttpURLConnection) url.openConnection();

            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);

            // TODO: switch(request.method)
            connection.setRequestMethod("GET");
            connection.setDoInput(true);

            result.responseCode = connection.getResponseCode();
            switch (result.responseCode) {
                case HttpStatus.SC_OK:
                    result.parsed = parser.parse(connection.getInputStream());
                    break;
                case HttpStatus.SC_NOT_MODIFIED:
                    if (BuildConfig.DEBUG)
                        Log.d(HttpHelper.class.getName(), "Not modified: " + url);
                    break;
                default:
                    if (BuildConfig.DEBUG)
                        Log.d(HttpHelper.class.getName(), "Server responded with: \n" + connection.getHeaderFields().toString());
                    break;
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != connection)
                connection.disconnect();
        }

        return result;
    }
}

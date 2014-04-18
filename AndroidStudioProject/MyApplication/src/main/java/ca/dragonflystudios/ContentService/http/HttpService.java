package ca.dragonflystudios.ContentService.Http;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;

import ca.dragonflystudios.content.ContentsExtractor;
import ca.dragonflystudios.content.StreamParser;

/**
 * Created by jun on 2014-04-18.
 *
 *
 */

public class HttpService <T> extends IntentService
{
    public static final String KEY_REQUST_URL = "dss_url";
    public static final String KEY_ETAG = "dss_etag";
    public static final String KEY_IF_MODIFIED_SINCE = "dss_ims";

    public HttpService(StreamParser<T> parser, ContentsExtractor<T> extractor)
    {
        super("Http Service");
        mHelper = new HttpHelper(parser);
        mExtractor = extractor;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final Bundle extras = intent.getExtras();
        final String requestUrl = extras.getString(KEY_REQUST_URL);
        final String etag = extras.getString(KEY_ETAG);
        final long imsDate = extras.getLong(KEY_IF_MODIFIED_SINCE);

        // We could objectify the HTTP request here. Maybe later.
        final T parsed = (T) mHelper.getContentAtUrl(requestUrl, etag, imsDate);
        final ContentValues[] contents = mExtractor.extract(parsed, null, null, null);
        return;
    }

    private HttpHelper<T> mHelper;
    private ContentsExtractor<T> mExtractor;
}

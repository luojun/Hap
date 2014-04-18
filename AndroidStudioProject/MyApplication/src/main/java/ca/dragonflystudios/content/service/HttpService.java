package ca.dragonflystudios.content.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;

import ca.dragonflystudios.content.service.processor.ContentsExtractor;
import ca.dragonflystudios.content.service.processor.StreamParser;

/**
 * Created by jun on 2014-04-18.
 *
 *
 */

// NOTE: We use IntentService for a quick start. Requests sent to an IntentService are queued and handled
//       sequentially. Down the road, we may need to either manipulate the queue or dispatch the requests to multiple
//       worker threads.
public class HttpService extends IntentService
{
    protected static class Request
    {
        protected Request(String url, String eTag, long ifModifiedSince)
        {
            this.url = url;
            this.eTag = eTag;
            this.ifModifiedSince = ifModifiedSince;
        }

        protected String url;
        protected String eTag;
        protected long ifModifiedSince;
    }

    protected static class Result
    {
        protected Result(int responseCode, Object parsed, String eTag, long ifModifiedSince)
        {
            this.responseCode = responseCode;
            this.parsed = parsed;
            this.eTag = eTag;
            this.ifModifiedSince = ifModifiedSince;
        }

        protected int responseCode;
        protected Object parsed;
        protected String eTag;
        protected long ifModifiedSince;
    }

    public static final String KEY_REQUST_URL = "dss_url";
    public static final String KEY_ETAG = "dss_etag";
    public static final String KEY_IF_MODIFIED_SINCE = "dss_ims";

    public HttpService(StreamParser parser, ContentsExtractor extractor)
    {
        super("Http Service");
        mExtractor = extractor;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final Bundle extras = intent.getExtras();
        final String requestUrl = extras.getString(KEY_REQUST_URL);
        final String eTag = extras.getString(KEY_ETAG);
        final long imsDate = extras.getLong(KEY_IF_MODIFIED_SINCE);
        final Request request = new Request(requestUrl, eTag, imsDate);
        final Result result = HttpHelper.request(request, mParser);
        final ContentValues[] contents = mExtractor.extract(result.parsed, null, null, null);

        // build ArrayList of ContentOperations
        // get ContentResolver ...
        // do updates: applyBatch
        // notify if Successful.
        return;
    }

    private ContentsExtractor mExtractor;
    private StreamParser mParser;
}

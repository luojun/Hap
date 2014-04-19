package ca.dragonflystudios.content.service;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;

import ca.dragonflystudios.content.service.processor.ContentsExtractor;
import ca.dragonflystudios.content.service.processor.StreamParser;
import ca.dragonflystudios.hap.BuildConfig;

/**
 * Created by jun on 2014-04-18.
 */

// NOTE: We use IntentService for a quick start. Requests sent to an IntentService are queued and handled
//       sequentially. Down the road, we may need to either manipulate the queue or dispatch the requests to multiple
//       worker threads.
public class Service extends IntentService
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

    public static final String KEY_AUTHORITY = "dss_authority";
    public static final String KEY_URI = "dss_uri";
    public static final String KEY_STAMPS_URI = "dss_stamps_uri";
    public static final String KEY_REQUST_URL = "dss_url";
    public static final String KEY_ETAG = "dss_etag";
    public static final String KEY_IF_MODIFIED_SINCE = "dss_ims";
    public static final String KEY_MODEL_INDEX = "dss_model_index";
    public static final String KEY_COLLECTION_INDEX = "dss_collection_index";
    public static final String KEY_SELECTION = "dss_selection";
    public static final String KEY_SELECTION_ARGS = "dss_selection_args";
    public static final String KEY_SORT_ORDER = "dss_sort_order";

    public Service(StreamParser parser, ContentsExtractor extractor)
    {
        super("Http Service");
        mExtractor = extractor;
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        // TAI: Any better way? Use Model and Collection ids? Rely on UriManager?
        //      Rely on static methods? Is that incompatible with the spirit of ContentProvider?
        //      Probably not a bad idea, so long as those static methods/variables are read-only.
        final Bundle extras = intent.getExtras();
        final String authority = extras.getString(KEY_AUTHORITY);
        final Uri uri = Uri.parse(extras.getString(KEY_URI));
        final Uri stampsUri = Uri.parse(extras.getString(KEY_STAMPS_URI));
        final int collectionIndex = extras.getInt(KEY_COLLECTION_INDEX);
        final String requestUrl = extras.getString(KEY_REQUST_URL);
        final String eTag = extras.getString(KEY_ETAG);
        final long imsDate = extras.getLong(KEY_IF_MODIFIED_SINCE);
        final Request request = new Request(requestUrl, eTag, imsDate);
        final Result result = HttpHelper.request(request, mParser);
        final ContentValues[] contents = mExtractor.extract(result.parsed, null, null, null);

        final ContentResolver cr = getContentResolver();
        final ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

        if (contents.length > 0) {

            // TAI: this deletes all old items ...
            operations.add(ContentProviderOperation.newDelete(uri).build());

            for (ContentValues cvs : contents)
                operations.add(ContentProviderOperation.newInsert(uri).withValues(cvs).build());

            final ContentValues stamps = new ContentValues();
            stamps.put(KEY_COLLECTION_INDEX, collectionIndex);
            stamps.put(KEY_ETAG, eTag);
            stamps.put(KEY_IF_MODIFIED_SINCE, imsDate);
            operations.add(ContentProviderOperation.newUpdate(stampsUri).withValues(stamps).build());

            try {
                ContentProviderResult[] results = cr.applyBatch(authority, operations);
                if (BuildConfig.DEBUG)
                    Log.d(getClass().getName(), results.toString());
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (OperationApplicationException e) {
                e.printStackTrace();
            }
        }
        return;
    }

    private ContentsExtractor mExtractor;
    private StreamParser mParser;
}

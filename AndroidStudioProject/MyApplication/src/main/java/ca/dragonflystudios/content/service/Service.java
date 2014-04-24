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

import ca.dragonflystudios.content.provider.UriMapper;
import ca.dragonflystudios.content.service.processor.ContentsExtractor;
import ca.dragonflystudios.content.service.processor.StreamParser;
import ca.dragonflystudios.content.service.processor.json.JsonProcessor;
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

    public static final String KEY_COLLECITON_ID = "dss_collection_id";
    public static final String KEY_SELECTION = "dss_selection";
    public static final String KEY_SELECTION_ARGS = "dss_selection_args";
    public static final String KEY_SORT_ORDER = "dss_sort_order";
    public static final String KEY_ETAG = "dss_etag";
    public static final String KEY_IF_MODIFIED_SINCE = "dss_ims";

    public Service()
    {
        super("Sync Service");

        // TODO: lazy initialization of static processor instances and assignment of them to mParser
        // and  mExtractor according to content-type of response
        JsonProcessor processor = new JsonProcessor();
        mParser = processor;
        mExtractor = processor;
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        // TAI: Any better way? Use Model and Collection ids? Rely on UriManager?
        //      Rely on static methods? Is that incompatible with the spirit of ContentProvider?
        //      Probably not a bad idea, so long as those static methods/variables are read-only.
        final Bundle extras = intent.getExtras();
        final int collectionId = extras.getInt(KEY_COLLECITON_ID);
        final String selection = extras.getString(KEY_SELECTION);
        final String[] selectionArgs = extras.getStringArray(KEY_SELECTION_ARGS);
        final String sortOrder = extras.getString(KEY_SORT_ORDER);
        final String eTag = extras.getString(KEY_ETAG);
        final long ifModifiedSince = extras.getLong(KEY_IF_MODIFIED_SINCE);
        final String requestUrl = UriMapper.cid2url(collectionId, selection, selectionArgs, sortOrder);
        final Uri uri = UriMapper.cid2uri(collectionId);
        final Uri stampsUri = UriMapper.cid2suri(collectionId);

        final Request request = new Request(requestUrl, eTag, ifModifiedSince);
        final Result result = HttpHelper.request(request, mParser);
        final ContentValues[] contents = mExtractor.extract(result.parsed, null, null, null);

        final ContentResolver cr = getContentResolver();
        final ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

        if (contents.length > 0) {

            // WAIL: this deletes all old items ...
            operations.add(ContentProviderOperation.newDelete(uri).withSelection(selection, selectionArgs).build());

            for (ContentValues cvs : contents)
                operations.add(ContentProviderOperation.newInsert(uri).withValues(cvs).build());

            // WAIL: the literal strings that are column names of the stamps table
            final ContentValues stamps = new ContentValues();
            stamps.put("collection_id", collectionId);
            stamps.put("etag", result.eTag);
            stamps.put("ifmodifiedsince", result.ifModifiedSince);
            operations.add(ContentProviderOperation.newUpdate(stampsUri).withValues(stamps).build());

            try {
                ContentProviderResult[] results = cr.applyBatch(uri.getAuthority(), operations);
                if (BuildConfig.DEBUG)
                    Log.d(getClass().getName(), results.toString());
                getContentResolver().notifyChange(uri, null);
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

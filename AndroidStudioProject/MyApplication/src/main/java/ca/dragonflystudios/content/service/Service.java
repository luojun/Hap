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
import ca.dragonflystudios.content.processor.ContentsExtractor;
import ca.dragonflystudios.content.processor.StreamParser;
import ca.dragonflystudios.content.processor.json.JsonProcessor;
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
        protected Request(String url)
        {
            this.url = url;
        }

        protected String url;
    }

    protected static class Result
    {
        protected Result(int responseCode, Object parsed)
        {
            this.responseCode = responseCode;
            this.parsed = parsed;
        }

        protected int responseCode;
        protected Object parsed;
    }

    public static final String KEY_COLLECTION_ID = "dss_collection_id";
    public static final String KEY_SELECTION = "dss_selection";
    public static final String KEY_SELECTION_ARGS = "dss_selection_args";
    public static final String KEY_SORT_ORDER = "dss_sort_order";

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
        final int collectionId = extras.getInt(KEY_COLLECTION_ID);
        final String selection = extras.getString(KEY_SELECTION);
        final String[] selectionArgs = extras.getStringArray(KEY_SELECTION_ARGS);
        final String sortOrder = extras.getString(KEY_SORT_ORDER);
        final String requestUrl = UriMapper.cid2url(collectionId, selection, selectionArgs, sortOrder);
        final Uri uri = UriMapper.cid2uri(collectionId);

        final Request request = new Request(requestUrl);
        final Result result = HttpHelper.request(request, mParser);
        final ContentValues[] contents = mExtractor.extract(result.parsed, collectionId);

        final ContentResolver cr = getContentResolver();
        final ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

        if (contents.length > 0) {

            // WAIL: this deletes all old items ...
            operations.add(ContentProviderOperation.newDelete(uri).withSelection(selection, selectionArgs).build());

            for (ContentValues cvs : contents)
                operations.add(ContentProviderOperation.newInsert(uri).withValues(cvs).build());

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

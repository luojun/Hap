package ca.dragonflystudios.hap;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.Locale;

import ca.dragonflystudios.haptic.AcceGyro;
import ca.dragonflystudios.haptic.AcceGyroGraphView;
import ca.dragonflystudios.haptic.GyroGestureRecognizer;

public class HapActivity extends Activity implements TextToSpeech.OnInitListener
{
    private static final boolean LOG = false;
    private AcceGyroGraphView mGraphView;
    private AcceGyro.Logger mLogger;
    private AcceGyro mAcceGyro;
    private GyroGestureRecognizer mRecognizer;

    private NavigableContent mContent;
    private TextToSpeech mTts;

    // TODO: refactor the following and NavigableContent in terms of MEC -- Model Endu Controller
    // Endu <=> View; Windah <=> Window (ah is beginning of German Ahre; Endu is part of French entendu, like how "view" originated from French "seen".

    private BroadcastReceiver mReceiver = new BroadcastReceiver()
    {
        private void playMp4(NavigableContent.Mp4 mp4)
        {
            // mTts.speak(mp4.teaser, TextToSpeech.QUEUE_FLUSH, null);
            Uri uri = Uri.parse(mp4.url);
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setDataAndType(uri, "audio/mp3");
            HapActivity.this.startActivity(i);
        }

        @Override
        public void onReceive(Context context, Intent intent)
        {
            Log.w(getClass().getName(), "received: " + intent.getStringExtra("TYPE") + " " + intent.getStringExtra("VALUE"));
            GyroGestureRecognizer.GyroGesture gesture = GyroGestureRecognizer.GyroGesture.valueOf(GyroGestureRecognizer.GyroGesture.class, intent.getStringExtra("VALUE"));
            switch (gesture) {
                case UP:
                    if (mContent.gotoParent()) {
                        mTts.speak((String) mContent.getContentDescription(), TextToSpeech.QUEUE_FLUSH, null);
                    } else
                        mTts.speak("Sorry. Cannot go up anymore.", TextToSpeech.QUEUE_FLUSH, null);
                    break;
                case DOWN:
                    if (mContent.gotoChild()) {
                        Object content = mContent.getContent();
                        if (content instanceof String[]) {
                            mTts.speak((String) mContent.getContentDescription() + ". Tilt left or right to choose.", TextToSpeech.QUEUE_FLUSH, null);
                            mTts.speak((String) mContent.getContent(), TextToSpeech.QUEUE_ADD, null);
                        } else if (content instanceof String) {
                            mTts.speak((String) content, TextToSpeech.QUEUE_FLUSH, null);
                        } else if (content instanceof NavigableContent.Mp4) {
                            playMp4((NavigableContent.Mp4) content);
                        }
                    } else
                        mTts.speak("Sorry. Cannot go down anymore.", TextToSpeech.QUEUE_FLUSH, null);
                    break;
                case LEFT:
                    if (mContent.gotoPreviousSibling()) {
                        Object content = mContent.getContent();
                        if (content instanceof String) {
                            mTts.speak((String) content, TextToSpeech.QUEUE_FLUSH, null);
                        } else if (content instanceof NavigableContent.Mp4) {
                            playMp4((NavigableContent.Mp4) content);
                        }
                    } else
                        mTts.speak("Sorry. No more before this one.", TextToSpeech.QUEUE_FLUSH, null);
                    break;
                case RIGHT:
                    if (mContent.gotoNextSibling()) {
                        Object content = mContent.getContent();
                        if (content instanceof String) {
                            mTts.speak((String) content, TextToSpeech.QUEUE_FLUSH, null);
                        } else if (content instanceof NavigableContent.Mp4) {
                            playMp4((NavigableContent.Mp4) content);
                        }
                    } else
                        mTts.speak("Sorry. No more after this one.", TextToSpeech.QUEUE_FLUSH, null);
                    break;
                case CLOCKWISE:
                    break;
                case COUNTERCLOCKWISE:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mContent = new NavigableContent(this, getLoaderManager());
        mTts = new TextToSpeech(this, this);
        mGraphView = new AcceGyroGraphView(this);
        mAcceGyro = new AcceGyro((SensorManager) getSystemService(SENSOR_SERVICE));
        mRecognizer = new GyroGestureRecognizer(this);
        mAcceGyro.addObserver(mGraphView);
        mAcceGyro.addObserver(mRecognizer);
        if (BuildConfig.DEBUG && LOG) {
            mLogger = new AcceGyro.Logger();
            mAcceGyro.addObserver(mLogger);
        }
        setContentView(mGraphView);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter(GyroGestureRecognizer.NAVIGATION_ACTION));
        mAcceGyro.startListening();
    }

    @Override
    protected void onPause()
    {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        mAcceGyro.stopListening();
        super.onPause();
    }

    @Override
    protected void onDestroy()
    {
        mAcceGyro.deleteObservers();
        if (mTts != null)
            mTts.shutdown();
        super.onDestroy();
    }

    @Override
    public void onInit(int status)
    {
        if (status == TextToSpeech.SUCCESS) {
            Log.w(getClass().getName(), "TTS initialized.");
            int result = mTts.setLanguage(Locale.CANADA);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w(getClass().getName(), "Locale Canada is not supported");
            }
        } else
            Log.w(getClass().getName(), "Failed to initialize TTS.");
    }
}

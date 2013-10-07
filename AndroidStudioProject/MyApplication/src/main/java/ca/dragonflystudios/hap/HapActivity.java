package ca.dragonflystudios.hap;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.Locale;

public class HapActivity extends Activity implements TextToSpeech.OnInitListener {
    private static final boolean LOG = false;
    private AcceGyroGraphView mGraphView;
    private AcceGyro.Logger mLogger;
    private AcceGyro mAcceGyro;
    private GyroGestureRecognizer mRecognizer;

    private PilotableContent mContent;
    private TextToSpeech mTts;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.w(getClass().getName(), "received: " + intent.getStringExtra("TYPE") + " " + intent.getStringExtra("VALUE"));
            GyroGestureRecognizer.GyroGesture gesture = GyroGestureRecognizer.GyroGesture.valueOf(GyroGestureRecognizer.GyroGesture.class, intent.getStringExtra("VALUE"));
            switch (gesture) {
                case UP:
                    if (mContent.up()) {
                        mTts.speak((String)mContent.getContentDescription(), TextToSpeech.QUEUE_FLUSH, null);
                    } else
                        mTts.speak("Sorry. Cannot go up anymore.", TextToSpeech.QUEUE_FLUSH, null);
                    break;
                case DOWN:
                    if (mContent.down()) {
                        Object content = mContent.getContent();
                        if (content instanceof String[]) {
                            mTts.speak((String)mContent.getContentDescription() + ". Tilt left or right to choose.", TextToSpeech.QUEUE_FLUSH, null);
                            mTts.speak((String)mContent.getContent(), TextToSpeech.QUEUE_ADD, null);
                        } else if (content instanceof String)
                            mTts.speak((String)content, TextToSpeech.QUEUE_FLUSH, null);
                    } else
                        mTts.speak("Sorry. Cannot go down anymore.", TextToSpeech.QUEUE_FLUSH, null);
                    break;
                case LEFT:
                    if (mContent.previous()) {
                        mTts.speak((String)mContent.getContent(), TextToSpeech.QUEUE_FLUSH, null);
                    } else
                        mTts.speak("Sorry. No more before this one.", TextToSpeech.QUEUE_FLUSH, null);
                    break;
                case RIGHT:
                    if (mContent.next()) {
                        mTts.speak((String)mContent.getContent(), TextToSpeech.QUEUE_FLUSH, null);
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContent = new PilotableContent();
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
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter(GyroGestureRecognizer.NAVIGATION_ACTION));
        mAcceGyro.startListening();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        mAcceGyro.stopListening();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mAcceGyro.deleteObservers();
        if (mTts != null)
            mTts.shutdown();
        super.onDestroy();
    }

    @Override
    public void onInit(int status) {
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

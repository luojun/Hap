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

    private TextToSpeech mTts;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.w(getClass().getName(), "received: " + intent.getStringExtra("TYPE") + " " + intent.getStringExtra("VALUE"));
            mTts.speak(intent.getStringExtra("VALUE"), TextToSpeech.QUEUE_FLUSH, null);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

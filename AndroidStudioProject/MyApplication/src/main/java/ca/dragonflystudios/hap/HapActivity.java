package ca.dragonflystudios.hap;

import android.app.Activity;
import android.hardware.SensorManager;
import android.os.Bundle;

public class HapActivity extends Activity {
    private static final boolean LOG = false;
    private AcceGyroGraphView mGraphView;
    private AcceGyro.Logger mLogger;
    private AcceGyro mAcceGyro;
    private GyroGestureRecognizer mRecognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGraphView = new AcceGyroGraphView(this);
        mAcceGyro = new AcceGyro((SensorManager) getSystemService(SENSOR_SERVICE));
        mRecognizer = new GyroGestureRecognizer();
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
        mAcceGyro.startListening();
    }

    @Override
    protected void onPause() {
        mAcceGyro.stopListening();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mAcceGyro.deleteObservers();
        super.onDestroy();
    }
}

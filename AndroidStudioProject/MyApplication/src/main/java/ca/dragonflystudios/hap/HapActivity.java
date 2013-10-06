package ca.dragonflystudios.hap;

import android.app.Activity;
import android.hardware.SensorManager;
import android.os.Bundle;

public class HapActivity extends Activity {
    private static final boolean LOG = true;
    private AcceGyroGraphView mGraphView;
    private AcceGyro.Logger mLogger;
    private AcceGyro mAcceGyro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGraphView = new AcceGyroGraphView(this);
        mAcceGyro = new AcceGyro((SensorManager) getSystemService(SENSOR_SERVICE));
        mAcceGyro.addObserver(mGraphView);
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
        mAcceGyro.deleteObserver(mGraphView);
        if (BuildConfig.DEBUG && LOG)
            mAcceGyro.deleteObserver(mLogger);
        super.onDestroy();
    }
}

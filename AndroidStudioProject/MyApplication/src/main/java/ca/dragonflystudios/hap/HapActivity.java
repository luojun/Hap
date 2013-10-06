package ca.dragonflystudios.hap;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class HapActivity extends Activity {
    private static final boolean LOG = false;
    private AcceGyroGraphView mGraphView;
    private AcceGyro.Logger mLogger;
    private AcceGyro mAcceGyro;
    private GyroGestureRecognizer mRecognizer;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.w(getClass().getName(), "received: " + intent.getStringExtra("TYPE") + " " + intent.getStringExtra("VALUE"));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        super.onDestroy();
    }
}

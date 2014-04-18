package ca.dragonflystudios.hap;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.Observable;
import java.util.Observer;

public class GyroGestureRecognizer implements Observer
{
    public static final String NAVIGATION_ACTION = "hap_navigation_action";

    public enum GyroGesture
    {
        DOWN(0), UP(1), RIGHT(2), LEFT(3), COUNTERCLOCKWISE(4), CLOCKWISE(5);

        private final int ordinal;

        GyroGesture(int n)
        {
            ordinal = n;
        }

        private static GyroGesture[] allValues = values();

        public static GyroGesture getGesture(int dim, float value)
        {
            return allValues[dim * 2 + ((value >= 0f) ? 1 : 0)];
        }
    }

    public static final float RECOGNITION_THRESHOLD = 0.4f;
    public static final float IDLE_THRESHOLD = 0.1f;
    public static final float DIFF_THRESHOLD = 0.2f;

    private enum RecognizerState
    {
        IDLE, RETURNING
    }

    private LocalBroadcastManager mLocalBroadcastManager;
    private RecognizerState mState = RecognizerState.IDLE;
    private int mDimOfMax = 0;

    public GyroGestureRecognizer(Context context)
    {
        mState = RecognizerState.IDLE;
        mDimOfMax = 0;
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(context);
    }

    @Override
    public void update(Observable observable, Object data)
    {
        final float[] values = ((AcceGyro) observable).getAngularSpeed();

        // TODO: use timer to delay returning?
        switch (mState) {
            case IDLE:
                float maxAbs = 0f;
                float medAbs = 0f;
                mDimOfMax = 0;
                for (int i = 0; i < AcceGyro.DOF; i++) {
                    final float abs = Math.abs(values[i]);
                    if (abs > maxAbs) {
                        medAbs = maxAbs;
                        maxAbs = abs;
                        mDimOfMax = i;
                    }
                }

                if (maxAbs > RECOGNITION_THRESHOLD && (maxAbs - medAbs) > DIFF_THRESHOLD) {
                    Log.w(getClass().getName(), "recognized: " + mDimOfMax + "; " + values[mDimOfMax]);
                    mLocalBroadcastManager.sendBroadcast(new Intent().setAction(NAVIGATION_ACTION).putExtra("TYPE", "gyro_gesture").putExtra("VALUE", GyroGesture.getGesture(mDimOfMax, values[mDimOfMax]).name()));
                    mState = RecognizerState.RETURNING;
                }
                break;
            case RETURNING:
                if (Math.max(Math.abs(values[0]), Math.max(Math.abs(values[1]), Math.abs(values[2]))) < IDLE_THRESHOLD) {
                    mState = RecognizerState.IDLE;
                }
                break;
        }
    }

}

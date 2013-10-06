package ca.dragonflystudios.hap;

import android.util.Log;

import java.util.Observable;
import java.util.Observer;

public class GyroGestureRecognizer implements Observer {

    public static final float RECOGNITION_THRESHOLD = 0.4f;
    public static final float IDLE_THRESHOLD = 0.1f;
    public static final float DIFF_THRESHOLD = 0.2f;

    private enum RecognizerState {
        IDLE, RETURNING
    }

    private RecognizerState mState = RecognizerState.IDLE;
    private int mDimOfMax = 0;

    public GyroGestureRecognizer() {
        mState = RecognizerState.IDLE;
        mDimOfMax = 0;
    }

    @Override
    public void update(Observable observable, Object data) {
        final float[] values = ((AcceGyro) observable).getAngularSpeed();

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
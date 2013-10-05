package ca.dragonflystudios.hap;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.util.Log;

public class AcceGyro {
    public interface AccelerationObserver {
        public void onUpdateAccleration(float[] acceleration);
    }

    public interface AngularSpeedObserver {
        public void onUpdateAngularSpeed(float[] angularSpeed);
    }

    public interface AcceGyroObserver {
        public void onUpdate(float[] acceleration, float[] angularSpeed);
    }


    private AccelerationObserver mAccelerationObserver;
    private AngularSpeedObserver mAngularSpeedObserver;
    private AcceGyroObserver mAcceGyroObserver;

    public AcceGyro(AcceGyroObserver o) {
        mAcceGyroObserver = o;
    }

    public AcceGyro(AccelerationObserver aco, AngularSpeedObserver aso) {
        mAccelerationObserver = aco;
        mAngularSpeedObserver = aso;
    }

    public static final float ALPHA = 0.8f;
    public static final int DOF = 3;

    private final float[] mGravity = new float[DOF];
    private final float[] mAcceleration = new float[DOF];
    private final float[] mAngularSpeed = new float[DOF];

    public boolean update(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                updateAcceleration(event);
                break;
            case Sensor.TYPE_GYROSCOPE:
                updateAngularSpeed(event);
                break;
            default:
                return false;
        }

        if (null != mAcceGyroObserver)
            mAcceGyroObserver.onUpdate(mAcceleration, mAngularSpeed);

        return true;
    }

    private void updateAcceleration(SensorEvent event) {
        for (int i = 0; i < DOF; i++) {
            mGravity[i] = ALPHA * mGravity[i] + (1 - ALPHA) * event.values[i];
            mAcceleration[i] = event.values[i] - mGravity[i];
            Log.d(getClass().getName(), "Acceleration: " + ((i == 0) ? "    x = " : ((i == 1) ? "    y = " : "    z = ")) + event.values[i]);
        }
        if (null != mAccelerationObserver)
            mAccelerationObserver.onUpdateAccleration(mAcceleration);
    }

    private void updateAngularSpeed(SensorEvent event) {
        for (int i = 0; i < DOF; i++) {
            mAngularSpeed[i] = event.values[i];
            Log.d(getClass().getName(), "Angular Speed: " + ((i == 0) ? "    x = " : ((i == 1) ? "    y = " : "    z = ")) + event.values[i]);
        }
        if (null != mAngularSpeedObserver)
            mAngularSpeedObserver.onUpdateAngularSpeed(mAngularSpeed);
    }

    public float[] getAcceleration() {
        return mAcceleration;
    }

    public float[] getAngularSpeed() {
        return mAngularSpeed;
    }
}

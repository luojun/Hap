package ca.dragonflystudios.hap;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.Observable;
import java.util.Observer;

public class AcceGyro extends Observable implements SensorEventListener
{
    public static final float ALPHA = 0.8f;
    public static final int DOF = 3;

    public static class Logger implements Observer
    {
        @Override
        public void update(Observable observable, Object data)
        {
            final float[] values = (float[]) data;
            for (int i = 0; i < DOF; i++)
                Log.d(getClass().getName(), "Acceleration: " + ((i == 0) ? "    x = " : ((i == 1) ? "    y = " : "    z = ")) + values[i]);

            for (int i = DOF; i < 2 * DOF; i++)
                Log.d(getClass().getName(), "Angular Speed: " + ((i == DOF) ? "    x = " : ((i == DOF + 1) ? "    y = " : "    z = ")) + values[i]);
        }
    }

    public AcceGyro(SensorManager sensorManager)
    {
        mSensorManager = sensorManager;
    }

    private final float[] mGravity = new float[DOF];
    private final float[] mAcceleration = new float[DOF];
    private final float[] mAngularSpeed = new float[DOF];
    private final float[] mValues = new float[2 * DOF];

    private SensorManager mSensorManager;

    public void startListening()
    {
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void stopListening()
    {
        mSensorManager.unregisterListener(this);
    }

    private void update(SensorEvent event)
    {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                updateAcceleration(event);
                setChanged();
                break;
            case Sensor.TYPE_GYROSCOPE:
                updateAngularSpeed(event);
                setChanged();
                break;
            default:
                return;
        }

        notifyObservers(mValues);
    }

    private void updateAcceleration(SensorEvent event)
    {
        for (int i = 0; i < DOF; i++) {
            mGravity[i] = ALPHA * mGravity[i] + (1 - ALPHA) * event.values[i];
            mAcceleration[i] = event.values[i] - mGravity[i];
            mValues[i] = mAcceleration[i];
        }
    }

    private void updateAngularSpeed(SensorEvent event)
    {
        for (int i = 0; i < DOF; i++) {
            mAngularSpeed[i] = event.values[i];
            mValues[i + DOF] = mAngularSpeed[i];
        }
    }

    public float[] getAcceleration()
    {
        return mAcceleration;
    }

    public float[] getAngularSpeed()
    {
        return mAngularSpeed;
    }

    public float[] getValues()
    {
        return mValues;
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        update(event);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
    }
}

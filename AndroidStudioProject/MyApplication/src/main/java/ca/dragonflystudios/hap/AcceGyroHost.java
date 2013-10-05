package ca.dragonflystudios.hap;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

public class AcceGyroHost implements SensorEventListener {
    private AcceGyro mAcceGyro;

    public AcceGyroHost(AcceGyro.AcceGyroObserver observer) {
        mAcceGyro = new AcceGyro(observer);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        mAcceGyro.update(event);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}

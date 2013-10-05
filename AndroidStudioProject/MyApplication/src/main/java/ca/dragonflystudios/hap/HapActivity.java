/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ca.dragonflystudios.hap;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class HapActivity extends Activity {
    private SensorManager mSensorManager;
    private GraphView mGraphView;
    private AcceGyroHost mAcceGryoHost;

    public static class AcceGyro {
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
                mAngularSpeed[0] = event.values[0];
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

    public static class AcceGyroHost implements SensorEventListener {
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

    private class GraphView extends View implements AcceGyro.AcceGyroObserver {
        private Bitmap mBitmap;
        private Paint mPaint = new Paint();
        private Canvas mCanvas = new Canvas();
        private Path mPath = new Path();
        private float mLastValues[] = new float[3];
        private int mColors[] = new int[3];
        private float mLastX;
        private float mScale;
        private float mYOffset;
        private float mMaxX;
        private float mSpeed = 0.2f;
        private float mWidth;
        private float mHeight;

        public GraphView(Context context) {
            super(context);
            mColors[0] = Color.argb(255, 255, 0, 0);
            mColors[1] = Color.argb(255, 0, 255, 0);
            mColors[2] = Color.argb(255, 0, 0, 255);

            mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
            mCanvas.setBitmap(mBitmap);
            mCanvas.drawColor(0x00000000);
            mYOffset = h * 0.5f;
            mScale = -(h * 0.5f * (1.0f / (SensorManager.STANDARD_GRAVITY * 2)));
            mWidth = w;
            mHeight = h;
            if (mWidth < mHeight) {
                mMaxX = w;
            } else {
                mMaxX = w - 50;
            }
            mLastX = mMaxX;
            super.onSizeChanged(w, h, oldw, oldh);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            synchronized (this) {
                if (mBitmap != null) {
                    final Paint paint = mPaint;
                    final Path path = mPath;
                    final int outer = 0xFFC0C0C0;
                    final int inner = 0xFFff7010;

                    if (mLastX >= mMaxX) {
                        mLastX = 0;
                        final Canvas cavas = mCanvas;
                        final float yoffset = mYOffset;
                        final float maxx = mMaxX;
                        final float oneG = SensorManager.STANDARD_GRAVITY * mScale;
                        paint.setColor(0xFFAAAAAA);
                        cavas.drawColor(0xFFFFFFFF);
                        cavas.drawLine(0, yoffset, maxx, yoffset, paint);
                        cavas.drawLine(0, yoffset + oneG, maxx, yoffset + oneG, paint);
                        cavas.drawLine(0, yoffset - oneG, maxx, yoffset - oneG, paint);
                    }
                    canvas.drawBitmap(mBitmap, 0, 0, null);

                    if (mWidth < mHeight) {
                        float w0 = mWidth * 0.333333f;
                        float w = w0 - 32;
                        float x = w0 * 0.5f;
                        for (int i = 0; i < 3; i++) {
                            canvas.save(Canvas.MATRIX_SAVE_FLAG);
                            canvas.translate(x, w * 0.5f + 4.0f);
                            canvas.save(Canvas.MATRIX_SAVE_FLAG);
                            paint.setColor(outer);
                            canvas.scale(w, w);
                            canvas.restore();
                            canvas.scale(w - 5, w - 5);
                            paint.setColor(inner);
                            canvas.drawPath(path, paint);
                            canvas.restore();
                            x += w0;
                        }
                    } else {
                        float h0 = mHeight * 0.333333f;
                        float h = h0 - 32;
                        float y = h0 * 0.5f;
                        for (int i = 0; i < 3; i++) {
                            canvas.save(Canvas.MATRIX_SAVE_FLAG);
                            canvas.translate(mWidth - (h * 0.5f + 4.0f), y);
                            canvas.save(Canvas.MATRIX_SAVE_FLAG);
                            paint.setColor(outer);
                            canvas.scale(h, h);
                            canvas.restore();
                            canvas.scale(h - 5, h - 5);
                            paint.setColor(inner);
                            canvas.drawPath(path, paint);
                            canvas.restore();
                            y += h0;
                        }
                    }

                }
            }
        }

        public void onUpdate(float[] acceleration, float[] angularSpeed) {
            synchronized (this) {
                if (mBitmap != null) {
                    final Canvas canvas = mCanvas;
                    final Paint paint = mPaint;
                    float deltaX = mSpeed;
                    float newX = mLastX + deltaX;

                    for (int i = 0; i < 3; i++) {
                        final float v = mYOffset + acceleration[i] * mScale;
                        paint.setColor(mColors[i]);
                        canvas.drawLine(mLastX, mLastValues[i], newX, v, paint);
                        mLastValues[i] = v;
                    }
                    mLastX += mSpeed;

                    invalidate();
                }

            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Be sure to call the super class.
        super.onCreate(savedInstanceState);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mGraphView = new GraphView(this);
        mAcceGryoHost = new AcceGyroHost(mGraphView);
        setContentView(mGraphView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(mAcceGryoHost,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(mAcceGryoHost,
                mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(mAcceGryoHost);
        super.onPause();
    }
}

package ca.dragonflystudios.hap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.SensorManager;
import android.view.View;

import java.util.Observable;
import java.util.Observer;

public class AcceGyroGraphView extends View implements Observer {

    private final static int BACKGROUND_COLOR = 0xFF000000;
    private final static int LINE_COLOR = 0xFFAAAAAA;
    private final static float STEP_SIZE = 3f;

    private Bitmap mBitmap;
    private Paint mPaint = new Paint();
    private Canvas mCanvas = new Canvas();
    private int mWidth, mHeight;
    private float mLastValuesAcceleration[] = new float[3];
    private float mLastValuesAngularSpeed[] = new float[3];
    private int mColors[] = new int[3];
    private float mLastX;
    private float mScale;
    private float mYOffsetAcceleration[] = new float[3];
    private float mYOffsetAngularSpeed[] = new float[3];
    private float mMaxX;

    public AcceGyroGraphView(Context context) {
        super(context);
        mColors[0] = Color.argb(255, 255, 0, 0);
        mColors[1] = Color.argb(255, 0, 255, 0);
        mColors[2] = Color.argb(255, 0, 0, 255);
        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        synchronized (this) {
            mWidth = w;
            mHeight = h;
            resetBitmap();
        }
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        synchronized (this) {
            if (mBitmap != null) {
                if (mLastX >= mMaxX)
                    resetBitmap();
                canvas.drawBitmap(mBitmap, 0, 0, null);
            }
        }
    }

    private void resetBitmap() {
        mBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.RGB_565);
        mCanvas.setBitmap(mBitmap);
        mCanvas.drawColor(BACKGROUND_COLOR);
        for (int i = 0; i < 3; i++) {
            mYOffsetAcceleration[i] = mHeight * 0.2f + mHeight * (i * 0.1f);
            mYOffsetAngularSpeed[i] = mYOffsetAcceleration[i] + mHeight * 0.5f;
        }
        mScale = -(mHeight * 0.5f * (1.0f / (SensorManager.STANDARD_GRAVITY * 2)));
        mMaxX = mWidth;
        mLastX = 0;

        mPaint.setColor(LINE_COLOR);
        mCanvas.drawColor(BACKGROUND_COLOR);
        for (int i = 0; i < 3; i++) {
            mCanvas.drawLine(0, mYOffsetAcceleration[i], mMaxX, mYOffsetAcceleration[i], mPaint);
            mCanvas.drawLine(0, mYOffsetAngularSpeed[i], mMaxX, mYOffsetAngularSpeed[i], mPaint);
        }
    }

    @Override
    public void update(Observable observable, Object data) {
        synchronized (this) {
            if (mBitmap != null) {
                final float[] values = (float[]) data;
                final Canvas canvas = mCanvas;
                final Paint paint = mPaint;
                float newX = mLastX + STEP_SIZE;

                for (int i = 0; i < 3; i++) {
                    paint.setColor(mColors[i]);

                    final float vAcc = mYOffsetAcceleration[i] + values[i] * mScale;
                    canvas.drawLine(mLastX, mLastValuesAcceleration[i], newX, vAcc, paint);
                    mLastValuesAcceleration[i] = vAcc;

                    final float vAng = mYOffsetAngularSpeed[i] + values[i + AcceGyro.DOF] * mScale;
                    canvas.drawLine(mLastX, mLastValuesAngularSpeed[i], newX, vAng, paint);
                    mLastValuesAngularSpeed[i] = vAng;
                }
                mLastX = newX;
                invalidate();
            }

        }
    }
}

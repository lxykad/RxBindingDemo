package com.lxy.binding.annimation.widget;

import android.animation.Keyframe;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.concurrent.Semaphore;

/**
 * @author a
 */
public class MovePathView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private SurfaceHolder mSurfaceHolder;
    private Paint mPaint;
    private Keyframe mKeyframe;
    private int mAlpha;
    private volatile boolean isDrawing;

    private Semaphore mLightLineSemaphore, mDarkLineSemaphore;

    private Path path1;

    public MovePathView(Context context) {
        this(context, null);
    }

    public MovePathView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MovePathView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {
        setZOrderOnTop(true);
        mSurfaceHolder = getHolder();
        mSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
        mSurfaceHolder.addCallback(this);

        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAntiAlias(true);

        mLightLineSemaphore = new Semaphore(1);
        mDarkLineSemaphore = new Semaphore(1);

        path1 = new Path();

    }

    public void setPath(Path path) {
        mAlpha = 0;
    }

    public void startRun() {
        isDrawing = true;
        new Thread(this).start();
    }

    public void stopRun() {
        isDrawing = false;
    }

    public void startDraw(Canvas canvas) {
//        path1.moveTo(310, 0);
//
//        path1.lineTo(310, 400);
//        path1.lineTo(210, 500);
//        path1.lineTo(210, 600);
//        path1.lineTo(310, 700);
//        path1.lineTo(310, 1280);
//
//
//        canvas.drawPath(path1, mPaint);
//
//        try {
//            mDarkLineSemaphore.acquire();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        PathMeasure measure = new PathMeasure(path1, false);
//        float length = measure.getLength();
//
//        System.out.println("lenght=====" + length);
    }


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        startRun();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        System.out.println("path=======surfaceChanged");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        stopRun();
    }

    @Override
    public void run() {

        while (isDrawing) {

            Canvas canvas = mSurfaceHolder.lockCanvas();
            if (canvas == null) {
                return;
            }
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            startDraw(canvas);
            mSurfaceHolder.unlockCanvasAndPost(canvas);
        }
    }
}

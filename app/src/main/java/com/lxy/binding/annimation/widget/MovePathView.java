package com.lxy.binding.annimation.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.Keyframe;
import android.animation.ValueAnimator;
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
import android.widget.Toast;

import java.util.Arrays;
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
    private PathMeasure pathMeasure;
    private float[] mPoints = new float[]{100f, 200f, 300f, 400f};

    private Points mPathPoint;
    private float mAnimPercent;

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
        mPaint.setStrokeWidth(4);
        mPaint.setAntiAlias(true);

        mLightLineSemaphore = new Semaphore(1);
        mDarkLineSemaphore = new Semaphore(1);

        path1 = new Path();
        pathMeasure = new PathMeasure();

    }

    public void setPath(Path path) {
        mPathPoint = new Points(path);
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

        if (mPathPoint != null) {
            float[] points = mPathPoint.mPoints;
            canvas.drawPoints(points, mPaint);

        }

    }

    public void startAnim(int duration) {

        ValueAnimator animator = ValueAnimator.ofFloat(1f, 0).setDuration(duration);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mAnimPercent = (float) valueAnimator.getAnimatedValue();
                System.out.println("value=====" + mAnimPercent);
                invalidate();
            }
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);


            }
        });
        animator.start();

    }


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        startRun();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

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

    public static class Points {

        int mPointNumbers;
        float[] mPoints;


        public Points(Path path) {
            init(path);
        }

        private void init(Path path) {
            PathMeasure pathMeasure = new PathMeasure(path, false);
            float length = pathMeasure.getLength();

            mPointNumbers = (int) (length + 1);
            mPoints = new float[mPointNumbers * 2];


            int index = 0;
            float[] mCurrentPosition = new float[2];

            for (int i = 0; i < mPointNumbers; i++) {
                float distance = i * length / (mPointNumbers - 1);
                pathMeasure.getPosTan(distance, mCurrentPosition, null);

                // 赋值每个点的坐标
                mPoints[index] = mCurrentPosition[0];
                mPoints[index + 1] = mCurrentPosition[1];
                index += 2;

            }
            mPointNumbers = mPoints.length;
        }

        /**
         * 裁剪数组
         * start end 百分比
         */
        float[] getRangePoints(float start, float end) {

            int startIndex = (int) (mPointNumbers * start);
            int endIndex = (int) (mPointNumbers * end);

            //必须是偶数，因为需要float[]{x,y}这样x和y要配对的
            if (startIndex % 2 != 0) {
                //直接减，不用担心 < 0  因为0是偶数，哈哈
                --startIndex;
            }
            if (endIndex % 2 != 0) {
                //不用检查越界
                ++endIndex;
            }

            return startIndex > endIndex ? Arrays.copyOfRange(mPoints, endIndex, startIndex) : null;
        }

    }

}

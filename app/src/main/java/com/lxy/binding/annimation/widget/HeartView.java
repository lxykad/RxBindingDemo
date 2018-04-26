package com.lxy.binding.annimation.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

/**
 * @author a
 */
public class HeartView extends View {

    private static final int PATH_WIDTH = 2;
    // 起始点
    private static final int[] START_POINT = new int[]{
            300, 270
    };
    // 爱心下端点
    private static final int[] BOTTOM_POINT = new int[]{
            300, 400
    };
    // 右侧控制点
    private static final int[] LEFT_CONTROL_POINT = new int[]{
            450, 200
    };
    // 左侧控制点
    private static final int[] RIGHT_CONTROL_POINT = new int[]{
            150, 200
    };

    private PathMeasure mPathMeasure;
    private Paint mPaint;
    private Path mPath;
    private float[] mCurrentPosition = new float[2];


    public HeartView(Context context) {
        this(context, null);
    }

    public HeartView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HeartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(PATH_WIDTH);
        mPaint.setColor(Color.RED);

        mPath = new Path();
        mPath.moveTo(START_POINT[0], START_POINT[1]);
        // 两条贝塞尔曲线
        mPath.quadTo(RIGHT_CONTROL_POINT[0], RIGHT_CONTROL_POINT[1], BOTTOM_POINT[0],
                BOTTOM_POINT[1]);
        mPath.quadTo(LEFT_CONTROL_POINT[0], LEFT_CONTROL_POINT[1], START_POINT[0], START_POINT[1]);

        mPathMeasure = new PathMeasure(mPath, false);
        mCurrentPosition = new float[2];
        mCurrentPosition[0] = 300f;
        mCurrentPosition[1] = 270f;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 画心形
        canvas.drawPath(mPath, mPaint);
        // 画左右两个圆圈
        canvas.drawCircle(RIGHT_CONTROL_POINT[0], RIGHT_CONTROL_POINT[1], 5, mPaint);
        canvas.drawCircle(LEFT_CONTROL_POINT[0], LEFT_CONTROL_POINT[1], 5, mPaint);
        // 移动圆环的初始位置
        canvas.drawCircle(mCurrentPosition[0], mCurrentPosition[1], 10, mPaint);
    }

    /**
     * 开启路径动画
     *
     * @param duration
     */
    public void startAnim(long duration) {

        // 获取path路径的长度
        float length = mPathMeasure.getLength();
        ValueAnimator animator = ValueAnimator.ofFloat(length);
        animator.setDuration(duration);
        // 减速插值器
        animator.setInterpolator(new DecelerateInterpolator());

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float distance = (float) valueAnimator.getAnimatedValue();
                System.out.println("dis=====" + distance);

                mPathMeasure.getPosTan(distance, mCurrentPosition, null);
                postInvalidate();

            }
        });
        animator.start();
    }

}

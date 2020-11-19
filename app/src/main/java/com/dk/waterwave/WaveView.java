package com.dk.waterwave;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * Y = A * sin(w*x+φ) + K
 * Y Y轴的坐标点
 * A 最高点和最低点差
 * w 角速度
 * x 当前x轴坐标点
 * φ 初相，反映在坐标就是图像左右移动，这里通过不断改变φ,达到波浪移动效果
 * K 偏距，反映在坐标系上则为图像的上移或下移。
 */
public class WaveView extends View {

    private int A;//波峰波谷差距
    private int K;//偏距，图像上下移动，Y轴
    private double W;//角速度 W＝2兀/T, T为转动周期
    private double φ = 0;//初相
    private double speed = 0.03;//φ每次递减变化的值
    private int pMoveX = 20;//sin变换，每次x轴向右的步长

    private Context mContext;
    private Paint mPaint;
    private Path mPath;
    private ValueAnimator valueAnimator;
    private int color;
    private int startDelayedMills;//延迟开始时间

    public WaveView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        getAttrs(attrs);
        A = dp2px(30);//控制波浪高度
        K = A;
        //w = 2 * Math.PI / dp2px(1080);//控制速度，也就是两个波谷距离
        initPaint();
        initAnimation();
    }

    private void getAttrs(AttributeSet attributeSet) {
        TypedArray typedArray = mContext.obtainStyledAttributes(attributeSet, R.styleable.WaveView);
        color = typedArray.getColor(R.styleable.WaveView_waveColor, Color.CYAN);
        startDelayedMills = typedArray.getInt(R.styleable.WaveView_waveStartDelayed, 0);
        typedArray.recycle();
    }

    private void initPaint() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setColor(color);
        mPath = new Path();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        W = 2 * Math.PI / dp2px(500);//控制速度，也就是两个波谷距离
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawSin(canvas);//绘制
    }

    /**
     * 从一边波动到另一边，x从0点开始，每次向右pMoveX个单位取点，
     * 同时根据sin公式求出当前x所对应的y点 ->得坐标（x,y）
     * -> mPath绘制出来
     */
    private void drawSin(Canvas mCanvas) {
        φ -= speed;
        mPath.reset();//重置路径
        mPath.moveTo(0, 0);//从左上角开始

        float y;//Y坐标
        for (float x = 0; x <= getWidth(); x = x + pMoveX) {
            y = (float) (A * Math.sin(W * x + φ) + K);
            mPath.lineTo(x, y);
            Log.d("dk", "φ：" + φ
                    + "，x:" + x + "，y:" + y
                    + "，w:" + W + "，pMoveX:" + pMoveX
                    + "，A:" + A + "，K:" + K);
        }

        //绘制出曲线下方的矩形 / 填充矩形
        mPath.lineTo(getWidth(), getHeight());//移动到屏幕最右下角点
        mPath.lineTo(0, getHeight());//再移动到屏幕最左下角点
        mPath.close();//闭合到上方曲线

        //绘制出曲线
        mCanvas.drawPath(mPath, mPaint);
    }

    private void initAnimation() {
        valueAnimator = ValueAnimator.ofInt(0, getWidth());
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.setDuration(1000);
        valueAnimator.setRepeatMode(ValueAnimator.RESTART);
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Log.d("dk", "动画变化..");
                invalidate();//刷新页面，调用onDraw()
            }
        });
        startAnimation();
    }

    public void startAnimation() {
        if (valueAnimator != null) {
            valueAnimator.setStartDelay(startDelayedMills);
            valueAnimator.start();
        }
    }

    public void stopAnimation() {
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
    }

    private int dp2px(float dpValue) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,//单位dp
                dpValue,//传入要转换的dp值
                getResources().getDisplayMetrics()//获取屏幕的显示指标
        );
    }
}

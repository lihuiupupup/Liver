package com.lihui.android.liver.view;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.lihui.android.liver.R;

/**
 * create by lihui
 */
public class DanceProgressView extends LinearLayout {

    private ImageView load1;
    private ImageView load2;
    private ImageView load3;

    private ObjectAnimator scaleAnimator1X;
    private ObjectAnimator scaleAnimator1Y;
    private ObjectAnimator scaleAnimator2X;
    private ObjectAnimator scaleAnimator2Y;
    private ObjectAnimator scaleAnimator3X;
    private ObjectAnimator scaleAnimator3Y;
    private AnimatorSet animatorSet;

    private int duration = 2000;


    public DanceProgressView(Context context) {
        this(context,null);
    }

    public DanceProgressView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public DanceProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setUp(context);
    }

    private void setUp(Context context) {
        setOrientation(HORIZONTAL);
        load1 = new ImageView(context);
        load1.setImageDrawable(context.getResources().getDrawable(R.mipmap.ic_load));
        load2 = new ImageView(context);
        load2.setImageDrawable(context.getResources().getDrawable(R.mipmap.ic_load));
        load3 = new ImageView(context);
        load3.setImageDrawable(context.getResources().getDrawable(R.mipmap.ic_load));

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        load1.setLayoutParams(layoutParams);
        load2.setLayoutParams(layoutParams);
        load3.setLayoutParams(layoutParams);

        load1.setScaleX(0);
        load2.setScaleX(0);
        load3.setScaleX(0);
        load1.setScaleY(0);
        load2.setScaleY(0);
        load3.setScaleY(0);
        addView(load1);
        addView(load2);
        addView(load3);
    }

    public void start() {
        scaleAnimator1X = ObjectAnimator.ofFloat(load1,"scaleX",0,1.0f,1.3f,1.0f,0,0,0,0,0);
        scaleAnimator1X.setDuration(duration);
        scaleAnimator1X.setRepeatCount(ValueAnimator.INFINITE);
        scaleAnimator1Y = ObjectAnimator.ofFloat(load1,"scaleY",0,1.0f,1.3f,1.0f,0,0,0,0,0);
        scaleAnimator1Y.setDuration(duration);
        scaleAnimator1Y.setRepeatCount(ValueAnimator.INFINITE);
        scaleAnimator2X = ObjectAnimator.ofFloat(load2,"scaleX",0,0,0,1.0f,1.3f,1.0f,0,0,0);
        scaleAnimator2X.setDuration(duration);
        scaleAnimator2X.setRepeatCount(ValueAnimator.INFINITE);
        scaleAnimator2Y = ObjectAnimator.ofFloat(load2,"scaleY",0,0,0,1.0f,1.3f,1.0f,0,0,0);
        scaleAnimator2Y.setDuration(duration);
        scaleAnimator2Y.setRepeatCount(ValueAnimator.INFINITE);
        scaleAnimator3X = ObjectAnimator.ofFloat(load3,"scaleX",0,0,0,0,0,1.0f,1.3f,1.0f,0);
        scaleAnimator3X.setDuration(duration);
        scaleAnimator3X.setRepeatCount(ValueAnimator.INFINITE);
        scaleAnimator3Y = ObjectAnimator.ofFloat(load3,"scaleY",0,0,0,0,0,1.0f,1.3f,1.0f,0);
        scaleAnimator3Y.setDuration(duration);
        scaleAnimator3Y.setRepeatCount(ValueAnimator.INFINITE);
        animatorSet = new AnimatorSet();
        animatorSet.setDuration(duration);

        animatorSet.playTogether(scaleAnimator1X,scaleAnimator1Y,scaleAnimator2X,scaleAnimator2Y,scaleAnimator3X,scaleAnimator3Y);
        animatorSet.start();
    }

    public void cancel() {
        if (animatorSet != null &&animatorSet.isRunning()) {
            animatorSet.cancel();
        }
    }
}


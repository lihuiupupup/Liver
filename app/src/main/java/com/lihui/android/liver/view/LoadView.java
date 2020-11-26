package com.lihui.android.liver.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.lihui.android.liver.R;

public class LoadView extends LinearLayout {
    public LoadView(Context context) {
        super(context);
        init(context);
    }

    public LoadView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LoadView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setBackground(getResources().getDrawable(R.drawable.shape_load));
        setOrientation(VERTICAL);
        setPadding(40, 40, 40, 40);
        float d = context.getResources().getDisplayMetrics().density;
        LinearLayout.LayoutParams layoutParams = new LayoutParams((int) d*200, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        layoutParams.bottomMargin = 40;

        setLayoutParams(layoutParams);


        danceProgressView = new DanceProgressView(context);
        LinearLayout.LayoutParams layoutParams1 = (LayoutParams) danceProgressView.getLayoutParams();
        if (layoutParams1 == null) {
            layoutParams1 = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        layoutParams1.bottomMargin = 40;
        layoutParams1.gravity = Gravity.CENTER_HORIZONTAL;
        danceProgressView.setLayoutParams(layoutParams1);
        addView(danceProgressView);
        textView = new TextView(context);
        ViewGroup.LayoutParams layoutParams2 = textView.getLayoutParams();
        if (layoutParams2 == null) {
            layoutParams2 = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        layoutParams2.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        layoutParams2.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        textView.setLayoutParams(layoutParams2);
        textView.setText("加载中...");
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        textView.setTextSize(10);
        addView(textView);

    }

    public void toggleLoading() {
        if (this.getVisibility() == View.VISIBLE) {
            this.setVisibility(View.GONE);

        } else {
            this.setVisibility(View.VISIBLE);
        }
    }

    DanceProgressView danceProgressView;
    TextView textView;

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (danceProgressView != null) {
            if (visibility == VISIBLE) {
                danceProgressView.start();
            } else {
                danceProgressView.cancel();
                textView.setText("加载中...");
            }
        }
    }

    public void setTip(String tip) {
        textView.setText(tip);
    }
}

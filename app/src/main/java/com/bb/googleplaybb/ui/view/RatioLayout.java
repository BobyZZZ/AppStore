package com.bb.googleplaybb.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.bb.googleplaybb.R;

/**
 * Created by Boby on 2018/7/14.
 */

public class RatioLayout extends FrameLayout {

    private float ratio;

    public RatioLayout(@NonNull Context context) {
        super(context);
    }

    public RatioLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RatioLayout);
        ratio = typedArray.getFloat(R.styleable.RatioLayout_ratio, -1);
        System.out.println("ratio:"+ratio);
    }

    public RatioLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int wideMode = MeasureSpec.getMode(widthMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        //宽确定，高不确定，ratio大于0
        if (wideMode == MeasureSpec.EXACTLY && heightMode != MeasureSpec.EXACTLY && ratio > 0) {
            int imageWidth = width - getPaddingLeft() - getPaddingRight();
            System.out.println("width:"+width);

            int imageHeight = (int) (imageWidth / ratio);
            height = imageHeight + getPaddingTop() + getPaddingBottom();
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height,MeasureSpec.EXACTLY);//此处模式传MeasureSpec.EXACTLY固定模式
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}

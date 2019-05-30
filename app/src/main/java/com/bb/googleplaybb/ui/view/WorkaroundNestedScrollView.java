package com.bb.googleplaybb.ui.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by Boby on 2019/5/30.
 */

public class WorkaroundNestedScrollView extends NestedScrollView {
    public WorkaroundNestedScrollView(@NonNull Context context) {
        super(context);
    }

    public WorkaroundNestedScrollView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public WorkaroundNestedScrollView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            computeScroll();
        }
        return super.onInterceptTouchEvent(ev);
    }
}

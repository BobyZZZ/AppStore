package com.bb.googleplaybb.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.bb.googleplaybb.R;
import com.bb.googleplaybb.manager.AppDownloadManager;
import com.bb.googleplaybb.utils.UIUtils;

/**
 * Created by Boby on 2019/4/24.
 */

public class DownloadButton extends android.support.v7.widget.AppCompatTextView {
    private Paint mPaint, mPaint2;
    private RectF mRect;
    private int mWidth;
    private String mProgress = "下载";
    private float mX;
    private float mY;

    public DownloadButton(Context context) {
        super(context);
        init(context);
    }

    public DownloadButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DownloadButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mWidth = w;
        mRect.bottom = mRect.top + h;

        //x,y,用于进度文字
        Paint.FontMetrics fontMetrics = mPaint2.getFontMetrics();
        float distance = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom;
        mX = (mRect.left + mWidth) / 2;
        mY = (mRect.top + getMeasuredHeight()) / 2 + distance;
        super.onSizeChanged(w, h, oldw, oldh);
    }

    private void init(Context context) {
        //画进度
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(0xff1296db);
        mPaint.setStyle(Paint.Style.FILL);
        //写字
        mPaint2 = new Paint();
        mPaint2.setAntiAlias(true);
        mPaint2.setDither(true);
        mPaint2.setColor(Color.BLACK);
        mPaint2.setStrokeWidth(5);
        mPaint2.setTextAlign(Paint.Align.CENTER);
        mPaint2.setTextSize(UIUtils.dip2px(14));

        //progressRect
        mRect = new RectF();
        int[] is = new int[2];
        getLocationInWindow(is);
        mRect.left = is[0];
        mRect.top = is[1];
        mRect.right = mRect.left;

        //设置背景边框
        Drawable drawable = context.getResources().getDrawable(R.drawable.shape_download_button_bg);
        setBackground(drawable);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawRoundRect(mRect, UIUtils.dip2px(4), UIUtils.dip2px(4), mPaint);
        canvas.drawText(mProgress, mX, mY, mPaint2);
    }

    private float progress;
    private int state;

    public void setProgress(float progress) {
        if (this.progress == progress) {
            return;
        }
        this.progress = progress >= 1 ? 0 : progress;
        mRect.right = mRect.left + this.progress * mWidth;
        invalidate();
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        if (this.state == state) {
            return;
        }
        this.state = state;
        switch (state) {
            case AppDownloadManager.STATE_UNDO:
                mProgress = "下载";
                break;
            case AppDownloadManager.STATE_PAUSE:
                mProgress = "继续";
                break;
            case AppDownloadManager.STATE_WAITING:
                mProgress = "等待";
                break;
            case AppDownloadManager.STATE_DOWNLOADING:
                mProgress = "暂停";
                break;
            case AppDownloadManager.STATE_ERROR:
                mProgress = "重试";
                break;
            case AppDownloadManager.STATE_SUCCESS:
                mProgress = "安装";
                mRect.right = mRect.left;
                break;
        }
        invalidate();
    }
}

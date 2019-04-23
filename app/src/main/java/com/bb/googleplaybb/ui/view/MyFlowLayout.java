package com.bb.googleplaybb.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.bb.googleplaybb.utils.UIUtils;

import java.util.ArrayList;

/**
 * Created by Boby on 2018/7/17.
 */

public class MyFlowLayout extends ViewGroup {

    private ArrayList<Line> lineList = new ArrayList<>();
    private int mCurrentWidth = 0;//一行中所有控件占的宽度
    private int horizontalSpace = UIUtils.dip2px(5);
    private int verticalSpace = UIUtils.dip2px(6);
    private int maxLine = 50;

    private Line mLine = null;

    public MyFlowLayout(Context context) {
        super(context);
    }

    public MyFlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyFlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        System.out.println("共有："+lineList.size()+"行");
        int left = l + getPaddingLeft();
        int top = t + getPaddingTop();
        for (int j = 0; j < lineList.size(); j++) {
            Line line = lineList.get(j);
            line.layout(left, top);
            top += line.maxHeight + verticalSpace;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();//控件的总宽度
        int height = MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop() - getPaddingBottom();
        System.out.println("控件的总宽度：" + width);
        System.out.println("控件的总高度：" + height);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int childCount = getChildCount();
        System.out.println("childCount:" + childCount);

        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);

            int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(width, widthMode == MeasureSpec.EXACTLY ? MeasureSpec.AT_MOST : widthMode);//父控件确定时，包裹内容；其余情况跟父控件一致，最大为width
            int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height, heightMode == MeasureSpec.EXACTLY ? MeasureSpec.AT_MOST : heightMode);

            childView.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            int childWidth = childView.getMeasuredWidth();
            int childHeight = childView.getMeasuredHeight();
            System.out.println("子控件的宽度：" + childWidth);
            System.out.println("子控件的高度：" + childHeight);

            mCurrentWidth += childWidth;
            if (mCurrentWidth <= width) {//当前行总宽度小于控件总宽度，则可以将添加在当前行
                //将当前子控件添加到行对象中
                if (mLine == null) {
                    mLine = new Line();
                }
                mLine.addView(childView);
                //加上水平间距
                mCurrentWidth += horizontalSpace;
                if (mCurrentWidth >= width) {
                    //换行
                    if (!newLine()) {
                        break;
                    }
                }
            } else {
                if (mLine.getViewCount() == 0) {
                    //一行只有一个超长的子控件
                    mLine.addView(childView);//强行添加
                    if (!newLine()) {
                        break;
                    }
                } else {
                    //所剩空间不足以添加一个子控件，换行，添加进去
                    if (!newLine()) {
                        break;
                    }
                    mLine.addView(childView);
                    mCurrentWidth += childWidth + horizontalSpace;
                }
            }
        }
        //将最后一行添加到集合中
        if (mLine != null && mLine.getViewCount() > 0 && !lineList.contains(mLine)) {
            lineList.add(mLine);
        }


        int totalWidth = MeasureSpec.getSize(widthMeasureSpec);
        //此处不能直接用MeasureSpec.getSize(widthMeasureSpec)来获取高度，应该通过测量计算获取高度;
        int totalHeight = 0;
        for (int i = 0; i < lineList.size(); i++) {
            Line line = lineList.get(i);
            totalHeight += line.maxHeight;
        }
        totalHeight += (lineList.size()-1)*verticalSpace + getPaddingTop() + getPaddingBottom();

        setMeasuredDimension(totalWidth, totalHeight);
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private boolean newLine() {
        lineList.add(mLine);
        if (lineList.size() < maxLine) {
            mLine = new Line();
            mCurrentWidth = 0;//清零
            System.out.println("换行了");
            return true;
        }
        return false;
    }

    class Line {
        private ArrayList<View> viewList = new ArrayList<>();
        private int totalWidth;
        private int maxHeight;

        public void addView(View childView) {
            viewList.add(childView);
            int measuredWidth = childView.getMeasuredWidth();
            int measuredHeight = childView.getMeasuredHeight();
            totalWidth += measuredWidth;

            if (measuredHeight > maxHeight) {
                maxHeight = measuredHeight;
            }
        }

        public int getViewCount() {
            return viewList.size();
        }

        public void layout(int left, int top) {
            int count = viewList.size();
            int width = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
            int surplus = width - totalWidth - (count - 1) * horizontalSpace;//剩余的宽度
            int addWidth = surplus / count;//平均分配的宽度

            for (int i = 0; i < viewList.size(); i++) {
                View view = viewList.get(i);
                int measuredWidth = view.getMeasuredWidth() + addWidth;
                int measuredHeight = view.getMeasuredHeight();

                int widthMeasureSpec = MeasureSpec.makeMeasureSpec(measuredWidth, MeasureSpec.EXACTLY);
                int heightMeasureSpec = MeasureSpec.makeMeasureSpec(measuredHeight, MeasureSpec.EXACTLY);

                view.measure(widthMeasureSpec, heightMeasureSpec);//重新测量

                int offSet = 0;
                if (measuredHeight < maxHeight) {
                    //向下偏移量
                    offSet = (maxHeight - measuredHeight) / 2;
                }
                view.layout(left, top + offSet, left + measuredWidth, top + offSet + measuredHeight);
                left += measuredWidth + horizontalSpace;
            }
        }
    }
}

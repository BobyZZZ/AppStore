package com.bb.googleplaybb.ui.adapter.holder;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bb.googleplaybb.R;
import com.bb.googleplaybb.domain.AppInfo;
import com.bb.googleplaybb.utils.UIUtils;

/**
 * Created by Boby on 2018/7/17.
 */

public class HomeDetailDesHolder extends BaseHolder<AppInfo> {

    private LinearLayout llDisplay;
    private RelativeLayout rlControl;
    private TextView tvName, tvDes;
    private ImageView ivArr;

    @Override
    public View initView() {
        View view = UIUtils.inflate(R.layout.layout_home_detail_des);
        llDisplay = view.findViewById(R.id.ll_display);
        rlControl = view.findViewById(R.id.rl_control);
        tvName = view.findViewById(R.id.tv_name);
        tvDes = view.findViewById(R.id.tv_des);
        ivArr = view.findViewById(R.id.iv_arr);

        rlControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle();
            }
        });
        return view;
    }

    private boolean isOpen = false;

    private void toggle() {
        final LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) llDisplay.getLayoutParams();
        ValueAnimator animator;
        int shortHeight = getShortHeight();
        int longHeight = getLongHeight();
//        if (shortHeight >= longHeight) {
//            return;
//        }
        System.out.println("shortHeight:" + shortHeight);
        System.out.println("longHeight:" + longHeight);
        if (isOpen) {
            isOpen = false;
            //关闭  大->小
            animator = ValueAnimator.ofInt(longHeight, shortHeight);
        } else {
            isOpen = true;
            //打开 小->大
            animator = ValueAnimator.ofInt(shortHeight, longHeight);
        }
        animator.setDuration(200);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Integer value = (Integer) animation.getAnimatedValue();
                params.height = value;
                llDisplay.setLayoutParams(params);
            }
        });

        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                    }
                }.start();
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                if (isOpen) {
                    //向上
                    ivArr.setImageResource(R.drawable.arrow_up);
                    //scrollView滑到底部
                    ScrollView scrollView = getScrollView();
                    scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                } else {
                    //向下
                    ivArr.setImageResource(R.drawable.arrow_down);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.start();
    }

    private ScrollView getScrollView() {
        ViewParent parent = tvDes.getParent();
        while (!(parent instanceof ScrollView)) {
            parent = parent.getParent();
        }
        return (ScrollView) parent;
    }

    @Override
    public void refreshView(AppInfo data) {
        tvName.setText(data.name);
        tvDes.setText(data.des);

        tvDes.post(new Runnable() {
            @Override
            public void run() {
                //将高度设置为7行的高度
                int height = getShortHeight();
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) llDisplay.getLayoutParams();
                params.height = height;
                llDisplay.setLayoutParams(params);
            }
        });
    }

    public int getShortHeight() {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tvDes.getLayoutParams();
        int width = tvDes.getMeasuredWidth();

        TextView textView = new TextView(UIUtils.getContext());
        textView.setLayoutParams(params);
        textView.setMaxLines(7);
        textView.setText(getData().des);

        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(2000, View.MeasureSpec.AT_MOST);

//        textView.measure(0, 0);//不可代替下面
        textView.measure(widthMeasureSpec, heightMeasureSpec);
        int height = textView.getMeasuredHeight();

        return height;
    }

    public int getLongHeight() {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tvDes.getLayoutParams();
        int width = tvDes.getMeasuredWidth();

        TextView textView = new TextView(UIUtils.getContext());
        textView.setLayoutParams(params);
        textView.setText(getData().des);

        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(2000, View.MeasureSpec.AT_MOST);

        textView.measure(widthMeasureSpec, heightMeasureSpec);
        int height = textView.getMeasuredHeight();

        return height;
    }
}
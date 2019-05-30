package com.bb.googleplaybb.ui.adapter.holder;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.support.design.widget.AppBarLayout;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bb.googleplaybb.R;
import com.bb.googleplaybb.domain.AppInfo;
import com.bb.googleplaybb.ui.view.ConflictNestedScrollView;
import com.bb.googleplaybb.utils.UIUtils;

/**
 * Created by Boby on 2018/7/17.
 */

public class HomeDetailDesHolder extends BaseHolder<AppInfo> {

    private LinearLayout llDisplay,llControl;
    private RelativeLayout rlControl;
    private TextView tvName, tvDes;
    private ImageView ivArr;
    AppBarLayout mAppBarLayout;

    private boolean isOpen = false;

    public HomeDetailDesHolder(AppBarLayout appBar) {
        mAppBarLayout = appBar;
    }

    @Override
    public View initView() {
        View view = UIUtils.inflate(R.layout.layout_home_detail_des);
        llControl = view.findViewById(R.id.ll_control);
        llDisplay = view.findViewById(R.id.ll_display);
        rlControl = view.findViewById(R.id.rl_control);
        tvName = view.findViewById(R.id.tv_name);
        tvDes = view.findViewById(R.id.tv_des);
        ivArr = view.findViewById(R.id.iv_arr);

        llControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("zyc", "onClick: " );
                toggle();
            }
        });
//        rlControl.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                toggle();
//            }
//        });
        return view;
    }

    private void toggle() {
        final LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) llDisplay.getLayoutParams();
        ValueAnimator animator;
        int shortHeight = getShortHeight();
        int longHeight = getLongHeight();
        if (shortHeight >= longHeight) {
            isOpen = !isOpen;
            ivArr.setImageResource(isOpen ? R.drawable.arrow_up : R.drawable.arrow_down);
            return;
        }

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
                if (!isOpen) {
                    mAppBarLayout.setExpanded(true);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (isOpen) {
                    //向上
                    ivArr.setImageResource(R.drawable.arrow_up);
//                    //scrollView滑到底部
                    mAppBarLayout.setExpanded(false);
                    ConflictNestedScrollView scrollView = getScrollView();
                    scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                } else {
                    //向下
                    ivArr.setImageResource(R.drawable.arrow_down);
//                    mAppBarLayout.setExpanded(true);
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

    private ConflictNestedScrollView getScrollView() {
        ViewParent parent = tvDes.getParent();
        while (!(parent instanceof ConflictNestedScrollView)) {
            parent = parent.getParent();
        }
        return (ConflictNestedScrollView) parent;
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
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,18);
        textView.setText(getData().des);

        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(2000, View.MeasureSpec.AT_MOST);

//        textView.measure(0, 0);//不可代替下面
        textView.measure(widthMeasureSpec, heightMeasureSpec);
        int height = textView.getMeasuredHeight();

        System.out.println("shortHeight:" + height + "---getHeight:" + textView.getHeight());
        return height;
    }

    public int getLongHeight() {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tvDes.getLayoutParams();
        int width = tvDes.getMeasuredWidth();

        TextView textView = new TextView(UIUtils.getContext());
        textView.setLayoutParams(params);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,18);
        textView.setText(getData().des);

        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(2000, View.MeasureSpec.AT_MOST);

        textView.measure(widthMeasureSpec, heightMeasureSpec);
        int height = textView.getMeasuredHeight();

        System.out.println("longHeight:" + height + "---getHeight:" + textView.getHeight());
        return height;
    }
}

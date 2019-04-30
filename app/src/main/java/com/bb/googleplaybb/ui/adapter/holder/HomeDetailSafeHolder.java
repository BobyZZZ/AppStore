package com.bb.googleplaybb.ui.adapter.holder;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bb.googleplaybb.R;
import com.bb.googleplaybb.domain.AppInfo;
import com.bb.googleplaybb.net.NetHelper;
import com.bb.googleplaybb.utils.BitmapHelper;
import com.bb.googleplaybb.utils.UIUtils;
import com.lidroid.xutils.BitmapUtils;

import java.util.ArrayList;

/**
 * Created by Boby on 2018/7/17.
 */

public class HomeDetailSafeHolder extends BaseHolder<AppInfo> {

    private ImageView ivArr;
    private ImageView[] ivIcons;
    private ImageView[] desIcons;
    private TextView[] tvDess;
    private LinearLayout[] llDess;
    private LinearLayout llDisplay;
    private int height;
    private LinearLayout llControl;
    private BitmapUtils mBitmapUtils;

    @Override
    public View initView() {
        ivIcons = new ImageView[4];
        desIcons = new ImageView[4];
        tvDess = new TextView[4];
        llDess = new LinearLayout[4];

        //安全信息
        View safeInfoView = UIUtils.inflate(R.layout.layout_home_detail_safeinfo);
        llControl = safeInfoView.findViewById(R.id.ll_control);
        ivArr = safeInfoView.findViewById(R.id.iv_arr);

        ivIcons[0] = safeInfoView.findViewById(R.id.iv_icon1);
        ivIcons[1] = safeInfoView.findViewById(R.id.iv_icon2);
        ivIcons[2] = safeInfoView.findViewById(R.id.iv_icon3);
        ivIcons[3] = safeInfoView.findViewById(R.id.iv_icon4);

        desIcons[0] = safeInfoView.findViewById(R.id.iv_des_icon1);
        desIcons[1] = safeInfoView.findViewById(R.id.iv_des_icon2);
        desIcons[2] = safeInfoView.findViewById(R.id.iv_des_icon3);
        desIcons[3] = safeInfoView.findViewById(R.id.iv_des_icon4);

        tvDess[0] = safeInfoView.findViewById(R.id.tv_des1);
        tvDess[1] = safeInfoView.findViewById(R.id.tv_des2);
        tvDess[2] = safeInfoView.findViewById(R.id.tv_des3);
        tvDess[3] = safeInfoView.findViewById(R.id.tv_des4);

        llDisplay = safeInfoView.findViewById(R.id.ll_display);
//        llDisplay.measure(0, 0);
//        height = llDisplay.getMeasuredHeight();

        llDess[0] = safeInfoView.findViewById(R.id.ll_root1);
        llDess[1] = safeInfoView.findViewById(R.id.ll_root2);
        llDess[2] = safeInfoView.findViewById(R.id.ll_root3);
        llDess[3] = safeInfoView.findViewById(R.id.ll_root4);

        mBitmapUtils = BitmapHelper.getBitmapUtils();

        return safeInfoView;
    }

    @Override
    public void refreshView(AppInfo data) {
        ArrayList<AppInfo.SafeInfo> safe = data.safe;
        for (int i = 0; i < 4; i++) {
            if (i < safe.size()) {
                mBitmapUtils.display(ivIcons[i], NetHelper.URL + safe.get(i).safeUrl);
                mBitmapUtils.display(desIcons[i], NetHelper.URL + safe.get(i).safeDesUrl);
                tvDess[i].setText(safe.get(i).safeDes);
            } else {
                ivIcons[i].setVisibility(View.GONE);
                llDess[i].setVisibility(View.GONE);
            }
        }
        llDisplay.measure(0, 0);
        height = llDisplay.getMeasuredHeight();//获取理论高度
//        height = llDisplay.getHeight();//获取实际展示时的高度

        //设置点击事件
        llControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle();
            }
        });
    }

    private boolean isOpen = true;

    private void toggle() {
        ValueAnimator valueAnimator = null;
        if (isOpen) {
            //打开改为关闭
            isOpen = false;
            valueAnimator = ValueAnimator.ofInt(height, 0);
        } else {
            isOpen = true;
            valueAnimator = ValueAnimator.ofInt(0, height);
        }
        valueAnimator.setDuration(200);

        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
//                System.out.println("animation:" + animation.getAnimatedValue());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) animation.getAnimatedValue());
                llDisplay.setLayoutParams(params);
            }
        });

        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (isOpen) {
                    //向上
                    ivArr.setImageResource(R.drawable.arrow_up);
                } else {
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
        valueAnimator.start();//开始动画
    }
}

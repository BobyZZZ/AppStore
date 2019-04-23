package com.bb.googleplaybb.ui.fragment;

import android.graphics.Color;
import android.graphics.drawable.StateListDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bb.googleplaybb.net.protocol.RankNetProtocol;
import com.bb.googleplaybb.ui.view.LoadingPage;
import com.bb.googleplaybb.ui.view.MyFlowLayout;
import com.bb.googleplaybb.utils.DrawableUtils;
import com.bb.googleplaybb.utils.UIUtils;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Boby on 2018/7/10.
 */

public class RankFragment extends BaseFragment {

    private RankNetProtocol mRankProtocol;
    private ArrayList<String> data;

    @Override
    public LoadingPage.ResultState onLoad() {
        mRankProtocol = new RankNetProtocol();
        data = mRankProtocol.getData(0);
        return check(data);
    }

    @Override
    public View onCreateSuccessView() {
        ScrollView scrollView = new ScrollView(UIUtils.getContext());
//        FlowLayout flowLayout = new FlowLayout(UIUtils.getContext());
        MyFlowLayout flowLayout = new MyFlowLayout(UIUtils.getContext());
//        flowLayout.setHorizontalSpacing(UIUtils.dip2px(5));
//        flowLayout.setVerticalSpacing(UIUtils.dip2px(8));

        int padding = UIUtils.dip2px(5);
        flowLayout.setPadding(padding, padding, padding, padding);

        for (int i = 0; i < data.size(); i++) {
            final String text = this.data.get(i);

            TextView textView = new TextView(UIUtils.getContext());
            textView.setGravity(Gravity.CENTER);
            textView.setText(text);
            textView.setTextColor(Color.WHITE);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 23);

            padding = UIUtils.dip2px(9);
            textView.setPadding(padding, padding, padding, padding);

            //设置背景选择器
            //产生随机颜色,30-230
            Random random = new Random();
            int r = random.nextInt(200) + 30;
            int g = random.nextInt(200) + 30;
            int b = random.nextInt(200) + 30;
            int normalColor = Color.rgb(r,g,b);
            int pressColor = 0xffcecece;

            //圆角的半径
            float radius = UIUtils.dip2px(8);
//            GradientDrawable normal = DrawableUtils.getGradientDrawable(Color.rgb(r, g, b), UIUtils.dip2px(8));
//            GradientDrawable press = DrawableUtils.getGradientDrawable(color, UIUtils.dip2px(8));
//            textView.setBackgroundDrawable(normal);
            //获取一个选择器
            StateListDrawable selector = DrawableUtils.getSelector(normalColor, pressColor, radius);
            textView.setBackgroundDrawable(selector);

            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(UIUtils.getContext(),text,Toast.LENGTH_SHORT).show();
                }
            });
            flowLayout.addView(textView);
        }

        scrollView.addView(flowLayout);
        return scrollView;
    }
}

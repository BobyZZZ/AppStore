package com.bb.googleplaybb.ui.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bb.googleplaybb.domain.RecommendInfo;
import com.bb.googleplaybb.net.protocol.RecommendNetProtocol;
import com.bb.googleplaybb.ui.activity.HomeDetailActivity;
import com.bb.googleplaybb.ui.view.LoadingPage;
import com.bb.googleplaybb.ui.view.fly.StellarMap;
import com.bb.googleplaybb.utils.UIUtils;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Boby on 2018/7/10.
 */

public class RecommendFragment extends BaseFragment {

    private RecommendNetProtocol mRecommendProtocol;
    private ArrayList<RecommendInfo> data;

    @Override
    public LoadingPage.ResultState onLoad() {
        mRecommendProtocol = new RecommendNetProtocol();
        data = mRecommendProtocol.getData(0);
        return check(data);
    }

    @Override
    public View onCreateSuccessView() {
        StellarMap stellarMap = new StellarMap(UIUtils.getContext());
        stellarMap.setRegularity(8, 6);//设置规则，9行6列
        int padding = UIUtils.dip2px(10);
        stellarMap.setInnerPadding(padding,padding,padding,padding);
        stellarMap.setAdapter(new RecommendAdapter());

        stellarMap.zoomIn();//不设置首次进入没有内容，需在setadapter之后调用；
        return stellarMap;
    }

    class RecommendAdapter implements StellarMap.Adapter {

        @Override
        public int getGroupCount() {
            return 3;
        }

        //第group组有多少数据
        @Override
        public int getCount(int group) {
            int count = data.size() / getGroupCount();
            if (group == getGroupCount() - 1) {
                //当前为最后一页
                count += data.size() % getGroupCount();
            }
            return count;
        }

        @Override
        public View getView(int group, int position, View convertView) {
            position += group * getCount(group - 1);
            System.out.println("position:" + position);
            TextView textView = new TextView(UIUtils.getContext());
            final RecommendInfo recommendInfo = data.get(position);
            final String text = recommendInfo.name;
            textView.setText(text);
//            int padding = UIUtils.dip2px(10);
//            textView.setPadding(padding, padding, padding, padding);
            Random random = new Random();

            //设置颜色,30-230
            int r = random.nextInt(200) + 30;
            int g = random.nextInt(200) + 30;
            int b = random.nextInt(200) + 30;
            textView.setTextColor(Color.rgb(r, g, b));

            //设置大小,20-28sp
            int size = random.nextInt(9) + 20;
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);

            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Toast.makeText(UIUtils.getContext(), text, Toast.LENGTH_SHORT).show();
                    //进入详情页
                    Intent intent = new Intent(UIUtils.getContext(), HomeDetailActivity.class);
                    intent.putExtra(HomeDetailActivity.PACKAGENAME,recommendInfo.packageName);
                    intent.putExtra(HomeDetailActivity.APPNAME,recommendInfo.name);
                    startActivity(intent);
                }
            });
            return textView;
        }

        @Override
        public int getNextGroupOnZoom(int group, boolean isZoomIn) {
            System.out.println("isZoomIn:" + isZoomIn);
            if (isZoomIn) {
                //向下滑，显示上一组
                group--;
                if (group < 0) {
                    group = getGroupCount() - 1;//显示最后一组
                }
            } else {
                //向上滑，显示下一组
                group++;
                if (group > getGroupCount() - 1) {
                    group = 0;//显示第一组
                }
            }
            return group;
        }
    }

}

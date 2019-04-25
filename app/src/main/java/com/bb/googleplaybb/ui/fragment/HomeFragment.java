package com.bb.googleplaybb.ui.fragment;

import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.bb.googleplaybb.R;
import com.bb.googleplaybb.domain.AppInfo;
import com.bb.googleplaybb.net.NetHelper;
import com.bb.googleplaybb.net.protocol.HomeNetProtocol;
import com.bb.googleplaybb.ui.activity.HomeDetailActivity;
import com.bb.googleplaybb.ui.adapter.MyBaseAdapter;
import com.bb.googleplaybb.ui.adapter.holder.BaseHolder;
import com.bb.googleplaybb.ui.adapter.holder.HomeHolder;
import com.bb.googleplaybb.ui.view.LoadingPage;
import com.bb.googleplaybb.ui.view.MyListView;
import com.bb.googleplaybb.utils.BitmapHelper;
import com.bb.googleplaybb.utils.UIUtils;
import com.lidroid.xutils.BitmapUtils;

import java.util.ArrayList;

/**
 * Created by Boby on 2018/7/10.
 */

public class HomeFragment extends BaseFragment {

    private ArrayList<AppInfo> data;
    private ArrayList<String> pictures;

    private int mPreviousSelected = 0;
    private ViewPager viewPager;
    private HomeNetProtocol mHomeProtocol;

    @Override
    public LoadingPage.ResultState onLoad() {
        //请求网络
        mHomeProtocol = new HomeNetProtocol();
        data = mHomeProtocol.getData(0);
        pictures = mHomeProtocol.getPictures();
        return check(data);
    }

    //onLoad()方法成功才走此方法
    @Override
    public View onCreateSuccessView() {
        MyListView listView = new MyListView(UIUtils.getContext());

        //轮播条最外层RelativeLayout
        RelativeLayout rlRoot = new RelativeLayout(UIUtils.getContext());
        AbsListView.LayoutParams params = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, UIUtils.dip2px(180));
        rlRoot.setLayoutParams(params);

        //创建一个轮播条
        viewPager = new ViewPager(UIUtils.getContext());
        RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        viewPager.setLayoutParams(params1);
        viewPager.setAdapter(new PagerAdapter());
        viewPager.setCurrentItem(pictures.size() * 100000);
        rlRoot.addView(viewPager);
        //viewPager设置触摸事件，点击停止自动轮播，松开回复轮播
        viewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        UIUtils.getHandler().removeCallbacksAndMessages(null);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        new AutoPlayTask().start();
                        break;
                }
                return false;
            }
        });

        //添加指示器
        final LinearLayout llContainer = new LinearLayout(UIUtils.getContext());
        RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        params2.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params2.rightMargin = UIUtils.dip2px(7);
        params2.bottomMargin = UIUtils.dip2px(7);
        llContainer.setLayoutParams(params2);

        mPreviousSelected = 0;
        //指示器内添加小圆点
        for (int i = 0; i < pictures.size(); i++) {
            ImageView point = new ImageView(UIUtils.getContext());
            LinearLayout.LayoutParams params3 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            if (i != 0) {
                //设置左边距
                params3.leftMargin = UIUtils.dip2px(3);
            } else {
                params3.leftMargin = 0;
            }

            if (i == mPreviousSelected) {
                point.setImageResource(R.drawable.indicator_selected);
            } else {
                point.setImageResource(R.drawable.indicator_normal);
            }
            point.setLayoutParams(params3);
            llContainer.addView(point);
        }
        rlRoot.addView(llContainer);

        //viewPager设置页面切换监听事件
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                position = position % pictures.size();
                ImageView point = (ImageView) llContainer.getChildAt(position);
                ImageView prePoint = (ImageView) llContainer.getChildAt(mPreviousSelected);

                prePoint.setImageResource(R.drawable.indicator_normal);
                point.setImageResource(R.drawable.indicator_selected);

                mPreviousSelected = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        //将轮播条所在布局添加为头布局
        listView.addHeaderView(rlRoot);
        listView.setAdapter(new HomeAdapter(data));

        AutoPlayTask autoPlayTask = new AutoPlayTask();
        autoPlayTask.start();//开始轮播

        //listview设置点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println("onItemClick:"+position);
                AppInfo appInfo = data.get(position - 1);//减去头布局
                //打开详情页面
                Intent intent = new Intent(UIUtils.getContext(), HomeDetailActivity.class);
                intent.putExtra(HomeDetailActivity.PACKAGENAME, appInfo.packageName);
                intent.putExtra(HomeDetailActivity.APPNAME, appInfo.name);
                startActivity(intent);
            }
        });
        return listView;
    }

    class PagerAdapter extends android.support.v4.view.PagerAdapter {

        private BitmapUtils bitmapUtils;

        public PagerAdapter() {
            bitmapUtils = BitmapHelper.getBitmapUtils();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            position = position % pictures.size();
            String url = pictures.get(position);
            ImageView imageView = new ImageView(UIUtils.getContext());
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            bitmapUtils.display(imageView, NetHelper.URL + url);

            container.addView(imageView);
            return imageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }

    class HomeAdapter extends MyBaseAdapter<AppInfo> {

        public HomeAdapter(ArrayList<AppInfo> data) {
            super(data);
        }

        @Override
        protected ArrayList<AppInfo> onLoadMore() {
            ArrayList<AppInfo> moreData = mHomeProtocol.getData(data.size());
            return moreData;
        }

        @Override
        protected BaseHolder getHolder(int position) {
            return new HomeHolder();
        }
    }

    class AutoPlayTask implements Runnable {

        public void start() {
            //清楚之前设置的所有事件，因为handler是在myapplication中创建的全局变量
            UIUtils.getHandler().removeCallbacksAndMessages(null);
            UIUtils.getHandler().postDelayed(this, 2000);
        }

        @Override
        public void run() {
            //切换到下一页
            int currentItem = viewPager.getCurrentItem();
            currentItem++;
            viewPager.setCurrentItem(currentItem);

            UIUtils.getHandler().postDelayed(this, 2000);
        }
    }
}

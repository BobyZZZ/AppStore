package com.bb.googleplaybb.ui.fragment;

import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;

import com.bb.googleplaybb.domain.AppInfo;
import com.bb.googleplaybb.net.protocol.AppNetProtocol;
import com.bb.googleplaybb.ui.activity.HomeDetailActivity;
import com.bb.googleplaybb.ui.adapter.MyBaseAdapter;
import com.bb.googleplaybb.ui.adapter.holder.AppHolder;
import com.bb.googleplaybb.ui.adapter.holder.BaseHolder;
import com.bb.googleplaybb.ui.adapter.holder.HomeHolder;
import com.bb.googleplaybb.ui.view.LoadingPage;
import com.bb.googleplaybb.ui.view.MyListView;
import com.bb.googleplaybb.utils.UIUtils;

import java.util.ArrayList;

/**
 * Created by Boby on 2018/7/10.
 */

public class AppFragment extends BaseFragment {

    private ArrayList<AppInfo> data;
    private AppNetProtocol mProtocol;
    private HomeAdapter mAdapter;

    @Override
    public LoadingPage.ResultState onLoad() {
        mProtocol = new AppNetProtocol();
        data = mProtocol.getData(0);
        return check(data);
    }

    @Override
    public View onCreateSuccessView() {
        MyListView view = new MyListView(UIUtils.getContext());
        mAdapter = new HomeAdapter(data);
        view.setAdapter(mAdapter);

        view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println("onItemClick:"+position);
                AppInfo appInfo = data.get(position);//减去头布局
                //打开详情页面
                Intent intent = new Intent(UIUtils.getContext(), HomeDetailActivity.class);
                intent.putExtra(HomeDetailActivity.PACKAGENAME, appInfo.packageName);
                intent.putExtra(HomeDetailActivity.APPNAME, appInfo.name);
                startActivity(intent);
            }
        });
        return view;
    }

    class HomeAdapter extends MyBaseAdapter<AppInfo> {

        public HomeAdapter(ArrayList<AppInfo> data) {
            super(data);
        }

        @Override
        protected ArrayList<AppInfo> onLoadMore() {
            ArrayList<AppInfo> moreData = mProtocol.getData(data.size());
            return moreData;
        }

        @Override
        protected BaseHolder getHolder(int position) {
            return new HomeHolder();
        }
    }

    public void refresh() {
        mAdapter.notifyDataSetChanged();
    }
}

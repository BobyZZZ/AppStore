package com.bb.googleplaybb.ui.fragment;

import android.view.View;

import com.bb.googleplaybb.domain.AppInfo;
import com.bb.googleplaybb.net.protocol.AppNetProtocol;
import com.bb.googleplaybb.ui.adapter.MyBaseAdapter;
import com.bb.googleplaybb.ui.adapter.holder.AppHolder;
import com.bb.googleplaybb.ui.adapter.holder.BaseHolder;
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

    @Override
    public LoadingPage.ResultState onLoad() {
        mProtocol = new AppNetProtocol();
        data = mProtocol.getData(0);
        return check(data);
    }

    @Override
    public View onCreateSuccessView() {
        MyListView view = new MyListView(UIUtils.getContext());
        view.setAdapter(new AppAdapter(data));
        return view;
    }

    class AppAdapter extends MyBaseAdapter<AppInfo> {

        public AppAdapter(ArrayList<AppInfo> data) {
            super(data);
        }

        //是否允许加载更多
        @Override
        public boolean hasMore() {
            return super.hasMore();
        }

        @Override
        protected ArrayList<AppInfo> onLoadMore() {
//            for (int i = 0; i < 22; i++) {
//                moreData.add("更多数据" + i);
//            }
            ArrayList<AppInfo> moreData = mProtocol.getData(data.size());
            return moreData;
        }

        @Override
        protected BaseHolder getHolder(int position) {
            return new AppHolder();
        }

    }
}

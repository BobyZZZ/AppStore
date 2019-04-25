package com.bb.googleplaybb.ui.fragment;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import com.bb.googleplaybb.domain.TopicInfo;
import com.bb.googleplaybb.net.protocol.TopicNetProtocol;
import com.bb.googleplaybb.ui.activity.AppOfTypeActivity;
import com.bb.googleplaybb.ui.adapter.MyBaseAdapter;
import com.bb.googleplaybb.ui.adapter.holder.BaseHolder;
import com.bb.googleplaybb.ui.adapter.holder.TopicHolder;
import com.bb.googleplaybb.ui.view.LoadingPage;
import com.bb.googleplaybb.ui.view.MyListView;
import com.bb.googleplaybb.utils.UIUtils;

import java.util.ArrayList;

/**
 * Created by Boby on 2018/7/10.
 */

public class TopicFragment extends BaseFragment {

    private TopicNetProtocol mTopicProtocol;
    private ArrayList<TopicInfo> data;

    @Override
    public LoadingPage.ResultState onLoad() {
        mTopicProtocol = new TopicNetProtocol();
        data = mTopicProtocol.getData(0);
        return check(data);
    }

    @Override
    public View onCreateSuccessView() {
        MyListView myListView = new MyListView(UIUtils.getContext());
        myListView.setAdapter(new TopicAdapter(data));

        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TopicInfo topicInfo = data.get(position);
                AppOfTypeActivity.startAppOfTypeActivity(UIUtils.getContext(),topicInfo.typeUrl);
            }
        });
        return myListView;
    }

    class TopicAdapter extends MyBaseAdapter<TopicInfo> {
        public TopicAdapter(ArrayList<TopicInfo> data) {
            super(data);
        }

        @Override
        protected ArrayList<TopicInfo> onLoadMore() {
            return mTopicProtocol.getData(data.size());
        }

        @Override
        protected BaseHolder getHolder(int position) {
            return new TopicHolder();
        }
    }
}

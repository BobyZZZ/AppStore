package com.bb.googleplaybb.ui.fragment;

import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bb.googleplaybb.R;
import com.bb.googleplaybb.domain.TopicInfo;
import com.bb.googleplaybb.global.GooglePlayApplication;
import com.bb.googleplaybb.net.NetHelper;
import com.bb.googleplaybb.net.protocol.TopicNetProtocol;
import com.bb.googleplaybb.ui.adapter.MyBaseAdapter;
import com.bb.googleplaybb.ui.adapter.holder.BaseHolder;
import com.bb.googleplaybb.ui.adapter.holder.TopicHolder;
import com.bb.googleplaybb.ui.view.LoadingPage;
import com.bb.googleplaybb.ui.view.MyListView;
import com.bb.googleplaybb.utils.UIUtils;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

/**
 * Created by Boby on 2018/7/10.
 */

public class TopicFragment2 extends BaseFragment {

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
//        MyListView myListView = new MyListView(UIUtils.getContext());
//        myListView.setAdapter(new TopicAdapter(data));
//        return myListView;
        RecyclerView recyclerView = new RecyclerView(UIUtils.getContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(UIUtils.getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(new MyBaseAdapter());
        return recyclerView;
    }

//    class TopicAdapter extends MyBaseAdapter<TopicInfo> {
//        public TopicAdapter(ArrayList<TopicInfo> data) {
//            super(data);
//        }
//
//        @Override
//        protected ArrayList<TopicInfo> onLoadMore() {
//            return mTopicProtocol.getData(data.size());
//        }
//
//        @Override
//        protected BaseHolder getHolder(int position) {
//            return new TopicHolder();
//        }
//    }

    class MyBaseAdapter extends RecyclerView.Adapter<MyBaseAdapter.VH>{

        class VH extends RecyclerView.ViewHolder {
            ImageView ivPic;
            TextView tvDes;
            public VH(View itemView) {
                super(itemView);
                ivPic = itemView.findViewById(R.id.iv_pic);
                tvDes = itemView.findViewById(R.id.tv_des);
            }
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.list_item_topic, parent, false);
            return new VH(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final VH holder, int position) {
            final TopicInfo topicInfo = data.get(position);
            holder.tvDes.setText(topicInfo.des);
            holder.tvDes.post(new Runnable() {
                @Override
                public void run() {
//                utils.display(ivPic, NetHelper.URL + data.url);
                    Glide.with(GooglePlayApplication.getContext()).load(NetHelper.URL + topicInfo.url).into(holder.ivPic);
                }
            });
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }
}

package com.bb.googleplaybb.ui.adapter.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bb.googleplaybb.R;
import com.bb.googleplaybb.domain.TopicInfo;
import com.bb.googleplaybb.global.GooglePlayApplication;
import com.bb.googleplaybb.net.NetHelper;
import com.bb.googleplaybb.utils.BitmapHelper;
import com.bb.googleplaybb.utils.UIUtils;
import com.bumptech.glide.Glide;
import com.lidroid.xutils.BitmapUtils;

/**
 * Created by Boby on 2018/7/14.
 */

public class TopicHolder extends BaseHolder<TopicInfo> {

    private ImageView ivPic;
    private TextView tvDes;

    private BitmapUtils utils;

    @Override
    public View initView() {
        View view = UIUtils.inflate(R.layout.list_item_topic);
        ivPic = view.findViewById(R.id.iv_pic);
        tvDes = view.findViewById(R.id.tv_des);

        utils = BitmapHelper.getBitmapUtils();
        return view;
    }

    @Override
    public void refreshView(final TopicInfo data) {
        tvDes.setText(data.des);
        tvDes.post(new Runnable() {
            @Override
            public void run() {
                Glide.with(GooglePlayApplication.getContext()).load(NetHelper.URL + data.url).into(ivPic);
            }
        });

    }
}

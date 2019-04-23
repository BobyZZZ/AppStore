package com.bb.googleplaybb.ui.adapter.holder;

import android.text.format.Formatter;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bb.googleplaybb.R;
import com.bb.googleplaybb.domain.AppInfo;
import com.bb.googleplaybb.http.HttpHelper;
import com.bb.googleplaybb.net.NetHelper;
import com.bb.googleplaybb.ui.view.ProgressArc;
import com.bb.googleplaybb.utils.BitmapHelper;
import com.bb.googleplaybb.utils.UIUtils;
import com.lidroid.xutils.BitmapUtils;

/**
 * Created by Boby on 2018/7/13.
 */

public class AppHolder extends BaseHolder<AppInfo> {
    private TextView tvName,tvSize,tvDes;
    private ImageView ivIcon;
    private RatingBar rbStart;

    private BitmapUtils utils;
    private FrameLayout mFlProgress;
    private ProgressArc mProgressArc;

    @Override
    public View initView() {
        View view = UIUtils.inflate(R.layout.list_item_home);
        tvName = view.findViewById(R.id.tv_name);
        tvSize = view.findViewById(R.id.tv_size);
        tvDes = view.findViewById(R.id.tv_des);
        ivIcon = view.findViewById(R.id.iv_icon);
        rbStart = view.findViewById(R.id.rb_start);
        mFlProgress = view.findViewById(R.id.fl_progress);

        mProgressArc = new ProgressArc(UIUtils.getContext());
        //设置进度条直径
        mProgressArc.setArcDiameter(UIUtils.dip2px(31));
        //设置进度条颜色
        mProgressArc.setProgressColor(R.color.progress);
        mProgressArc.setBackgroundResource(R.drawable.ic_download);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);

        //将进度条添加到布局
        mFlProgress.addView(mProgressArc, params);

        utils = BitmapHelper.getBitmapUtils();
        return view;
    }

    @Override
    public void refreshView(AppInfo data) {
        tvName.setText(data.name);
        tvSize.setText(Formatter.formatFileSize(UIUtils.getContext(),data.size));
        tvDes.setText(data.des);
        rbStart.setRating(data.stars);

        utils.display(ivIcon, NetHelper.URL + data.iconUrl);
    }
}

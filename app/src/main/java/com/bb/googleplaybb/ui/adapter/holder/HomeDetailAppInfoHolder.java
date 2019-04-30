package com.bb.googleplaybb.ui.adapter.holder;

import android.text.format.Formatter;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bb.googleplaybb.R;
import com.bb.googleplaybb.domain.AppInfo;
import com.bb.googleplaybb.net.NetHelper;
import com.bb.googleplaybb.utils.BitmapHelper;
import com.bb.googleplaybb.utils.UIUtils;
import com.lidroid.xutils.BitmapUtils;

/**
 * Created by Boby on 2018/7/17.
 */

public class HomeDetailAppInfoHolder extends BaseHolder<AppInfo> {

    private ImageView ivIcon;
    private TextView tvName;
    private TextView tvDownloadNum;
    private TextView tvSize;
    private TextView tvDate;
    private TextView tvVersion;
    private RatingBar rbStar;

    private BitmapUtils mBitmapUtils;

    @Override
    public View initView() {
        //app信息
        View appInfoView = UIUtils.inflate(R.layout.layout_home_detail_appinfo);
        ivIcon = appInfoView.findViewById(R.id.iv_icon);
        tvName = appInfoView.findViewById(R.id.tv_name);
        tvDownloadNum = appInfoView.findViewById(R.id.tv_download_num);
        tvSize = appInfoView.findViewById(R.id.tv_size);
        tvDate = appInfoView.findViewById(R.id.tv_date);
        tvVersion = appInfoView.findViewById(R.id.tv_version);
        rbStar = appInfoView.findViewById(R.id.rb_start);

        mBitmapUtils = BitmapHelper.getBitmapUtils();

        return appInfoView;
    }

    @Override
    public void refreshView(AppInfo data) {
        tvName.setText(data.name);
        tvDownloadNum.setText("下载量:" + data.downloadNum);
        tvDate.setText(data.date);
        tvSize.setText(Formatter.formatFileSize(UIUtils.getContext(), data.size));
        tvVersion.setText("版本：" + data.version);
        rbStar.setRating(data.stars);
        mBitmapUtils.display(ivIcon, NetHelper.URL + data.iconUrl);
    }
}

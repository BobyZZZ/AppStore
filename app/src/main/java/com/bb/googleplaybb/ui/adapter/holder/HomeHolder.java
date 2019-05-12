package com.bb.googleplaybb.ui.adapter.holder;

import android.text.format.Formatter;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bb.googleplaybb.R;
import com.bb.googleplaybb.domain.AppInfo;
import com.bb.googleplaybb.domain.DownloadInfo;
import com.bb.googleplaybb.manager.AppDownloadManager;
import com.bb.googleplaybb.manager.DBUtils;
import com.bb.googleplaybb.net.NetHelper;
import com.bb.googleplaybb.ui.view.ProgressArc;
import com.bb.googleplaybb.utils.BitmapHelper;
import com.bb.googleplaybb.utils.UIUtils;
import com.lidroid.xutils.BitmapUtils;

public class HomeHolder extends BaseHolder<AppInfo> implements AppDownloadManager.DownloadObserver, View.OnClickListener {
    private TextView tvName, tvSize, tvDes;
    private ImageView ivIcon;
    private RatingBar rbStart;

    private BitmapUtils utils;
    private FrameLayout flProgress;
    private ProgressArc mProgressArc;

    private int mCurrentState;
    private float mProgress;
    private TextView tvDownload;
    private AppDownloadManager mDm;

    @Override
    public View initView() {
        View view = UIUtils.inflate(R.layout.list_item_home);
        tvName = view.findViewById(R.id.tv_name);
        tvSize = view.findViewById(R.id.tv_size);
        tvDes = view.findViewById(R.id.tv_des);
        ivIcon = view.findViewById(R.id.iv_icon);
        rbStart = view.findViewById(R.id.rb_start);
        tvDownload = view.findViewById(R.id.tv_download);
        flProgress = view.findViewById(R.id.fl_progress);

        utils = BitmapHelper.getBitmapUtils();

        flProgress.setOnClickListener(this);

        mProgressArc = new ProgressArc(UIUtils.getContext());
        //设置进度条直径
        mProgressArc.setArcDiameter(UIUtils.dip2px(31));
        //设置进度条颜色
        mProgressArc.setProgressColor(R.color.progress);
        mProgressArc.setBackgroundResource(R.drawable.ic_download);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);

        //将进度条添加到布局
        flProgress.addView(mProgressArc, params);

        mDm = AppDownloadManager.getDownloadManager();
        mDm.registeredObserver(this);

        return view;
    }

    @Override
    public void refreshView(AppInfo data) {
        tvName.setText(data.name);
        tvSize.setText(Formatter.formatFileSize(UIUtils.getContext(), data.size));
        tvDes.setText(data.des);
        rbStart.setRating(data.stars);

        utils.display(ivIcon, NetHelper.URL + data.iconUrl);

        DownloadInfo downloadInfo = mDm.getDownloadInfo(data);
        if (downloadInfo == null) {
            downloadInfo = DownloadInfo.copy(data);
            long downloadedSize = AppDownloadManager.getDownloadedSize(downloadInfo);
            if (downloadedSize == 0) {
                mCurrentState = AppDownloadManager.STATE_UNDO;
                mProgress = 0;
            } else if (downloadedSize == data.size) {
                mCurrentState = AppDownloadManager.STATE_SUCCESS;
                mProgress = 0;
            }  else if (downloadedSize > data.size) {
                mCurrentState = AppDownloadManager.STATE_ERROR;
                mProgress = 0;
                DBUtils.getInstance().deleteThreadInfo(data.id);
            } else {
                mCurrentState = AppDownloadManager.STATE_PAUSE;
                mProgress = downloadedSize / (float) data.size;
            }
        } else {
            mCurrentState = downloadInfo.mCurrentState;
            mProgress = downloadInfo.getProgress();
        }

        refreshUI(mCurrentState, mProgress, data.id);//第一次手动调用
    }

    private void refreshUI(int state, float progress, String id) {
        if (!getData().id.equals(id)) {
            return;
        }
        mCurrentState = state;
        mProgress = progress;
        switch (state) {
            case AppDownloadManager.STATE_UNDO:
                mProgressArc.setBackgroundResource(R.drawable.ic_download);
                mProgressArc.setStyle(ProgressArc.PROGRESS_STYLE_NO_PROGRESS);
                tvDownload.setText("下载");
                break;
            case AppDownloadManager.STATE_PAUSE:
                mProgressArc.setBackgroundResource(R.drawable.ic_resume);
                mProgressArc.setStyle(ProgressArc.PROGRESS_STYLE_NO_PROGRESS);
                mProgressArc.setProgress(progress, false);
                tvDownload.setText("继续");
                break;
            case AppDownloadManager.STATE_WAITING:
                mProgressArc.setBackgroundResource(R.drawable.ic_download);
                mProgressArc.setStyle(ProgressArc.PROGRESS_STYLE_WAITING);
                tvDownload.setText("等待");
                break;
            case AppDownloadManager.STATE_DOWNLOADING:
                mProgressArc.setBackgroundResource(R.drawable.ic_pause);
                mProgressArc.setStyle(ProgressArc.PROGRESS_STYLE_DOWNLOADING);
                mProgressArc.setProgress(progress, false);
                tvDownload.setText((int) (progress * 100) + "%");
                break;
            case AppDownloadManager.STATE_SUCCESS:
                mProgressArc.setBackgroundResource(R.drawable.ic_install);
                mProgressArc.setStyle(ProgressArc.PROGRESS_STYLE_NO_PROGRESS);
                tvDownload.setText("安装");
                break;
            case AppDownloadManager.STATE_ERROR:
                mProgressArc.setBackgroundResource(R.drawable.ic_redownload);
                mProgressArc.setStyle(ProgressArc.PROGRESS_STYLE_NO_PROGRESS);
                tvDownload.setText("失败");
                break;
        }
    }

    public void refreshUIonUIThread(final DownloadInfo downloadInfo) {
        if (getData().id.equals(downloadInfo.id)) {
            UIUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int state = downloadInfo.mCurrentState;
                    float progress = downloadInfo.getProgress();
                    refreshUI(state, progress, downloadInfo.id);
                }
            });
        }
    }

    //在主线程和子线程中都有调用
    @Override
    public void notifyDownloadStateChange(DownloadInfo downloadInfo) {
        refreshUIonUIThread(downloadInfo);
    }

    //在子线程中调用
    @Override
    public void notifyDownloadProgressChange(DownloadInfo downloadInfo) {
        if (downloadInfo.getProgress() != mProgress) {
            refreshUIonUIThread(downloadInfo);
        }
    }

    @Override
    public void onClick(View v) {
        AppInfo data = getData();
        switch (v.getId()) {
            case R.id.fl_progress:
                if (mCurrentState == AppDownloadManager.STATE_UNDO || mCurrentState == AppDownloadManager.STATE_PAUSE || mCurrentState == AppDownloadManager.STATE_ERROR) {
                    mDm.download(data);
                } else if (mCurrentState == AppDownloadManager.STATE_DOWNLOADING || mCurrentState == AppDownloadManager.STATE_WAITING) {
                    mDm.pause(data);
                } else if (mCurrentState == AppDownloadManager.STATE_SUCCESS) {
                    mDm.install(data);
                }
                break;
        }
    }
}

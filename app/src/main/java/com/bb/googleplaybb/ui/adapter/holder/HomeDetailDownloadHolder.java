package com.bb.googleplaybb.ui.adapter.holder;

import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.bb.googleplaybb.R;
import com.bb.googleplaybb.domain.AppInfo;
import com.bb.googleplaybb.domain.DownloadInfo;
import com.bb.googleplaybb.manager.AppDownloadManager;
import com.bb.googleplaybb.ui.view.ProgressHorizontal;
import com.bb.googleplaybb.utils.UIUtils;

import java.io.File;

import cn.sharesdk.onekeyshare.OnekeyShare;

/**
 * Created by Boby on 2018/7/18.
 */

public class HomeDetailDownloadHolder extends BaseHolder<AppInfo> implements AppDownloadManager.DownloadObserver, View.OnClickListener {

    private FrameLayout flProgress;
    private AppDownloadManager mDm;
    private ProgressHorizontal mProgressHorizontal;
    private int mCurrentState;
    private float mProgress;
    private Button btnDownload;
    private Button mBtnShare;

    @Override
    public View initView() {
        View view = UIUtils.inflate(R.layout.layout_home_detail_download);
        btnDownload = view.findViewById(R.id.btn_download);
        mBtnShare = view.findViewById(R.id.btn_share);
        btnDownload.setOnClickListener(this);
        mBtnShare.setOnClickListener(this);

        flProgress = view.findViewById(R.id.fl_progress);
        flProgress.setOnClickListener(this);

        mProgressHorizontal = new ProgressHorizontal(UIUtils.getContext());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        mProgressHorizontal.setLayoutParams(params);
        mProgressHorizontal.setProgressBackgroundResource(R.drawable.progress_bg);
        mProgressHorizontal.setProgressResource(R.drawable.progress_normal);
        mProgressHorizontal.setProgressTextColor(Color.WHITE);// 进度文字颜色
        mProgressHorizontal.setProgressTextSize(UIUtils.dip2px(20));// 进度文字大小

        //给帧布局填充进度条
        flProgress.addView(mProgressHorizontal);

        mDm = AppDownloadManager.getDownloadManager();
        mDm.registeredObserver(this);
        return view;
    }

    @Override
    public void refreshView(AppInfo data) {
        DownloadInfo downloadInfo = mDm.getDownloadInfo(data);
        if (downloadInfo == null) {
            downloadInfo = DownloadInfo.copy(data);
            File file = new File(downloadInfo.getFilePath());
            if (!file.exists()) {
                mCurrentState = AppDownloadManager.STATE_UNDO;
                mProgress = 0;
            } else if (file.length() == data.size) {
                mCurrentState = AppDownloadManager.STATE_SUCCESS;
                mProgress = 1;
            } else {
                mCurrentState = AppDownloadManager.STATE_PAUSE;
                mProgress = file.length() / (float) data.size;
            }
        } else {
            mCurrentState = downloadInfo.mCurrentState;
            mProgress = downloadInfo.getProgress();
        }

        refreshUI(mCurrentState, mProgress);
    }

    private void refreshUI(int state, float progress) {
        System.out.println("refreshUI");
        System.out.println("state:" + state + ";progress:" + progress);

        mCurrentState = state;
        mProgress = progress;
        switch (state) {
            case AppDownloadManager.STATE_UNDO:
                btnDownload.setVisibility(View.VISIBLE);
                flProgress.setVisibility(View.GONE);
                btnDownload.setText("下载");
                break;
            case AppDownloadManager.STATE_PAUSE:
                btnDownload.setVisibility(View.GONE);
                flProgress.setVisibility(View.VISIBLE);
                mProgressHorizontal.setProgress(progress);
                mProgressHorizontal.setCenterText("暂停");
                break;
            case AppDownloadManager.STATE_WAITING:
                btnDownload.setVisibility(View.VISIBLE);
                flProgress.setVisibility(View.GONE);
                btnDownload.setText("等待中");
                mProgressHorizontal.setCenterText(null);
                break;
            case AppDownloadManager.STATE_DOWNLOADING:
                btnDownload.setVisibility(View.GONE);
                flProgress.setVisibility(View.VISIBLE);
                mProgressHorizontal.setProgress(progress);
                break;
            case AppDownloadManager.STATE_SUCCESS:
                btnDownload.setVisibility(View.VISIBLE);
                flProgress.setVisibility(View.GONE);
                btnDownload.setText("安装");
                break;
            case AppDownloadManager.STATE_ERROR:
                btnDownload.setVisibility(View.VISIBLE);
                flProgress.setVisibility(View.GONE);
                btnDownload.setText("下载失败");
                break;
        }
    }

    public void refreshUIonUIThread(final DownloadInfo downloadInfo) {
        System.out.println("refreshUIonUIThread");
        if (getData().id.equals(downloadInfo.id)) {
            UIUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int state = downloadInfo.mCurrentState;
                    float progress = downloadInfo.getProgress();
                    System.out.println("progress:" + progress);
                    refreshUI(state, progress);
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
//        downloadInfo = mDm.getDownloadInfo(getData());
//        if (downloadInfo == null || downloadInfo.mCurrentState == DownloadManager.STATE_PAUSE || downloadInfo.mCurrentState == DownloadManager.STATE_ERROR) {
//            mDm.start(getData());
//        } else if (downloadInfo.mCurrentState == DownloadManager.STATE_DOWNLOADING || downloadInfo.mCurrentState == DownloadManager.STATE_WAITING) {
//            mDm.pause(getData());
//        } else {
//            mDm.install(getData());
//        }
        AppInfo data = getData();
        switch (v.getId()) {
            case R.id.btn_download:
            case R.id.fl_progress:
                if (mCurrentState == AppDownloadManager.STATE_UNDO || mCurrentState == AppDownloadManager.STATE_PAUSE || mCurrentState == AppDownloadManager.STATE_ERROR) {
                    mDm.download(data);
                } else if (mCurrentState == AppDownloadManager.STATE_DOWNLOADING || mCurrentState == AppDownloadManager.STATE_WAITING) {
                    mDm.pause(data);
                } else if (mCurrentState == AppDownloadManager.STATE_SUCCESS) {
                    mDm.install(data);
                }
                break;
            case R.id.btn_share:
                System.out.println("btn_share clicked");
                showShare(data);
                break;
        }
    }

    private void showShare(AppInfo appInfo) {
        OnekeyShare oks = new OnekeyShare();
        //关闭sso授权
        oks.disableSSOWhenAuthorize();

        // title标题，微信、QQ和QQ空间等平台使用
        oks.setTitle("这是一个你没玩过的全新版本");
        // titleUrl QQ和QQ空间跳转链接
        oks.setTitleUrl("http://sharesdk.cn");
        // text是分享文本，所有平台都需要这个字段
        oks.setText(appInfo.name + "，快来下载这个应用吧");
        // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
        oks.setImagePath("/sdcard/2.jpg");//确保SDcard下面存在此张图片
        // url在微信、微博，Facebook等平台中使用
        oks.setUrl(appInfo.downloadUrl);
        // comment是我对这条分享的评论，仅在人人网使用
        oks.setComment("我是测试评论文本");
        // 启动分享GUI
        oks.show(UIUtils.getContext());
    }
}

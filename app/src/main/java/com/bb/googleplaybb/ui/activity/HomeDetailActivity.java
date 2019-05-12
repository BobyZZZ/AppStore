package com.bb.googleplaybb.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.Formatter;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bb.googleplaybb.R;
import com.bb.googleplaybb.domain.AppInfo;
import com.bb.googleplaybb.domain.DownloadInfo;
import com.bb.googleplaybb.manager.AppDownloadManager;
import com.bb.googleplaybb.manager.DBUtils;
import com.bb.googleplaybb.net.NetHelper;
import com.bb.googleplaybb.net.protocol.HomeDetailNetProtocol;
import com.bb.googleplaybb.ui.adapter.holder.HomeDetailDesHolder;
import com.bb.googleplaybb.ui.adapter.holder.HomeDetailPicHolder;
import com.bb.googleplaybb.ui.adapter.holder.HomeDetailSafeHolder;
import com.bb.googleplaybb.ui.view.DownloadButton;
import com.bb.googleplaybb.ui.view.LoadingPage;
import com.bb.googleplaybb.utils.BitmapHelper;
import com.bb.googleplaybb.utils.UIUtils;
import com.lidroid.xutils.BitmapUtils;

import java.util.Random;

import cn.sharesdk.onekeyshare.OnekeyShare;

/**
 * Created by Boby on 2018/7/16.
 */

public class HomeDetailActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String PACKAGENAME = "packageName";
    public static final String APPNAME = "appName";

    private String packageName;
    private AppInfo appinfo;
    private Toolbar mToolbar;
    private String mAppName;
    private ImageView ivIcon;
    private TextView tvName;
    private TextView tvDownloadNum;
    private TextView tvSize;
    private TextView tvDate;
    private TextView tvVersion;
    private RatingBar rbStar;
    private AppBarLayout mAppBar;
    private DownloadButton mVDownload;
    private AppDownloadManager mDownloadManager;

    public static void startHomeDetailActivity(Context context, String packageName, String appName) {
        Intent intent = new Intent(context, HomeDetailActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(PACKAGENAME, packageName);
        intent.putExtra(APPNAME, appName);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_detail_md);
        NestedScrollView flContainer = findViewById(R.id.fl_container);
        packageName = getIntent().getStringExtra(PACKAGENAME);
        mAppName = getIntent().getStringExtra(APPNAME);

        LoadingPage mLoadingPage = new LoadingPage(UIUtils.getContext()) {
            @Override
            protected ResultState onLoad() {
                return HomeDetailActivity.this.onLoad();
            }

            @Override
            public View onCreateSuccessView() {
                return HomeDetailActivity.this.onCreateSuccessView();
            }
        };
        mLoadingPage.loadData();
        flContainer.addView(mLoadingPage);
        initToolBar();
    }

//    private void initAppBar() {
//        AppBarLayout appBar = findViewById(R.id.appBar);
//        appBar.addOnOffsetChangedListener(new OnOffsetListener() {
//            @Override
//            public void onChange(AppBarLayout appBarLayout, int state) {
//                switch (state) {
//                    case IDEL:
//                        break;
//                    case EXPAND:
////                        Toast.makeText(getApplicationContext(), "EXPAND", Toast.LENGTH_SHORT).show();
//                        break;
//                    case COLLASP:
////                        Toast.makeText(getApplicationContext(), "COLLASP", Toast.LENGTH_SHORT).show();
//                        break;
//                }
//            }
//        });
//    }

    private void initToolBar() {
        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setTitleTextColor(Color.BLACK);
        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);

        //设置返回键
//        mToolbar.setNavigationIcon(R.drawable.ic_feedback);
        ActionBar supportActionBar = getSupportActionBar();
        supportActionBar.setDisplayHomeAsUpEnabled(true);
        supportActionBar.setHomeButtonEnabled(true);

        //设置随机颜色
        mAppBar = findViewById(R.id.appBar);
        Random random = new Random();
        int r = random.nextInt(150) + 30;
        int g = random.nextInt(150) + 30;
        int b = random.nextInt(150) + 30;
        int color = Color.rgb(r, g, b);
        mAppBar.setBackgroundColor(color);

        //设置遮罩颜色
        CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsingToolbarLayout);
        collapsingToolbarLayout.setContentScrimColor(color);
//        FloatingActionButton floatingActionButton = findViewById(R.id.fab);
//        floatingActionButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public LoadingPage.ResultState onLoad() {
        //请求网络,返回结果不为null，--> setRightPage()-->onCreateSuccessView()
        HomeDetailNetProtocol homeDetailProtocol = new HomeDetailNetProtocol(packageName);
        //无分页时参数传-1
        appinfo = homeDetailProtocol.getData(-1);
        if (appinfo != null) {
            return LoadingPage.ResultState.RESULT_SUCCESS;
        }
        return LoadingPage.ResultState.RESULT_ERROR;
    }

    public View onCreateSuccessView() {
        View rootView = UIUtils.inflate(R.layout.activity_home_detail_succeed);//加载布局

        //初始化app信息模块
        initAppInfo();
        initDownloadAndShare(rootView);
        mAppBar.setVisibility(View.VISIBLE);

        //初始化安全模块
        FrameLayout flSafeView = rootView.findViewById(R.id.fl_safe);
        HomeDetailSafeHolder detailSafeHolder = new HomeDetailSafeHolder();
        detailSafeHolder.setData(appinfo);
        flSafeView.addView(detailSafeHolder.getmRootView());

        //初始化截图模块
        HorizontalScrollView picView = rootView.findViewById(R.id.hsv_pic);
        HomeDetailPicHolder detailPicHolder = new HomeDetailPicHolder();
        detailPicHolder.setData(appinfo);
        picView.addView(detailPicHolder.getmRootView());

        //初始化应用介绍模块
        FrameLayout flDesView = rootView.findViewById(R.id.fl_des);
        HomeDetailDesHolder detailDesHolder = new HomeDetailDesHolder(mAppBar);
        detailDesHolder.setData(appinfo);
        flDesView.addView(detailDesHolder.getmRootView());

        //初始化下载模块
//        FrameLayout flDownload = rootView.findViewById(R.id.fl_download);
//        HomeDetailDownloadHolder detailDownloadHolder = new HomeDetailDownloadHolder();
//        detailDownloadHolder.setData(appinfo);
//        flDownload.addView(detailDownloadHolder.getmRootView());

        return rootView;
    }

    private void initAppInfo() {
        ivIcon = findViewById(R.id.iv_icon);
        tvName = findViewById(R.id.tv_name);
        tvDownloadNum = findViewById(R.id.tv_download_num);
        tvSize = findViewById(R.id.tv_size);
        tvDate = findViewById(R.id.tv_date);
        tvVersion = findViewById(R.id.tv_version);
        rbStar = findViewById(R.id.rb_start);

        BitmapUtils bitmapUtils = BitmapHelper.getBitmapUtils();
        bitmapUtils.display(ivIcon, NetHelper.URL + appinfo.iconUrl);
        tvName.setText(appinfo.name);
        tvDownloadNum.setText("下载量:" + appinfo.downloadNum);
        tvDate.setText(appinfo.date);
        tvSize.setText(Formatter.formatFileSize(UIUtils.getContext(), appinfo.size));
        tvVersion.setText("版本：" + appinfo.version);
        rbStar.setRating(appinfo.stars);
    }

    private void initDownloadAndShare(View rootView) {
        mDownloadManager = AppDownloadManager.getDownloadManager();
        mVDownload = rootView.findViewById(R.id.vDownload);
        TextView vShare = rootView.findViewById(R.id.vShare);
        vShare.setOnClickListener(this);

        DownloadInfo downloadInfo = mDownloadManager.getDownloadInfo(appinfo);
        final int mCurrentState;
        final float mProgress;
        if (downloadInfo == null) {
            downloadInfo = DownloadInfo.copy(appinfo);
            long downloadedSize = AppDownloadManager.getDownloadedSize(downloadInfo);
            if (downloadedSize == 0) {
                mCurrentState = AppDownloadManager.STATE_UNDO;
                mProgress = 0;
            } else if (downloadedSize == appinfo.size) {
                mCurrentState = AppDownloadManager.STATE_SUCCESS;
                mProgress = 0;
            } else if (downloadedSize > appinfo.size) {
                mCurrentState = AppDownloadManager.STATE_ERROR;
                mProgress = 0;
                DBUtils.getInstance().deleteThreadInfo(appinfo.id);
            } else {
                mCurrentState = AppDownloadManager.STATE_PAUSE;
                mProgress = downloadedSize / (float) appinfo.size;
            }
        } else {
            mCurrentState = downloadInfo.mCurrentState;
            mProgress = downloadInfo.getProgress();
        }
        mVDownload.post(new Runnable() {
            @Override
            public void run() {
                mVDownload.setProgress(mProgress);
                mVDownload.setState(mCurrentState);
            }
        });

        mVDownload.setOnClickListener(this);

        mDownloadManager.registeredObserver(new AppDownloadManager.DownloadObserver() {
            @Override
            public void notifyDownloadStateChange(final DownloadInfo downloadInfo) {
                if (appinfo.id.equals(downloadInfo.id)) {
                    UIUtils.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mVDownload.setState(downloadInfo.mCurrentState);
                        }
                    });
                }
            }

            @Override
            public void notifyDownloadProgressChange(final DownloadInfo downloadInfo) {
                if (appinfo.id.equals(downloadInfo.id)) {
                    UIUtils.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mVDownload.setProgress(downloadInfo.getProgress());
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.vDownload:
                int state = mVDownload.getState();
                if (state == AppDownloadManager.STATE_UNDO || state == AppDownloadManager.STATE_PAUSE || state == AppDownloadManager.STATE_ERROR) {
                    mDownloadManager.download(appinfo);
                } else if (state == AppDownloadManager.STATE_DOWNLOADING || state == AppDownloadManager.STATE_WAITING) {
                    mDownloadManager.pause(appinfo);
                } else if (state == AppDownloadManager.STATE_SUCCESS) {
                    mDownloadManager.install(appinfo);
                }
                break;
            case R.id.vShare:
                showShare(appinfo);
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
        oks.setTitleUrl(appInfo.downloadUrl);
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

    public abstract class OnOffsetListener implements AppBarLayout.OnOffsetChangedListener {
        public final int IDEL = 0;
        public final int COLLASP = 1;
        public final int EXPAND = 2;

        public int currentState = IDEL;

        @Override
        public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
            if (verticalOffset == 0) {
                if (currentState != EXPAND) {
                    onChange(appBarLayout, EXPAND);
                }
                currentState = EXPAND;
            } else if (Math.abs(verticalOffset) >= appBarLayout.getTotalScrollRange()) {
                if (currentState != COLLASP) {
                    onChange(appBarLayout, COLLASP);
                }
                currentState = COLLASP;
            } else {
                if (currentState != IDEL) {
                    onChange(appBarLayout, IDEL);
                }
                currentState = IDEL;
            }
        }

        public abstract void onChange(AppBarLayout appBarLayout, int state);
    }
}

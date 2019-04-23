package com.bb.googleplaybb.ui.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;

import com.bb.googleplaybb.R;
import com.bb.googleplaybb.domain.AppInfo;
import com.bb.googleplaybb.net.protocol.HomeDetailNetProtocol;
import com.bb.googleplaybb.ui.adapter.holder.HomeDetailAppInfoHolder;
import com.bb.googleplaybb.ui.adapter.holder.HomeDetailDesHolder;
import com.bb.googleplaybb.ui.adapter.holder.HomeDetailDownloadHolder;
import com.bb.googleplaybb.ui.adapter.holder.HomeDetailPicHolder;
import com.bb.googleplaybb.ui.adapter.holder.HomeDetailSafeHolder;
import com.bb.googleplaybb.ui.view.LoadingPage;
import com.bb.googleplaybb.utils.UIUtils;

/**
 * Created by Boby on 2018/7/16.
 */

public class HomeDetailActivity extends AppCompatActivity {

    private String packageName;
    private AppInfo appinfo;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_detail);
        FrameLayout flContainer = findViewById(R.id.fl_container);
        packageName = getIntent().getStringExtra("packageName");

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
//        setContentView(mLoadingPage);
//        initActionBar();
    }

//        private void initActionBar() {
//        ActionBar actionBar = getSupportActionBar();
//        actionBar.setDisplayHomeAsUpEnabled(true);
//    }

    private void initToolBar() {
        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setTitleTextColor(Color.BLACK);
        mToolbar.setTitle(packageName);
        setSupportActionBar(mToolbar);

        //设置返回键
//        mToolbar.setNavigationIcon(R.drawable.ic_feedback);
        ActionBar supportActionBar = getSupportActionBar();
        supportActionBar.setDisplayHomeAsUpEnabled(true);
        supportActionBar.setHomeButtonEnabled(true);
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
        FrameLayout flAppView = rootView.findViewById(R.id.fl_app_info);
        HomeDetailAppInfoHolder detailAppInfoHolder = new HomeDetailAppInfoHolder();
        detailAppInfoHolder.setData(appinfo);
        flAppView.addView(detailAppInfoHolder.getmRootView());

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
        HomeDetailDesHolder detailDesHolder = new HomeDetailDesHolder();
        detailDesHolder.setData(appinfo);
        flDesView.addView(detailDesHolder.getmRootView());

        //初始化下载模块
        FrameLayout flDownload = rootView.findViewById(R.id.fl_download);
        HomeDetailDownloadHolder detailDownloadHolder = new HomeDetailDownloadHolder();
        detailDownloadHolder.setData(appinfo);
        flDownload.addView(detailDownloadHolder.getmRootView());

        return rootView;
    }
}

package com.bb.googleplaybb.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bb.googleplaybb.ui.view.LoadingPage;
import com.bb.googleplaybb.utils.UIUtils;

import java.util.ArrayList;

public abstract class BaseFragment extends Fragment {

    private LoadingPage mLoadingPage;
    protected boolean inited = false;

    public boolean isInited() {
        return inited;
    }

    public void setInited(boolean inited) {
        this.inited = inited;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        mLoadingPage = new LoadingPage(UIUtils.getContext()) {
//
//            @Override
//            public View onCreateSuccessView() {
//                // 注意:此处一定要调用BaseFragment的onCreateSuccessView, 否则栈溢出
//                return BaseFragment.this.onCreateSuccessView();
//            }
//
//            @Override
//            public ResultState onLoad() {
//                return BaseFragment.this.onLoad();
//            }
//
//        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mLoadingPage = new LoadingPage(UIUtils.getContext()) {

            @Override
            public View onCreateSuccessView() {
                // 注意:此处一定要调用BaseFragment的onCreateSuccessView, 否则栈溢出
                return BaseFragment.this.onCreateSuccessView();
            }

            @Override
            public ResultState onLoad() {
                return BaseFragment.this.onLoad();
            }

        };
        return mLoadingPage;
    }

    // 加载成功的布局, 必须由子类来实现
    public abstract View onCreateSuccessView();

    // 加载网络数据, 必须由子类来实现
    public abstract LoadingPage.ResultState onLoad();

    // 开始加载数据
    public void loadData() {
        if (mLoadingPage != null) {
            mLoadingPage.loadData();
            setInited(true);
        }
    }

    public LoadingPage.ResultState check(ArrayList data) {
        if (data != null) {
            if (data.size() > 0) {
                return LoadingPage.ResultState.RESULT_SUCCESS;
            } else {
                return LoadingPage.ResultState.RESULT_EMPTY;
            }
        }
        return LoadingPage.ResultState.RESULT_ERROR;
    }
}
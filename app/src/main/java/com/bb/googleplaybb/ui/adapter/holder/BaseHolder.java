package com.bb.googleplaybb.ui.adapter.holder;

import android.view.View;

/**
 * Created by Boby on 2018/7/13.
 */

public abstract class BaseHolder<T> {
    private View mRootView;
    private T data;

    public BaseHolder() {
        mRootView = initView();
        mRootView.setTag(this);
    }

    public void setData(T data) {
        this.data = data;
        refreshView(data);
    }

    public T getData() {
        return data;
    }

    //加载布局，初始化控件
    public abstract View initView();

    /**
     * 刷新界面
     * @param data
     */
    public abstract void refreshView(T data);

    public View getmRootView() {
        return mRootView;
    }
}

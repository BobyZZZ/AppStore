package com.bb.googleplaybb.ui.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.bb.googleplaybb.R;
import com.bb.googleplaybb.manager.ThreadManager;
import com.bb.googleplaybb.utils.UIUtils;



public abstract class LoadingPage extends FrameLayout {
    private static final int STATE_UNDO = 1;
    private static final int STATE_LOADING = 2;
    private static final int STATE_ERROR = 3;
    private static final int STATE_EMPTY = 4;
    private static final int STATE_SUCCESS = 5;

    private int mCurrentState = STATE_UNDO;

    private View mLoadingView;
    private View mErrorView;
    private View mEmptyView;
    private View mSuccessView;

    public LoadingPage(@NonNull Context context) {
        super(context);
        initView();
    }

    public LoadingPage(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public LoadingPage(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    //加载中、加载成功、加载失败、内容为空、undo
    public void initView() {
        //加载中
        if (mLoadingView == null) {
            mLoadingView = UIUtils.inflate(R.layout.page_loading);
            addView(mLoadingView);
        }

        //加载失败
        if (mErrorView == null) {
            mErrorView = UIUtils.inflate(R.layout.page_error);
            mErrorView.findViewById(R.id.btn_retry).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadData();
                }
            });
            addView(mErrorView);
        }

        //内容为空页
        if (mEmptyView == null) {
            mEmptyView = UIUtils.inflate(R.layout.page_empty);
            addView(mEmptyView);
        }

        setRightPage();
    }

    private void setRightPage() {
        mLoadingView.setVisibility((mCurrentState == STATE_UNDO ||
                mCurrentState == STATE_LOADING) ? View.VISIBLE : View.GONE);

        mErrorView.setVisibility((mCurrentState == STATE_ERROR) ? View.VISIBLE : View.GONE);
        mEmptyView.setVisibility((mCurrentState == STATE_EMPTY) ? View.VISIBLE : View.GONE);


        if (mCurrentState == STATE_SUCCESS) {
            if (mSuccessView != null) {
                removeView(mSuccessView);
            }
            mSuccessView = onCreateSuccessView();
            if (mSuccessView != null) {
                addView(mSuccessView);
            }
        }

        if (mSuccessView != null) {
            mSuccessView.setVisibility((mCurrentState == STATE_SUCCESS) ? View.VISIBLE : View.INVISIBLE);
        }
    }

    public void loadData() {
        if (mCurrentState != STATE_LOADING) {
            mCurrentState = STATE_LOADING;

            ThreadManager.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    final ResultState result = onLoad();

                    UIUtils.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (result != null) {
                                mCurrentState = result.getState();
                                setRightPage();
                            }
                        }
                    });
                }
            });
        }
    }

    /**
     * 加载数据
     *
     * @return
     */
    protected abstract ResultState onLoad();

    /**
     * 获取数据成功时调用
     *
     * @return
     */
    public abstract View onCreateSuccessView();

    public enum ResultState {
        RESULT_LOADING(STATE_LOADING),RESULT_ERROR(STATE_ERROR), RESULT_EMPTY(STATE_EMPTY), RESULT_SUCCESS(STATE_SUCCESS);

        public int getState() {
            return state;
        }

        private int state;

        private ResultState(int state) {
            this.state = state;
        }

    }
}

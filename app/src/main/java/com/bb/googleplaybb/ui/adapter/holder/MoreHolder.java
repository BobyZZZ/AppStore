package com.bb.googleplaybb.ui.adapter.holder;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bb.googleplaybb.R;
import com.bb.googleplaybb.utils.UIUtils;

/**
 * Created by Boby on 2018/7/13.
 */

public class MoreHolder extends BaseHolder<Integer> {

    public final int MORE_MORE = 1;
    public final int MORE_NONE = 2;
    public final int MORE_ERROR = 3;

    private TextView tvError;
    private LinearLayout llMore;

    public MoreHolder(boolean hasMore) {
        setData(hasMore ? MORE_MORE : MORE_NONE);
    }

    @Override
    public View initView() {
        View view = UIUtils.inflate(R.layout.list_item_more);
        llMore = view.findViewById(R.id.ll_more);
        tvError = view.findViewById(R.id.tv_error);
        return view;
    }

    //setData方法中调用了此方法
    @Override
    public void refreshView(Integer data) {
        switch (data) {
            case MORE_MORE:
                llMore.setVisibility(View.VISIBLE);
                tvError.setVisibility(View.GONE);
                break;
            case MORE_NONE:
                llMore.setVisibility(View.GONE);
                tvError.setVisibility(View.GONE);
                break;
            case MORE_ERROR:
                llMore.setVisibility(View.GONE);
                tvError.setVisibility(View.VISIBLE);
                break;
        }
    }
}

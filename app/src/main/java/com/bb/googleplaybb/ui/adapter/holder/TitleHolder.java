package com.bb.googleplaybb.ui.adapter.holder;

import android.view.View;
import android.widget.TextView;

import com.bb.googleplaybb.R;
import com.bb.googleplaybb.domain.TypeInfo;
import com.bb.googleplaybb.utils.UIUtils;

/**
 * Created by Boby on 2018/7/16.
 */

public class TitleHolder extends BaseHolder<TypeInfo> {

    private TextView tvTitle;

    @Override
    public View initView() {
        View view = UIUtils.inflate(R.layout.list_item_type_title);
        tvTitle = view.findViewById(R.id.tv_title);
        return view;
    }

    @Override
    public void refreshView(TypeInfo data) {
        tvTitle.setText(data.title);
    }
}

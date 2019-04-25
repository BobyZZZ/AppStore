package com.bb.googleplaybb.ui.adapter.holder;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bb.googleplaybb.R;
import com.bb.googleplaybb.domain.TypeInfo;
import com.bb.googleplaybb.http.HttpHelper;
import com.bb.googleplaybb.net.NetHelper;
import com.bb.googleplaybb.ui.activity.AppOfTypeActivity;
import com.bb.googleplaybb.utils.BitmapHelper;
import com.bb.googleplaybb.utils.UIUtils;
import com.lidroid.xutils.BitmapUtils;

/**
 * Created by Boby on 2018/7/16.
 */

public class TypeHolder extends BaseHolder<TypeInfo> implements View.OnClickListener {

    private LinearLayout llRoot1, llRoot2, llRoot3;
    private ImageView ivIcon1, ivIcon2, ivIcon3;
    private TextView tvName1, tvName2, tvName3;
    private BitmapUtils mBitmapUtils;

    private TypeInfo data;

    @Override
    public View initView() {
        View view = UIUtils.inflate(R.layout.list_item_type_normal);

        llRoot1 = view.findViewById(R.id.ll_root1);
        ivIcon1 = view.findViewById(R.id.iv_icon1);
        tvName1 = view.findViewById(R.id.tv_name1);

        llRoot2 = view.findViewById(R.id.ll_root2);
        ivIcon2 = view.findViewById(R.id.iv_icon2);
        tvName2 = view.findViewById(R.id.tv_name2);

        llRoot3 = view.findViewById(R.id.ll_root3);
        ivIcon3 = view.findViewById(R.id.iv_icon3);
        tvName3 = view.findViewById(R.id.tv_name3);

        mBitmapUtils = BitmapHelper.getBitmapUtils();

        llRoot1.setOnClickListener(this);
        llRoot2.setOnClickListener(this);
        llRoot3.setOnClickListener(this);
        return view;
    }

    @Override
    public void refreshView(TypeInfo data) {
        this.data = data;

        if (!shouldHide(data.name1, data.url1)) {
            tvName1.setText(data.name1);
            mBitmapUtils.display(ivIcon1, NetHelper.URL + data.url1);
        } else {
            llRoot1.setVisibility(View.GONE);
        }
        if (!shouldHide(data.name2, data.url2)) {
            tvName2.setText(data.name2);
            mBitmapUtils.display(ivIcon2, NetHelper.URL + data.url2);
        } else {
            llRoot2.setVisibility(View.GONE);
        }
        if (!shouldHide(data.name3, data.url3)) {
            tvName3.setText(data.name3);
            mBitmapUtils.display(ivIcon3, NetHelper.URL + data.url3);
        } else {
            llRoot3.setVisibility(View.GONE);
        }
    }

    private boolean shouldHide(String name, String url) {
        return name == null || TextUtils.isEmpty(name) || url == null || TextUtils.isEmpty(url);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_root1:
                AppOfTypeActivity.startAppOfTypeActivity(UIUtils.getContext(), data.type1);
                break;
            case R.id.ll_root2:
                AppOfTypeActivity.startAppOfTypeActivity(UIUtils.getContext(), data.type2);
                break;
            case R.id.ll_root3:
                AppOfTypeActivity.startAppOfTypeActivity(UIUtils.getContext(), data.type3);
                break;
        }
    }
}

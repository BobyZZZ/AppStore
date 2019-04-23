package com.bb.googleplaybb.ui.adapter.holder;

import android.view.View;
import android.widget.ImageView;

import com.bb.googleplaybb.R;
import com.bb.googleplaybb.domain.AppInfo;
import com.bb.googleplaybb.net.NetHelper;
import com.bb.googleplaybb.utils.BitmapHelper;
import com.bb.googleplaybb.utils.UIUtils;
import com.lidroid.xutils.BitmapUtils;

import java.util.ArrayList;

/**
 * Created by Boby on 2018/7/17.
 */

public class HomeDetailPicHolder extends BaseHolder<AppInfo> {

    private ImageView ivPic[];
    private BitmapUtils mBitmapUtils;

    @Override
    public View initView() {
        ivPic = new ImageView[5];

        View view = UIUtils.inflate(R.layout.layout_home_detail_pic);
        ivPic[0] = view.findViewById(R.id.iv_pic1);
        ivPic[1] = view.findViewById(R.id.iv_pic2);
        ivPic[2] = view.findViewById(R.id.iv_pic3);
        ivPic[3] = view.findViewById(R.id.iv_pic4);
        ivPic[4] = view.findViewById(R.id.iv_pic5);

        mBitmapUtils = BitmapHelper.getBitmapUtils();
        return view;
    }

    @Override
    public void refreshView(AppInfo data) {
        ArrayList<String> screen = data.screen;
        for (int i = 0; i < 5; i++) {
            if (i < screen.size()) {
                mBitmapUtils.display(ivPic[i], NetHelper.URL + screen.get(i));
            } else {
                ivPic[i].setVisibility(View.GONE);
            }
        }
    }
}

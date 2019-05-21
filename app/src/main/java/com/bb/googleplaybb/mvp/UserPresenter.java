package com.bb.googleplaybb.mvp;

import android.text.TextUtils;
import android.view.TextureView;
import android.widget.Toast;

import com.bb.googleplaybb.domain.User;
import com.bb.googleplaybb.utils.LoginUtils;
import com.bb.googleplaybb.utils.UIUtils;

/**
 * Created by Boby on 2019/5/16.
 */

public class UserPresenter {
    private IUserView mView;
    private LoginUtils mLoginUtils;

    public UserPresenter(IUserView view) {
        mView = view;
        mLoginUtils = LoginUtils.getInstance();
    }

    public boolean register() {
        String id = mView.getId();
        String pwd = mView.getPwd();
        String userName = mView.getUserName();
        String photoPath = mView.getPhotoPath();
        if (TextUtils.isEmpty(photoPath)) {
            Toast.makeText(UIUtils.getContext(), "头像不能为空", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(id) || TextUtils.isEmpty(pwd) || TextUtils.isEmpty(userName)) {
            Toast.makeText(UIUtils.getContext(), "输入不能为空", Toast.LENGTH_SHORT).show();
            return false;
        }
        boolean result = mLoginUtils.insertUser(id, pwd, userName,photoPath);
        if (!result) {
            Toast.makeText(UIUtils.getContext(), "注册失败", Toast.LENGTH_SHORT).show();
        }
        return result;
    }

    public boolean login() {
        String id = mView.getId();
        String pwd = mView.getPwd();
        User user = mLoginUtils.findUser(id, pwd);
        return user != null;
    }
}

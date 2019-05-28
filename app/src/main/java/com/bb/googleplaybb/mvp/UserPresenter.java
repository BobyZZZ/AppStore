package com.bb.googleplaybb.mvp;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.widget.Toast;

import com.bb.googleplaybb.domain.User;
import com.bb.googleplaybb.utils.LoginUtils2;
import com.bb.googleplaybb.utils.UIUtils;

/**
 * Created by Boby on 2019/5/16.
 */

public class UserPresenter {
    private IUserView mView;

    public UserPresenter(IUserView view) {
        mView = view;
    }

    public void register(final OnRegisterResult onRegisterResult) {
        final String id = mView.getId();
        final String pwd = mView.getPwd();
        final String userName = mView.getUserName();
        final String photoPath = mView.getPhotoPath();
        if (TextUtils.isEmpty(photoPath)) {
            Toast.makeText(UIUtils.getContext(), "头像不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(id) || TextUtils.isEmpty(pwd) || TextUtils.isEmpty(userName)) {
            Toast.makeText(UIUtils.getContext(), "输入不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        LoginUtils2.register(id, pwd, userName, photoPath, new LoginUtils2.OnResult<Boolean>() {
            @Override
            public void onResult(Boolean result) {
                if (onRegisterResult != null) {
                    onRegisterResult.onResult(result);
                }
            }
        });
    }

    public void login(@NonNull final OnLoginResult onLoginResult) {
        final String id = mView.getId();
        final String pwd = mView.getPwd();

        LoginUtils2.login(id, pwd, new LoginUtils2.OnResult<User>() {
            @Override
            public void onResult(User result) {
                if (onLoginResult != null) {
                    onLoginResult.onResult(result);
                }
            }
        });

    }

    public interface OnLoginResult {
        void onResult(User result);
    }

    public interface OnRegisterResult {
        void onResult(boolean result);
    }
}

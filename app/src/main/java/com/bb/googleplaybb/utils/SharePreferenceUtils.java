package com.bb.googleplaybb.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.bb.googleplaybb.domain.User;

/**
 * Created by Boby on 2019/5/16.
 */

public class SharePreferenceUtils {

    public static void setUser(String user_id, String user_pwd) {
        SharedPreferences preferences = UIUtils.getContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("user_id", user_id);
        editor.putString("user_pwd", user_pwd);
        editor.apply();
    }

    public static User getUser() {
        SharedPreferences preferences = UIUtils.getContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        String user_id = preferences.getString("user_id", "");
        String user_pwd = preferences.getString("user_pwd", "");
        if (!TextUtils.isEmpty(user_id) && !TextUtils.isEmpty(user_pwd)) {
            return new User(user_id, user_pwd, null, null);
        }
        return null;
    }
}

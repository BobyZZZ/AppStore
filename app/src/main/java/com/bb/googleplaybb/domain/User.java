package com.bb.googleplaybb.domain;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import com.bb.googleplaybb.utils.LoginUtils2;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Boby on 2019/5/16.
 */

public class User implements Parcelable {
    public static final int RESULT_RESPONSE_FAILED = 200;
    public static final int RESULT_ID_OR_PWD_WRONG = 201;
    public static final int RESULT_FINDED_USER = 202;

    private String user_id;
    private String user_pwd;
    private String user_name;
    private String user_photo_path;

    private int resultCode = RESULT_RESPONSE_FAILED;

    public User() {

    }

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public User(String user_id, String user_pwd, String user_name, String user_photo_path) {
        this.user_id = user_id;
        this.user_pwd = user_pwd;
        this.user_name = user_name;
        this.user_photo_path = user_photo_path;
        resultCode = RESULT_FINDED_USER;
    }

    protected User(Parcel in) {
        user_id = in.readString();
        user_pwd = in.readString();
        user_name = in.readString();
        user_photo_path = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUser_pwd() {
        return user_pwd;
    }

    public String getUser_photo_path() {
        return user_photo_path;
    }

    public void setUser_photo_path(String user_photo_path) {
        this.user_photo_path = user_photo_path;
    }

    public void setUser_pwd(String user_pwd) {
        this.user_pwd = user_pwd;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    @Override
    public String toString() {
        return "User{" +
                "user_id='" + user_id + '\'' +
                ", user_pwd='" + user_pwd + '\'' +
                ", user_name='" + user_name + '\'' +
                ", user_photo_path='" + user_photo_path + '\'' +
                ", resultCode=" + resultCode +
                '}';
    }

    public static User fromResponse(String responseBody) {
        try {
            JSONObject jo = new JSONObject(responseBody).getJSONObject("data");
            String user_id = jo.getString("user_id");
            String user_pwd = jo.getString("user_pwd");
            String user_name = jo.getString("user_name");
            String user_photo_path = jo.getString("user_photo_path");
            User user = new User(user_id, user_pwd, user_name, user_photo_path);
            return user;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(user_id);
        dest.writeString(user_pwd);
        dest.writeString(user_name);
        dest.writeString(user_photo_path);
    }
}

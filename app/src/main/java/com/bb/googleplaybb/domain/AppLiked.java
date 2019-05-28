package com.bb.googleplaybb.domain;

import android.database.Cursor;


/**
 * 收藏应用bean
 * Created by Boby on 2019/5/16.
 */

public class AppLiked {
    //收藏表字段
    public static final String APP_ID = "app_id";
    public static final String APP_NAME = "app_name";
    public static final String APP_DES = "app_des";
    public static final String APP_PACKAGE_NAME = "app_package_name";
    public static final String APP_ICON = "app_icon";
    public static final String USER_ID = "user_id";

    private String user_id;
    private String app_id;
    private String app_name;
    private String app_des;
    private String app_package_name;
    private String app_icon;

    public AppLiked() {

    }

    public AppLiked(Cursor cursor) {
        user_id = cursor.getString(cursor.getColumnIndex(USER_ID));
        app_id = cursor.getString(cursor.getColumnIndex(APP_ID));
        app_name = cursor.getString(cursor.getColumnIndex(APP_NAME));
        app_des = cursor.getString(cursor.getColumnIndex(APP_DES));
        app_package_name = cursor.getString(cursor.getColumnIndex(APP_PACKAGE_NAME));
        app_icon = cursor.getString(cursor.getColumnIndex(APP_ICON));
    }

    public AppLiked(String user_id, String app_id, String app_name, String app_des, String app_package_name, String app_icon) {
        this.user_id = user_id;
        this.app_id = app_id;
        this.app_name = app_name;
        this.app_des = app_des;
        this.app_package_name = app_package_name;
        this.app_icon = app_icon;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getApp_id() {
        return app_id;
    }

    public void setApp_id(String app_id) {
        this.app_id = app_id;
    }

    public String getApp_name() {
        return app_name;
    }

    public void setApp_name(String app_name) {
        this.app_name = app_name;
    }

    public String getApp_des() {
        return app_des;
    }

    public void setApp_des(String app_des) {
        this.app_des = app_des;
    }

    public String getApp_package_name() {
        return app_package_name;
    }

    public void setApp_package_name(String app_package_name) {
        this.app_package_name = app_package_name;
    }

    public String getApp_icon() {
        return app_icon;
    }

    public void setApp_icon(String app_icon) {
        this.app_icon = app_icon;
    }

    @Override
    public String toString() {
        return "AppLiked{" +
                "user_id='" + user_id + '\'' +
                ", app_id='" + app_id + '\'' +
                ", app_name='" + app_name + '\'' +
                ", app_des='" + app_des + '\'' +
                ", app_package_name='" + app_package_name + '\'' +
                ", app_icon='" + app_icon + '\'' +
                '}';
    }
}

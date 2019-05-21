package com.bb.googleplaybb.domain;

/**
 * Created by Boby on 2019/5/16.
 */

public class User {
    private String user_id;
    private String user_pwd;
    private String user_name;
    private String user_photo_path;

    public User(String user_id, String user_pwd, String user_name, String user_photo_path) {
        this.user_id = user_id;
        this.user_pwd = user_pwd;
        this.user_name = user_name;
        this.user_photo_path = user_photo_path;
    }

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
}

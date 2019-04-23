package com.bb.googleplaybb.domain;

import java.util.ArrayList;

/**
 * Created by Boby on 2018/7/13.
 */

public class AppInfo {
    public String id;
    public String name;
    public String packageName;
    public String iconUrl;
    public String downloadUrl;
    public String des;
    public float stars;
    public long size;

    //补充字段
    public String author;
    public String date;
    public String downloadNum;
    public ArrayList<SafeInfo> safe;
    public ArrayList<String> screen;
    public String version;

    public static class SafeInfo {
        public String safeDes;
        public String safeDesUrl;
        public String safeUrl;
    }
}

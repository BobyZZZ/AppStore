package com.bb.googleplaybb.net.protocol;

import com.bb.googleplaybb.domain.AppInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Boby on 2018/7/16.
 */

public class HomeDetailNetProtocol extends BaseNetProtocol<AppInfo> {

    private String packageName;
    public HomeDetailNetProtocol(String packageName) {
        this.packageName = packageName;
    }
    @Override
    public AppInfo parseData(String json) {
        try {
            AppInfo appInfo = new AppInfo();

            JSONObject jo = new JSONObject(json);
            appInfo.id = jo.getString("id");
            appInfo.name = jo.getString("name");
            appInfo.packageName = jo.getString("packageName");
            appInfo.iconUrl = jo.getString("iconUrl");
            appInfo.stars = (float) jo.getDouble("stars");
            appInfo.size = jo.getLong("size");
            appInfo.downloadUrl = jo.getString("downloadUrl");
            appInfo.des = jo.getString("des");
            //补充字段
            appInfo.author = jo.getString("author");
            appInfo.date = jo.getString("date");
            appInfo.downloadNum = jo.getString("downloadNum");
            appInfo.version = jo.getString("version");

            JSONArray ja = jo.getJSONArray("safe");
            ArrayList<AppInfo.SafeInfo> safe = new ArrayList<>();
            for(int i =0;i<ja.length();i++) {
                JSONObject jo1 = ja.getJSONObject(i);

                AppInfo.SafeInfo safeInfo = new AppInfo.SafeInfo();
                safeInfo.safeDes = jo1.getString("safeDes");
                safeInfo.safeDesUrl = jo1.getString("safeDesUrl");
                safeInfo.safeUrl = jo1.getString("safeUrl");
                safe.add(safeInfo);
            }
            appInfo.safe = safe;


            JSONArray ja1 = jo.getJSONArray("screen");
            ArrayList<String> screen = new ArrayList<>();
            for(int j=0;j<ja1.length();j++) {
                screen.add((String) ja1.get(j));
            }
            appInfo.screen = screen;

            return appInfo;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getParams() {
        return "";
    }

    @Override
    public String getkey() {
        return "app/"+packageName+"/"+packageName;
    }

    @Override
    public String getCacheName() {
        return packageName;
    }
}

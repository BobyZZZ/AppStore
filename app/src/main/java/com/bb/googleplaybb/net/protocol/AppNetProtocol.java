package com.bb.googleplaybb.net.protocol;

import com.bb.googleplaybb.domain.AppInfo;
import com.bb.googleplaybb.http.protocol.BaseProtocol;
import com.bb.googleplaybb.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Boby on 2018/7/14.
 */

public class AppNetProtocol extends BaseNetProtocol<ArrayList<AppInfo>> {
    @Override
    public ArrayList<AppInfo> parseData(String json) {
        try {
            if (!StringUtils.isEmpty(json)) {
                JSONArray ja = new JSONArray(json);

                if (ja != null) {
                    ArrayList<AppInfo> appInfos = new ArrayList<>();
                    for(int i=0;i<ja.length();i++) {
                        AppInfo appInfo = new AppInfo();
                        JSONObject appInfoObject = ja.getJSONObject(i);
                        appInfo.id = appInfoObject.getString("id");
                        appInfo.name = appInfoObject.getString("name");
                        appInfo.packageName = appInfoObject.getString("packageName");
                        appInfo.iconUrl = appInfoObject.getString("iconUrl");
                        appInfo.stars = (float) appInfoObject.getDouble("stars");
                        appInfo.size = appInfoObject.getLong("size");
                        appInfo.downloadUrl = appInfoObject.getString("downloadUrl");
                        appInfo.des = appInfoObject.getString("des");

                        appInfos.add(appInfo);
                    }
                    return appInfos;
                }
            }
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
    public String getKey() {
        return "app/applist";
    }

    public String getCacheName() {
        return "applist";
    }
}

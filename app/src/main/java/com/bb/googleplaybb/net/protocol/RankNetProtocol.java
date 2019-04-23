package com.bb.googleplaybb.net.protocol;

import com.bb.googleplaybb.domain.RecommendInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Boby on 2018/7/14.
 */

public class RankNetProtocol extends BaseNetProtocol<ArrayList<RecommendInfo>> {
    @Override
    public ArrayList<RecommendInfo> parseData(String json) {
        if (json != null) {
            try {
                JSONArray ja = new JSONArray(json);
                if (ja != null) {
                    ArrayList<RecommendInfo> list = new ArrayList<>();
                    for(int i = 0 ;i<ja.length();i++) {
                        RecommendInfo recommendInfo = new RecommendInfo();
                        JSONObject jo = ja.getJSONObject(i);
                        recommendInfo.id = jo.getString("id");
                        recommendInfo.name = jo.getString("name");
                        recommendInfo.packageName = jo.getString("packageName");
                        list.add(recommendInfo);
                    }
                    return list;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public String getParams() {
        return "";
    }

    @Override
    public String getKey() {
        return "app/hotlist";
    }

    @Override
    public String getCacheName() {
        return "hotlist";
    }
}

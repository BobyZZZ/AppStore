package com.bb.googleplaybb.http.protocol;

import com.bb.googleplaybb.domain.TopicInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Boby on 2018/7/14.
 */

public class TopicProtocol extends BaseProtocol<ArrayList<TopicInfo>> {
    @Override
    public ArrayList<TopicInfo> parseData(String json) {
        if (json != null) {
            try {
                JSONArray ja = new JSONArray(json);
                if (ja != null) {
                    ArrayList<TopicInfo> list = new ArrayList<>();
                    for(int i =0;i<ja.length();i++) {
                        TopicInfo topicInfo = new TopicInfo();
                        JSONObject jo = ja.getJSONObject(i);
                        topicInfo.url = jo.getString("url");
                        topicInfo.des = jo.getString("des");

                        list.add(topicInfo);
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
    public String getkey() {
        return "subject";
    }
}

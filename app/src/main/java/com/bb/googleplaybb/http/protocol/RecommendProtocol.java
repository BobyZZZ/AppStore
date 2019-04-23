package com.bb.googleplaybb.http.protocol;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Boby on 2018/7/14.
 */

public class RecommendProtocol extends BaseProtocol<ArrayList<String>> {
    @Override
    public ArrayList<String> parseData(String json) {
        if (json != null) {
            try {
                JSONArray ja = new JSONArray(json);
                if (ja != null) {
                    ArrayList<String> list = new ArrayList<>();
                    for(int i = 0 ;i<ja.length();i++) {
                        String keyword = (String) ja.get(i);
                        list.add(keyword);
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
        return "recommend";
    }
}

package com.bb.googleplaybb.net.protocol;

import com.bb.googleplaybb.domain.TypeInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Boby on 2018/7/16.
 */

public class TypeNetProtocol extends BaseNetProtocol<ArrayList<TypeInfo>> {
    @Override
    public ArrayList<TypeInfo> parseData(String json) {
        try {
            JSONArray ja = new JSONArray(json);
            ArrayList<TypeInfo> list = new ArrayList<>();
            for (int i = 0; i < ja.length(); i++) {
                JSONObject jo = ja.getJSONObject(i);

                //获取标题条目
                if (jo.has("title")) {
                    TypeInfo info = new TypeInfo();
                    info.title = jo.getString("title");
                    info.isTitle = true;

                    list.add(info);
                }

                //获取正常条目
                if (jo.has("infos")) {
                    JSONArray ja1 = jo.getJSONArray("infos");
                    for (int j = 0; j < ja1.length(); j++) {
                        TypeInfo typeInfo = new TypeInfo();

                        JSONObject jo1 = ja1.getJSONObject(j);
                        typeInfo.name1 = jo1.getString("name1");
                        typeInfo.name2 = jo1.getString("name2");
                        typeInfo.name3 = jo1.getString("name3");
                        typeInfo.url1 = jo1.getString("url1");
                        typeInfo.url2 = jo1.getString("url2");
                        typeInfo.url3 = jo1.getString("url3");
                        typeInfo.type1 = jo1.getString("type1");
                        typeInfo.type2 = jo1.getString("type2");
                        typeInfo.type3 = jo1.getString("type3");
                        typeInfo.isTitle = false;

                        list.add(typeInfo);
                    }
                }
            }
            return list;
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
        return "app/categorylist";
    }

    @Override
    public String getCacheName() {
        return "categorylist";
    }
}

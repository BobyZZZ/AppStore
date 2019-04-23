package com.bb.googleplaybb.net;

import com.bb.googleplaybb.global.GooglePlayApplication;
import com.bb.googleplaybb.utils.UIUtils;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Boby on 2018/10/30.
 */

public class NetHelper {
    //    public static final String URL = "http://192.168.43.169:8080/WebInfos/";
    public static String URL = "http://" + GooglePlayApplication.getIp() + ":8080/WebInfos/";

    public static Response get(String url) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }
}

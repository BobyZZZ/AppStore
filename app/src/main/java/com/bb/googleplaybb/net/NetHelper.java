package com.bb.googleplaybb.net;

import android.util.Log;

import com.bb.googleplaybb.global.GooglePlayApplication;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NetHelper {
    public static String URL = "http://" + GooglePlayApplication.getIp() + ":8080/WebInfos/";

    public static void modifyId(String ip) {
        URL = "http://" + ip + ":8080/WebInfos/";
    }

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

    public static Response download(String url, long start, long end) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).addHeader("RANGE", "bytes=" + start + "-" + end).build();
        Call call = client.newCall(request);
        try {
            Response response = call.execute();
            return response;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void uploadImage(String url, String filePath, final OnUploadResultCallback onUploadResult) {
        File file = new File(filePath);
        if (file.exists()) {
            OkHttpClient client = new OkHttpClient();
            MultipartBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("name", "a.jpg", RequestBody.create(MediaType.parse("image/jpg"), file))
                    .build();
            Request request = new Request.Builder().url(url)
                    .post(body)
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    if (onUploadResult != null) {
                        Log.e("upload", "onFailure: ", e);
                        onUploadResult.onFailure(call);
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (onUploadResult != null) {
                        onUploadResult.onResponse(call, response);
                        Log.e("upload", "onResponse: ");
                    }
                }
            });
        }
    }

    public interface OnUploadResultCallback {
        void onFailure(Call call);

        void onResponse(Call call, Response response);
    }
}

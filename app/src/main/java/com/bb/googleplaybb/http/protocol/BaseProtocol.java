package com.bb.googleplaybb.http.protocol;

import android.text.TextUtils;

import com.bb.googleplaybb.http.HttpHelper;
import com.bb.googleplaybb.net.NetHelper;
import com.bb.googleplaybb.utils.IOUtils;
import com.bb.googleplaybb.utils.StringUtils;
import com.bb.googleplaybb.utils.UIUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Response;

/**
 * 具有缓存数据功能
 * Created by Boby on 2018/7/13.
 */

public abstract class BaseProtocol<T> {

    public T getData(int index) {
        //获取缓存数据
        String result = getCache(index);
        if (StringUtils.isEmpty(result)) {
            //从网络获取数据
            result = getDataFromServer(index);
        }

        if (!StringUtils.isEmpty(result)) {
            //解析数据
            T appInfos = parseData(result);
            if (appInfos != null) {
                return appInfos;
            }
        }
        return null;
    }

    private String getCache(int index) {
        File cacheDir = UIUtils.getContext().getCacheDir();
        File cacheFile = new File(cacheDir, getkey() + "?index=" + index + getParams());
        BufferedReader bufferedReader = null;
        if (cacheFile.exists()) {
            try {
                bufferedReader = new BufferedReader(new FileReader(cacheFile));
                String deadLine = bufferedReader.readLine();
                long deadTime = Long.parseLong(deadLine);
                if (deadTime > System.currentTimeMillis()) {
                    //未过期，获取数据
                    StringBuffer sb = new StringBuffer();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        sb.append(line);
                    }
                    String result = sb.toString();
                    if (!StringUtils.isEmpty(result)) {
                        System.out.println("获取缓存" + index + "结果为：" + result);
                        return result;
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                IOUtils.close(bufferedReader);
            }
        }
        return null;
    }

    private String getDataFromServer(int index) {
//        http://www.baidu.com/home?index=0&name=ergouzi&age=1
        HttpHelper.HttpResult httpResult = HttpHelper.get(HttpHelper.URL + getkey() + "?index=" + index + getParams());
        if (httpResult != null) {
            String json = httpResult.getString();
            //缓存数据
            if(!TextUtils.isEmpty(json))
            setCache(json, index);
            System.out.println("从网络获取数据结果为：" + json);
            return json;
        }
        return null;
    }

    private void setCache(String json, int index) {
        File cacheDir = UIUtils.getContext().getCacheDir();
        File cacheFile = new File(cacheDir, getkey() + "?index=" + index + getParams());
        FileWriter writer = null;
        try {
            writer = new FileWriter(cacheFile);
            long deadLine = System.currentTimeMillis() + 30 * 60 * 1000;
            writer.write(deadLine + "\n");
            writer.flush();

            writer.write(json);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.close(writer);
        }
    }

    public abstract T parseData(String json);

    public abstract String getParams();

    public abstract String getkey();
}

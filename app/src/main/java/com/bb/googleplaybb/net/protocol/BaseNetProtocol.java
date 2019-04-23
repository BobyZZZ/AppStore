package com.bb.googleplaybb.net.protocol;

import android.text.TextUtils;

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

import okhttp3.Response;

/**
 * 使用okhttp
 * 具有缓存数据功能
 * Created by Boby on 2018/7/13.
 */

public abstract class BaseNetProtocol<T> {

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
        File cacheFile = new File(cacheDir, getCacheName() + "?index=" + index);
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

    /**
     * @param index 用于分页，无分页时传-1
     * @return
     */
    private String getDataFromServer(int index) {
        Response response = null;
        if (index > -1) {
            int newIndex = (index / 20) % 3;
            response = NetHelper.get(NetHelper.URL + getKey() + newIndex);
        } else {
            response = NetHelper.get(NetHelper.URL + getKey());
        }
        String json = null;
        if (response != null) {
            try {
//                byte[] bytes = response.body().bytes();
//                json = new String(bytes,"GB2312");
                json = response.body().string();
                //缓存数据
                if(!TextUtils.isEmpty(json))
                    setCache(json, index);
                System.out.println("从网络获取数据结果为：" + json);
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                response.body().close();
            }
        }
        return json;
    }

    private void setCache(String json, int index) {
        File cacheDir = UIUtils.getContext().getCacheDir();
        File cacheFile = new File(cacheDir, getCacheName() + "?index=" + index);
        FileWriter writer = null;
        try {
            writer = new FileWriter(cacheFile);
            long deadLine = System.currentTimeMillis() + 60 * 1000;
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

    public abstract String getKey();

    public String getCacheName(){
        return "";
    }
}

package com.bb.googleplaybb.global;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Process;

import com.mob.MobSDK;

/**
 * Created by Boby on 2018/7/10.
 */

public class GooglePlayApplication extends Application {

    private static Context context;
    private static Handler handler;
    private static int mainThreadId;
    private static String ip;

    @Override
    public void onCreate() {
        super.onCreate();
        //初始化MobSDK
        MobSDK.init(this);

        context = getApplicationContext();
        handler = new Handler();
        mainThreadId = Process.myTid();
    }

    public static Context getContext() {
        return context;
    }

    public static Handler getHandler() {
        return handler;
    }

    public static int getMainThreadId() {
        return mainThreadId;
    }

    public static String getIp() {
       return getContext().getSharedPreferences("ipConfig",Context.MODE_PRIVATE).getString("ip","10.0.0.2");
    }

    public static void putIp(String ip) {
        getContext().getSharedPreferences("ipConfig",Context.MODE_PRIVATE).edit().putString("ip",ip).commit();
    }
}

package com.bb.googleplaybb.utils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Process;
import android.view.View;

import com.bb.googleplaybb.global.GooglePlayApplication;

/**
 * Created by Boby on 2018/7/10.
 */

public class UIUtils {

    public static Context getContext() {
        return GooglePlayApplication.getContext();
    }

    public static Handler getHandler() {
        return GooglePlayApplication.getHandler();
    }

    public static int getMainThreadId() {
        return GooglePlayApplication.getMainThreadId();
    }

    //获取字符串
    public static String getString(int id) {
        return getContext().getResources().getString(id);
    }

    //获取字符数组
    public static String[] getStringArray(int id) {
        return getContext().getResources().getStringArray(id);
    }

    //获取图片
    public static Drawable getDrawable(int id) {
        return getContext().getResources().getDrawable(id);
    }

    //获取颜色
    public static int getColor(int id) {
        return getContext().getResources().getColor(id);
    }

    //获取颜色集合
    public static ColorStateList getColorStateList(int id) {
        ColorStateList colorStateList = getContext().getResources().getColorStateList(id);
        return colorStateList;
    }

    //获取尺寸
    public static float getDimension(int id) {
        return getContext().getResources().getDimension(id);
    }

    //dp转px
    public static int dip2px(float dip) {
        float density = getContext().getResources().getDisplayMetrics().density;
        return (int) (dip * density);//px = dip * density
    }

    //px转dp
    public static float px2dip(int px) {
        float density = getContext().getResources().getDisplayMetrics().density;
        return px / density;
    }

    //判断是否是主线程
    public static boolean isRunOnUiThread() {
        int myTid = Process.myTid();
        if (myTid == getMainThreadId()) {
            return true;
        }
        return false;
    }

    //在主线程中运行
    public static void runOnUiThread(final Runnable runnable) {
        if (isRunOnUiThread()) {
            runnable.run();
        } else {
            getHandler().post(runnable);
        }
    }

    public static View inflate(int id) {
        return View.inflate(getContext(),id,null);
    }
}

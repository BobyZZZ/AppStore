package com.bb.googleplaybb.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Process;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.bb.googleplaybb.global.GooglePlayApplication;

/**
 * Created by Boby on 2018/7/10.
 */

public class UIUtils {

    public static int getStatusBarHeight(){
        int result = 0;
        int resourceId = getContext().getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getContext().getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static void setStatusBarTransparent(Activity activity) {
        ViewGroup contentView = activity.getWindow().findViewById(Window.ID_ANDROID_CONTENT);
        contentView.getChildAt(0).setFitsSystemWindows(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //5.x开始需要把颜色设置透明，否则导航栏会呈现系统默认的浅灰色
            View decorView = activity.getWindow().getDecorView();
            //两个 flag 要结合使用，表示让应用的主体内容占用系统状态栏的空间
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);

            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
            //导航栏颜色也可以正常设置
//                window.setNavigationBarColor(Color.TRANSPARENT);
        } else {
            WindowManager.LayoutParams attributes = activity.getWindow().getAttributes();
            int flagTranslucentStatus = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
            int flagTranslucentNavigation = WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
            attributes.flags |= flagTranslucentStatus;
//                attributes.flags |= flagTranslucentNavigation;
            activity.getWindow().setAttributes(attributes);
        }
    }

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

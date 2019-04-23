package com.bb.googleplaybb.utils;

import com.lidroid.xutils.BitmapUtils;

/**
 * Created by Boby on 2018/7/14.
 */

public class BitmapHelper {

    private static BitmapUtils utils;

    public static BitmapUtils getBitmapUtils() {
        if (utils == null) {
            synchronized (BitmapHelper.class) {
                if (utils == null) {
                    utils = new BitmapUtils(UIUtils.getContext());
                }
            }
        }
        return utils;
    }
}

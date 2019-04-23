package com.bb.googleplaybb.utils;

import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;

import com.bb.googleplaybb.R;

import java.nio.channels.Selector;

/**
 * Created by Boby on 2018/7/14.
 */

public class DrawableUtils {

    public static GradientDrawable getGradientDrawable(int color, float radius) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        gradientDrawable.setColor(color);
        gradientDrawable.setCornerRadius(radius);
        return gradientDrawable;
    }

    public static StateListDrawable getSelector(int normalColor, int pressColor, float radius) {
        GradientDrawable bgPress = getGradientDrawable(pressColor, radius);
        GradientDrawable bgNormal = getGradientDrawable(normalColor, radius);

        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, bgPress);
        stateListDrawable.addState(new int[]{}, bgNormal);

        return stateListDrawable;
    }
}

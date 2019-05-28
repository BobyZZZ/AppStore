package com.bb.googleplaybb.utils;

import android.app.Activity;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.util.ArrayList;

/**
 * Created by Boby on 2019/5/28.
 */

public class KeyBoardUtils {
    public static void hideKeyBoardWhenTouchOtherView(Activity activity, MotionEvent ev, ArrayList<View> excludeTouchHideInputViews) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (excludeTouchHideInputViews != null && excludeTouchHideInputViews.size() > 0) {
                for (View view : excludeTouchHideInputViews) {
                    if (inTouch(view, ev)) {
                        return;
                    }
                }
            }

            View currentFocusView = activity.getCurrentFocus();
            if (shouldHideKeyBoard(currentFocusView, ev)) {
                InputMethodManager manager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (manager != null) {
                    manager.hideSoftInputFromWindow(currentFocusView.getWindowToken(),0);
                }
            }
        }
    }

    private static boolean shouldHideKeyBoard(View currentFocusView, MotionEvent ev) {
        if (currentFocusView != null && currentFocusView instanceof EditText) {
            return !inTouch(currentFocusView,ev);
        }
        return false;
    }

    private static boolean inTouch(View view, MotionEvent ev) {
        int[] location = new int[2];
        view.getLocationInWindow(location);
        int left = location[0];
        int top = location[1];
        if (ev.getRawX() > left && ev.getRawX() < left + view.getWidth() && ev.getRawY() > top && ev.getRawY() < top + view.getHeight()) {
            return true;
        }
        return false;
    }
}

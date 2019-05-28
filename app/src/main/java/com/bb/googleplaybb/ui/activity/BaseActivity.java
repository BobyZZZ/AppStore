package com.bb.googleplaybb.ui.activity;

import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;

import com.bb.googleplaybb.utils.KeyBoardUtils;

import java.util.ArrayList;

public class BaseActivity extends AppCompatActivity {

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        KeyBoardUtils.hideKeyBoardWhenTouchOtherView(this, ev,getExcludeTouchHideInputViews());
        return super.dispatchTouchEvent(ev);
    }

    protected ArrayList<View> getExcludeTouchHideInputViews() {
        return null;
    }
}

package com.bb.googleplaybb.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.bb.googleplaybb.R;
import com.bb.googleplaybb.utils.UIUtils;

import static com.bb.googleplaybb.utils.UIUtils.getStatusBarHeight;

/**
 * Created by Boby on 2019/4/23.
 */

public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_detail_md);
//        setStatusBarTransparent(this);
        initToolBar();
    }

    private void initToolBar(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        int statusBarHeight = getStatusBarHeight();
//        toolbar.setPadding(0,statusBarHeight,0,0);
        toolbar.setTitle("title");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    public static void start(Context context) {
        context.startActivity(new Intent(context,TestActivity.class));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}

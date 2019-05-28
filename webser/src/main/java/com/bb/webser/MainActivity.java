package com.bb.webser;

import android.provider.SyncStateContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.yanzhenjie.andserver.AndServer;
import com.yanzhenjie.andserver.Server;

import org.apache.httpcore.util.NetUtils;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private View mStop;
    private View mStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mStart = findViewById(R.id.btn_start);
        mStop = findViewById(R.id.btn_stop);

        mStart.setOnClickListener(this);
        mStop.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start:


                break;
            case R.id.btn_stop:

                break;
        }
    }
}

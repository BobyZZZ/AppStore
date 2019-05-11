package com.bb.mytest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.concurrent.ConcurrentLinkedQueue;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ConcurrentLinkedQueue<Integer> objects = new ConcurrentLinkedQueue<>();
        objects.offer(1);
        objects.offer(2);
        objects.offer(3);

        Log.i("ConcurrentLinkedQueue", "onCreate: " + objects.poll() + "-" + objects.size());
        Log.i("ConcurrentLinkedQueue", "onCreate: " + objects.poll() + "-" + objects.size());
        Log.i("ConcurrentLinkedQueue", "onCreate: " + objects.poll() + "-" + objects.size());
    }
}

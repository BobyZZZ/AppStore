package com.bb.mytest;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private NotificationManager mNotifyManager;
    private int progress;
    private int finish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "channel_id");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("channel_id", "channel_name", NotificationManager.IMPORTANCE_LOW);
            channel.enableLights(false);
            channel.enableVibration(false);
            channel.setVibrationPattern(new long[]{0});
            channel.setSound(null, null);
            mNotifyManager.createNotificationChannel(channel);
        }
        mBuilder.setContentTitle("Picture Download")
                .setContentText("Download in progress")
                .setOngoing(false)
                .setTicker("notification ticker")
                .setSound(null)
                .setVibrate(new long[]{0})
                .setDefaults(NotificationCompat.FLAG_LOCAL_ONLY)
                .setSmallIcon(android.R.drawable.stat_notify_chat);
        new NotificationThread(mBuilder).start();
        new NotificationThread(mBuilder).start();
    }

    class NotificationThread extends Thread {
        NotificationCompat.Builder mBuilder;

        public NotificationThread(NotificationCompat.Builder builder) {
            this.mBuilder = builder;
        }

        @Override
        public void run() {
            int incr;
            for (incr = 0; incr <= 50; incr++) {
                for(int i=0;i<6000;i++) {

                }
                progress++;
                mBuilder.setProgress(100, progress, false);
                mBuilder.setSound(null);
                mNotifyManager.notify(0, mBuilder.build());
            }
            if (++finish == 2) {
                mBuilder.setContentText("Download complete").setProgress(0, 0, false);
                mBuilder.setAutoCancel(true);
                mBuilder.setOngoing(false);
                mNotifyManager.cancelAll();
                mNotifyManager.notify(0, mBuilder.build());
                Log.e("zyc", "complete: ");
            }
        }
    }
}

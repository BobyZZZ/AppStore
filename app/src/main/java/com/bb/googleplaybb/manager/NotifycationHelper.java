package com.bb.googleplaybb.manager;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RemoteViews;

import com.bb.googleplaybb.R;
import com.bb.googleplaybb.broadcastReceiver.NotificationBroadcastReceiver;
import com.bb.googleplaybb.domain.DownloadInfo;
import com.bb.googleplaybb.net.NetHelper;
import com.bb.googleplaybb.utils.UIUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;

import java.util.concurrent.ExecutionException;

/**
 * Created by Boby on 2019/5/12.
 */

public class NotifycationHelper {
    public static final int ACTION_CANCEL = 100;
    public static final int ACTION_NOTIFY = 101;

    public static final int NOTIFICATION_CANCEL = 0;

    private static NotificationManager manager;
    private static NotifycationHelper notifycationHelper;
    private Bitmap mBitmap;

    private NotifycationHelper() {
    }

    public static NotifycationHelper getInstance() {
        if (notifycationHelper == null) {
            synchronized (NotifycationHelper.class) {
                if (notifycationHelper == null) {
                    notifycationHelper = new NotifycationHelper();
                    manager = (NotificationManager) UIUtils.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                }
            }
        }
        return notifycationHelper;
    }

    @SuppressLint("NewApi")
    public void notify(int id, float pro) {
        Notification.Builder builder = new Notification.Builder(UIUtils.getContext());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("id", "name", NotificationManager.IMPORTANCE_DEFAULT);
            builder.setChannelId("id");
            manager.createNotificationChannel(channel);
        }
        builder.setSmallIcon(R.mipmap.ic_launcher_round);
        builder.setContentTitle("title");
        builder.setContentText(pro * 100 + "%");
        builder.setWhen(System.currentTimeMillis());
        builder.setShowWhen(true);
        Notification notification = builder.build();
        manager.notify(id, notification);
    }

    @SuppressLint("NewApi")
    public void notify(DownloadInfo downloadInfo) {
        int id = Integer.parseInt(downloadInfo.id);
        Notification.Builder builder = new Notification.Builder(UIUtils.getContext());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(downloadInfo.id, downloadInfo.name, NotificationManager.IMPORTANCE_DEFAULT);
            builder.setChannelId(downloadInfo.id);
            manager.createNotificationChannel(channel);
        }

        RemoteViews remoteView = getRemoteView(downloadInfo);
        builder.setSmallIcon(R.mipmap.ic_launcher_round);
        builder.setDefaults(Notification.FLAG_ONLY_ALERT_ONCE);
        builder.setCustomContentView(remoteView);
        manager.notify(id, builder.build());
    }

    public RemoteViews getRemoteView(final DownloadInfo downloadInfo) {
        final RemoteViews remoteViews = new RemoteViews(UIUtils.getContext().getPackageName(), R.layout.notification_download_layout);
        remoteViews.setTextViewText(R.id.notify_name, downloadInfo.name);
        remoteViews.setTextViewText(R.id.notify_progress, (int) (downloadInfo.getProgress() * 100) + "%");
        ThreadManager.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = null;
                try {
                    bitmap = Glide.with(UIUtils.getContext())
                            .asBitmap()
                            .load(NetHelper.URL + downloadInfo.icon)
                            .submit(UIUtils.dip2px(50), UIUtils.dip2px(50))
                            .get();
                    remoteViews.setImageViewBitmap(R.id.iv_icon, bitmap);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });
        Intent intent = new Intent(UIUtils.getContext(), NotificationBroadcastReceiver.class);
        intent.putExtra("action", ACTION_CANCEL);
        intent.putExtra("id", downloadInfo.id);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(UIUtils.getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(UIUtils.getContext(), Integer.parseInt(downloadInfo.id), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.cancel, pendingIntent);
        return remoteViews;
    }

    public void cancelById(int id) {
        manager.cancel(id);
    }
}

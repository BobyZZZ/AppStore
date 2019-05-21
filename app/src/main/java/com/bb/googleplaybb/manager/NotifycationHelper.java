package com.bb.googleplaybb.manager;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.FileProvider;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.RemoteViews;

import com.bb.googleplaybb.BuildConfig;
import com.bb.googleplaybb.R;
import com.bb.googleplaybb.broadcastReceiver.NotificationBroadcastReceiver;
import com.bb.googleplaybb.domain.DownloadInfo;
import com.bb.googleplaybb.net.NetHelper;
import com.bb.googleplaybb.utils.UIUtils;
import com.bumptech.glide.Glide;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class NotifycationHelper {
    public static final int ACTION_CANCEL = 100;
    public static final int ACTION_SHOW = 101;
    public static final int ACTION_FINISHED = 102;
    public static final int ACTION_UPDATE = 103;

    private static NotificationManager manager;
    private static NotifycationHelper notifycationHelper;
    private HashMap<String, Bitmap> mBitmapHashMap = new HashMap<>();
    private HashMap<String, Notification> mNotifications = new HashMap<>();
    private AppDownloadManager mDownloadManager = AppDownloadManager.getDownloadManager();

    private NotifycationHelper() {
    }

    public static NotifycationHelper getInstance() {
        if (notifycationHelper == null) {
            synchronized (NotifycationHelper.class) {
                if (notifycationHelper == null) {
                    notifycationHelper = new NotifycationHelper();
                    manager = (NotificationManager) UIUtils.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        NotificationChannel channel = new NotificationChannel("id", "name", NotificationManager.IMPORTANCE_LOW);
                        channel.setDescription("description of this notification");
                        channel.setLightColor(Color.GREEN);
                        channel.setName("name of this notification");
                        channel.setShowBadge(true);
                        manager.createNotificationChannel(channel);
                    }
                }
            }
        }
        return notifycationHelper;
    }

    @SuppressLint("NewApi")
    public void showNotification(String id) {
        if (!mNotifications.containsKey(id)) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(UIUtils.getContext(), "id");
            DownloadInfo info = mDownloadManager.getDownloadInfo(id);

            builder.setSmallIcon(R.mipmap.ic_launcher_round);
            builder.setContentTitle(info.name);
            builder.setOngoing(true);
            builder.setContentIntent(null);
            builder.setDeleteIntent(null);

            RemoteViews remoteView = getRemoteView(id, false);
            builder.setCustomContentView(remoteView);
            Notification notification = builder.build();
            manager.notify(Integer.parseInt(id), notification);
            mNotifications.put(id, notification);
        }
    }

    public void updateNotification(String id) {
        Notification notification = mNotifications.get(id);
        if (notification != null) {
            DownloadInfo downloadInfo = mDownloadManager.getDownloadInfo(id);

            notification.contentView.setTextViewText(R.id.notify_progress, getProgressText(downloadInfo));
            manager.notify(Integer.parseInt(downloadInfo.id), notification);
        }
    }

    public void showFinishedNotification(String id) {
        Notification notification = mNotifications.get(id);
        if (notification != null) {
            DownloadInfo downloadInfo = mDownloadManager.getDownloadInfo(id);
            manager.cancel(Integer.parseInt(id));

            notification.flags &= ~NotificationCompat.FLAG_ONGOING_EVENT;//setongoing(false);
            notification.flags |= NotificationCompat.FLAG_AUTO_CANCEL;
            String text = "点击安装";

            //点击安装
            Intent intent = getInstallIntent(downloadInfo);
            PendingIntent pendingIntent = PendingIntent.getActivity(UIUtils.getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            notification.contentIntent = pendingIntent;

            notification.contentView.setTextViewText(R.id.notify_progress, text);
            notification.contentView.setTextViewText(R.id.cancel, "");
            manager.notify(Integer.parseInt(downloadInfo.id), notification);

            mNotifications.remove(downloadInfo.id);
        }
    }

    private Intent getInstallIntent(DownloadInfo downloadInfo) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri;
        //判断是否是安卓7.0以上
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            uri = FileProvider.getUriForFile(UIUtils.getContext(),
                    BuildConfig.APPLICATION_ID + ".fileProvider",
                    new File(downloadInfo.getFilePath()));
        } else {
            uri = Uri.parse("file://" + downloadInfo.getFilePath());
        }
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        return intent;
    }


    private RemoteViews getRemoteView(String id, boolean finish) {
        final DownloadInfo downloadInfo = mDownloadManager.getDownloadInfo(id);

        final RemoteViews remoteViews = new RemoteViews(UIUtils.getContext().getPackageName(), R.layout.notification_download_layout);
        remoteViews.setTextViewText(R.id.notify_name, downloadInfo.name);
        Bitmap bitmap = mBitmapHashMap.get(downloadInfo.id);
        if (bitmap == null) {
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
                        mBitmapHashMap.put(downloadInfo.id, bitmap);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            remoteViews.setImageViewBitmap(R.id.iv_icon, bitmap);
        }

        String text = null;
        if (finish || downloadInfo.mCurrentState == AppDownloadManager.STATE_SUCCESS) {
            text = "100%";
            remoteViews.setTextViewText(R.id.cancel, "");
        } else {
            text = getProgressText(downloadInfo);
            remoteViews.setTextViewText(R.id.cancel, "取消");

            Intent intent = new Intent(UIUtils.getContext(), NotificationBroadcastReceiver.class);
            intent.putExtra("action", ACTION_CANCEL);
            intent.putExtra("id", downloadInfo.id);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(UIUtils.getContext(), Integer.parseInt(downloadInfo.id), intent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.cancel, pendingIntent);
        }
        remoteViews.setTextViewText(R.id.notify_progress, text);

        return remoteViews;
    }

    private String getProgressText(DownloadInfo downloadInfo) {
        String downloaded = Formatter.formatFileSize(UIUtils.getContext(), downloadInfo.mDownloadedSize);
        String size = Formatter.formatFileSize(UIUtils.getContext(), downloadInfo.size);
        String text = downloaded + " / " + size + "      " + (int) (downloadInfo.getProgress() * 100) + "%";
        return text;
    }

    public void cancelById(int id) {
        mNotifications.remove(id + "");
        manager.cancel(id);
    }
}

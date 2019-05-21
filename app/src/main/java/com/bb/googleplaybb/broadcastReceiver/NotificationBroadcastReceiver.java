package com.bb.googleplaybb.broadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.bb.googleplaybb.manager.AppDownloadManager;
import com.bb.googleplaybb.manager.NotifycationHelper;

/**
 * Created by Boby on 2019/5/12.
 */

public class NotificationBroadcastReceiver extends BroadcastReceiver {
    private String ACTION = "com.bb.googleplaybb.broadcastReceiver.NotificationBroadcastReceiver";
    private AppDownloadManager mDownloadManager = AppDownloadManager.getDownloadManager();
    private String TAG = "NotificationBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        final NotifycationHelper helper = NotifycationHelper.getInstance();
        final String id = intent.getStringExtra("id");
        if (id != null) {
            int action = intent.getIntExtra("action", 0);
            switch (action) {
                case NotifycationHelper.ACTION_SHOW:
                    helper.showNotification(id);
                    break;
                case NotifycationHelper.ACTION_CANCEL:
                    mDownloadManager.pause(mDownloadManager.getDownloadInfo(id));
                    break;
                case NotifycationHelper.ACTION_FINISHED:
                    helper.showFinishedNotification(id);
                    break;
                case NotifycationHelper.ACTION_UPDATE:
                    helper.updateNotification(id);
                    break;
            }
        }
    }
}

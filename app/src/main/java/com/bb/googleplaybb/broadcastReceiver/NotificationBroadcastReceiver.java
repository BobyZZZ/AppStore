package com.bb.googleplaybb.broadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.bb.googleplaybb.domain.DownloadInfo;
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
        NotifycationHelper helper = NotifycationHelper.getInstance();
        String id = intent.getStringExtra("id");
        DownloadInfo downloadInfo = mDownloadManager.getDownloadInfo(id);
        if (downloadInfo != null) {
            int action = intent.getIntExtra("action", 0);
            switch (action) {
                case NotifycationHelper.ACTION_NOTIFY:
                        helper.notify(downloadInfo);
//                    helper.notify(Integer.parseInt(downloadInfo.id),downloadInfo.getProgress());
                    break;
                case NotifycationHelper.ACTION_CANCEL:
                    helper.cancelById(Integer.parseInt(id));
                    break;
            }
        }
    }
}

package com.bb.googleplaybb.utils;

import android.content.Context;
import android.content.Intent;

import com.bb.googleplaybb.broadcastReceiver.NotificationBroadcastReceiver;

/**
 * Created by Boby on 2019/5/12.
 */

public class NotificationBroadcastHelper {
    public static void send(int actionCode, String id) {
        Context context = UIUtils.getContext();
        Intent intent = new Intent(context, NotificationBroadcastReceiver.class);
        intent.putExtra("action", actionCode);
        intent.putExtra("id", id);
        context.sendBroadcast(intent);
    }
}

package com.selfservit.util;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class mInterfaceReceiver extends BroadcastReceiver {
    public mInterfaceReceiver() {
        //constructor
    }

    @ Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
            Intent serviceIntent = new Intent(context, mInterfaceService.class);
            context.startService(serviceIntent);
            Intent queueIntent = new Intent(context, mQueueService.class);
            context.startService(queueIntent);
            Intent LocationIntent = new Intent(context, mLocationService.class);
            context.startService(LocationIntent);
        }
        if (intent.getAction().equalsIgnoreCase("com.example.checkservice")) {
            if (!isServiceRunning(context) || !isProcessRunning(context)) {
                Intent queueIntent = new Intent(context, mQueueService.class);
                context.startService(queueIntent);
            }
        }
    }
    private boolean isServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (mInterfaceService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }

        return false;
    }
    private boolean isProcessRunning(Context context) {
        ActivityManager manager1 = (ActivityManager)context.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo processInfo : manager1.getRunningAppProcesses()) {
            if (processInfo.processName.equals(context.getPackageName())) {
                return true;
            }
        }
        return false;
    }
}

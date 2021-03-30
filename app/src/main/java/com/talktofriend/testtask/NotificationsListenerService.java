package com.talktofriend.testtask;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.service.notification.StatusBarNotification;

import java.time.LocalDate;
import java.time.LocalTime;

public class NotificationsListenerService extends android.service.notification.NotificationListenerService {

    public String text, appName, ico, date, time;

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn){
        String minutes;
        appName = sbn.getPackageName();
        text = sbn.getNotification().extras.getString("android.text");
        if(LocalTime.now().getMinute()<=9){
            minutes = "0"+ LocalTime.now().getMinute();
        }
        else{
            minutes = String.valueOf(LocalTime.now().getMinute());
        }

        time = LocalTime.now().getHour() + ":" + minutes;
        date = String.valueOf(LocalDate.now().getDayOfMonth() + "." + LocalDate.now().getMonthValue() + "." + LocalDate.now().getYear());

        Intent intent = new  Intent(getApplicationContext().getPackageName());
        intent.putExtra("app", getAppName(appName));
        intent.putExtra("text", text);
        intent.putExtra("time", time);
        intent.putExtra("date", date);

        sendBroadcast(intent);
    }

    private String getAppName(String packageName){
        final PackageManager pm = getApplicationContext().getPackageManager();
        ApplicationInfo ai;
        try { ai = pm.getApplicationInfo(packageName, 0); }
        catch (final PackageManager.NameNotFoundException e) { ai = null; }
        return  (String) (ai != null ? pm.getApplicationLabel(ai) : "(unknown)");
    }


    @Override
    public void onNotificationRemoved(StatusBarNotification sbn){
            StatusBarNotification[] activeNotifications = this.getActiveNotifications();

            if(activeNotifications != null && activeNotifications.length > 0) {
                for (int i = 0; i < activeNotifications.length; i++) {
                        Intent intent = new  Intent("com.github.chagall.notificationlistenerexample");
                        intent.putExtra("app", appName);
                        sendBroadcast(intent);
                        break;
                }
            }
    }

}

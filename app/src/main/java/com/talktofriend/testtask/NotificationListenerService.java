package com.talktofriend.testtask;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * MIT License
 *
 *  Copyright (c) 2016 Fábio Alves Martins Pereira (Chagall)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
public class NotificationListenerService extends android.service.notification.NotificationListenerService {

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

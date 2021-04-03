package com.talktofriend.testtask;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Toast;
import androidx.core.app.NotificationCompat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.talktofriend.testtask.App.CHANNEL_ID;

public class NotificationsListenerService extends android.service.notification.NotificationListenerService {

    private String text, appName, date;
    private String packageName;

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent.getExtras().getString(Constants.NOTIFICATION_SERVICE_FLAG).equals(Constants.START_SERVICE)  ){

            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this,
                    0, notificationIntent, 0);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Notification Listener")
                    .setContentText("Start Service")
                    .setSmallIcon(R.drawable.ic_baseline_notification_important_24)
                    .setContentIntent(pendingIntent)
                    .build();

            startForeground(1, notification);
        }
        else {
            stopForeground(true);
            stopSelfResult(startId);
        }
        return START_STICKY;
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn){
            appName = sbn.getPackageName();
            Bundle extras = sbn.getNotification().extras;
            text = extras.getString("android.text");

            SimpleDateFormat formatter = new SimpleDateFormat(Constants.DATETIME_FORMAT);
            Date dateD = new Date();

            date = formatter.format(dateD);

            Toast.makeText(this, date, Toast.LENGTH_SHORT).show();

            Intent intent = new  Intent(getApplicationContext().getPackageName());
            intent.putExtra(Constants.TITLE, getAppName(appName));
            intent.putExtra(Constants.TEXT, text);
            intent.putExtra(Constants.DATE, date);
            intent.getIntExtra("ico", 0);
            intent.putExtra(Constants.PACKAGE_NAME, packageName);

            sendBroadcast(intent);
    }

    private String getAppName(String packageName){
        final PackageManager pm = getApplicationContext().getPackageManager();
        ApplicationInfo ai;
        try { ai = pm.getApplicationInfo(packageName, 0); }
        catch (final PackageManager.NameNotFoundException e) { ai = null; }
        return  (String) (ai != null ? pm.getApplicationLabel(ai) : "(unknown)");
    }

}

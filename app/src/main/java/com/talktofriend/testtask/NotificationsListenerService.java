package com.talktofriend.testtask;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
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
            if(appName != getApplicationContext().getPackageName()){
                Log.i("Meow", appName);
                Bundle extras = sbn.getNotification().extras;
                text = extras.getString("android.text");

                SimpleDateFormat formatter = new SimpleDateFormat(Constants.DATETIME_FORMAT);
                Date dateD = new Date();

                date = formatter.format(dateD);

                ApplicationInfo applicationInfo = null;
                try {
                    applicationInfo = getApplicationContext().getPackageManager().getApplicationInfo(appName, PackageManager.GET_META_DATA);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                int icon= applicationInfo.icon;
                Log.i("Meow", icon+"");
                Toast.makeText(this, date, Toast.LENGTH_SHORT).show();

                Intent intent = new  Intent(getApplicationContext().getPackageName());
                intent.putExtra(Constants.PACKAGE_NAME, appName);
                intent.putExtra(Constants.TITLE, getAppName(appName));
                intent.putExtra(Constants.TEXT, text);
                intent.putExtra(Constants.DATE, date);
                intent.putExtra(Constants.ICO, icon);

                sendBroadcast(intent);
            }

    }

    private String getAppName(String packageName){
        final PackageManager pm = getApplicationContext().getPackageManager();
        ApplicationInfo ai;
        try { ai = pm.getApplicationInfo(packageName, 0); }
        catch (final PackageManager.NameNotFoundException e) { ai = null; }
        return  (String) (ai != null ? pm.getApplicationLabel(ai) : "(unknown)");
    }


    private Drawable getActivityIcon(
            Context context,
            String packageName, String activityName) {

        PackageManager packageManager = context.getPackageManager();

        Intent intent = new Intent();
        intent.setComponent(new ComponentName(packageName, activityName));
        ResolveInfo resolveInfo = packageManager.resolveActivity(intent, 0);

        return resolveInfo.loadIcon(packageManager);
    }

}

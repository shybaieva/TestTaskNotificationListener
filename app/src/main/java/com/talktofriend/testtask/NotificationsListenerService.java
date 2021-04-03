package com.talktofriend.testtask;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;

import static com.talktofriend.testtask.App.CHANNEL_ID;

public class NotificationsListenerService extends android.service.notification.NotificationListenerService {

    private String text, appName, date, time;
    private int ico;

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
        return START_NOT_STICKY;
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn){
            appName = sbn.getPackageName();
            text = sbn.getNotification().extras.getString("android.text");

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date dateD = new Date();

            date = formatter.format(dateD);

            Toast.makeText(this, date, Toast.LENGTH_SHORT).show();

            ico = sbn.getNotification().extras.getInt(Notification.EXTRA_SMALL_ICON, 0);
            Log.i("Meow", ico + "");

            Intent intent = new  Intent(getApplicationContext().getPackageName());
            intent.putExtra("app", getAppName(appName));
            intent.putExtra("text", text);
            intent.putExtra("date", date);
            intent.putExtra("ico", ico);

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

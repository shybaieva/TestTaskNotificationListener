package com.talktofriend.testtask;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import static com.talktofriend.testtask.Constants.PERMISSION;

public class MainActivity extends AppCompatActivity implements GetFilterChoice {

    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";

    private NotificationReceiver notificationReceiver;
    private AlertDialog enableNotificationListenerAlertDialog;

    private RecyclerView recyclerView;
    private ImageView noNotificationImg;
    private ImageButton startBtn, filterBtn;

    private SharedPreferences sharedPreferences;

    private int filterChoice = 0;

    private boolean isServiceStarted = false, isPermissionGiven = false;

    private String title;
    private String text;
    private int icon;
    private String packageAppName;
    private String date;
    private ArrayList<String> titles, texts, dates, times, packageNames;
    private ArrayList<Integer> icons;
    private DataBaseHelper dataBaseHelper;
    private RecyclerAdapter recyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        checkPermission();

        startBtn.setImageResource(setButtonImg());

        setRecyclerView();
        readNotificationFromDB(filterChoice);

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isServiceStarted){
                    isServiceStarted = true;
                    Intent serviceIntent = new Intent(getApplicationContext(), NotificationsListenerService.class);
                    serviceIntent.putExtra(Constants.NOTIFICATION_SERVICE_FLAG, Constants.START_SERVICE);
                    ContextCompat.startForegroundService(getApplicationContext(), serviceIntent);

                    connectWithNotificationListenerService();

                    startBtn.setImageResource(R.drawable.stop);
                    Toast.makeText(MainActivity.this, "Service started", Toast.LENGTH_SHORT).show();
                }
                else {
                    isServiceStarted = false;
                    connectWithNotificationListenerService();
                    unregisterReceiver(notificationReceiver);
                    startBtn.setImageResource(R.drawable.start);
                    Toast.makeText(MainActivity.this, "Service stopped", Toast.LENGTH_SHORT).show();
                }
            }
        });

        filterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFilterAlertDialog();
            }
        });
    }

    private void init(){
        recyclerView = findViewById(R.id.recycler);
        noNotificationImg = findViewById(R.id.noNotificationImg);
        startBtn = findViewById(R.id.startService);
        filterBtn = findViewById(R.id.filter);

        titles = new ArrayList<>();
        texts = new ArrayList<>();
        icons = new ArrayList<>();
        dates = new ArrayList<>();
        times = new ArrayList<>();
        packageNames = new ArrayList<>();

        dataBaseHelper = new DataBaseHelper(getApplicationContext());
        recyclerAdapter = new RecyclerAdapter(getApplicationContext(), titles, texts, icons, dates, times, packageNames);

        sharedPreferences = getSharedPreferences(PERMISSION, MODE_PRIVATE);
    }

    private void setRecyclerView(){
        recyclerView.setAdapter(recyclerAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
    }

    private void refreshRecyclerView(){
        titles.add(title);
        texts.add(text);
        icons.add(icon);
        packageNames.add(packageAppName);
        String newDate = date.substring(0, date.indexOf(" "));
        String time = date.substring(newDate.length());
        dates.add(newDate); times.add(time);

        recyclerAdapter.notifyDataSetChanged();
        if(noNotificationImg.getVisibility() != View.INVISIBLE)
            noNotificationImg.setVisibility(View.INVISIBLE);
    }

    private void checkPermission(){
        if(!isNotificationServiceEnabled()){
            enableNotificationListenerAlertDialog = buildNotificationServiceAlertDialog();
            enableNotificationListenerAlertDialog.show();
        }
    }

    private void readNotificationFromDB(int filterChoice){
        Cursor cursor = dataBaseHelper.readNotificationsData(filterChoice);
        if(cursor.getCount() == 0 || cursor==null){
            Toast.makeText(this, "NO DATA", Toast.LENGTH_SHORT).show();
        }
        else{
            noNotificationImg.setVisibility(View.INVISIBLE);
            while (cursor.moveToNext()){
                titles.add(cursor.getString(1));
                texts.add(cursor.getString(2));
                packageNames.add(cursor.getString(3));

                String dateFromDb = cursor.getString(4);
                String newDate = dateFromDb.substring(0, dateFromDb.indexOf(" "));
                String time = dateFromDb.substring(newDate.length());

                dates.add(newDate); times.add(time);
            }
        }
    }

    private void saveNotification(){
        dataBaseHelper.addNewNotification(title, text, packageAppName, date);
    }

    private boolean isNotificationServiceEnabled(){
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(),
                ENABLED_NOTIFICATION_LISTENERS);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void onSendFilterChoice(int choice) {
        filterChoice = choice;
        switch (filterChoice){
            case 1: {
                Toast.makeText(this, "Per hour", Toast.LENGTH_SHORT).show();
                break;
            }
            case 2:{
                Toast.makeText(this, "Per day", Toast.LENGTH_SHORT).show();
                break;
            }
            case 3:{
                Toast.makeText(this, "Per month", Toast.LENGTH_SHORT).show();
                break;
            }
            default:{
                Toast.makeText(this, "All", Toast.LENGTH_SHORT).show();
                break;
            }
        }

        titles.clear();texts.clear();icons.clear();dates.clear();times.clear();

        recyclerAdapter.notifyDataSetChanged();
        readNotificationFromDB(choice);
        recyclerAdapter.notifyDataSetChanged();
    }

    private AlertDialog buildNotificationServiceAlertDialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.AlertDialogPermissionTitle);
        alertDialogBuilder.setMessage(R.string.AlertDialogPermissionText);
        alertDialogBuilder.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));
                        isServiceStarted = false;
                        isPermissionGiven = true;

                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean(PERMISSION ,isPermissionGiven);
                        editor.apply();
                    }
                });
        alertDialogBuilder.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                       isServiceStarted =true;
                       isPermissionGiven = false;
                       Toast.makeText(MainActivity.this, "App cannot work without notification permission", Toast.LENGTH_SHORT).show();
                    }
                });
        return(alertDialogBuilder.create());
    }

    private void openFilterAlertDialog(){
        FilterAlertDialog filterAlertDialog = new FilterAlertDialog();
        Bundle args = new Bundle();
        args.putInt(Constants.FILTER, filterChoice);
        filterAlertDialog.setArguments(args);
        filterAlertDialog.show(getSupportFragmentManager(), "Filter");
    }

    private int setButtonImg(){
        sharedPreferences = getSharedPreferences(Constants.SERVICE_STATE, MODE_PRIVATE);
        if(sharedPreferences.getBoolean(Constants.SERVICE_STATE, false) == false){
            return R.drawable.start;
        }
        else return R.drawable.stop;
    }

    private void connectWithNotificationListenerService(){
        if(isServiceStarted){
            notificationReceiver = new NotificationReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(getApplicationContext().getPackageName());
            registerReceiver(notificationReceiver,intentFilter);
        }

        else{
            isServiceStarted = false;
            Intent stopIntent = new Intent(MainActivity.this, NotificationsListenerService.class);
            stopIntent.putExtra(Constants.NOTIFICATION_SERVICE_FLAG,Constants.STOP_SERVICE);
            startForegroundService(stopIntent);
        }
    }

    public class NotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            title = intent.getStringExtra(Constants.TITLE);
            text = intent.getStringExtra(Constants.TEXT);
            date = intent.getStringExtra(Constants.DATE);
            packageAppName = intent.getStringExtra(Constants.PACKAGE_NAME);
            icon = intent.getIntExtra(Constants.ICO, 0);
            saveNotification();
            refreshRecyclerView();
        }
    }
}

package com.talktofriend.testtask;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements GetFilterChoice {

    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";

    private NotificationReceiver notificationReceiver;
    private AlertDialog enableNotificationListenerAlertDialog;

    private RecyclerView recyclerView;
    private ImageView noNotificationImg;
    private ImageButton startBtn, filterBtn;

    private SharedPreferences sharedPreferences;
    private static final String PERMISSION = "permission";

    private int filterChoice = 1;

    private boolean isServiceStarted = false, isPermissionGiven = false;

    private String title, text, icon, date, time;
    private ArrayList<String> titles, texts, icons, dates, times;
    private DataBaseHelper dataBaseHelper;
    private RecyclerAdapter recyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        recyclerView.setAdapter(recyclerAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        readNotificationFromDB(filterChoice);

        isPermissionGiven = sharedPreferences.getBoolean(PERMISSION, false);

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isServiceStarted){
                    isServiceStarted = true;
                    if(isPermissionGiven){
                        notificationReceiver = new NotificationReceiver();
                        IntentFilter intentFilter = new IntentFilter();
                        intentFilter.addAction(getApplicationContext().getPackageName());
                        registerReceiver(notificationReceiver,intentFilter);
                        startBtn.setImageResource(R.drawable.stop);
                        Toast.makeText(MainActivity.this, "Service started", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        checkPermission();
                    }
                }
                else {
                    isServiceStarted = false;
                    if(isPermissionGiven){
                        unregisterReceiver(notificationReceiver);
                        startBtn.setImageResource(R.drawable.start);
                        Toast.makeText(MainActivity.this, "Service stopped", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        checkPermission();
                    }
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

        dataBaseHelper = new DataBaseHelper(getApplicationContext());
        recyclerAdapter = new RecyclerAdapter(getApplicationContext(), titles, texts, icons, dates, times);

        sharedPreferences = getSharedPreferences(PERMISSION, MODE_PRIVATE);
    }

    private void refreshRecyclerView(){
        titles.add(title); texts.add(text); icons.add("icon");  dates.add(date); times.add(time);
        recyclerAdapter.notifyDataSetChanged();
    }

    private void checkPermission(){
        if(!isNotificationServiceEnabled()){
            enableNotificationListenerAlertDialog = buildNotificationServiceAlertDialog();
            enableNotificationListenerAlertDialog.show();
        }
    }

    private void readNotificationFromDB(int filterChoice){
        Cursor cursor = dataBaseHelper.readNotificationsData(filterChoice);
        if(cursor.getCount() == 0){
            Toast.makeText(this, "NO DATA", Toast.LENGTH_SHORT).show();
        }
        else{
            noNotificationImg.setVisibility(View.INVISIBLE);
            while (cursor.moveToNext()){
                titles.add(cursor.getString(1));
                texts.add(cursor.getString(2));
                icons.add(cursor.getString(3));
                dates.add(cursor.getString(4));
                times.add(cursor.getString(5));
            }
        }
    }

    private void saveNotification(){
        dataBaseHelper.addNewNotification(title, text, "ico", date, time);
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
        Toast.makeText(this, choice+"", Toast.LENGTH_SHORT).show();
        filterChoice = choice;
        titles.clear();texts.clear();icons.clear();dates.clear();times.clear();
        recyclerAdapter.notifyDataSetChanged();
        readNotificationFromDB(choice);
        recyclerAdapter.notifyDataSetChanged();
    }

    public class NotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
           title = intent.getStringExtra("app");
           text = intent.getStringExtra("text");
            if(text.length()>20)
                text = text.substring(0, 20);
           time = intent.getStringExtra("time");
           date = intent.getStringExtra("date");
          // icon = intent.getIntExtra("ico");
           saveNotification();
           refreshRecyclerView();
        }
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
        filterAlertDialog.show(getSupportFragmentManager(), "Filter");
    }
}

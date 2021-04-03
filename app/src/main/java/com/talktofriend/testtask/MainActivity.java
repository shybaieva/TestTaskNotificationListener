package com.talktofriend.testtask;

import android.app.ActivityManager;
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
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.icu.util.LocaleData;
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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
    private Drawable ico;
    private String packageName;
    private String date;
    private ArrayList<String> titles, texts, dates, times;
    private ArrayList<Integer> icons;
    private ArrayList<Drawable> icos;
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
       // icos = new ArrayList<>();

        dataBaseHelper = new DataBaseHelper(getApplicationContext());
        recyclerAdapter = new RecyclerAdapter(getApplicationContext(), titles, texts, icons, dates, times);

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
        titles.add(checkStringSize(title));
        texts.add(checkStringSize(text));
        icons.add(icon);

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
                icons.add(cursor.getInt(3));
                String dateFromDb = cursor.getString(4);
                String newDate = dateFromDb.substring(0, dateFromDb.indexOf(" "));
                String time = dateFromDb.substring(newDate.length());
                dates.add(newDate); times.add(time);
            }
        }
    }

    private void saveNotification(){
        dataBaseHelper.addNewNotification(title, text, icon, date);
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
           title = intent.getStringExtra(Constants.TITLE);
           text = intent.getStringExtra(Constants.TEXT);
           date = intent.getStringExtra(Constants.DATE);
           packageName = intent.getStringExtra(Constants.PACKAGE_NAME);

           icon = intent.getIntExtra(Constants.ICO, 0);
           Log.i("Meow", icon + " ICON IN MAIN");
            saveNotification();
           refreshRecyclerView();
           //ico = getAllICONS(packageName);
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

    private String checkStringSize(String str){
        if(str.isEmpty())
            return "";
        if(str.length()>15)
            return str.substring(0, 16);
        else return str;
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
            startService(stopIntent);
        }
    }

//    public Drawable getAllICONS(String packageName) {
//        Drawable ico = null;
//        PackageManager pm = getPackageManager();
//
//        ActivityManager am1 = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
//
//        List<ActivityManager.RunningTaskInfo> processes = am1
//                .getRunningTasks(Integer.MAX_VALUE);
//
//        if (processes != null) {
//            //for (int k = 0; k < 5; k++) {
//                // String pkgName = app.getPackageName();
//
//                Log.e("packageName-->", "" + packageName);
//
//                try {
//                    String pName = (String) pm.getApplicationLabel(pm
//                            .getApplicationInfo(packageName,
//                                    PackageManager.GET_META_DATA));
//                    //name.add("" + pName);
//                    ApplicationInfo a = pm.getApplicationInfo(packageName,
//                            PackageManager.GET_META_DATA);
//
//                    ico = getPackageManager().getApplicationIcon(
//                            processes.topActivity.getPackageName());
//                    getPackageManager();
//                    Log.e("ico-->", "" + ico);
//
//                } catch (PackageManager.NameNotFoundException e) {
//                    Log.e("ERROR", "Unable to find icon for package '"
//                            + packageName + "': " + e.getMessage());
//                }
//                // icons.put(processes.get(k).topActivity.getPackageName(),ico);
//               // icos.add(ico);
//
//            //}
//        }
//        return  ico;
//    }
}

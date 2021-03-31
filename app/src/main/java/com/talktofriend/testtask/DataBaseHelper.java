package com.talktofriend.testtask;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;

public class DataBaseHelper extends SQLiteOpenHelper {

    private Context context;
    private static final String DATABASE_NAME = "Notifications.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "NotificationTable";

    private static final String NOTIFICATION_ID = "_id";
    private static final String NOTIFICATION_TITLE = "title";
    private static final String NOTIFICATION_TEXT = "text";
    private static final String NOTIFICATION_ICON = "icon";
    private static final String NOTIFICATION_DATE ="date";
    private static final String NOTIFICATION_TIME ="time";

    public DataBaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query =
                "CREATE TABLE " + TABLE_NAME + " (" + NOTIFICATION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + NOTIFICATION_TITLE +" TEXT, " + NOTIFICATION_TEXT + " TEXT, " + NOTIFICATION_ICON + " TEXT, "
                + NOTIFICATION_DATE + " DATE, " + NOTIFICATION_TIME + " TIME);";

        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    void addNewNotification(String title, String text, String ico, String date, String time){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(NOTIFICATION_TITLE, title);
        contentValues.put(NOTIFICATION_TEXT, text);
        contentValues.put(NOTIFICATION_ICON, ico);
        contentValues.put(NOTIFICATION_DATE, date);
        contentValues.put(NOTIFICATION_TIME, time);

        long result = db.insert(TABLE_NAME, null, contentValues);
        if(result == -1){
            Toast.makeText(context, "FAILED OPERATION", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(context, "NOTIFICATION ADDED", Toast.LENGTH_SHORT).show();
        }
    }

    Cursor readNotificationsData(int filterChoice){

        String query ;
        switch (filterChoice){
            case 2: {
                int lastHour = LocalTime.now().getHour()-1;
                Toast.makeText(context, lastHour +"", Toast.LENGTH_SHORT).show();
                query = "SELECT * FROM " + TABLE_NAME + " WHERE " + NOTIFICATION_TIME + " >= " + lastHour;
                break;
            }
            case 3: {

                query = "SELECT * FROM " + TABLE_NAME + " WHERE " + NOTIFICATION_DATE + " >= " + LocalDate.now();
                break;
            }
            case 4:{
                query = "SELECT * FROM " + TABLE_NAME + " WHERE " + NOTIFICATION_DATE + " >= " + LocalDate.now().getMonth();
                break;
            }
            default:{
                query = "SELECT * FROM " + TABLE_NAME;
                break;
            }
        }


        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = null;
        if(db!=null){
            cursor = db.rawQuery(query, null);
        }
        return  cursor;
    }
}

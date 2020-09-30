package com.example.courseplanner;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Bundle;

public class NotificationHelper extends ContextWrapper {
    private Context mainContext;
    private PendingIntent pIntent;
    private AlarmManager alarmManager;
    private Intent intent;

    NotificationHelper(Context context){
        super(context);

        mainContext = getApplicationContext();

        intent = new Intent(this, NotificationReceiver.class);

        alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
    }

    public void addNotification(long timeInMillis, int id){
        DBHelper courseDB  = MainActivity.getCourseDB();

        Bundle args = new Bundle();
        args.putString("ID", String.valueOf(id));
        intent.putExtra("ARGS", args);
        pIntent = PendingIntent.getBroadcast(mainContext, id, intent, PendingIntent.FLAG_ONE_SHOT);

        alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pIntent);
    }

    public void cancelNotification(int id){
        pIntent = PendingIntent.getBroadcast(mainContext, id, intent, PendingIntent.FLAG_ONE_SHOT);

        alarmManager.cancel(pIntent);
    }
}

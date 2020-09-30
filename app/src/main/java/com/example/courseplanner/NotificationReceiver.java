package com.example.courseplanner;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationReceiver extends BroadcastReceiver {
    public NotificationReceiver() {}

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle args = intent.getBundleExtra("ARGS");
        DBHelper courseDB = new DBHelper(context);
        Cursor result = courseDB.getAllOfTable(courseDB.getNotificationTableName(), courseDB.getIdPkField(), args.getString("ID"));
        String title;
        String message;

        if(result.moveToFirst()) {
            title = result.getString(result.getColumnIndex(courseDB.getTableNameFkField())) + " Alert";
            if (result.getString(result.getColumnIndex(courseDB.getTableNameFkField())).equals(courseDB.getStartDateField())) { //if this is a alert for the start of a new course
                message = result.getString(result.getColumnIndex(courseDB.getNameField())) + " is scheduled to begin soon.";
            } else if (result.getString(result.getColumnIndex(courseDB.getTableNameFkField())).equals(courseDB.getEndDateField())) { //if this is an alert for the end of a course
                message = result.getString(result.getColumnIndex(courseDB.getNameField())) + " is scheduled to end soon";
            } else if (result.getString(result.getColumnIndex(courseDB.getTableNameFkField())).equals(courseDB.getRequirementTableName())) { //if an assessment is due soon
                message = result.getString(result.getColumnIndex(courseDB.getNameField())) + " is scheduled soon";
            } else { //an unexpected alert is being made
                message = "Error: unexpected alert was made";
            }
        } else { //this alert was not properly erased if this executes
            title = "ERROR";
            message = "Error: unexpected alert attempted to execute";
        }

        NotificationCompat.Builder newNotification = new NotificationCompat.Builder(context, ChannelHandler.CHANNEL_1)
                .setSmallIcon(R.drawable.ic_book_black_24dp)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE);


        Intent notifyIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, result.getInt(result.getColumnIndex(courseDB.getIdPkField())), notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        //to be able to launch your activity from the notification
        newNotification.setContentIntent(pendingIntent);
        Notification notificationCompat = newNotification.build();
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(context);

        managerCompat.notify(result.getInt(result.getColumnIndex(courseDB.getIdPkField())), notificationCompat);
    }
}

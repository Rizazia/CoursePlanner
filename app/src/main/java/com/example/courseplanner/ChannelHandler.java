package com.example.courseplanner;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class ChannelHandler extends Application {
    public static final String CHANNEL_1 = "channel1";
    @Override
    public void onCreate(){
        super.onCreate();
        createNotificationChannels();
    }

    private void createNotificationChannels(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_1,
                    "Channel 1",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Display alerts created about coming events");

            NotificationManager noteManager = getSystemService(NotificationManager.class);
            noteManager.createNotificationChannel(channel);
        }
    }
}

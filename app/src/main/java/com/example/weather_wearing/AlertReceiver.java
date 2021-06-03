package com.example.weather_wearing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import androidx.core.app.NotificationCompat;

public class AlertReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        String type = intent.getStringExtra("type");
        if(type.equals("1")){
            Notification1(context);
        } else if(type.equals("2")){
            Notification2(context);
        }
    }

    public void Notification1(Context context){
        NotificationHelper notificationHelper = new NotificationHelper(context);
        String temp = "現在溫度：" + NotificationHelper.temp_Notification;
        String temp_high = "最高溫：" + NotificationHelper.temp_high_Notification;
        String temp_low = "最低溫：" + NotificationHelper.temp_low_Notification;
        String feeling = "體感溫度：" + NotificationHelper.feeling_Notification;
        Bitmap picture = NotificationHelper.bitmap_Notification;
        String sun = NotificationHelper.sun_Notification;
        String rain = NotificationHelper.rain_Notification;
        String mask = NotificationHelper.mask_Notification;

        NotificationCompat.Builder nb = notificationHelper
                .getChannel_1_Notification("Weather & Wearing",temp , temp , temp_high , temp_low , feeling, picture, sun, rain, mask);
        notificationHelper.getManager().notify(1, nb.build());
    }

    public void Notification2(Context context){
        NotificationHelper notificationHelper = new NotificationHelper(context);
        NotificationCompat.Builder nb2 = notificationHelper
                .getChannel_2_Notification("Weather & Wearing");
        notificationHelper.getManager().notify(2, nb2.build());
    }
}
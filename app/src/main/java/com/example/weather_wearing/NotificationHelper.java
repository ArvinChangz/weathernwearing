package com.example.weather_wearing;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class NotificationHelper extends ContextWrapper {
    public static final String channel_1_ID = "channel1ID";
    public static final String channel_1_Name = "channel 1";
    public static final String channel_2_ID = "channel2ID";
    public static final String channel_2_Name = "channel 2";
    public static String temp_Notification;
    public static String temp_high_Notification;
    public static String temp_low_Notification;
    public static String feeling_Notification;
    public static Bitmap bitmap_Notification;
    public static String sun_Notification;
    public static String rain_Notification;
    public static String mask_Notification;
    private NotificationManager mManager;
    public static boolean rainNow_Notification = false;
    public static String ifRain_Notification;
    public static int rain_Future;

    public NotificationHelper(Context base) {
        super(base);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannels();
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    public void createChannels() {
        NotificationChannel channel_1 = new NotificationChannel(channel_1_ID, channel_1_Name, NotificationManager.IMPORTANCE_DEFAULT);
        channel_1.enableLights(true);
        channel_1.enableVibration(true);
        channel_1.setLightColor(R.color.colorPrimary);
        channel_1.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        getManager().createNotificationChannel(channel_1);

        NotificationChannel channel_2 = new NotificationChannel(channel_2_ID, channel_2_Name, NotificationManager.IMPORTANCE_DEFAULT);
        channel_2.enableLights(true);
        channel_2.enableVibration(true);
        channel_2.setLightColor(R.color.colorPrimary);
        channel_2.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        getManager().createNotificationChannel(channel_2);
    }

    public NotificationManager getManager() {
        if (mManager == null) {
            mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mManager;
    }

    public NotificationCompat.Builder getChannel_1_Notification(String title, String message, String string1, String string2, String string3, String string4, Bitmap b, String string5, String string6, String string7) {
        Intent NotificationIntent = new Intent(this, MainActivity.class);
        PendingIntent NotificationPendingIntent = PendingIntent.getActivity(this, 0, NotificationIntent, 0);

        if(temp_Notification == null){
            return new NotificationCompat.Builder(getApplicationContext(), channel_1_ID)
                    .setContentText("請點擊通知開啟 Weather & wearing")
                    .setSmallIcon(R.drawable.notification)
                    .setContentIntent(NotificationPendingIntent);
        } else {
            return new NotificationCompat.Builder(getApplicationContext(), channel_1_ID)
                    .setContentText(message)
                    .setStyle(new NotificationCompat.InboxStyle()
                            .addLine(string1)
                            .addLine(string2)
                            .addLine(string3)
                            .addLine(string4)
                            .addLine(string5)
                            .addLine(string6)
                            .addLine(string7)
                            .setSummaryText("每日天氣小提醒"))
                    .setLargeIcon(b)
                    .setSmallIcon(R.drawable.notification)
                    .setContentIntent(NotificationPendingIntent);
        }
    }

    public NotificationCompat.Builder getChannel_2_Notification(String title) {
        Intent NotificationIntent = new Intent(this, MainActivity.class);
        PendingIntent NotificationPendingIntent = PendingIntent.getActivity(this, 0, NotificationIntent, 0);

        return new NotificationCompat.Builder(getApplicationContext(), channel_2_ID)
                .setContentText("未來三小時內降雨機率大於40%")
                .setSmallIcon(R.drawable.notification)
                .setContentIntent(NotificationPendingIntent);
    }
}
package com.ignacio.partykneadsapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import android.graphics.Color;

public class OrderNotificationReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "order_notifications";
    private static final int NOTIFICATION_ID = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        makeNotification(context);
    }

    private void makeNotification(Context context) {
        Log.d("OrderNotificationReceiver", "Making notification...");

        // Notification channel ID
        String channelID = "NEW_ORDER";

        // Use a drawable resource for the small icon
        int smallIcon = R.drawable.add;  // Replace with your actual drawable resource

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelID)
                .setContentTitle("NEW ORDER")
                .setContentText("A customer has placed a new order with you! Check the order items and get them ready to go!\n")
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSmallIcon(smallIcon);  // Set the drawable resource for the icon

        // Get the notification manager
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create the notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = notificationManager.getNotificationChannel(channelID);
            if (notificationChannel == null) {
                int importance = NotificationManager.IMPORTANCE_HIGH;
                notificationChannel = new NotificationChannel(channelID, "New Order Notifications", importance);
                notificationChannel.setDescription("Notifications for new orders");
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(Color.GREEN);
                notificationChannel.enableVibration(true);
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        // Send the notification
        notificationManager.notify(NOTIFICATION_ID, builder.build());
        Log.d("OrderNotificationReceiver", "Notification sent.");
    }
}
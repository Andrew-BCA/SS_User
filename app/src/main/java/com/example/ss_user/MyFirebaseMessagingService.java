package com.example.ss_user;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Check if the message contains notification payload
        if (remoteMessage.getData().size() > 0) {
            // Extract data from the message
            String title = remoteMessage.getData().get("title");
            String body = remoteMessage.getData().get("body");

            // Show notification
            sendNotification(title, body);
        }
    }

    // Helper method to display a notification
    private void sendNotification(String title, String body) {
        Intent intent = new Intent(this, MainActivity.class); // Intent to open main screen or specific screen
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(this, "default_channel")
                .setSmallIcon(R.drawable.ss_logo)  // Set your notification icon
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);
    }
}

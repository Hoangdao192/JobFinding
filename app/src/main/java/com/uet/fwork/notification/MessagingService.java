package com.uet.fwork.notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.uet.fwork.MainActivity;
import com.uet.fwork.R;
import com.uet.fwork.chat.ChatActivity;
import com.uet.fwork.database.model.UserDeviceModel;
import com.uet.fwork.database.repository.UserDeviceRepository;

import java.util.Map;

public class MessagingService extends FirebaseMessagingService {
    private UserDeviceRepository repository = null;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        sendNotification(message.getData());
    }

    private void sendNotification(Map data) {
        try {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "Message")
                    .setSmallIcon(R.drawable.image_icon)
                    .setContentTitle((CharSequence) data.get("title"))
                    .setContentText((String) data.get("messageContent"))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    // Set the intent that will fire when the user taps the notification
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);
            NotificationManager notificationManager =  (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
//            if (ChatActivity.isActivityVisible()) {
//                if (ChatActivity.currentChatChanelId.equals((String) data.get("chanelId"))) {
//                    notificationManager.notify(0, builder.build());
//                }
//            } else {
//                notificationManager.notify(0, builder.build());
//            }
            notificationManager.notify(0, builder.build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }
}

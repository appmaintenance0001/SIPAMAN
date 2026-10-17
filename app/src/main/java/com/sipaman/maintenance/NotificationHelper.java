package com.sipaman.maintenance;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationHelper {

    private static final String CHANNEL_ID = "TASK_CHANNEL_V3";

    public static void showNotification(
            Context context,
            String title,
            String message,
            String taskId
    ) {
        if (context == null) return;

        // Buat channel notifikasi jika belum tersedia
        createNotificationChannel(context);

        String safeTaskId =
                taskId == null || taskId.trim().isEmpty()
                        ? "notification"
                        : taskId;

        // Buka DashboardActivity ketika notifikasi diketuk
        Intent intent = new Intent(context, DashboardActivity.class);
        intent.putExtra("taskId", safeTaskId);
        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                safeTaskId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
                        | PendingIntent.FLAG_IMMUTABLE
        );

        // Susun notifikasi
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setStyle(
                                new NotificationCompat.BigTextStyle()
                                        .bigText(message)
                        )
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setDefaults(NotificationCompat.DEFAULT_ALL);

        // Periksa izin notifikasi Android 13 ke atas
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(
                    android.Manifest.permission.POST_NOTIFICATIONS
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager =
                    context.getSystemService(NotificationManager.class);

            if (manager != null) {
                NotificationChannel channel =
                        manager.getNotificationChannel(CHANNEL_ID);

                if (channel != null) {
                    android.util.Log.d(
                            "SIPAMAN_NOTIF",
                            "Channel ID: " + channel.getId()
                                    + ", Importance: " + channel.getImportance()
                                    + ", Sound: " + channel.getSound()
                                    + ", Vibration: " + channel.shouldVibrate()
                    );
                }
            }
        }



        // Tampilkan notifikasi
        NotificationManagerCompat.from(context)
                .notify(safeTaskId.hashCode(), builder.build());
    }

    private static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationManager manager =
                context.getSystemService(NotificationManager.class);

        if (manager == null) return;

        // Jangan menimpa pengaturan channel yang sudah ada
        if (manager.getNotificationChannel(CHANNEL_ID) != null) {
            return;
        }

        // Pengaturan suara notifikasi
        AudioAttributes audioAttributes =
                new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(
                                AudioAttributes.CONTENT_TYPE_SONIFICATION
                        )
                        .build();

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Notifikasi Task SI-PAMAN",
                NotificationManager.IMPORTANCE_HIGH
        );

        channel.setDescription(
                "Pemberitahuan task dan aktivitas SI-PAMAN"
        );

        channel.enableVibration(true);

        channel.setSound(
                RingtoneManager.getDefaultUri(
                        RingtoneManager.TYPE_NOTIFICATION
                ),
                audioAttributes
        );

        manager.createNotificationChannel(channel);
    }
}
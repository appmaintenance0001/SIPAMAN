package com.sipaman.maintenance;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class TaskReminderWorker extends Worker {

    public TaskReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {

        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        // 🔥 hanya jalan jam 08:00 - 09:00
        if (hour < 8 || hour >= 9) {
            return Result.success();
        }

        DatabaseReference database = FirebaseDatabase.getInstance().getReference("tasks");

        database.get().addOnCompleteListener(task -> {

            if (!task.isSuccessful()) return;

            DataSnapshot snapshot = task.getResult();

            int overdueCount = 0;
            int todayCount = 0;

            SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
            Date today = new Date();

            for (DataSnapshot data : snapshot.getChildren()) {

                Task t = data.getValue(Task.class);

                if (t == null) continue;

                if ("DONE".equals(t.getStatus())) continue;

                try {
                    Date dueDate = sdf.parse(t.getDue());

                    if (dueDate != null) {

                        if (sdf.format(today).equals(sdf.format(dueDate))) {
                            todayCount++;
                        }

                        if (today.after(dueDate)) {
                            overdueCount++;
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // 🔔 kirim notif summary
            if (overdueCount > 0 || todayCount > 0) {

                StringBuilder msg = new StringBuilder();

                if (overdueCount > 0) {
                    msg.append("🚨 ").append(overdueCount).append(" overdue\n");
                }

                if (todayCount > 0) {
                    msg.append("⚠️ ").append(todayCount).append(" hari ini");
                }

                NotificationHelper.showNotification(
                        getApplicationContext(),
                        "Reminder Task 📊",
                        msg.toString(),
                        "worker"
                );
            }

        });

        // 🔥 WAJIB tetap return di sini
        return Result.success();
    }
}
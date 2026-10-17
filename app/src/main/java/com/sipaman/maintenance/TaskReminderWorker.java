package com.sipaman.maintenance;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TaskReminderWorker extends Worker {

    private static final String TAG = "SIPAMAN_REMINDER";

    public TaskReminderWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params
    ) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {

        try {
            DatabaseReference database =
                    FirebaseDatabase.getInstance()
                            .getReference("tasks");

            DataSnapshot snapshot =
                    Tasks.await(database.get(), 20, TimeUnit.SECONDS);

            int overdueCount = 0;
            int todayCount = 0;

            SimpleDateFormat sdf =
                    new SimpleDateFormat("d/M/yyyy", Locale.getDefault());

            sdf.setLenient(false);

            Date today = new Date();
            String todayString = sdf.format(today);

            for (DataSnapshot data : snapshot.getChildren()) {

                Task task = data.getValue(Task.class);

                if (task == null) continue;

                if ("DONE".equalsIgnoreCase(task.getStatus())) {
                    continue;
                }

                String due = task.getDue();

                if (due == null || due.trim().isEmpty()) {
                    continue;
                }

                try {
                    Date dueDate = sdf.parse(due.trim());

                    if (dueDate == null) continue;

                    if (todayString.equals(sdf.format(dueDate))) {
                        todayCount++;
                    }

                    if (dueDate.before(sdf.parse(todayString))) {
                        overdueCount++;
                    }

                } catch (Exception dateError) {
                    Log.w(TAG, "Format due date tidak valid: " + due,
                            dateError);
                }
            }

            Log.d(TAG, "Tanggal hari ini: " + todayString);
            Log.d(TAG, "Jumlah task overdue: " + overdueCount);
            Log.d(TAG, "Jumlah task jatuh tempo hari ini: " + todayCount);

            StringBuilder message = new StringBuilder();

            if (overdueCount > 0) {
                message.append("🚨 ")
                        .append(overdueCount)
                        .append(" task overdue");
            }

            if (todayCount > 0) {
                if (message.length() > 0) {
                    message.append("\n");
                }

                message.append("⚠️ ")
                        .append(todayCount)
                        .append(" task jatuh tempo hari ini");
            }

            if (message.length() > 0) {
                NotificationHelper.showNotification(
                        getApplicationContext(),
                        "Reminder Task SI-PAMAN",
                        message.toString(),
                        "worker_daily"
                );
            }

            Log.d(TAG, "Pemeriksaan task selesai.");

            return Result.success();

        } catch (Exception e) {
            Log.e(TAG, "Gagal memeriksa task Firebase.", e);
            return Result.retry();
        }
    }
}
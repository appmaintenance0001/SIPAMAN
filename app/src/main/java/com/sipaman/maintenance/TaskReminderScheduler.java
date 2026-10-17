package com.sipaman.maintenance;

import android.content.Context;
import android.util.Log;
import androidx.work.ExistingPeriodicWorkPolicy;

import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;



public final class TaskReminderScheduler {

    private static final String TAG = "SIPAMAN_SCHEDULER";

    private static final String DAILY_WORK_NAME =
            "SI_PAMAN_DAILY_TASK_REMINDER_V2";

    private TaskReminderScheduler() {
    }

    public static void scheduleDaily(Context context) {
        Context appContext = context.getApplicationContext();

        Calendar nextRun = Calendar.getInstance();
        nextRun.set(Calendar.HOUR_OF_DAY, 8);
        nextRun.set(Calendar.MINUTE, 0);
        nextRun.set(Calendar.SECOND, 0);
        nextRun.set(Calendar.MILLISECOND, 0);

        if (nextRun.getTimeInMillis() <= System.currentTimeMillis()) {
            nextRun.add(Calendar.DAY_OF_YEAR, 1);
        }

        long delay = nextRun.getTimeInMillis()
                - System.currentTimeMillis();

        PeriodicWorkRequest request =
                new PeriodicWorkRequest.Builder(
                        TaskReminderWorker.class,
                        24,
                        TimeUnit.HOURS,
                        15,
                        TimeUnit.MINUTES
                )
                        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                        .build();

        WorkManager.getInstance(appContext)
                .cancelUniqueWork("SI_PAMAN_DAILY_TASK_REMINDER");

        WorkManager.getInstance(appContext)
                .enqueueUniquePeriodicWork(
                        DAILY_WORK_NAME,
                        ExistingPeriodicWorkPolicy.KEEP,
                        request
                );

        Log.d(TAG, "Permintaan reminder periodik dikirim.");
    }

}
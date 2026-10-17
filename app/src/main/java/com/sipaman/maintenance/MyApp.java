package com.sipaman.maintenance;

import android.app.Application;

import com.cloudinary.android.MediaManager;

import java.util.HashMap;
import java.util.Map;

public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Konfigurasi Cloudinary tetap dipertahankan.
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "sipaman");
        config.put("secure", "true");

        try {
            MediaManager.init(this, config);
        } catch (Exception e) {
            // Aman jika Cloudinary sudah diinisialisasi.
        }

        // Jadwalkan reminder otomatis SI-PAMAN.
        TaskReminderScheduler.scheduleDaily(this);
    }
}
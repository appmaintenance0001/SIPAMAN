package com.sipaman.maintenance;

import android.app.Application;

import com.cloudinary.android.MediaManager;

import java.util.HashMap;
import java.util.Map;

public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // 🔥 CONFIG CLOUDINARY (UNSIGNED MODE)
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "sipaman"); // ⚠️ HARUS SESUAI CLOUDINARY
        config.put("secure", "true");

        MediaManager.init(this, config);
    }
}
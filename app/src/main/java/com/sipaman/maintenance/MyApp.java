package com.sipaman.maintenance;

import android.app.Application;
import android.util.Log;

import com.cloudinary.android.MediaManager;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory;

import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class MyApp extends Application {

    private static final String TAG = "SIPAMAN_AI";
    private static final String AI_APP_NAME = "sipaman-ai-assistant";

    private FirebaseApp aiFirebaseApp;

    public FirebaseApp getAiFirebaseApp() {
        return aiFirebaseApp;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Debug App Check untuk Firebase utama SI-PAMAN.
        if (BuildConfig.DEBUG) {
            try {
                FirebaseAppCheck.getInstance()
                        .installAppCheckProviderFactory(
                                DebugAppCheckProviderFactory.getInstance()
                        );

                Log.i(TAG,
                        "Debug App Check dipasang pada Firebase default.");
            } catch (Exception e) {
                Log.e(TAG,
                        "Gagal memasang Debug App Check pada Firebase default.",
                        e);
            }
        }

        // Konfigurasi Cloudinary tetap dipertahankan.
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "sipaman");
        config.put("secure", "true");

        try {
            MediaManager.init(this, config);
        } catch (Exception e) {
            Log.w(TAG, "Cloudinary sudah diinisialisasi.", e);
        }

        // Pertahankan inisialisasi Firebase sekunder.
        initializeAiFirebase();

        // Jadwalkan reminder otomatis SI-PAMAN.
        TaskReminderScheduler.scheduleDaily(this);
    }

    private void initializeAiFirebase() {
        try {
            FirebaseApp existingApp = null;

            for (FirebaseApp app : FirebaseApp.getApps(this)) {
                if (AI_APP_NAME.equals(app.getName())) {
                    existingApp = app;
                    break;
                }
            }

            if (existingApp != null) {
                aiFirebaseApp = existingApp;

                // Pastikan Debug App Check juga terpasang pada
                // Firebase sekunder untuk kebutuhan pengujian.
                if (BuildConfig.DEBUG) {
                    FirebaseAppCheck.getInstance(aiFirebaseApp)
                            .installAppCheckProviderFactory(
                                    DebugAppCheckProviderFactory.getInstance()
                            );
                }

                Log.i(TAG,
                        "Firebase AI sekunder sudah diinisialisasi.");
                return;
            }

            JSONObject json;

            try (InputStream input = getAssets().open(
                    "firebase-ai/google-services.json")) {

                byte[] bytes = new byte[input.available()];
                int offset = 0;

                while (offset < bytes.length) {
                    int count = input.read(
                            bytes,
                            offset,
                            bytes.length - offset
                    );

                    if (count < 0) {
                        break;
                    }

                    offset += count;
                }

                json = new JSONObject(
                        new String(
                                bytes,
                                0,
                                offset,
                                StandardCharsets.UTF_8
                        )
                );
            }

            JSONObject projectInfo = json.getJSONObject("project_info");

            JSONObject client = json.getJSONArray("client")
                    .getJSONObject(0);

            JSONObject clientInfo = client.getJSONObject("client_info");

            JSONObject apiKey = client.getJSONArray("api_key")
                    .getJSONObject(0);

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setApplicationId(
                            clientInfo.getString("mobilesdk_app_id"))
                    .setApiKey(
                            apiKey.getString("current_key"))
                    .setProjectId(
                            projectInfo.getString("project_id"))
                    .setGcmSenderId(
                            projectInfo.getString("project_number"))
                    .build();

            Log.i(TAG,
                    "AI Project ID: " + options.getProjectId());

            Log.i(TAG,
                    "AI App ID: " + options.getApplicationId());

            aiFirebaseApp = FirebaseApp.initializeApp(
                    this,
                    options,
                    AI_APP_NAME
            );

            if (aiFirebaseApp == null) {
                throw new IllegalStateException(
                        "Firebase AI sekunder gagal diinisialisasi."
                );
            }

            if (BuildConfig.DEBUG) {
                FirebaseAppCheck.getInstance(aiFirebaseApp)
                        .installAppCheckProviderFactory(
                                DebugAppCheckProviderFactory.getInstance()
                        );
            }

            Log.i(TAG,
                    "Firebase AI sekunder berhasil diinisialisasi.");

        } catch (Exception e) {
            aiFirebaseApp = null;

            Log.e(TAG,
                    "Inisialisasi Firebase AI sekunder gagal.",
                    e);
        }
    }
}
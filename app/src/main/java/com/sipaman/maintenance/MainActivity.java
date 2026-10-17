package com.sipaman.maintenance;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {

    private MaterialButton btnMasuk;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SplashScreen.installSplashScreen(this); // WAJIB

        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_main); // ✅ HANYA SEKALI

        if (android.os.Build.VERSION.SDK_INT >= 33) {

            requestPermissions(
                    new String[]{
                            android.Manifest.permission.POST_NOTIFICATIONS
                    },
                    100);

        }

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {

                    if (!task.isSuccessful()) return;

                    String token = task.getResult();

                    Log.d("FCM TOKEN", token);

                });

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            checkLogin(user);
        }

        btnMasuk = findViewById(R.id.btnMasuk);

        btnMasuk.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        });

    }
    private void checkLogin(FirebaseUser user) {

        user.reload().addOnCompleteListener(task -> {

            if (!task.isSuccessful()) {

                FirebaseAuth.getInstance().signOut();
                return;
            }

            if (!user.isEmailVerified()) {

                FirebaseAuth.getInstance().signOut();

                return;
            }

            startActivity(new Intent(
                    MainActivity.this,
                    DashboardActivity.class));

            finish();

        });

    }
}
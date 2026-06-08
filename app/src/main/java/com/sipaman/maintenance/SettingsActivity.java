package com.sipaman.maintenance;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;


public class SettingsActivity extends AppCompatActivity {

    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        btnLogout = findViewById(R.id.btnLogout);

        btnLogout.setOnClickListener(v -> {

            new MaterialAlertDialogBuilder(this)
                    .setTitle("Logout")
                    .setMessage("Yakin ingin keluar dari aplikasi?")

                    .setPositiveButton("Logout", (dialog, which) -> {
                        FirebaseAuth.getInstance().signOut();

                        startActivity(new Intent(this, LoginActivity.class));
                        finish();
                    })

                    .setNegativeButton("Batal", (dialog, which) -> {
                        dialog.dismiss();
                    })

                    .show();
        });
    }
}
package com.sipaman.maintenance;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    private MaterialButton btnMasuk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Full screen modern layout
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_main);

        // Inisialisasi button
        btnMasuk = findViewById(R.id.btnMasuk);

        // Klik → pindah ke LoginActivity
        btnMasuk.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }
}
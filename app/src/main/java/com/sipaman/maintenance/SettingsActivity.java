package com.sipaman.maintenance;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;

public class SettingsActivity extends AppCompatActivity {

    private static final int REQ_POST_NOTIFICATIONS = 301;

    private TextView tvUserName, tvUserEmail, tvUserRole, tvNotificationStatus, tvAppVersion;
    private SwitchMaterial switchNotification;
    private MaterialButton btnResetPassword, btnClearCache, btnAbout, btnLogout;

    private FirebaseAuth auth;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        auth = FirebaseAuth.getInstance();
        prefs = getSharedPreferences("settings_prefs", MODE_PRIVATE);

        bindViews();
        loadUserInfo();
        setupNotificationSwitch();
        setupActions();
        showAppVersion();
    }

    private void bindViews() {
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvUserRole = findViewById(R.id.tvUserRole);
        tvNotificationStatus = findViewById(R.id.tvNotificationStatus);
        tvAppVersion = findViewById(R.id.tvAppVersion);

        switchNotification = findViewById(R.id.switchNotification);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        btnClearCache = findViewById(R.id.btnClearCache);
        btnAbout = findViewById(R.id.btnAbout);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void loadUserInfo() {
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            tvUserName.setText("Belum login");
            tvUserEmail.setText("-");
            tvUserRole.setText("-");
            return;
        }

        tvUserEmail.setText(user.getEmail() == null ? "-" : user.getEmail());

        FirebaseDatabase.getInstance()
                .getReference("users")
                .child(user.getUid())
                .get()
                .addOnSuccessListener(snapshot -> {
                    String name = snapshot.child("name").getValue(String.class);
                    String role = snapshot.child("role").getValue(String.class);

                    tvUserName.setText(isEmpty(name) ? "Pengguna SI-PAMAN" : name);
                    tvUserRole.setText(isEmpty(role) ? "Teknisi" : role);
                })
                .addOnFailureListener(e -> {
                    tvUserName.setText("Pengguna SI-PAMAN");
                    tvUserRole.setText("Teknisi");
                });
    }

    private void setupNotificationSwitch() {
        boolean enabled = prefs.getBoolean("task_notifications_enabled", true);

        if (!hasNotificationPermission()) {
            enabled = false;
        }

        switchNotification.setChecked(enabled);
        updateNotificationStatus(enabled);

        switchNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && !hasNotificationPermission()) {
                requestNotificationPermission();
                return;
            }

            prefs.edit()
                    .putBoolean("task_notifications_enabled", isChecked)
                    .apply();

            updateNotificationStatus(isChecked);
        });
    }

    private void setupActions() {
        btnResetPassword.setOnClickListener(v -> sendResetPasswordEmail());
        btnClearCache.setOnClickListener(v -> confirmClearCache());
        btnAbout.setOnClickListener(v -> showAboutDialog());
        btnLogout.setOnClickListener(v -> confirmLogout());
    }

    private void sendResetPasswordEmail() {
        FirebaseUser user = auth.getCurrentUser();

        if (user == null || isEmpty(user.getEmail())) {
            Toast.makeText(this, "Email user tidak ditemukan", Toast.LENGTH_SHORT).show();
            return;
        }

        btnResetPassword.setEnabled(false);

        auth.sendPasswordResetEmail(user.getEmail())
                .addOnCompleteListener(task -> {
                    btnResetPassword.setEnabled(true);

                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Email reset password telah dikirim", Toast.LENGTH_LONG).show();
                    } else {
                        String message = task.getException() == null
                                ? "Gagal mengirim email reset password"
                                : task.getException().getMessage();
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void confirmClearCache() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Bersihkan Cache")
                .setMessage("Cache foto sementara akan dihapus dari perangkat ini.")
                .setPositiveButton("Bersihkan", (dialog, which) -> {
                    long deleted = clearCacheDir(getCacheDir());
                    Toast.makeText(this, "Cache dibersihkan: " + deleted + " file", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private long clearCacheDir(File file) {
        if (file == null || !file.exists()) return 0;

        long count = 0;

        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    count += clearCacheDir(child);
                }
            }
        }

        if (!file.equals(getCacheDir()) && file.delete()) {
            count++;
        }

        return count;
    }

    private void showAboutDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Tentang SI-PAMAN")
                .setMessage("SI-PAMAN membantu monitoring task maintenance, dokumentasi foto before-after, dan status pekerjaan teknisi.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void confirmLogout() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Logout")
                .setMessage("Yakin ingin keluar dari aplikasi?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    auth.signOut();

                    Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void showAppVersion() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            tvAppVersion.setText("Versi " + info.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            tvAppVersion.setText("Versi 1.0");
        }
    }

    private boolean hasNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true;
        }

        return ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    REQ_POST_NOTIFICATIONS
            );
        }
    }

    private void updateNotificationStatus(boolean enabled) {
        tvNotificationStatus.setText(enabled
                ? "Status notifikasi aktif"
                : "Status notifikasi nonaktif");
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQ_POST_NOTIFICATIONS) {
            boolean granted = grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED;

            switchNotification.setChecked(granted);
            prefs.edit()
                    .putBoolean("task_notifications_enabled", granted)
                    .apply();
            updateNotificationStatus(granted);

            Toast.makeText(
                    this,
                    granted ? "Notifikasi diaktifkan" : "Izin notifikasi ditolak",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}

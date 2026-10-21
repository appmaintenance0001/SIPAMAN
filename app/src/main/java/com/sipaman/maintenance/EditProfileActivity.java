package com.sipaman.maintenance;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView btnBack;
    private ImageView imgProfile;
    private TextInputEditText edtName;
    private TextInputEditText edtPhone;
    private TextInputEditText edtSite;
    private MaterialButton btnChangePhoto;
    private MaterialButton btnSave;
    private View progressOverlay;

    private DatabaseReference userRef;
    private FirebaseUser currentUser;
    private Uri selectedPhotoUri;
    private String currentPhotoUrl = "";
    private boolean isSaving = false;

    private final ExecutorService imageExecutor = Executors.newSingleThreadExecutor();

    private final ActivityResultLauncher<String> photoPicker =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri == null) return;
                selectedPhotoUri = uri;
                imgProfile.setImageURI(uri);
                Toast.makeText(this, "Foto dipilih. Tekan Simpan Perubahan untuk mengunggah.", Toast.LENGTH_LONG).show();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        btnBack = findViewById(R.id.btnBack);
        imgProfile = findViewById(R.id.imgProfile);
        edtName = findViewById(R.id.edtName);
        edtPhone = findViewById(R.id.edtPhone);
        edtSite = findViewById(R.id.edtSite);
        btnChangePhoto = findViewById(R.id.btnChangePhoto);
        btnSave = findViewById(R.id.btnSave);
        progressOverlay = findViewById(R.id.profileProgress);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Sesi login tidak ditemukan. Silakan login kembali.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(currentUser.getUid());

        btnBack.setOnClickListener(v -> finish());
        btnChangePhoto.setOnClickListener(v -> photoPicker.launch("image/*"));
        btnSave.setOnClickListener(v -> saveProfile());

        loadData();
    }

    private void loadData() {
        setBusy(true, "Memuat profil...");

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isFinishing() || isDestroyed()) return;

                edtName.setText(readString(snapshot, "name"));
                edtPhone.setText(readString(snapshot, "phone"));
                edtSite.setText(readString(snapshot, "site"));

                currentPhotoUrl = readString(snapshot, "photoUrl");
                if (currentPhotoUrl.isEmpty()) currentPhotoUrl = readString(snapshot, "profileImageUrl");
                if (currentPhotoUrl.isEmpty()) currentPhotoUrl = readString(snapshot, "imageUrl");

                if (!currentPhotoUrl.isEmpty()) {
                    loadProfileImage(currentPhotoUrl);
                }

                setBusy(false, null);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (isFinishing() || isDestroyed()) return;
                setBusy(false, null);
                Toast.makeText(EditProfileActivity.this,
                        "Gagal memuat profil: " + error.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private String readString(DataSnapshot snapshot, String key) {
        String value = snapshot.child(key).getValue(String.class);
        return value == null ? "" : value;
    }

    private void saveProfile() {
        if (isSaving) return;

        String name = textOf(edtName);
        String phone = textOf(edtPhone);
        String site = textOf(edtSite);

        if (name.isEmpty()) {
            edtName.setError("Nama wajib diisi");
            edtName.requestFocus();
            return;
        }

        if (currentUser == null || userRef == null) {
            Toast.makeText(this, "User belum login", Toast.LENGTH_SHORT).show();
            return;
        }

        setBusy(true, selectedPhotoUri != null ? "Mengunggah foto profil..." : "Menyimpan profil...");

        if (selectedPhotoUri != null) {
            uploadProfilePhoto(selectedPhotoUri, name, phone, site);
        } else {
            writeProfile(name, phone, site, currentPhotoUrl);
        }
    }

    private String textOf(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private void uploadProfilePhoto(Uri photoUri, String name, String phone, String site) {
        try {
            MediaManager.get()
                    .upload(photoUri)
                    .unsigned("sipaman_preset")
                    .option("folder", "sipaman/profiles")
                    .option("resource_type", "image")
                    .callback(new UploadCallback() {
                        @Override
                        public void onStart(String requestId) {
                            runOnUiThread(() -> {
                                if (!isFinishing() && !isDestroyed()) {
                                    btnSave.setText("Mengunggah foto...");
                                }
                            });
                        }

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {
                            // Progress bar indeterminate ditampilkan selama proses.
                        }

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            Object secureUrl = resultData == null ? null : resultData.get("secure_url");
                            if (secureUrl == null) {
                                runOnUiThread(() -> {
                                    if (isFinishing() || isDestroyed()) return;
                                    setBusy(false, null);
                                    Toast.makeText(EditProfileActivity.this,
                                            "Upload berhasil tetapi URL foto tidak ditemukan.",
                                            Toast.LENGTH_LONG).show();
                                });
                                return;
                            }

                            String uploadedUrl = secureUrl.toString();
                            runOnUiThread(() -> {
                                if (isFinishing() || isDestroyed()) return;
                                writeProfile(name, phone, site, uploadedUrl);
                            });
                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            runOnUiThread(() -> {
                                if (isFinishing() || isDestroyed()) return;
                                setBusy(false, null);
                                Toast.makeText(EditProfileActivity.this,
                                        "Upload foto gagal: " + error.getDescription(),
                                        Toast.LENGTH_LONG).show();
                            });
                        }

                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) {
                            runOnUiThread(() -> {
                                if (isFinishing() || isDestroyed()) return;
                                setBusy(false, null);
                                Toast.makeText(EditProfileActivity.this,
                                        "Upload dijadwalkan ulang. Periksa koneksi internet lalu coba lagi.",
                                        Toast.LENGTH_LONG).show();
                            });
                        }
                    })
                    .dispatch();
        } catch (Exception e) {
            setBusy(false, null);
            Toast.makeText(this, "Tidak dapat memulai upload foto: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void writeProfile(String name, String phone, String site, String photoUrl) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("phone", phone);
        updates.put("site", site);
        if (photoUrl != null && !photoUrl.trim().isEmpty()) {
            updates.put("photoUrl", photoUrl);
        }

        userRef.updateChildren(updates)
                .addOnSuccessListener(unused -> {
                    if (isFinishing() || isDestroyed()) return;
                    currentPhotoUrl = photoUrl == null ? "" : photoUrl;
                    selectedPhotoUri = null;
                    setBusy(false, null);
                    Toast.makeText(EditProfileActivity.this,
                            "Profil berhasil diperbarui",
                            Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(error -> {
                    if (isFinishing() || isDestroyed()) return;
                    setBusy(false, null);
                    Toast.makeText(EditProfileActivity.this,
                            "Gagal menyimpan profil: " + error.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void setBusy(boolean busy, String message) {
        isSaving = busy;
        if (btnSave != null) {
            btnSave.setEnabled(!busy);
            btnSave.setText(busy
                    ? (message == null ? "Memproses..." : message)
                    : "SIMPAN PERUBAHAN");
        }
        if (btnChangePhoto != null) btnChangePhoto.setEnabled(!busy);
        if (progressOverlay != null) {
            progressOverlay.setVisibility(busy ? View.VISIBLE : View.GONE);
        }
    }

    private void loadProfileImage(String imageUrl) {
        imageExecutor.execute(() -> {
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) new URL(imageUrl).openConnection();
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.setInstanceFollowRedirects(true);
                try (InputStream input = connection.getInputStream()) {
                    Bitmap bitmap = BitmapFactory.decodeStream(input);
                    if (bitmap != null) {
                        runOnUiThread(() -> {
                            if (!isFinishing() && !isDestroyed()) imgProfile.setImageBitmap(bitmap);
                        });
                    }
                }
            } catch (Exception e) {
                android.util.Log.w("EditProfile", "Gagal memuat foto profil", e);
            } finally {
                if (connection != null) connection.disconnect();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        imageExecutor.shutdown();
    }
}

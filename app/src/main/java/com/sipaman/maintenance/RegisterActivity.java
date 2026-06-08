package com.sipaman.maintenance;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class RegisterActivity extends AppCompatActivity {

    private EditText etNama, etEmail, etPassword;
    private MaterialButton btnRegister;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etNama = findViewById(R.id.etNama);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {

        String nama = etNama.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (nama.isEmpty()) {
            etNama.setError("Nama wajib diisi");
            return;
        }

        if (email.isEmpty()) {
            etEmail.setError("Email wajib diisi");
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password wajib diisi");
            return;
        }

        // 🔥 REGISTER AUTH
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        FirebaseUser user = mAuth.getCurrentUser();

                        if (user == null) {
                            Toast.makeText(this, "User gagal dibuat", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String uid = user.getUid();

                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("name", nama);
                        userMap.put("email", email);
                        userMap.put("role", "teknisi");

                        // 🔥 SIMPAN FIRESTORE
                        db.collection("users")
                                .document(uid)
                                .set(userMap)
                                .addOnSuccessListener(aVoid -> {

                                    Toast.makeText(this, "Register berhasil", Toast.LENGTH_SHORT).show();

                                    startActivity(new Intent(this, DashboardActivity.class));
                                    finish();

                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Gagal simpan user", Toast.LENGTH_SHORT).show();
                                });

                    } else {

                        String error = "Register gagal";
                        if (task.getException() != null) {
                            error = task.getException().getMessage();
                        }

                        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                    }
                });
    }
}
package com.sipaman.maintenance;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import java.util.HashMap;
import java.util.Map;


public class RegisterActivity extends AppCompatActivity {

    private EditText etNama, etEmail, etPassword;
    private MaterialButton btnRegister;

    private FirebaseAuth mAuth;
    private DatabaseReference database;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        database = FirebaseDatabase.getInstance().getReference();

        mAuth = FirebaseAuth.getInstance();

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

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    if (!task.isSuccessful()) {

                        Toast.makeText(
                                RegisterActivity.this,
                                task.getException().getMessage(),
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }

                    FirebaseUser user = mAuth.getCurrentUser();

                    if (user == null) {

                        Toast.makeText(
                                RegisterActivity.this,
                                "User gagal dibuat",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    String uid = user.getUid();

                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("name", nama);
                    userMap.put("email", email);
                    userMap.put("role", "teknisi");

                    database.child("users")
                            .child(uid)
                            .setValue(userMap)
                            .addOnSuccessListener(unused -> {

                                Toast.makeText(
                                        RegisterActivity.this,
                                        "Register berhasil",
                                        Toast.LENGTH_SHORT
                                ).show();

                                startActivity(
                                        new Intent(
                                                RegisterActivity.this,
                                                DashboardActivity.class
                                        )
                                );

                                finish();

                            })
                            .addOnFailureListener(e -> {

                                Toast.makeText(
                                        RegisterActivity.this,
                                        e.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();

                            });

                });
        }

    }
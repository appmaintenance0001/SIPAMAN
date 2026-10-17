package com.sipaman.maintenance;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.ImageView;

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

    private EditText etConfirmPassword;
    private EditText etPhone;
    private AutoCompleteTextView etSite;
    private AutoCompleteTextView etRole;

    private final String[] siteList = {
            "Pulubala",
            "Paguat",
            "Makasar"
    };

    private final String[] roleList = {
            "Admin",
            "Supervisor",
            "Teknisi"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();

        etNama = findViewById(R.id.etNama);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etPhone = findViewById(R.id.etPhone);
        etSite = findViewById(R.id.etSite);
        ArrayAdapter<String> siteAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        siteList);

        etSite.setAdapter(siteAdapter);
        etSite.setOnClickListener(v -> etSite.showDropDown());
        etSite.setKeyListener(null);

        etRole = findViewById(R.id.etRole);
        ArrayAdapter<String> roleAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        roleList);

        etRole.setAdapter(roleAdapter);
        etRole.setOnClickListener(v -> etRole.showDropDown());
        etRole.setKeyListener(null);

        btnRegister = findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {

        String nama = etNama.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String site = etSite.getText().toString().trim();
        String role = etRole.getText().toString().trim();

        // Nama
        if (nama.isEmpty()) {
            etNama.setError("Nama wajib diisi");
            etNama.requestFocus();
            return;
        }

        // Email
        if (email.isEmpty()) {
            etEmail.setError("Email wajib diisi");
            etEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Format email tidak valid");
            etEmail.requestFocus();
            return;
        }

        // Password
        if (password.length() < 6) {
            etPassword.setError("Password minimal 6 karakter");
            etPassword.requestFocus();
            return;
        }

        // Konfirmasi Password
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Password tidak sama");
            etConfirmPassword.requestFocus();
            return;
        }

        // Site
        if (site.isEmpty()) {
            Toast.makeText(this,
                    "Silakan pilih Site",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Role
        if (role.isEmpty()) {
            Toast.makeText(this,
                    "Silakan pilih Role",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Nomor HP
        if (!phone.isEmpty()) {

            if (phone.length() < 10 || phone.length() > 15) {

                etPhone.setError("Nomor HP tidak valid");
                etPhone.requestFocus();
                return;

            }

        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    if (!task.isSuccessful()) {

                        Toast.makeText(
                                this,
                                task.getException().getMessage(),
                                Toast.LENGTH_LONG
                        ).show();

                        return;

                    }

                    FirebaseUser user = mAuth.getCurrentUser();

                    if (user == null) return;

                    HashMap<String, Object> map = new HashMap<>();

                    map.put("name", nama);
                    map.put("email", email);
                    map.put("phone", phone);
                    map.put("site", site);
                    map.put("role", role);
                    map.put("createdAt", System.currentTimeMillis());

                    database.child("users")
                            .child(user.getUid())
                            .setValue(map)
                            .addOnSuccessListener(unused -> {

                                user.sendEmailVerification()
                                        .addOnSuccessListener(aVoid -> {

                                            Toast.makeText(
                                                    RegisterActivity.this,
                                                    "Email verifikasi BERHASIL dikirim",
                                                    Toast.LENGTH_LONG
                                            ).show();

                                            FirebaseAuth.getInstance().signOut();

                                            startActivity(new Intent(
                                                    RegisterActivity.this,
                                                    LoginActivity.class));

                                            finish();

                                        })
                                        .addOnFailureListener(e -> {

                                            Toast.makeText(
                                                    RegisterActivity.this,
                                                    "Error : " + e.getMessage(),
                                                    Toast.LENGTH_LONG
                                            ).show();

                                        });

                            });

                });
    }
}
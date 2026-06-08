package com.sipaman.maintenance;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthResult;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 🔥 WAJIB: INIT DULU
        mAuth = FirebaseAuth.getInstance();

        // 🔥 BARU BOLEH DIPAKAI
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
        }

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> loginUser());


        findViewById(R.id.btnSignup).setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );
    }

    private void loginUser() {

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty()) {
            etEmail.setError("Email wajib diisi");
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password wajib diisi");
            return;
        }

        // 🔥 LOGIN FIREBASE
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) { // ✅ WAJIB ADA

                        String uid = mAuth.getCurrentUser().getUid();

                        FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(uid)
                                .get()
                                .addOnSuccessListener(doc -> {

                                    String role = doc.getString("role");

                                    if ("admin".equals(role)) {
                                        startActivity(new Intent(this, DashboardActivity.class));
                                    } else {
                                        startActivity(new Intent(this, DashboardActivity.class));
                                    }

                                    finish();
                                });

                    } else {

                        String errorMessage = "Login gagal";

                        Exception e = task.getException();
                        if (e != null && e.getMessage() != null) {
                            errorMessage = e.getMessage();
                        }

                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }
}
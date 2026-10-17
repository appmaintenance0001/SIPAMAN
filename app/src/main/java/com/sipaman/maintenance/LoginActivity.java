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
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;

    private FirebaseAuth mAuth;

    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 🔥 WAJIB: INIT DULU
        mAuth = FirebaseAuth.getInstance();

        loadingDialog = new LoadingDialog(this);


        // 🔥 BARU BOLEH DIPAKAI
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {

            currentUser.reload().addOnCompleteListener(task -> {

                if (currentUser.isEmailVerified()) {

                    startActivity(new Intent(
                            LoginActivity.this,
                            DashboardActivity.class));

                    finish();

                } else {

                    FirebaseAuth.getInstance().signOut();

                }

            });

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

        loadingDialog.show();

        // 🔥 LOGIN FIREBASE
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                        if (user != null) {

                            user.reload().addOnCompleteListener(reloadTask -> {

                                if (!reloadTask.isSuccessful()) {

                                    loadingDialog.dismiss();

                                    Toast.makeText(
                                            LoginActivity.this,
                                            "Gagal memeriksa status verifikasi.",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    return;
                                }

                                if (user.isEmailVerified()) {

                                    loadingDialog.dismiss();

                                    Toast.makeText(
                                            LoginActivity.this,
                                            "Login berhasil",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    startActivity(new Intent(
                                            LoginActivity.this,
                                            DashboardActivity.class));

                                    finish();

                                } else {

                                    loadingDialog.dismiss();

                                    FirebaseAuth.getInstance().signOut();

                                    Toast.makeText(
                                            LoginActivity.this,
                                            "Email belum diverifikasi.\nSilakan cek email Anda.",
                                            Toast.LENGTH_LONG
                                    ).show();

                                }

                            });

                        }

                    } else {

                        loadingDialog.dismiss();

                        String message = "Login gagal";

                        if (task.getException() != null) {
                            message = task.getException().getMessage();
                        }

                        Toast.makeText(
                                LoginActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();

                    }
                });
    }
}
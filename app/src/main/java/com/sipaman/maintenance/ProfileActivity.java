package com.sipaman.maintenance;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    private ImageView btnBack;

    private TextView tvProfileName;
    private TextView tvProfileEmail;
    private TextView tvProfileRole;
    private TextView tvProfileSite;
    private TextView tvProfilePhone;

    private MaterialButton btnEditProfile;
    private MaterialButton btnChangePassword;
    private TextView tvName;
    private TextView tvRole;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initView();

        loadProfile();

        btnBack.setOnClickListener(v -> finish());

        btnEditProfile.setOnClickListener(v -> {

            startActivity(
                    new Intent(
                            ProfileActivity.this,
                            EditProfileActivity.class
                    )
            );

        });

        btnChangePassword.setOnClickListener(v ->

                Toast.makeText(
                        this,
                        "Menu Ganti Password akan dibuat",
                        Toast.LENGTH_SHORT
                ).show()

        );

    }

    private void initView() {

        btnBack = findViewById(R.id.btnBack);

        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        tvProfileRole = findViewById(R.id.tvProfileRole);
        tvProfileSite = findViewById(R.id.tvProfileSite);
        tvProfilePhone = findViewById(R.id.tvProfilePhone);
        tvName = findViewById(R.id.tvName);
        tvRole = findViewById(R.id.tvRole);

        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnChangePassword = findViewById(R.id.btnChangePassword);

    }

    private void loadProfile() {

        FirebaseUser user = FirebaseAuth
                .getInstance()
                .getCurrentUser();

        if (user == null) {

            finish();

            return;

        }

        DatabaseReference ref = FirebaseDatabase
                .getInstance()
                .getReference("users")
                .child(user.getUid());

        ref.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String name = snapshot.child("name").getValue(String.class);
                String email = snapshot.child("email").getValue(String.class);

                if (email == null || email.isEmpty()) {
                    email = user.getEmail();
                }
                String role = snapshot.child("role").getValue(String.class);
                String site = snapshot.child("site").getValue(String.class);
                String phone = snapshot.child("phone").getValue(String.class);

                tvProfileName.setText(name == null ? "-" : name);
                tvName.setText(name == null ? "-" : name);
                tvProfileEmail.setText(email == null ? "-" : email);
                tvProfileRole.setText(role == null ? "-" : role);
                tvRole.setText(role == null ? "-" : role);
                if (site == null || site.isEmpty()) {
                    site = "Pulubala";
                }

                tvProfileSite.setText(site);
                tvProfilePhone.setText(phone == null ? "-" : phone);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(ProfileActivity.this,
                        "Gagal mengambil data",
                        Toast.LENGTH_SHORT).show();

            }

        });

    }

}
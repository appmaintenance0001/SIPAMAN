package com.sipaman.maintenance;

import android.os.Bundle;
import android.widget.ImageView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;

import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView btnBack;

    private TextInputEditText edtName;
    private TextInputEditText edtPhone;
    private TextInputEditText edtSite;

    private MaterialButton btnSave;

    private DatabaseReference userRef;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        btnBack = findViewById(R.id.btnBack);

        edtName = findViewById(R.id.edtName);
        edtPhone = findViewById(R.id.edtPhone);
        edtSite = findViewById(R.id.edtSite);

        btnSave = findViewById(R.id.btnSave);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if(currentUser!=null){
            userRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(currentUser.getUid());

            Toast.makeText(this,
                    currentUser.getUid(),
                    Toast.LENGTH_LONG).show();

            loadData();
        }

        btnBack.setOnClickListener(v -> finish());

    }


    private void loadData(){

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (!snapshot.exists()) return;

                // Cek semua data yang diterima dari Firebase
                for (DataSnapshot child : snapshot.getChildren()) {
                    android.util.Log.d("PROFILE",
                            child.getKey() + " = " + child.getValue());
                }

                edtName.setText(snapshot.child("name").getValue(String.class));
                edtPhone.setText(snapshot.child("phone").getValue(String.class));

                String site = snapshot.child("site").getValue(String.class);

                Toast.makeText(EditProfileActivity.this,
                        "Site = " + site,
                        Toast.LENGTH_LONG).show();

                edtSite.setText(site);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(EditProfileActivity.this,
                        "Gagal mengambil data",
                        Toast.LENGTH_SHORT).show();

            }
        });

    }
}

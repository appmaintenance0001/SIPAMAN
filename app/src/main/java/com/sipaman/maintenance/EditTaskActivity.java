package com.sipaman.maintenance;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class EditTaskActivity extends AppCompatActivity {

    EditText etProject, etJenis, etMulai, etDue;
    Button btnUpdate;

    String id, status;

    List<String> beforeUrls = new ArrayList<>();
    List<String> afterUrls = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);

        // 🔥 INIT VIEW
        etProject = findViewById(R.id.etProject);
        etJenis = findViewById(R.id.etJenis);
        etMulai = findViewById(R.id.etMulai);
        etDue = findViewById(R.id.etDue);
        btnUpdate = findViewById(R.id.btnUpdate);

        // 🔥 AMBIL DATA DARI INTENT
        Intent intent = getIntent();

        id = intent.getStringExtra("id");
        String project = intent.getStringExtra("project");
        String jenis = intent.getStringExtra("jenis");
        String mulai = intent.getStringExtra("mulai");
        String due = intent.getStringExtra("due");
        status = intent.getStringExtra("status");

        // optional (kalau kirim list foto nanti)
        if (intent.getStringArrayListExtra("beforeUrls") != null) {
            beforeUrls = intent.getStringArrayListExtra("beforeUrls");
        }

        if (intent.getStringArrayListExtra("afterUrls") != null) {
            afterUrls = intent.getStringArrayListExtra("afterUrls");
        }

        // 🔥 SET KE INPUT
        etProject.setText(project);
        etJenis.setText(jenis);
        etMulai.setText(mulai);
        etDue.setText(due);

        // 🔥 FIREBASE
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("tasks");

        // 🔥 BUTTON UPDATE
        btnUpdate.setOnClickListener(v -> {

            if (id == null || id.isEmpty()) {
                Toast.makeText(this, "ID tidak ditemukan", Toast.LENGTH_SHORT).show();
                return;
            }

            String newProject = etProject.getText().toString().trim();
            String newJenis = etJenis.getText().toString().trim();
            String newMulai = etMulai.getText().toString().trim();
            String newDue = etDue.getText().toString().trim();

            // 🔥 VALIDASI
            if (newProject.isEmpty() || newJenis.isEmpty()) {
                Toast.makeText(this, "Field tidak boleh kosong", Toast.LENGTH_SHORT).show();
                return;
            }

            // 🔥 UPDATE OBJECT
            Task updated = new Task();
            updated.setId(id);
            updated.setProject(newProject);
            updated.setJenis(newJenis);
            updated.setMulai(newMulai);
            updated.setDue(newDue);
            updated.setStatus(status);

            // 🔥 JAGA DATA FOTO
            updated.setBeforeUrls(beforeUrls);
            updated.setAfterUrls(afterUrls);

            // 🔥 SAVE KE FIREBASE
            db.child(id).setValue(updated);

            Toast.makeText(this, "Task berhasil diupdate", Toast.LENGTH_SHORT).show();

            finish();
        });
    }
}
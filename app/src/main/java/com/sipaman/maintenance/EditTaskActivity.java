package com.sipaman.maintenance;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;
import android.content.Intent;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        etProject = findViewById(R.id.etProject);
        etJenis = findViewById(R.id.etJenis);
        etMulai = findViewById(R.id.etMulai);
        etDue = findViewById(R.id.etDue);
        btnUpdate = findViewById(R.id.btnUpdate);

        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        String project = intent.getStringExtra("project");
        String jenis = intent.getStringExtra("jenis");
        String mulai = intent.getStringExtra("mulai");
        String due = intent.getStringExtra("due");
        status = intent.getStringExtra("status");

        if (intent.getStringArrayListExtra("beforeUrls") != null) {
            beforeUrls = intent.getStringArrayListExtra("beforeUrls");
        }
        if (intent.getStringArrayListExtra("afterUrls") != null) {
            afterUrls = intent.getStringArrayListExtra("afterUrls");
        }

        etProject.setText(project);
        etJenis.setText(jenis);
        etMulai.setText(mulai);
        etDue.setText(due);

        DatabaseReference db = FirebaseDatabase.getInstance().getReference("tasks");

        btnUpdate.setOnClickListener(v -> {
            if (id == null || id.isEmpty()) {
                Toast.makeText(this, "ID tidak ditemukan", Toast.LENGTH_SHORT).show();
                return;
            }

            String newProject = etProject.getText().toString().trim();
            String newJenis = etJenis.getText().toString().trim();
            String newMulai = etMulai.getText().toString().trim();
            String newDue = etDue.getText().toString().trim();

            if (newProject.isEmpty() || newJenis.isEmpty()) {
                Toast.makeText(this, "Field tidak boleh kosong", Toast.LENGTH_SHORT).show();
                return;
            }

            btnUpdate.setEnabled(false);
            db.child(id).get()
                    .addOnSuccessListener(snapshot -> {
                        if (isFinishing() || isDestroyed()) return;

                        Task oldTask = snapshot.getValue(Task.class);
                        if (oldTask == null) {
                            btnUpdate.setEnabled(true);
                            Toast.makeText(this, "Task tidak ditemukan", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Map<String, Object> updates = new HashMap<>();
                        updates.put("project", newProject);
                        updates.put("jenis", newJenis);
                        updates.put("mulai", newMulai);
                        updates.put("due", newDue);

                        db.child(id).updateChildren(updates)
                                .addOnSuccessListener(unused -> {
                                    if (isFinishing() || isDestroyed()) return;

                                    String changes = buildChanges(
                                            oldTask, newProject, newJenis, newMulai, newDue);
                                    String taskName = oldTask.getJudul() != null
                                            && !oldTask.getJudul().trim().isEmpty()
                                            ? oldTask.getJudul() : id;

                                    writeTaskLog(
                                            "Task Diedit",
                                            id,
                                            taskName,
                                            "Task \"" + taskName + "\" diperbarui.",
                                            changes
                                    );

                                    Toast.makeText(this, "Task berhasil diupdate",
                                            Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(error -> {
                                    if (isFinishing() || isDestroyed()) return;
                                    btnUpdate.setEnabled(true);
                                    Toast.makeText(this,
                                            "Gagal update task: " + error.getMessage(),
                                            Toast.LENGTH_LONG).show();
                                });
                    })
                    .addOnFailureListener(error -> {
                        if (isFinishing() || isDestroyed()) return;
                        btnUpdate.setEnabled(true);
                        Toast.makeText(this,
                                "Gagal membaca task: " + error.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });
        });
    }

    private String buildChanges(Task oldTask, String project, String jenis,
                                String mulai, String due) {
        StringBuilder changes = new StringBuilder();
        appendChange(changes, "Project", oldTask.getProject(), project);
        appendChange(changes, "Jenis", oldTask.getJenis(), jenis);
        appendChange(changes, "Tanggal mulai", oldTask.getMulai(), mulai);
        appendChange(changes, "Due date", oldTask.getDue(), due);
        return changes.length() == 0 ? "Tidak ada perubahan field." : changes.toString();
    }

    private void appendChange(StringBuilder changes, String label,
                              String oldValue, String newValue) {
        String oldText = oldValue == null ? "" : oldValue.trim();
        String newText = newValue == null ? "" : newValue.trim();
        if (oldText.equals(newText)) return;
        if (changes.length() > 0) changes.append("\\n");
        changes.append(label).append(": ")
                .append(oldText.isEmpty() ? "(kosong)" : oldText)
                .append(" → ")
                .append(newText.isEmpty() ? "(kosong)" : newText);
    }

    private void writeTaskLog(String type, String taskId, String taskName,
                              String message, String changes) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.w("SIPAMAN_HISTORY", "User belum login; riwayat task tidak ditulis.");
            return;
        }

        String uid = user.getUid();
        String fallbackName = user.getDisplayName();
        if (fallbackName == null || fallbackName.trim().isEmpty()) {
            fallbackName = user.getEmail() != null ? user.getEmail() : "Pengguna";
        }
        final String defaultName = fallbackName;

        FirebaseDatabase.getInstance().getReference("users").child(uid).child("name").get()
                .addOnSuccessListener(snapshot -> {
                    String userName = snapshot.getValue(String.class);
                    if (userName == null || userName.trim().isEmpty()) userName = defaultName;
                    saveTaskLog(uid, userName, type, taskId, taskName, message, changes);
                })
                .addOnFailureListener(error ->
                        saveTaskLog(uid, defaultName, type, taskId, taskName, message, changes));
    }

    private void saveTaskLog(String uid, String userName, String type, String taskId,
                             String taskName, String message, String changes) {
        DatabaseReference logsRef = FirebaseDatabase.getInstance().getReference("activity_logs");
        String logId = logsRef.push().getKey();
        if (logId == null) {
            Log.e("SIPAMAN_HISTORY", "Gagal membuat ID riwayat task.");
            return;
        }

        ActivityLog log = new ActivityLog(
                logId, uid, userName, type, System.currentTimeMillis(),
                "task", taskId, taskName, message, changes);
        logsRef.child(logId).setValue(log)
                .addOnFailureListener(error ->
                        Log.e("SIPAMAN_HISTORY", "Gagal menyimpan riwayat task.", error));
    }
}

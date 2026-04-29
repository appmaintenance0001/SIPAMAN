package com.sipaman.maintenance;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Date;

public class DetailTaskActivity extends AppCompatActivity {

    // 🔥 GLOBAL VIEW (WAJIB)
    private TextView txtProject, txtJenis, txtMulai, txtDue, txtStatus, txtSelesai;
    private TextView txtStatusBadge, txtStatusText;
    private TextView txtProjectValue, txtPic, txtPriority, txtDeskripsi;

    private Button btnSelesai;

    private RecyclerView rvBefore, rvAfter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_task);

        // ================= INIT VIEW =================
        txtProject = findViewById(R.id.txtProject);
        txtJenis = findViewById(R.id.txtJenis);
        txtMulai = findViewById(R.id.txtMulai);
        txtDue = findViewById(R.id.txtDue);
        txtStatus = findViewById(R.id.txtStatus);
        txtSelesai = findViewById(R.id.txtSelesai);

        txtStatusBadge = findViewById(R.id.txtStatusBadge);
        txtStatusText = findViewById(R.id.txtStatusText);

        txtProjectValue = findViewById(R.id.txtProjectValue);
        txtPic = findViewById(R.id.txtPic);
        txtPriority = findViewById(R.id.txtPriority);
        txtDeskripsi = findViewById(R.id.txtDeskripsi);

        btnSelesai = findViewById(R.id.btnSelesai);

        rvBefore = findViewById(R.id.rvBefore);
        rvAfter = findViewById(R.id.rvAfter);

        rvBefore.setLayoutManager(new GridLayoutManager(this, 3));
        rvAfter.setLayoutManager(new GridLayoutManager(this, 3));

        // 🔥 CEK VIEW (ANTI CRASH)
        if (txtStatusBadge == null) {
            Toast.makeText(this, "ERROR: View tidak ditemukan!", Toast.LENGTH_LONG).show();
            return;
        }

        // ================= DEFAULT BUTTON =================
        btnSelesai.setEnabled(false);
        btnSelesai.setAlpha(0.5f);

        // ================= AMBIL DATA INTENT =================
        String id = getIntent().getStringExtra("id");
        String project = getIntent().getStringExtra("project");
        String jenis = getIntent().getStringExtra("jenis");
        String mulai = getIntent().getStringExtra("mulai");
        String due = getIntent().getStringExtra("due");
        String status = getIntent().getStringExtra("status");
        String selesai = getIntent().getStringExtra("tanggalSelesai");

        String pic = getIntent().getStringExtra("pic");
        String priority = getIntent().getStringExtra("priority");
        String deskripsi = getIntent().getStringExtra("deskripsi");

        if (status == null) status = "ON_PROGRESS";

        // ================= SET STATUS BADGE =================
        if ("DONE".equals(status)) {
            txtStatusBadge.setText("Done");
            txtStatusBadge.setBackgroundResource(R.drawable.bg_status_done);

        } else if ("OVERDUE".equals(status)) {
            txtStatusBadge.setText("Overdue");
            txtStatusBadge.setBackgroundResource(R.drawable.bg_status_overdue);

        } else {
            txtStatusBadge.setText("On Progress");
            txtStatusBadge.setBackgroundResource(R.drawable.bg_status_progress);
        }

        // ================= SET DATA =================
        txtProjectValue.setText(project);
        txtProject.setText(project);

        txtJenis.setText(jenis);
        txtMulai.setText("Mulai: " + mulai);
        txtDue.setText("Due: " + due);

        txtSelesai.setText("Selesai: " + (selesai == null ? "-" : selesai));

        txtPic.setText(pic);
        txtPriority.setText(priority);
        txtDeskripsi.setText(deskripsi);

        // ================= STATUS TEXT =================
        String statusFix = status.replace("_", " ");
        txtStatus.setText(statusFix);
        txtStatusText.setText(statusFix);

        // ================= HIDE BUTTON JIKA DONE =================
        if ("DONE".equals(status)) {
            btnSelesai.setVisibility(View.GONE);
        }

        // ================= BUTTON SELESAI =================
        btnSelesai.setOnClickListener(v -> {

            if (id == null) return;

            DatabaseReference db = FirebaseDatabase.getInstance()
                    .getReference("tasks");

            // 🔥 UPDATE STATUS
            db.child(id).child("status").setValue("DONE");

            // 🔥 TANGGAL SELESAI
            String today = new java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    .format(new Date());

            db.child(id).child("tanggalSelesai").setValue(today);

            // 🔥 PINDAH KE INPUT AFTER
            Intent intent = new Intent(this, AddTaskActivity.class);
            intent.putExtra("id", id);
            intent.putExtra("mode", "after");
            startActivity(intent);
        });

        // ================= LOAD FOTO =================
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("tasks");

        db.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                Task task = snapshot.getValue(Task.class);

                if (task == null) return;

                // BEFORE
                List<String> beforeList = task.getBeforeUrls();
                if (beforeList == null) beforeList = new ArrayList<>();

                // AFTER
                List<String> afterList = task.getAfterUrls();
                if (afterList == null) afterList = new ArrayList<>();

                // 🔥 ENABLE BUTTON JIKA ADA AFTER
                boolean sudahAdaAfter = !afterList.isEmpty();

                btnSelesai.setEnabled(sudahAdaAfter);
                btnSelesai.setAlpha(sudahAdaAfter ? 1f : 0.5f);

                // SET ADAPTER
                rvBefore.setAdapter(new PhotoAdapter(DetailTaskActivity.this, beforeList));
                rvAfter.setAdapter(new PhotoAdapter(DetailTaskActivity.this, afterList));
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }
}
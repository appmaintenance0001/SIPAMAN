package com.sipaman.maintenance;


import android.content.Intent;

import android.os.Bundle;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.menu_delete) {

            new AlertDialog.Builder(this)
                    .setTitle("Hapus Task")
                    .setMessage("Yakin ingin menghapus?")
                    .setPositiveButton("Hapus", (d, w) -> {

                        String id = getIntent().getStringExtra("id");

                        FirebaseDatabase.getInstance()
                                .getReference("tasks")
                                .child(id)
                                .removeValue();

                        Toast.makeText(this, "Task dihapus", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .setNegativeButton("Batal", null)
                    .show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_task);


        Toolbar toolbar = findViewById(R.id.toolbar);

        toolbar.setTitle("DETAIL TASK");

        setSupportActionBar(toolbar);

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

        // ================= STATUS FIX (KONSISTEN DENGAN LIST) =================
        String finalStatus;

        if ("DONE".equals(status)) {

            finalStatus = "DONE";

            txtStatusBadge.setText("Done");
            txtStatusBadge.setBackgroundResource(R.drawable.bg_status_done);

            btnSelesai.setVisibility(View.GONE);

        } else if (due != null && isOverdue(due)) {

            finalStatus = "OVERDUE";

            txtStatusBadge.setText("Overdue");
            txtStatusBadge.setBackgroundResource(R.drawable.bg_status_overdue);

        } else {

            finalStatus = "ON PROGRESS";

            txtStatusBadge.setText("On Progress");
            txtStatusBadge.setBackgroundResource(R.drawable.bg_status_progress);
        }

// SET TEXT STATUS
        txtStatus.setText(finalStatus);
        txtStatusText.setText(finalStatus);

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


        // ================= HIDE BUTTON JIKA DONE =================
        if ("DONE".equals(status)) {
            btnSelesai.setVisibility(View.GONE);
        }

        // ================= BUTTON SELESAI =================
        btnSelesai.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            DetailTaskActivity.this,
                            AddTaskActivity.class
                    );

            intent.putExtra("mode", "after");

            intent.putExtra(
                    "id",
                    getIntent().getStringExtra("id")
            );

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
                btnSelesai.setEnabled(true);
                btnSelesai.setAlpha(1f);

                // SET ADAPTER
                rvBefore.setAdapter(new PhotoAdapter(DetailTaskActivity.this, beforeList));
                rvAfter.setAdapter(new PhotoAdapter(DetailTaskActivity.this, afterList));
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        loadTaskDetail();
    }



    private void loadTaskDetail() {

        String id =
                getIntent().getStringExtra("id");

        DatabaseReference db =
                FirebaseDatabase.getInstance()
                        .getReference("tasks");

        db.child(id)
                .get()
                .addOnSuccessListener(snapshot -> {

                    Task task =
                            snapshot.getValue(Task.class);

                    if (task == null) return;

                    // STATUS
                    String status = task.getStatus();

                    if ("DONE".equals(status)) {

                        txtStatusBadge.setText("DONE");

                        txtStatusBadge.setBackgroundResource(
                                R.drawable.bg_status_done
                        );

                        txtStatus.setText("DONE");

                        txtStatusText.setText("DONE");

                        btnSelesai.setVisibility(View.GONE);

                    } else {

                        txtStatusBadge.setText("ON PROGRESS");

                        txtStatusBadge.setBackgroundResource(
                                R.drawable.bg_status_progress
                        );
                    }

                    txtSelesai.setText(
                            "Selesai: "
                                    + task.getTanggalSelesai()
                    );

                    rvBefore.setAdapter(
                            new PhotoAdapter(
                                    DetailTaskActivity.this,
                                    task.getBeforeUrls()
                            )
                    );

                    rvAfter.setAdapter(
                            new PhotoAdapter(
                                    DetailTaskActivity.this,
                                    task.getAfterUrls()
                            )
                    );
                });
    }



        // ================= OVERDUE CHECK =================
        private boolean isOverdue(String dueDate) {

            try {

                SimpleDateFormat sdf =
                        new SimpleDateFormat(
                                "d/M/yyyy",
                                Locale.getDefault()
                        );

                Date due = sdf.parse(dueDate);

                return due.before(new Date());

            } catch (Exception e) {

                return false;
            }
        }
    }
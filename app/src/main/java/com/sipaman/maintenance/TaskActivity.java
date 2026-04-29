package com.sipaman.maintenance;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;
import android.view.View;
import android.view.KeyEvent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.chip.Chip;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.text.SimpleDateFormat;

public class TaskActivity extends AppCompatActivity {

    RecyclerView rvTask;
    TaskAdapter adapter;

    List<Task> taskList = new ArrayList<>();
    List<Task> filteredList = new ArrayList<>();

    DatabaseReference database;

    EditText etSearch;

    String currentFilter = "ALL";

    Chip chipAll, chipDone, chipProgress, chipOverdue;

    // 🔥 ANIMASI CHIP PREMIUM
    private void animateChipPremium(View chip) {
        chip.animate()
                .scaleX(1.08f)
                .scaleY(1.08f)
                .alpha(0.9f)
                .translationZ(12f)
                .setDuration(120)
                .withEndAction(() ->
                        chip.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .alpha(1f)
                                .translationZ(6f)
                                .setDuration(120)
                );
    }

    // 🔥 SET CHIP AKTIF
    private void setActiveChip(View selected, View... chips) {
        for (View chip : chips) {
            chip.setScaleX(1f);
            chip.setScaleY(1f);
            chip.setAlpha(0.8f);
        }

        selected.setScaleX(1.15f);
        selected.setScaleY(1.15f);
        selected.setAlpha(1f);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        rvTask = findViewById(R.id.rvTask);
        rvTask.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TaskAdapter(filteredList, task -> {

            Intent intent = new Intent(TaskActivity.this, DetailTaskActivity.class);

            intent.putExtra("id", task.getId());
            intent.putExtra("project", task.getProject());
            intent.putExtra("jenis", task.getJenis());
            intent.putExtra("mulai", task.getMulai());
            intent.putExtra("due", task.getDue());
            intent.putExtra("status", task.getStatus());
            intent.putExtra("selesai", task.getTanggalSelesai());

            startActivity(intent);
        });

        rvTask.setAdapter(adapter); // 🔥 WAJIB
        etSearch = findViewById(R.id.etSearch);

        // 🔥 DISABLE ENTER DI SEARCH
        etSearch.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER) return true;
            return false;
        });

        // 🔥 INIT CHIP
        chipAll = findViewById(R.id.chipAll);
        chipDone = findViewById(R.id.chipDone);
        chipProgress = findViewById(R.id.chipProgress);
        chipOverdue = findViewById(R.id.chipOverdue);

        // DEFAULT
        chipAll.setChecked(true);
        setActiveChip(chipAll, chipDone, chipProgress, chipOverdue);

        // 🔥 CLICK FILTER
        chipAll.setOnClickListener(v -> {
            currentFilter = "ALL";
            chipAll.setChecked(true);
            animateChipPremium(v);
            setActiveChip(chipAll, chipDone, chipProgress, chipOverdue);
            applyFilterAndSearch();
        });

        chipDone.setOnClickListener(v -> {
            currentFilter = "DONE";
            chipDone.setChecked(true);
            animateChipPremium(v);
            setActiveChip(chipDone, chipAll, chipProgress, chipOverdue);
            applyFilterAndSearch();
        });

        chipProgress.setOnClickListener(v -> {
            currentFilter = "ON_PROGRESS"; // ✅ FIX BUG
            chipProgress.setChecked(true);
            animateChipPremium(v);
            setActiveChip(chipProgress, chipAll, chipDone, chipOverdue);
            applyFilterAndSearch();
        });

        chipOverdue.setOnClickListener(v -> {
            currentFilter = "OVERDUE";
            chipOverdue.setChecked(true);
            animateChipPremium(v);
            setActiveChip(chipOverdue, chipAll, chipDone, chipProgress);
            applyFilterAndSearch();
        });

        // 🔥 FIREBASE
        database = FirebaseDatabase.getInstance().getReference("tasks");

        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                taskList.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Task task = ds.getValue(Task.class);
                    if (task != null) {
                        taskList.add(task);
                    }
                }

                updateChipCounter();
                applyFilterAndSearch();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(TaskActivity.this, "Gagal load data", Toast.LENGTH_SHORT).show();
            }
        });

        // 🔍 SEARCH REALTIME
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            public void afterTextChanged(android.text.Editable s) {
                applyFilterAndSearch();
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        // 🔥 FLOAT BUTTON
        FloatingActionButton btnAddTask = findViewById(R.id.btnAddTask);
        btnAddTask.setOnClickListener(v -> {
            startActivity(new Intent(TaskActivity.this, AddTaskActivity.class));
        });

        // 🔥 BOTTOM NAV
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_task);

        bottomNav.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.nav_dashboard) {
                startActivity(new Intent(this, DashboardActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_task) {
                return true;
            }

            return false;
        });
    }

    // 🔥 NORMALIZE STATUS
    private String normalizeStatus(String status) {
        if (status == null) return "";

        status = status.trim().toUpperCase();

        if (status.contains("DONE")) return "DONE";
        if (status.contains("PROGRESS")) return "ON_PROGRESS";
        if (status.contains("OVERDUE")) return "OVERDUE";

        return status;
    }

    private void updateChipCounter() {

        int countAll = 0;
        int countDone = 0;
        int countProgress = 0;
        int countOverdue = 0;

        SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
        Date today = new Date();

        for (Task t : taskList) {

            if (t == null) continue;

            countAll++;

            String status = normalizeStatus(t.getStatus());

            boolean isOverdue = false;

            try {
                Date dueDate = sdf.parse(t.getDue());
                if (dueDate != null && today.after(dueDate)) {
                    isOverdue = true;
                }
            } catch (Exception ignored) {}

            if (status.equals("DONE")) {
                countDone++;
            }

            if (status.equals("ON_PROGRESS") && !isOverdue) {
                countProgress++;
            }

            if (isOverdue && !status.equals("DONE")) {
                countOverdue++;
            }
        }

        // 🔥 SET TEXT CHIP
        chipAll.setText("Semua (" + countAll + ")");
        chipDone.setText("Done (" + countDone + ")");
        chipProgress.setText("Progress (" + countProgress + ")");
        chipOverdue.setText("Overdue (" + countOverdue + ")");
    }

    // 🔥 FILTER ENGINE FINAL (FIXED TOTAL)
    private void applyFilterAndSearch() {

        Log.d("TASK_DEBUG", "Adapter size: " + filteredList.size());
        Log.d("TASK_DEBUG", "Total task: " + taskList.size());
        Log.d("TASK_DEBUG", "Filtered: " + filteredList.size());


        filteredList.clear();

        String keyword = etSearch.getText().toString().toLowerCase();


        if (!keyword.isEmpty()) {

            if (keyword.contains("done")) {
                currentFilter = "DONE";
                chipDone.setChecked(true);
                setActiveChip(chipDone, chipAll, chipProgress, chipOverdue);
            }
            else if (keyword.contains("progress")) {
                currentFilter = "ON_PROGRESS";
                chipProgress.setChecked(true);
                setActiveChip(chipProgress, chipAll, chipDone, chipOverdue);
            }
            else if (keyword.contains("overdue")) {
                currentFilter = "OVERDUE";
                chipOverdue.setChecked(true);
                setActiveChip(chipOverdue, chipAll, chipDone, chipProgress);
            }
            else {
                currentFilter = "ALL";
                chipAll.setChecked(true);
                setActiveChip(chipAll, chipDone, chipProgress, chipOverdue);
            }
        }


        SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
        Date today = new Date();

        for (Task t : taskList) {

            if (t == null) continue;

            String project = t.getProject() == null ? "" : t.getProject().toLowerCase();
            String jenis = t.getJenis() == null ? "" : t.getJenis().toLowerCase();
            String statusSearch = t.getStatus() == null ? "" : t.getStatus().toLowerCase();
            String dueText = t.getDue() == null ? "" : t.getDue().toLowerCase();

            // 🔍 SEARCH (AMAN)
            if (!keyword.isEmpty()) {
                if (!project.contains(keyword) &&
                        !jenis.contains(keyword) &&
                        !statusSearch.contains(keyword) &&
                        !dueText.contains(keyword)) {
                    continue;
                }
            }

            String status = normalizeStatus(t.getStatus());

            boolean isOverdue = false;

            try {
                Date dueDate = sdf.parse(t.getDue());
                if (dueDate != null && today.after(dueDate)) {
                    isOverdue = true;
                }
            } catch (Exception ignored) {}

            // 🔘 FILTER
            if (currentFilter.equals("DONE") && !status.equals("DONE")) continue;

            if (currentFilter.equals("ON_PROGRESS")) {
                if (!status.equals("ON_PROGRESS") || isOverdue) continue;
            }

            if (currentFilter.equals("OVERDUE")) {
                if (!isOverdue || status.equals("DONE")) continue;
            }

            filteredList.add(t);
        }

        adapter.notifyDataSetChanged();
    }

}
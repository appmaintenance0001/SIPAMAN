package com.sipaman.maintenance;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;
import android.view.View;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;

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
    ValueEventListener taskListener;

    EditText etSearch;

    String currentFilter = "ALL";

    Chip chipAll, chipDone, chipProgress, chipOverdue;
    View filterContainer;

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
        rvTask.setHasFixedSize(true);

        adapter = new TaskAdapter(filteredList, task -> {

            Intent intent = new Intent(TaskActivity.this, DetailTaskActivity.class);

            intent.putExtra("id", task.getId());
            intent.putExtra("judul", task.getJudul());
            intent.putExtra("project", task.getProject());
            intent.putExtra("jenis", task.getJenis());
            intent.putExtra("mulai", task.getMulai());
            intent.putExtra("due", task.getDue());
            intent.putExtra("status", task.getStatus());
            intent.putExtra("tanggalSelesai", task.getTanggalSelesai());
            intent.putExtra("pic", task.getPic());
            intent.putExtra("priority", task.getPriority());
            intent.putExtra("deskripsi", task.getDeskripsi());

            startActivity(intent);
        });

        rvTask.setAdapter(adapter); // 🔥 WAJIB
        etSearch = findViewById(R.id.etSearch);
        filterContainer = findViewById(R.id.filterContainer);

        findViewById(R.id.btnTaskSearch).setOnClickListener(v -> {
            etSearch.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(etSearch, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        findViewById(R.id.btnTaskFilter).setOnClickListener(v -> {
            int visibility = filterContainer.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE;
            filterContainer.setVisibility(visibility);
        });

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
        currentFilter = getIntent().getStringExtra("filter");
        if (currentFilter == null) currentFilter = "ALL";
        applyActiveFilterChip();

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

        taskListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (isFinishing() || isDestroyed()) return;

                taskList.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Task task = ds.getValue(Task.class);
                    if (task != null) {
                        task.setId(ds.getKey());
                        taskList.add(task);
                    }
                }

                updateChipCounter();
                applyFilterAndSearch();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                if (isFinishing() || isDestroyed()) return;
                Toast.makeText(TaskActivity.this, "Gagal load data", Toast.LENGTH_SHORT).show();
            }
        };

        database.addValueEventListener(taskListener);

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

            if (id == R.id.nav_task) {
                return true;
            }

            Intent intent = null;

            if (id == R.id.nav_dashboard) {
                intent = new Intent(this, DashboardActivity.class);

            } else if (id == R.id.nav_calendar) {
                intent = new Intent(this, CalendarActivity.class);

            } else if (id == R.id.nav_project) {
                intent = new Intent(this, ProjectActivity.class);

            }

            if (intent != null) {
                startActivity(intent);

                // Animasi perpindahan lebih halus
                overridePendingTransition(
                        android.R.anim.fade_in,
                        android.R.anim.fade_out
                );

                finish();
                return true;
            }

            Toast.makeText(this, "Menu belum tersedia", Toast.LENGTH_SHORT).show();
            return false;
        });

        bottomNav.setSelectedItemId(R.id.nav_task);
    }

    @Override
    protected void onDestroy() {
        // Audit: listener Firebase realtime dilepas untuk mencegah leak Activity.
        if (database != null && taskListener != null) {
            database.removeEventListener(taskListener);
        }
        super.onDestroy();
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
                // Audit: due date bisa kosong dari Firebase, jadi parsing dibuat null-safe.
                if (t.getDue() != null) {
                    Date dueDate = sdf.parse(t.getDue());
                    if (dueDate != null && today.after(dueDate)) {
                        isOverdue = true;
                    }
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

    private void applyActiveFilterChip() {
        if ("DONE".equals(currentFilter)) {
            chipDone.setChecked(true);
            setActiveChip(chipDone, chipAll, chipProgress, chipOverdue);
        } else if ("ON_PROGRESS".equals(currentFilter)) {
            chipProgress.setChecked(true);
            setActiveChip(chipProgress, chipAll, chipDone, chipOverdue);
        } else if ("OVERDUE".equals(currentFilter)) {
            chipOverdue.setChecked(true);
            setActiveChip(chipOverdue, chipAll, chipDone, chipProgress);
        } else {
            currentFilter = "ALL";
            chipAll.setChecked(true);
            setActiveChip(chipAll, chipDone, chipProgress, chipOverdue);
        }
    }

    // 🔥 FILTER ENGINE FINAL (FIXED TOTAL)
    private void applyFilterAndSearch() {

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
            String judul =
                    t.getJudul() == null
                            ? ""
                            : t.getJudul().toLowerCase();

            String pic =
                    t.getPic() == null
                            ? ""
                            : t.getPic().toLowerCase();

            String priority =
                    t.getPriority() == null
                            ? ""
                            : t.getPriority().toLowerCase();

            // 🔍 SEARCH (AMAN)
            if (!judul.contains(keyword)
                    && !project.contains(keyword)
                    && !jenis.contains(keyword)
                    && !pic.contains(keyword)
                    && !priority.contains(keyword)
                    && !statusSearch.contains(keyword)
                    && !dueText.contains(keyword)) {
                continue;
            }

            String status = normalizeStatus(t.getStatus());

            boolean isOverdue = false;

            try {
                // Audit: hindari NPE saat field due belum ada/masih kosong.
                if (t.getDue() != null) {
                    Date dueDate = sdf.parse(t.getDue());
                    if (dueDate != null && today.after(dueDate)) {
                        isOverdue = true;
                    }
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

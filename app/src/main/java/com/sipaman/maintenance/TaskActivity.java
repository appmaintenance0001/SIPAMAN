package com.sipaman.maintenance;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;
import android.view.View;

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

    // 🔥 CHIP FILTER
    Chip chipAll, chipDone, chipProgress, chipOverdue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        rvTask = findViewById(R.id.rvTask);
        rvTask.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TaskAdapter(filteredList);
        rvTask.setAdapter(adapter);

        etSearch = findViewById(R.id.etSearch);

        // 🔥 INIT CHIP
        chipAll = findViewById(R.id.chipAll);
        chipDone = findViewById(R.id.chipDone);
        chipProgress = findViewById(R.id.chipProgress);
        chipOverdue = findViewById(R.id.chipOverdue);

        // default
        chipAll.setChecked(true);

        // 🔥 CLICK FILTER
        chipAll.setOnClickListener(v -> {
            currentFilter = "ALL";
            animateChip(v);
            applyFilterAndSearch();
        });

        chipDone.setOnClickListener(v -> {
            currentFilter = "DONE";
            animateChip(v);
            applyFilterAndSearch();
        });

        chipProgress.setOnClickListener(v -> {
            currentFilter = "ON_PROGRESS";
            animateChip(v);
            applyFilterAndSearch();
        });

        chipOverdue.setOnClickListener(v -> {
            currentFilter = "OVERDUE";
            animateChip(v);
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

                applyFilterAndSearch();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(TaskActivity.this, "Gagal load data", Toast.LENGTH_SHORT).show();
            }
        });

        // 🔍 SEARCH
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

    // 🔥 ANIMASI CHIP
    private void animateChip(View chip) {
        chip.animate().scaleX(1.1f).scaleY(1.1f).setDuration(120)
                .withEndAction(() ->
                        chip.animate().scaleX(1f).scaleY(1f).setDuration(120)
                );
    }

    // 🔥 NORMALIZE STATUS
    private String normalizeStatus(String status) {
        if (status == null) return "";
        return status.replace(" ", "_").toUpperCase();
    }

    // 🔥 FILTER ENGINE
    private void applyFilterAndSearch() {

        filteredList.clear();

        String keyword = etSearch.getText().toString().toLowerCase();

        SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
        Date today = new Date();

        for (Task t : taskList) {

            if (t == null) continue;

            String project = t.getProject() == null ? "" : t.getProject().toLowerCase();
            String jenis = t.getJenis() == null ? "" : t.getJenis().toLowerCase();

            if (!project.contains(keyword) && !jenis.contains(keyword)) continue;

            String status = normalizeStatus(t.getStatus());

            // DONE tidak masuk overdue
            if (status.equals("DONE") && currentFilter.equals("OVERDUE")) continue;

            boolean isOverdue = false;

            try {
                Date due = sdf.parse(t.getDue());
                if (due != null && today.after(due)) {
                    isOverdue = true;
                }
            } catch (Exception ignored) {}

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
package com.sipaman.maintenance;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import android.content.pm.PackageManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.*;

import androidx.work.*;

import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.utils.ColorTemplate;


public class DashboardActivity extends AppCompatActivity {

    DatabaseReference database;
    RecyclerView recyclerView;
    TaskAdapter adapter;
    List<Task> taskList;

    PieChart pieChart;


    private boolean isFirstLoad = true;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        pieChart = findViewById(R.id.pieChart);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        database = FirebaseDatabase.getInstance().getReference("tasks");

        taskList = new ArrayList<>();
        adapter = new TaskAdapter(taskList);
        recyclerView.setAdapter(adapter);

        prefs = getSharedPreferences("notif_prefs", MODE_PRIVATE);

        // 🔔 WORK MANAGER (BACKGROUND NOTIF)
        PeriodicWorkRequest workRequest =
                new PeriodicWorkRequest.Builder(TaskReminderWorker.class, 15, java.util.concurrent.TimeUnit.MINUTES)
                        .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "TASK_REMINDER",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
        );

        // 🔔 Permission Android 13+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission("android.permission.POST_NOTIFICATIONS") != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 1);
            }
        }

        // 🔥 FIREBASE LISTENER
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                taskList.clear();

                int overdueCount = 0;
                int todayCount = 0;
                int doneCount = 0;
                int progressCount = 0;

                SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
                Date today = new Date();

                for (DataSnapshot data : snapshot.getChildren()) {

                    Task task = data.getValue(Task.class);

                    if (task == null) continue;

                    taskList.add(task);

                    String status = task.getStatus();

// ✅ DONE
                    if ("DONE".equals(status)) {
                        doneCount++;
                        continue;
                    }

// ✅ ON PROGRESS
                    if ("ON_PROGRESS".equals(status)) {
                        progressCount++;
                    }

// tanggal logic
                    try {
                        Date dueDate = sdf.parse(task.getDue());

                        if (dueDate != null) {

                            if (sdf.format(today).equals(sdf.format(dueDate))) {
                                todayCount++;
                            }

                            if (today.after(dueDate)) {
                                overdueCount++;
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                adapter.notifyDataSetChanged();

                List<Integer> colors = new ArrayList<>();

                List<PieEntry> entries = new ArrayList<>();

                if (overdueCount > 0) {
                    entries.add(new PieEntry(overdueCount, "Overdue"));
                    colors.add(android.graphics.Color.RED);
                }

                if (todayCount > 0) {
                    entries.add(new PieEntry(todayCount, "Today"));
                    colors.add(android.graphics.Color.YELLOW);
                }

                if (progressCount > 0) {
                    entries.add(new PieEntry(progressCount, "On Progress"));
                    colors.add(android.graphics.Color.BLUE);
                }

                if (doneCount > 0) {
                    entries.add(new PieEntry(doneCount, "Done"));
                    colors.add(android.graphics.Color.GREEN);
                }

                PieDataSet dataSet = new PieDataSet(entries, "Task Status");
                dataSet.setColors(colors);

// styling
                dataSet.setValueTextSize(14f);

                PieData data = new PieData(dataSet);
                pieChart.setUsePercentValues(false);

                data.setValueTextSize(14f);
                data.setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        return String.valueOf((int) value);
                    }
                });

                pieChart.setData(data);

// animasi
                pieChart.animateY(1000);

// setting tambahan
                pieChart.getDescription().setEnabled(false);
                pieChart.setCenterText("Task Status");
                pieChart.setCenterTextSize(16f);
                pieChart.setUsePercentValues(false);
                pieChart.setDrawHoleEnabled(true);
                pieChart.setHoleRadius(60f);
                pieChart.setTransparentCircleRadius(65f);
                pieChart.invalidate();
                pieChart.getLegend().setTextSize(12f);
                pieChart.setEntryLabelTextSize(12f);

// 🔔 NOTIF SUMMARY (1x per hari)
                if (!isFirstLoad && !isTodayNotified()) {

                    if (overdueCount > 0 || todayCount > 0) {

                        StringBuilder message = new StringBuilder();

                        if (overdueCount > 0) {
                            message.append("🚨 ").append(overdueCount).append(" task overdue\n");
                        }

                        if (todayCount > 0) {
                            message.append("⚠️ ").append(todayCount).append(" task deadline hari ini");
                        }

                        NotificationHelper.showNotification(
                                DashboardActivity.this,
                                "Reminder Task 📊",
                                message.toString().trim(),
                                "summary"
                        );

                        saveTodayNotif();
                    }
                }

                // 🔥 HANDLE CLICK DARI NOTIF
                String taskId = getIntent().getStringExtra("taskId");

                if (taskId != null) {

                    adapter.setHighlightedTask(taskId);

                    recyclerView.post(() -> {
                        for (int i = 0; i < taskList.size(); i++) {
                            if (taskList.get(i).getId().equals(taskId)) {
                                recyclerView.scrollToPosition(i);
                                break;
                            }
                        }
                    });

                    getIntent().removeExtra("taskId");
                }

                isFirstLoad = false;
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(DashboardActivity.this, "Gagal ambil data", Toast.LENGTH_SHORT).show();
            }
        });

        // 🔹 tombol tambah
        FloatingActionButton btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(v -> {
            startActivity(new Intent(DashboardActivity.this, AddTaskActivity.class));
        });
    }

    // 🔥 CEK SUDAH NOTIF HARI INI
    private boolean isTodayNotified() {
        String lastDate = prefs.getString("last_notif_date", "");
        String today = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        return today.equals(lastDate);
    }

    // 🔥 SIMPAN TANGGAL NOTIF
    private void saveTodayNotif() {
        String today = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        prefs.edit().putString("last_notif_date", today).apply();
    }
}
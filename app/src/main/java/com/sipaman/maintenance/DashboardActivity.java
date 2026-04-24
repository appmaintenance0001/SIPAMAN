package com.sipaman.maintenance;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import android.content.pm.PackageManager;
import android.graphics.Color;

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
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieData;


public class DashboardActivity extends AppCompatActivity {

    DatabaseReference database;
    RecyclerView recyclerView;
    TaskAdapter adapter;
    List<Task> taskList;

    PieChart pieChart;

    TextView tvTotal, tvDone, tvProgress, tvOverdue;

    private boolean isFirstLoad = true;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // 🔹 INIT
        pieChart = findViewById(R.id.pieChart);

        recyclerView = findViewById(R.id.rvOverdue);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        taskList = new ArrayList<>(); // 🔥 WAJIB DULU
        adapter = new TaskAdapter(taskList);
        recyclerView.setAdapter(adapter);

        database = FirebaseDatabase.getInstance().getReference("tasks");

        prefs = getSharedPreferences("notif_prefs", MODE_PRIVATE);

        // 🔹 CARD TEXT
        tvTotal = findViewById(R.id.cardTotal).findViewById(R.id.tvTotal);
        tvDone = findViewById(R.id.cardDone).findViewById(R.id.tvTotal);
        tvProgress = findViewById(R.id.cardProgress).findViewById(R.id.tvTotal);
        tvOverdue = findViewById(R.id.cardOverdue).findViewById(R.id.tvTotal);

        TextView labelTotal = findViewById(R.id.cardTotal).findViewById(R.id.tvLabel);
        TextView labelDone = findViewById(R.id.cardDone).findViewById(R.id.tvLabel);
        TextView labelProgress = findViewById(R.id.cardProgress).findViewById(R.id.tvLabel);
        TextView labelOverdue = findViewById(R.id.cardOverdue).findViewById(R.id.tvLabel);

        labelTotal.setText("Total Task");
        labelDone.setText("Selesai");
        labelProgress.setText("On Progress");
        labelOverdue.setText("Overdue");

        // 🔥 FIREBASE
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
                    if (status == null) continue;

                    if (status.equals("DONE")) {
                        doneCount++;
                        continue;
                    }

                    if (status.equals("ON_PROGRESS")) {
                        progressCount++;
                    }

                    try {
                        Date dueDate = sdf.parse(task.getDue());

                        if (dueDate != null) {
                            if (today.after(dueDate)) {
                                overdueCount++;
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // 🔹 UPDATE CARD
                tvTotal.setText(String.valueOf(taskList.size()));
                tvDone.setText(String.valueOf(doneCount));
                tvProgress.setText(String.valueOf(progressCount));
                tvOverdue.setText(String.valueOf(overdueCount));

                adapter.notifyDataSetChanged();

                // 🔹 PIE CHART
                List<PieEntry> entries = new ArrayList<>();
                List<Integer> colors = new ArrayList<>();

                if (doneCount > 0) {
                    entries.add(new PieEntry(doneCount, "Done"));
                    colors.add(Color.GREEN);
                }

                if (progressCount > 0) {
                    entries.add(new PieEntry(progressCount, "Progress"));
                    colors.add(Color.YELLOW);
                }

                if (overdueCount > 0) {
                    entries.add(new PieEntry(overdueCount, "Overdue"));
                    colors.add(Color.RED);
                }

                PieDataSet dataSet = new PieDataSet(entries, "");
                dataSet.setColors(colors);
                dataSet.setValueTextSize(12f);

                PieData data = new PieData(dataSet);

                pieChart.setData(data);
                pieChart.setUsePercentValues(false);
                pieChart.getDescription().setEnabled(false);
                pieChart.setCenterText("Task Status");
                pieChart.animateY(1000);
                pieChart.invalidate();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(DashboardActivity.this, "Gagal ambil data", Toast.LENGTH_SHORT).show();
            }
        });

        // 🔹 BUTTON ADD
        FloatingActionButton btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(v -> {
            startActivity(new Intent(this, AddTaskActivity.class));
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
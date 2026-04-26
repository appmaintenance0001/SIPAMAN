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
import com.google.android.material.bottomnavigation.BottomNavigationView;

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
    List<Task> overdueList = new ArrayList<>();
    OverdueAdapter overdueAdapter;

    PieChart pieChart;

    TextView tvTotal, tvDone, tvProgress, tvOverdue;

    TextView tvLegendDone, tvLegendProgress, tvLegendPending, tvLegendOverdue;

    private boolean isFirstLoad = true;
    SharedPreferences prefs;
    private String normalizeStatus(String status) {
        if (status == null) return "";
        return status.replace(" ", "_").toUpperCase();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);



        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

// 🔥 PASANG LISTENER DULU
        bottomNav.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.nav_dashboard) {
                return true;
            }
            else if (id == R.id.nav_task) {
                startActivity(new Intent(this, TaskActivity.class));
                finish(); // 🔥 WAJIB
                return true;
            }
            else if (id == R.id.nav_calendar) {
                return true;
            }
            else if (id == R.id.nav_project) {
                return true;
            }
            else if (id == R.id.nav_setting) {
                return true;
            }

            return false;
        });

// 🔥 SET ACTIVE MENU PALING AKHIR
        bottomNav.setSelectedItemId(R.id.nav_dashboard);
        // 🔹 INIT
        pieChart = findViewById(R.id.pieChart);


        RecyclerView rvOverdue = findViewById(R.id.rvOverdue);
        rvOverdue.setLayoutManager(new LinearLayoutManager(this));

        overdueAdapter = new OverdueAdapter(overdueList);
        rvOverdue.setAdapter(overdueAdapter);


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

        tvLegendDone = findViewById(R.id.tvLegendDone);
        tvLegendProgress = findViewById(R.id.tvLegendProgress);
        tvLegendPending = findViewById(R.id.tvLegendPending);
        tvLegendOverdue = findViewById(R.id.tvLegendOverdue);



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

                    // 🔥 NORMALISASI STATUS
                    String originalStatus = task.getStatus();
                    String fixedStatus = normalizeStatus(originalStatus);

                    // 🔥 UPDATE KE FIREBASE (AMAN)
                    if (originalStatus != null && !fixedStatus.equals(originalStatus)) {
                        data.getRef().child("status").setValue(fixedStatus);
                    }

                    taskList.add(task);

                    String status = fixedStatus;

                    if ("DONE".equals(status)) {
                        doneCount++;
                        continue;
                    }

                    if ("ON_PROGRESS".equals(status)) {
                        progressCount++;
                    }

                    try {
                        Date dueDate = sdf.parse(task.getDue());

                        if (dueDate != null && today.after(dueDate)) {
                            overdueCount++;
                            overdueList.add(task);
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


                int total = doneCount + progressCount + overdueCount;



// hindari divide by zero
                if (total == 0) total = 1;

// hitung persen
                float donePercent = (doneCount * 100f) / total;
                float progressPercent = (progressCount * 100f) / total;
                float overduePercent = (overdueCount * 100f) / total;
                float pendingPercent = 100 - (donePercent + progressPercent + overduePercent);

                overdueAdapter.notifyDataSetChanged();
                adapter.notifyDataSetChanged();

                tvLegendDone.setText("🟢 Selesai " + Math.round(donePercent) + "%");
                tvLegendProgress.setText("🟡 Progress " + Math.round(progressPercent) + "%");
                tvLegendPending.setText("⚪ Pending " + Math.round(pendingPercent) + "%");
                tvLegendOverdue.setText("🔴 Overdue " + Math.round(overduePercent) + "%");

                // 🔹 PIE CHART
                List<PieEntry> entries = new ArrayList<>();
                List<Integer> colors = new ArrayList<>();

                if (doneCount > 0) {
                    entries.add(new PieEntry(doneCount, "Selesai"));
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

                if (pendingPercent > 0) {
                    entries.add(new PieEntry(pendingPercent, "Pending"));
                    colors.add(Color.GRAY);
                }

                PieDataSet dataSet = new PieDataSet(entries, "");
                dataSet.setColors(colors);
                dataSet.setValueTextSize(12f);

                PieData data = new PieData(dataSet);

// tampil persen di chart
                pieChart.setUsePercentValues(true);

                data.setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        return Math.round(value) + "%";
                    }
                });

                pieChart.setData(data);
                pieChart.getDescription().setEnabled(false);
                pieChart.setCenterText("Status Task");
                pieChart.animateY(1000);
                pieChart.invalidate();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(DashboardActivity.this, "Gagal ambil data", Toast.LENGTH_SHORT).show();
            }
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
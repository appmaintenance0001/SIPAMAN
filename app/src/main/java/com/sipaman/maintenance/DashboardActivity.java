package com.sipaman.maintenance;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {

    private final List<Task> overdueList = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());

    private DatabaseReference taskRef;
    private OverdueAdapter overdueAdapter;
    private PieChart pieChart;
    private TextView tvWelcomeName, tvTotal, tvDone, tvProgress, tvOverdue;
    private TextView tvLegendDone, tvLegendProgress, tvLegendPending, tvLegendOverdue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        bindViews();
        setupToolbarActions();
        setupBottomNav();
        setupSummaryCards();
        loadUserName();
        loadDashboardData();
    }

    private void bindViews() {
        tvWelcomeName = findViewById(R.id.tvWelcomeName);
        pieChart = findViewById(R.id.pieChart);

        tvTotal = findViewById(R.id.cardTotal).findViewById(R.id.tvTotal);
        tvDone = findViewById(R.id.cardDone).findViewById(R.id.tvTotal);
        tvProgress = findViewById(R.id.cardProgress).findViewById(R.id.tvTotal);
        tvOverdue = findViewById(R.id.cardOverdue).findViewById(R.id.tvTotal);

        tvLegendDone = findViewById(R.id.tvLegendDone);
        tvLegendProgress = findViewById(R.id.tvLegendProgress);
        tvLegendPending = findViewById(R.id.tvLegendPending);
        tvLegendOverdue = findViewById(R.id.tvLegendOverdue);

        TextView labelTotal = findViewById(R.id.cardTotal).findViewById(R.id.tvLabel);
        TextView labelDone = findViewById(R.id.cardDone).findViewById(R.id.tvLabel);
        TextView labelProgress = findViewById(R.id.cardProgress).findViewById(R.id.tvLabel);
        TextView labelOverdue = findViewById(R.id.cardOverdue).findViewById(R.id.tvLabel);

        labelTotal.setText("Total Task");
        labelDone.setText("Done");
        labelProgress.setText("On Progress");
        labelOverdue.setText("Overdue");

        RecyclerView rvOverdue = findViewById(R.id.rvOverdue);
        rvOverdue.setLayoutManager(new LinearLayoutManager(this));
        overdueAdapter = new OverdueAdapter(overdueList);
        rvOverdue.setAdapter(overdueAdapter);
    }

    private void setupToolbarActions() {
        findViewById(R.id.btnDashboardSearch).setOnClickListener(v ->
                startActivity(new Intent(this, TaskActivity.class))
        );

        findViewById(R.id.btnNotification).setOnClickListener(v ->
                startActivity(new Intent(this, NotificationActivity.class))
        );

        findViewById(R.id.btnLihatSemua).setOnClickListener(v ->
                startActivity(new Intent(this, TaskActivity.class).putExtra("filter", "OVERDUE"))
        );
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_dashboard) {
                return true;
            } else if (id == R.id.nav_task) {
                startActivity(new Intent(this, TaskActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_calendar) {
                startActivity(new Intent(this, CalendarActivity.class));
                finish();
                return true;
            }
            else if (id == R.id.nav_project) {
                startActivity(new Intent(this, ProjectActivity.class));
                finish();
                return true;
            }
            else if (id == R.id.nav_setting) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }

            Toast.makeText(this, "Menu belum tersedia", Toast.LENGTH_SHORT).show();
            return true;
        });
        bottomNav.setSelectedItemId(R.id.nav_dashboard);
    }

    private void setupSummaryCards() {
        findViewById(R.id.cardTotal).setBackgroundResource(R.drawable.bg_card_blue);
        findViewById(R.id.cardDone).setBackgroundResource(R.drawable.bg_card_green);
        findViewById(R.id.cardProgress).setBackgroundResource(R.drawable.bg_card_yellow);
        findViewById(R.id.cardOverdue).setBackgroundResource(R.drawable.bg_card_red);
    }

    private void loadUserName() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            tvWelcomeName.setText("Selamat datang");
            return;
        }

        String fallbackName = user.getDisplayName();
        if (isEmpty(fallbackName) && user.getEmail() != null) {
            fallbackName = user.getEmail().split("@")[0];
        }
        tvWelcomeName.setText("Selamat datang, " + (isEmpty(fallbackName) ? "User" : fallbackName));

        FirebaseDatabase.getInstance()
                .getReference("users")
                .child(user.getUid())
                .child("name")
                .get()
                .addOnSuccessListener(snapshot -> {
                    String name = snapshot.getValue(String.class);
                    if (!isEmpty(name)) {
                        tvWelcomeName.setText("Selamat datang, " + name);
                    }
                });
    }

    private void loadDashboardData() {
        taskRef = FirebaseDatabase.getInstance().getReference("tasks");
        taskRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                int totalCount = 0;
                int doneCount = 0;
                int progressCount = 0;
                int pendingCount = 0;
                int overdueCount = 0;

                overdueList.clear();

                for (DataSnapshot data : snapshot.getChildren()) {
                    Task task = data.getValue(Task.class);
                    if (task == null) continue;
                    task.setId(data.getKey());

                    totalCount++;

                    String status = normalizeStatus(task.getStatus());
                    boolean done = "DONE".equals(status);
                    boolean overdue = !done && isOverdue(task.getDue());

                    if (done) {
                        doneCount++;
                    } else if (overdue) {
                        overdueCount++;
                        overdueList.add(task);
                    } else if ("ON_PROGRESS".equals(status)) {
                        progressCount++;
                    } else {
                        pendingCount++;
                    }
                }

                sortAndLimitOverdue();
                updateSummary(totalCount, doneCount, progressCount, overdueCount);
                updateChart(doneCount, progressCount, pendingCount, overdueCount);
                overdueAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(DashboardActivity.this, "Gagal ambil data dashboard", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sortAndLimitOverdue() {
        Collections.sort(overdueList, new Comparator<Task>() {
            @Override
            public int compare(Task first, Task second) {
                return parseDate(second.getDue()).compareTo(parseDate(first.getDue()));
            }
        });

        while (overdueList.size() > 5) {
            overdueList.remove(overdueList.size() - 1);
        }
    }

    private void updateSummary(int total, int done, int progress, int overdue) {
        tvTotal.setText(String.valueOf(total));
        tvDone.setText(String.valueOf(done));
        tvProgress.setText(String.valueOf(progress));
        tvOverdue.setText(String.valueOf(overdue));
    }

    private void updateChart(int done, int progress, int pending, int overdue) {
        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        if (done > 0) {
            entries.add(new PieEntry(done, "Done"));
            colors.add(Color.parseColor("#16A34A"));
        }

        if (progress > 0) {
            entries.add(new PieEntry(progress, "Progress"));
            colors.add(Color.parseColor("#FACC15"));
        }

        if (pending > 0) {
            entries.add(new PieEntry(pending, "Pending"));
            colors.add(Color.parseColor("#94A3B8"));
        }

        if (overdue > 0) {
            entries.add(new PieEntry(overdue, "Overdue"));
            colors.add(Color.parseColor("#E11D48"));
        }

        if (entries.isEmpty()) {
            entries.add(new PieEntry(1, "Kosong"));
            colors.add(Color.parseColor("#E5E7EB"));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setValueTextColor(Color.parseColor("#111827"));
        dataSet.setValueTextSize(12f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf(Math.round(value));
            }
        });

        int chartTotal = Math.max(1, done + progress + pending + overdue);
        tvLegendDone.setText("Done " + percent(done, chartTotal) + "%");
        tvLegendProgress.setText("Progress " + percent(progress, chartTotal) + "%");
        tvLegendPending.setText("Pending " + percent(pending, chartTotal) + "%");
        tvLegendOverdue.setText("Overdue " + percent(overdue, chartTotal) + "%");

        pieChart.setData(data);
        pieChart.setUsePercentValues(false);
        pieChart.setDrawEntryLabels(false);
        pieChart.setHoleRadius(58f);
        pieChart.setTransparentCircleRadius(62f);
        pieChart.setCenterText("Status\nTask");
        pieChart.setCenterTextColor(Color.parseColor("#173A6A"));
        pieChart.setCenterTextSize(14f);
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.animateY(700);
        pieChart.invalidate();
    }

    private int percent(int value, int total) {
        return Math.round((value * 100f) / total);
    }

    private String normalizeStatus(String status) {
        if (status == null) return "";
        status = status.trim().toUpperCase(Locale.US).replace(" ", "_");
        if (status.contains("DONE")) return "DONE";
        if (status.contains("PROGRESS")) return "ON_PROGRESS";
        if (status.contains("OVERDUE")) return "OVERDUE";
        return status;
    }

    private boolean isOverdue(String dueDate) {
        try {
            Date due = dateFormat.parse(dueDate);
            return due != null && due.before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    private Date parseDate(String value) {
        try {
            Date date = dateFormat.parse(value);
            return date == null ? new Date(0) : date;
        } catch (Exception e) {
            return new Date(0);
        }
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}

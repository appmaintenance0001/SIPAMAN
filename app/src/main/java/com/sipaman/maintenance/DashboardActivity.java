package com.sipaman.maintenance;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
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
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {

    private final List<Task> overdueList = new ArrayList<>();
    private final List<Task> dueSoonList = new ArrayList<>();
    private final List<ActivityLog> recentActivityList = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());

    private final List<ProjectSummary> projectSummaryList = new ArrayList<>();

    private ProjectSummaryAdapter projectSummaryAdapter;
    private DashboardTaskAdapter overdueDashboardAdapter;
    private DashboardTaskAdapter dueSoonDashboardAdapter;
    private RecentActivityDashboardAdapter recentActivityAdapter;

    private DatabaseReference taskRef;
    private DatabaseReference projectRef;
    private DatabaseReference activityLogRef;
    private Query activityLogQuery;
    private ValueEventListener taskListener;
    private ValueEventListener projectListener;
    private ValueEventListener activityLogListener;
    private PieChart pieChart;
    private TextView tvWelcomeName;
    private TextView tvRole;
    private TextView chipDate;
    private TextView chipSite;
    private TextView tvNotificationBadge;
    private TextView tvTotal;
    private TextView tvDone;
    private TextView tvProgress;
    private TextView tvOverdue;
    private TextView tvProject;
    private TextView tvPic;
    private TextView tvCompletion;
    private TextView tvDueToday;
    private TextView tvLegendDone, tvLegendProgress, tvLegendPending, tvLegendOverdue;
    private TextView tvEmptyProjectSummary;
    private TextView tvEmptyDueSoon;
    private TextView tvEmptyOverdue;
    private TextView tvEmptyRecentActivity;
    private RecyclerView rvProjectSummary;
    private RecyclerView rvDueSoon;
    private RecyclerView rvOverdue;
    private RecyclerView rvRecentActivity;
    private CircularProgressIndicator progressCircle;

    private LinearProgressIndicator progressBar;

    private TextView tvProgressPercent;
    private TextView tvProgressTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        bindViews();
        setupToolbarActions();
        setupBottomNav();
        setupSummaryCards();
        setupQuickAction();

        loadUserName();
        loadCurrentDate();

        loadDashboardData();
        loadProjectCount();
        loadRecentActivities();
    }

    private void loadCurrentDate() {

        TextView chipDate = findViewById(R.id.chipDate);

        SimpleDateFormat sdf =
                new SimpleDateFormat("dd MMM yyyy", new Locale("id", "ID"));

        String tanggal = sdf.format(new Date());

        chipDate.setText("📅 " + tanggal);
    }

    private void bindViews() {
        tvWelcomeName = findViewById(R.id.tvWelcomeName);
        tvRole = findViewById(R.id.tvRole);
        chipDate = findViewById(R.id.chipDate);
        chipSite = findViewById(R.id.chipSite);
        tvNotificationBadge = findViewById(R.id.tvNotificationBadge);
        pieChart = findViewById(R.id.pieChart);

        tvTotal = findViewById(R.id.cardTotal).findViewById(R.id.tvTotal);
        tvDone = findViewById(R.id.cardDone).findViewById(R.id.tvTotal);
        tvProgress = findViewById(R.id.cardProgress).findViewById(R.id.tvTotal);
        tvOverdue = findViewById(R.id.cardOverdue).findViewById(R.id.tvTotal);
        tvProject = findViewById(R.id.cardProject).findViewById(R.id.tvTotal);
        tvPic = findViewById(R.id.cardPic).findViewById(R.id.tvTotal);
        tvCompletion = findViewById(R.id.cardCompletion)
                .findViewById(R.id.tvTotal);
        tvDueToday = findViewById(R.id.cardDueToday)
                .findViewById(R.id.tvTotal);

        TextView labelDueToday = findViewById(R.id.cardDueToday)
                .findViewById(R.id.tvLabel);

        labelDueToday.setText("Due Today");

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
        TextView labelProject =
                findViewById(R.id.cardProject).findViewById(R.id.tvLabel);

        TextView labelPic =
                findViewById(R.id.cardPic).findViewById(R.id.tvLabel);

        labelProject.setText("Project");
        labelPic.setText("PIC");

        rvOverdue = findViewById(R.id.rvOverdue);
        rvOverdue.setLayoutManager(new LinearLayoutManager(this));
        overdueDashboardAdapter = new DashboardTaskAdapter(overdueList, true);
        rvOverdue.setAdapter(overdueDashboardAdapter);

        rvDueSoon = findViewById(R.id.rvDueSoon);

        rvDueSoon.setLayoutManager(new LinearLayoutManager(this));

        dueSoonDashboardAdapter = new DashboardTaskAdapter(dueSoonList, false);

        rvDueSoon.setAdapter(dueSoonDashboardAdapter);

        rvRecentActivity = findViewById(R.id.rvRecentActivity);
        rvRecentActivity.setLayoutManager(new LinearLayoutManager(this));
        recentActivityAdapter = new RecentActivityDashboardAdapter(recentActivityList);
        rvRecentActivity.setAdapter(recentActivityAdapter);

        rvProjectSummary =
                findViewById(R.id.rvProjectSummary);

        rvProjectSummary.setLayoutManager(
                new LinearLayoutManager(this));

        projectSummaryAdapter =
                new ProjectSummaryAdapter(projectSummaryList);

        rvProjectSummary.setAdapter(projectSummaryAdapter);

        TextView labelCompletion =
                findViewById(R.id.cardCompletion)
                        .findViewById(R.id.tvLabel);

        labelCompletion.setText("Completion");

        progressCircle = findViewById(R.id.progressCircle);
        progressBar = findViewById(R.id.progressBar);
        tvProgressPercent = findViewById(R.id.tvProgressPercent);
        tvProgressTitle = findViewById(R.id.tvProgressTitle);
        tvEmptyProjectSummary = findViewById(R.id.tvEmptyProjectSummary);
        tvEmptyDueSoon = findViewById(R.id.tvEmptyDueSoon);
        tvEmptyOverdue = findViewById(R.id.tvEmptyOverdue);
        tvEmptyRecentActivity = findViewById(R.id.tvEmptyRecentActivity);

        chipDate.setText(new SimpleDateFormat("dd MMM yyyy", Locale.US).format(new Date()));
        chipSite.setText("SI-PAMAN");
    }

    private void setupQuickAction() {

        setupAction(
                R.id.actionAddTask,
                R.drawable.ic_add_task,
                "Tambah Task",
                "Buat Task Baru"
        );

        setupAction(
                R.id.actionCalendar,
                R.drawable.ic_calendar_dashboard,
                "Kalender",
                "Lihat Jadwal"
        );

        setupAction(
                R.id.actionProject,
                R.drawable.ic_project,
                "Project",
                "Kelola Project"
        );

        setupAction(
                R.id.actionReport,
                R.drawable.ic_report,
                "Report",
                "Lihat Laporan"
        );

        findViewById(R.id.actionAddTask).setOnClickListener(v ->
                startActivity(new Intent(this, AddTaskActivity.class))
        );

        findViewById(R.id.actionCalendar).setOnClickListener(v ->
                startActivity(new Intent(this, CalendarActivity.class))
        );

        findViewById(R.id.actionProject).setOnClickListener(v ->
                startActivity(new Intent(this, ProjectActivity.class))
        );

        findViewById(R.id.actionReport).setOnClickListener(v ->
                startActivity(new Intent(this, HistoryActivity.class))
        );

    }

    private void setupAction(
            int id,
            int icon,
            String title,
            String subtitle) {

        View view = findViewById(id);

        ImageView image =
                view.findViewById(R.id.imgAction);

        TextView tvTitle =
                view.findViewById(R.id.tvTitle);

        TextView tvSubtitle =
                view.findViewById(R.id.tvSubtitle);

        image.setImageResource(icon);

        tvTitle.setText(title);

        tvSubtitle.setText(subtitle);

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

        findViewById(R.id.btnProjectLihatSemua).setOnClickListener(v ->
                startActivity(new Intent(this, ProjectActivity.class))
        );

        findViewById(R.id.btnRecentActivityAll).setOnClickListener(v ->
                startActivity(new Intent(this, HistoryActivity.class))
        );
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_dashboard) {
                return true;
            }

            Intent intent = null;

            if (id == R.id.nav_task) {
                intent = new Intent(this, TaskActivity.class);

            } else if (id == R.id.nav_calendar) {
                intent = new Intent(this, CalendarActivity.class);

            } else if (id == R.id.nav_project) {
                intent = new Intent(this, ProjectActivity.class);

            } else if (id == R.id.nav_setting) {
                intent = new Intent(this, SettingsActivity.class);
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

        bottomNav.setSelectedItemId(R.id.nav_dashboard);
    }

    private void setupSummaryCards() {
        findViewById(R.id.cardTotal).setBackgroundResource(R.drawable.bg_summary_blue);
        findViewById(R.id.cardDone).setBackgroundResource(R.drawable.bg_summary_green);
        findViewById(R.id.cardProgress).setBackgroundResource(R.drawable.bg_summary_orange);
        findViewById(R.id.cardOverdue).setBackgroundResource(R.drawable.bg_summary_red);
        findViewById(R.id.cardProject).setBackgroundResource(R.drawable.bg_summary_purple);
        findViewById(R.id.cardPic).setBackgroundResource(R.drawable.bg_summary_cyan);
        findViewById(R.id.cardCompletion).setBackgroundResource(R.drawable.bg_summary_blue);
        findViewById(R.id.cardDueToday).setBackgroundResource(R.drawable.bg_summary_orange);
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
        tvWelcomeName.setText(isEmpty(fallbackName) ? "User" : fallbackName);
        tvRole.setText("Maintenance");

        FirebaseDatabase.getInstance()
                .getReference("users")
                .child(user.getUid())
                .get()
                .addOnSuccessListener(snapshot -> {

                    String name = snapshot.child("name").getValue(String.class);
                    String role = snapshot.child("role").getValue(String.class);
                    String site = snapshot.child("site").getValue(String.class);

                    if (name != null)
                        tvWelcomeName.setText(name);

                    if (role != null)
                        tvRole.setText(role);

                    if (site != null && !site.isEmpty()) {
                        chipSite.setText("📍 " + site);
                    } else {
                        chipSite.setText("📍 Pulubala");
                    }

                });
    }

    private void loadDashboardData() {
        taskRef = FirebaseDatabase.getInstance().getReference("tasks");
        taskListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (isFinishing() || isDestroyed()) return;

                int totalCount = 0;
                int doneCount = 0;
                int progressCount = 0;
                int pendingCount = 0;
                int overdueCount = 0;
                int dueTodayCount = 0;

                Set<String> picSet = new HashSet<>();
                Map<String, ProjectSummary> projectMap = new HashMap<>();

                overdueList.clear();
                dueSoonList.clear();
                projectSummaryList.clear();

                for (DataSnapshot data : snapshot.getChildren()) {
                    Task task = data.getValue(Task.class);
                    if (task == null) continue;
                    task.setId(data.getKey());

                    String projectId = task.getProjectId();

                    if (projectId == null || projectId.trim().isEmpty()) {
                        projectId = "UNKNOWN";
                    }

                    ProjectSummary summary = projectMap.get(projectId);

                    if (summary == null) {

                        summary = new ProjectSummary(
                                projectId,
                                task.getProject()
                        );

                        projectMap.put(projectId, summary);

                    }

                    summary.increaseTotal();


                    if (!isEmpty(task.getPic())) {
                        picSet.add(task.getPic().trim());
                    }

                    totalCount++;

                    String status = normalizeStatus(task.getStatus());
                    boolean done = "DONE".equals(status);
                    boolean overdue = !done && isOverdue(task.getDue());
                    if (!done && isDueToday(task.getDue())) {
                        dueTodayCount++;
                    }

                    if (done) {

                        doneCount++;

                        summary.increaseDone();

                    } else if (overdue) {

                        overdueCount++;

                        overdueList.add(task);

                        summary.increaseOverdue();

                    } else {

                        if (isDueSoon(task.getDue())) {
                            dueSoonList.add(task);
                        }

                        if ("ON_PROGRESS".equals(status)) {

                            progressCount++;

                            summary.increaseProgress();

                        } else {

                            pendingCount++;

                        }
                    }
                }

                sortAndLimitOverdue();
                sortAndLimitDueSoon();

                updateSummary(totalCount, doneCount, progressCount, overdueCount);
                tvNotificationBadge.setText(String.valueOf(overdueCount + dueSoonList.size()));
                tvNotificationBadge.setVisibility((overdueCount + dueSoonList.size()) > 0 ? View.VISIBLE : View.GONE);

                int completionRate = 0;

                if (totalCount > 0) {
                    completionRate = (doneCount * 100) / totalCount;
                }

                tvCompletion.setText(completionRate + "%");

                updateChart(doneCount, progressCount, pendingCount, overdueCount);

                tvPic.setText(String.valueOf(picSet.size()));
                tvDueToday.setText(String.valueOf(dueTodayCount));

// Tambahkan hasil perhitungan Project
                projectSummaryList.addAll(projectMap.values());
                projectSummaryAdapter.notifyDataSetChanged();

                overdueDashboardAdapter.notifyDataSetChanged();
                dueSoonDashboardAdapter.notifyDataSetChanged();
                updateTaskEmptyStates();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                if (isFinishing() || isDestroyed()) return;
                Toast.makeText(DashboardActivity.this, "Gagal ambil data dashboard", Toast.LENGTH_SHORT).show();
            }
        };

        taskRef.addValueEventListener(taskListener);
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

    private void sortAndLimitDueSoon() {
        Collections.sort(dueSoonList, new Comparator<Task>() {
            @Override
            public int compare(Task first, Task second) {
                return parseDate(first.getDue()).compareTo(parseDate(second.getDue()));
            }
        });

        while (dueSoonList.size() > 5) {
            dueSoonList.remove(dueSoonList.size() - 1);
        }
    }

    private void updateSummary(int total, int done, int progress, int overdue) {
        tvTotal.setText(String.valueOf(total));
        tvDone.setText(String.valueOf(done));
        tvProgress.setText(String.valueOf(progress));
        tvOverdue.setText(String.valueOf(overdue));
        tvProgressTitle.setText(done + " dari " + total + " Task Selesai");

        int completion = 0;

        if(total>0){

            completion = done*100/total;

        }

        progressCircle.setProgress(completion);

        progressBar.setProgress(completion);

        tvProgressPercent.setText(completion+"%");
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
        if (isEmpty(dueDate)) return false;

        try {
            Date due = dateFormat.parse(dueDate);
            return due != null && due.before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isDueSoon(String dueDate) {
        if (isEmpty(dueDate)) return false;

        try {

            Date due = dateFormat.parse(dueDate);

            if (due == null)
                return false;

            long diff = due.getTime() - System.currentTimeMillis();

            long days = diff / (1000 * 60 * 60 * 24);

            return days >= 0 && days <= 7;

        } catch (Exception e) {

            return false;

        }

    }

    private boolean isDueToday(String dueDate) {
        if (isEmpty(dueDate)) return false;

        try {
            Date due = dateFormat.parse(dueDate);
            if (due == null) return false;

            Calendar dueCalendar = Calendar.getInstance();
            dueCalendar.setTime(due);

            Calendar today = Calendar.getInstance();

            return dueCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                    && dueCalendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR);
        } catch (Exception e) {
            return false;
        }
    }

    private Date parseDate(String value) {
        if (isEmpty(value)) return new Date(0);

        try {
            Date date = dateFormat.parse(value);
            return date == null ? new Date(0) : date;
        } catch (Exception e) {
            return new Date(0);
        }
    }

    private void loadProjectCount() {

        projectRef = FirebaseDatabase.getInstance().getReference("projects");

        projectListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (isFinishing() || isDestroyed()) return;

                int totalProject = (int) snapshot.getChildrenCount();

                tvProject.setText(String.valueOf(totalProject));

            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        };

        projectRef.addValueEventListener(projectListener);

    }

    private void loadRecentActivities() {
        activityLogRef = FirebaseDatabase.getInstance().getReference("activity_logs");
        activityLogListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (isFinishing() || isDestroyed()) return;

                        recentActivityList.clear();

                        for (DataSnapshot data : snapshot.getChildren()) {
                            ActivityLog log = data.getValue(ActivityLog.class);
                            if (log == null) continue;

                            log.setId(data.getKey());
                            recentActivityList.add(log);
                        }

                        Collections.sort(recentActivityList, new Comparator<ActivityLog>() {
                            @Override
                            public int compare(ActivityLog first, ActivityLog second) {
                                return Long.compare(second.getTimestamp(), first.getTimestamp());
                            }
                        });

                        while (recentActivityList.size() > 5) {
                            recentActivityList.remove(recentActivityList.size() - 1);
                        }

                        recentActivityAdapter.notifyDataSetChanged();
                        updateRecentActivityEmptyState();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        if (isFinishing() || isDestroyed()) return;
                        Toast.makeText(DashboardActivity.this, "Gagal load aktivitas terbaru", Toast.LENGTH_SHORT).show();
                    }
                };

        // Audit: Dashboard hanya membutuhkan 5 aktivitas terbaru, query dibatasi untuk mengurangi data transfer.
        activityLogQuery = activityLogRef.orderByChild("timestamp")
                .limitToLast(5);
        activityLogQuery.addValueEventListener(activityLogListener);
    }

    @Override
    protected void onDestroy() {
        // Audit: semua listener realtime Dashboard dilepas untuk mencegah memory leak.
        if (taskRef != null && taskListener != null) {
            taskRef.removeEventListener(taskListener);
        }
        if (projectRef != null && projectListener != null) {
            projectRef.removeEventListener(projectListener);
        }
        if (activityLogQuery != null && activityLogListener != null) {
            activityLogQuery.removeEventListener(activityLogListener);
        }
        super.onDestroy();
    }

    private void updateTaskEmptyStates() {
        setEmptyState(rvProjectSummary, tvEmptyProjectSummary, projectSummaryList.isEmpty());
        setEmptyState(rvDueSoon, tvEmptyDueSoon, dueSoonList.isEmpty());
        setEmptyState(rvOverdue, tvEmptyOverdue, overdueList.isEmpty());
    }

    private void updateRecentActivityEmptyState() {
        setEmptyState(rvRecentActivity, tvEmptyRecentActivity, recentActivityList.isEmpty());
    }

    private void setEmptyState(RecyclerView recyclerView, TextView emptyView, boolean empty) {
        recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
        emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}

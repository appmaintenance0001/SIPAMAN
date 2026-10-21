package com.sipaman.maintenance;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.core.view.GravityCompat;
import com.google.android.material.navigation.NavigationView;

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

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    // Header Navigation Drawer
    private ImageView imgProfile;
    private TextView tvDrawerName;
    private TextView tvDrawerEmail;
    private TextView tvDrawerRole;

    private final ExecutorService profileImageExecutor =
            Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        bindViews();
        setupToolbarActions();
        setupBottomNav();
        setupSummaryCards();
        setupQuickAction();

        loadCurrentDate();

        loadDashboardData();
        loadProjectCount();
        loadRecentActivities();


    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserName();
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
        recentActivityAdapter = new RecentActivityDashboardAdapter(
                recentActivityList,
                log -> openActivityFromLog(log)
        );

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

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        // Hubungkan komponen header Navigation Drawer
        View headerView = navigationView.getHeaderView(0);

        imgProfile = headerView.findViewById(R.id.imgProfile);
        tvDrawerName = headerView.findViewById(R.id.tvDrawerName);
        tvDrawerEmail = headerView.findViewById(R.id.tvDrawerEmail);
        tvDrawerRole = headerView.findViewById(R.id.tvDrawerRole);
        ImageView btnMenu;

        btnMenu = findViewById(R.id.btnMenu);

        btnMenu.setOnClickListener(v -> {
            drawerLayout.openDrawer(GravityCompat.START);
        });

        navigationView.setNavigationItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.menu_profile) {

                startActivity(new Intent(
                        DashboardActivity.this,
                        ProfileActivity.class));

            } else if (id == R.id.menu_project) {

                startActivity(new Intent(this, ProjectActivity.class));

            } else if (id == R.id.menu_calendar) {

                startActivity(new Intent(this, CalendarActivity.class));

            } else if (id == R.id.menu_report) {

                startActivity(new Intent(this, ReportSummaryActivity.class));

            } else if (id == R.id.menu_settings) {

                startActivity(new Intent(this, SettingsActivity.class));

            } else if (id == R.id.menu_ai_assistant) {

                startActivity(new Intent(this, AiAssistantActivity.class));

            } else if (id == R.id.menu_logout) {

                FirebaseAuth.getInstance().signOut();

                Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

                finish();
            }

            drawerLayout.closeDrawer(GravityCompat.START);

            return true;
        });

        chipDate.setText(new SimpleDateFormat("dd MMM yyyy", Locale.US).format(new Date()));
        chipSite.setText("SI-PAMAN");
    }


    private void openActivityFromLog(ActivityLog log) {
        if (log == null) {
            startActivity(new Intent(this, HistoryActivity.class));
            return;
        }

        String type = log.getType() == null
                ? ""
                : log.getType().toLowerCase(Locale.ROOT);

        String entityType = log.getEntityType() == null
                ? ""
                : log.getEntityType().toLowerCase(Locale.ROOT);

        String entityId = log.getEntityId();

        // Log penghapusan tidak diarahkan ke detail karena datanya
        // mungkin sudah tidak tersedia.
        if (type.contains("hapus")
                || type.contains("delete")
                || entityId == null
                || entityId.trim().isEmpty()) {

            startActivity(new Intent(this, HistoryActivity.class));
            return;
        }

        if ("project".equals(entityType)) {
            Intent intent = new Intent(this, ProjectDetailActivity.class);
            intent.putExtra("projectId", entityId);
            startActivity(intent);
            return;
        }

        if ("task".equals(entityType)) {
            Intent intent = new Intent(this, DetailTaskActivity.class);
            intent.putExtra("id", entityId);
            startActivity(intent);
            return;
        }

        // Kompatibilitas dengan log lama yang belum memiliki entityType.
        String taskId = log.getTaskId();

        if (taskId != null && !taskId.trim().isEmpty()) {
            Intent intent = new Intent(this, DetailTaskActivity.class);
            intent.putExtra("id", taskId);
            startActivity(intent);
        } else {
            startActivity(new Intent(this, HistoryActivity.class));
        }
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

        // Dashboard-only visual treatment; click actions and destinations remain unchanged.
        setupQuickActionStyle(R.id.actionAddTask, R.drawable.bg_dashboard_action_blue,
                R.drawable.bg_dashboard_summary_icon_blue, "#2563EB");
        setupQuickActionStyle(R.id.actionCalendar, R.drawable.bg_dashboard_action_purple,
                R.drawable.bg_dashboard_summary_icon_purple, "#7C3AED");
        setupQuickActionStyle(R.id.actionProject, R.drawable.bg_dashboard_action_green,
                R.drawable.bg_dashboard_summary_icon_green, "#16803D");
        setupQuickActionStyle(R.id.actionReport, R.drawable.bg_dashboard_action_orange,
                R.drawable.bg_dashboard_summary_icon_orange, "#EA580C");

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
                startActivity(new Intent(this, ReportSummaryActivity.class))
        );

    }

    private void setupQuickActionStyle(
            int actionId,
            int backgroundRes,
            int iconBackgroundRes,
            String iconColor) {
        View action = findViewById(actionId);
        action.setBackgroundResource(backgroundRes);

        ImageView icon = action.findViewById(R.id.imgAction);
        icon.setColorFilter(Color.parseColor(iconColor));

        if (icon.getParent() instanceof View) {
            ((View) icon.getParent()).setBackgroundResource(iconBackgroundRes);
        }
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
        bottomNav.setItemActiveIndicatorEnabled(false);

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

        // Icon accents make the existing Firebase-backed summaries easier to scan.
        setupSummaryIcon(R.id.cardTotal, R.drawable.ic_task,
                R.drawable.bg_dashboard_summary_icon_blue, "#2563EB");
        setupSummaryIcon(R.id.cardDone, R.drawable.ic_check,
                R.drawable.bg_dashboard_summary_icon_green, "#16803D");
        setupSummaryIcon(R.id.cardProgress, R.drawable.ic_maintenance,
                R.drawable.bg_dashboard_summary_icon_orange, "#D97706");
        setupSummaryIcon(R.id.cardOverdue, R.drawable.ic_flag,
                R.drawable.bg_dashboard_summary_icon_red, "#E11D48");
        setupSummaryIcon(R.id.cardProject, R.drawable.ic_project,
                R.drawable.bg_dashboard_summary_icon_purple, "#7C3AED");
        setupSummaryIcon(R.id.cardPic, R.drawable.ic_user,
                R.drawable.bg_dashboard_summary_icon_cyan, "#0891B2");
        setupSummaryIcon(R.id.cardCompletion, R.drawable.ic_aman,
                R.drawable.bg_dashboard_summary_icon_blue, "#2563EB");
        setupSummaryIcon(R.id.cardDueToday, R.drawable.ic_calendar,
                R.drawable.bg_dashboard_summary_icon_orange, "#D97706");
    }

    private void setupSummaryIcon(int cardId, int iconRes, int backgroundRes, String tintColor) {
        ImageView icon = findViewById(cardId).findViewById(R.id.imgSummaryIcon);
        icon.setImageResource(iconRes);
        icon.setColorFilter(Color.parseColor(tintColor));

        if (icon.getParent() instanceof View) {
            ((View) icon.getParent()).setBackgroundResource(backgroundRes);
        }
    }

    private void loadUserName() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            tvWelcomeName.setText("Selamat datang");
            tvRole.setText("Maintenance");

            tvDrawerName.setText("Pengguna");
            tvDrawerEmail.setText("Tidak ada email");
            tvDrawerRole.setText("Maintenance");

            return;
        }

        // Email diambil langsung dari akun Firebase Authentication
        String email = user.getEmail();

        tvDrawerEmail.setText(
                email != null && !email.trim().isEmpty()
                        ? email
                        : "Email tidak tersedia"
        );

        // Nama sementara dari akun, sebelum data Realtime Database dimuat
        String fallbackName = user.getDisplayName();

        if (isEmpty(fallbackName) && email != null) {
            fallbackName = email.split("@")[0];
        }

        if (isEmpty(fallbackName)) {
            fallbackName = "Pengguna";
        }

        tvWelcomeName.setText(fallbackName);
        tvDrawerName.setText(fallbackName);
        tvRole.setText("Maintenance");
        tvDrawerRole.setText("Maintenance");

        // Ambil profil asli dari Firebase Realtime Database
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(user.getUid());

        userRef.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (isFinishing() || isDestroyed()) return;

                        String name = snapshot.child("name")
                                .getValue(String.class);

                        String role = snapshot.child("role")
                                .getValue(String.class);

                        String site = snapshot.child("site")
                                .getValue(String.class);

                        String photoUrl = snapshot.child("photoUrl")
                                .getValue(String.class);

// Tampilkan foto profil pada header hamburger
                        if (!isEmpty(photoUrl)) {
                            loadDrawerProfileImage(photoUrl);
                        } else {
                            imgProfile.setImageResource(R.drawable.ic_person);
                        }

                        // Nama pengguna
                        if (!isEmpty(name)) {
                            tvWelcomeName.setText(name);
                            tvDrawerName.setText(name);
                        }

                        // Jabatan/role
                        if (!isEmpty(role)) {
                            tvRole.setText(role);
                        }

                        // Jabatan dan lokasi pada drawer
                        String roleText = !isEmpty(role)
                                ? role
                                : "Maintenance";

                        String drawerInfo = roleText;

                        if (!isEmpty(site)) {
                            drawerInfo += " • " + site;
                            chipSite.setText("📍 " + site);
                        } else {
                            chipSite.setText("📍 Site belum diatur");
                        }

                        tvDrawerRole.setText(drawerInfo);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        if (isFinishing() || isDestroyed()) return;

                        Toast.makeText(
                                DashboardActivity.this,
                                "Gagal memuat profil pengguna",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
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

    private void loadDrawerProfileImage(String imageUrl) {
        if (isEmpty(imageUrl)) {
            imgProfile.setImageResource(R.drawable.ic_person);
            return;
        }

        profileImageExecutor.execute(() -> {
            HttpURLConnection connection = null;

            try {
                URL url = new URL(imageUrl);
                connection = (HttpURLConnection) url.openConnection();

                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.setInstanceFollowRedirects(true);

                try (InputStream input = connection.getInputStream()) {
                    Bitmap bitmap = BitmapFactory.decodeStream(input);

                    if (bitmap != null) {
                        runOnUiThread(() -> {
                            if (!isFinishing() && !isDestroyed()) {
                                imgProfile.setImageBitmap(bitmap);
                            }
                        });
                    }
                }

            } catch (Exception e) {
                android.util.Log.e(
                        "DashboardProfile",
                        "Gagal memuat foto profil hamburger",
                        e
                );
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
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
        profileImageExecutor.shutdown();
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

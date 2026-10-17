package com.sipaman.maintenance;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CalendarActivity extends AppCompatActivity {

    private final Locale localeId = new Locale("id", "ID");
    private final Calendar currentMonth = Calendar.getInstance();
    private final Calendar selectedDate = Calendar.getInstance();
    private final Map<String, List<Task>> tasksByDate = new HashMap<>();
    private final SimpleDateFormat keyFormat = new SimpleDateFormat("yyyyMMdd", Locale.US);
    private final SimpleDateFormat firebaseDateFormat = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
    private final SimpleDateFormat monthTitleFormat = new SimpleDateFormat("MMMM yyyy", localeId);
    private final SimpleDateFormat selectedTitleFormat = new SimpleDateFormat("d MMMM yyyy", localeId);

    private GridLayout calendarGrid;
    private LinearLayout taskContainer;
    private TextView tvMonthTitle, tvSelectedDate, tvEmptyTask;
    private DatabaseReference database;
    private ValueEventListener taskListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        bindViews();
        setupToolbarActions();
        setupMonthControls();
        setupBottomNav();

        selectedDate.set(Calendar.HOUR_OF_DAY, 0);
        selectedDate.set(Calendar.MINUTE, 0);
        selectedDate.set(Calendar.SECOND, 0);
        selectedDate.set(Calendar.MILLISECOND, 0);

        currentMonth.set(Calendar.DAY_OF_MONTH, 1);

        database = FirebaseDatabase.getInstance().getReference("tasks");
        loadTasks();
    }

    private void bindViews() {
        calendarGrid = findViewById(R.id.calendarGrid);
        taskContainer = findViewById(R.id.taskContainer);
        tvMonthTitle = findViewById(R.id.tvMonthTitle);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        tvEmptyTask = findViewById(R.id.tvEmptyTask);
    }

    private void setupToolbarActions() {
        findViewById(R.id.btnCalendarMenu).setOnClickListener(v -> {
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
        });

        findViewById(R.id.btnCalendarToday).setOnClickListener(v -> {
            Calendar today = Calendar.getInstance();
            selectedDate.setTime(today.getTime());
            currentMonth.set(Calendar.YEAR, today.get(Calendar.YEAR));
            currentMonth.set(Calendar.MONTH, today.get(Calendar.MONTH));
            currentMonth.set(Calendar.DAY_OF_MONTH, 1);
            renderCalendar();
            renderTasksForSelectedDate();
        });
    }

    private void setupMonthControls() {
        findViewById(R.id.btnPrevMonth).setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, -1);
            selectedDate.set(Calendar.YEAR, currentMonth.get(Calendar.YEAR));
            selectedDate.set(Calendar.MONTH, currentMonth.get(Calendar.MONTH));
            selectedDate.set(Calendar.DAY_OF_MONTH, 1);
            renderCalendar();
            renderTasksForSelectedDate();
        });

        findViewById(R.id.btnNextMonth).setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, 1);
            selectedDate.set(Calendar.YEAR, currentMonth.get(Calendar.YEAR));
            selectedDate.set(Calendar.MONTH, currentMonth.get(Calendar.MONTH));
            selectedDate.set(Calendar.DAY_OF_MONTH, 1);
            renderCalendar();
            renderTasksForSelectedDate();
        });
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_calendar);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_calendar) {
                return true;
            }

            Intent intent = null;

            if (id == R.id.nav_dashboard) {
                intent = new Intent(this, DashboardActivity.class);

            } else if (id == R.id.nav_task) {
                intent = new Intent(this, TaskActivity.class);

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

        bottomNav.setSelectedItemId(R.id.nav_calendar);
    }

    private void loadTasks() {
        taskListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (isFinishing() || isDestroyed()) return;

                tasksByDate.clear();

                for (DataSnapshot data : snapshot.getChildren()) {
                    Task task = data.getValue(Task.class);
                    if (task == null || isEmpty(task.getDue())) continue;

                    try {
                        Date due = firebaseDateFormat.parse(task.getDue());
                        if (due == null) continue;

                        String key = keyFormat.format(due);
                        List<Task> list = tasksByDate.get(key);
                        if (list == null) {
                            list = new ArrayList<>();
                            tasksByDate.put(key, list);
                        }
                        list.add(task);
                    } catch (Exception ignored) {
                    }
                }

                renderCalendar();
                renderTasksForSelectedDate();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                if (isFinishing() || isDestroyed()) return;
                Toast.makeText(CalendarActivity.this, "Gagal load kalender", Toast.LENGTH_SHORT).show();
            }
        };

        database.addValueEventListener(taskListener);
    }

    @Override
    protected void onDestroy() {
        // Audit: lepaskan listener realtime agar Activity tidak tertahan setelah ditutup.
        if (database != null && taskListener != null) {
            database.removeEventListener(taskListener);
        }
        super.onDestroy();
    }

    private void renderCalendar() {
        calendarGrid.removeAllViews();
        tvMonthTitle.setText(capitalize(monthTitleFormat.format(currentMonth.getTime())));

        String[] days = {"Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min"};
        for (String day : days) {
            TextView header = createCalendarCell(day);
            header.setTextColor(Color.parseColor("#6B7280"));
            header.setTextSize(12);
            header.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            calendarGrid.addView(header);
        }

        Calendar cursor = (Calendar) currentMonth.clone();
        int firstDay = cursor.get(Calendar.DAY_OF_WEEK);
        int startOffset = firstDay == Calendar.SUNDAY ? 6 : firstDay - 2;
        cursor.add(Calendar.DAY_OF_MONTH, -startOffset);

        for (int i = 0; i < 42; i++) {
            Calendar date = (Calendar) cursor.clone();
            TextView cell = createCalendarCell(String.valueOf(date.get(Calendar.DAY_OF_MONTH)));

            boolean inCurrentMonth = date.get(Calendar.MONTH) == currentMonth.get(Calendar.MONTH);
            boolean isSelected = isSameDay(date, selectedDate);
            boolean hasTask = tasksByDate.containsKey(keyFormat.format(date.getTime()));

            cell.setTextColor(inCurrentMonth ? Color.parseColor("#374151") : Color.parseColor("#D1D5DB"));

            if (isSelected) {
                cell.setTextColor(Color.WHITE);
                cell.setTypeface(null, android.graphics.Typeface.BOLD);
                cell.setBackgroundResource(R.drawable.bg_calendar_selected);
            } else if (hasTask && inCurrentMonth) {
                cell.setTextColor(Color.parseColor("#0D47A1"));
                cell.setTypeface(null, android.graphics.Typeface.BOLD);
                cell.setBackgroundResource(R.drawable.bg_calendar_task_day);
            }

            cell.setOnClickListener(v -> {
                selectedDate.setTime(date.getTime());
                currentMonth.set(Calendar.YEAR, date.get(Calendar.YEAR));
                currentMonth.set(Calendar.MONTH, date.get(Calendar.MONTH));
                currentMonth.set(Calendar.DAY_OF_MONTH, 1);
                renderCalendar();
                renderTasksForSelectedDate();
            });

            calendarGrid.addView(cell);
            cursor.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    private TextView createCalendarCell(String text) {
        TextView view = new TextView(this);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = dp(40);
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(dp(2), dp(3), dp(2), dp(3));
        view.setLayoutParams(params);
        view.setGravity(Gravity.CENTER);
        view.setText(text);
        view.setTextSize(13);
        return view;
    }

    private void renderTasksForSelectedDate() {
        taskContainer.removeAllViews();
        tvSelectedDate.setText(capitalize(selectedTitleFormat.format(selectedDate.getTime())));

        List<Task> tasks = tasksByDate.get(keyFormat.format(selectedDate.getTime()));
        boolean hasTasks = tasks != null && !tasks.isEmpty();

        taskContainer.setVisibility(hasTasks ? View.VISIBLE : View.GONE);
        tvEmptyTask.setVisibility(hasTasks ? View.GONE : View.VISIBLE);

        if (!hasTasks) return;

        for (Task task : tasks) {
            taskContainer.addView(createTaskRow(task));
        }
    }

    private View createTaskRow(Task task) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setBackgroundResource(R.drawable.bg_calendar_task_item);
        row.setPadding(dp(12), dp(10), dp(12), dp(10));

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        rowParams.setMargins(0, 0, 0, dp(8));
        row.setLayoutParams(rowParams);

        TextView title = new TextView(this);
        title.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        title.setText(getTaskTitle(task));
        title.setTextColor(Color.parseColor("#111827"));
        title.setTextSize(14);
        title.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView badge = new TextView(this);
        badge.setText(getDisplayStatus(task));
        badge.setTextColor(Color.parseColor("#374151"));
        badge.setTextSize(11);
        badge.setGravity(Gravity.CENTER);
        badge.setPadding(dp(9), dp(4), dp(9), dp(4));
        badge.setBackgroundResource(getStatusBackground(task));

        row.addView(title);
        row.addView(badge);
        row.setOnClickListener(v -> openTaskDetail(task));

        return row;
    }

    private void openTaskDetail(Task task) {
        Intent intent = new Intent(this, DetailTaskActivity.class);
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
    }

    private String getTaskTitle(Task task) {
        if (!isEmpty(task.getJudul())) return task.getJudul();
        if (!isEmpty(task.getProject())) return task.getProject();
        return "Task Maintenance";
    }

    private String getDisplayStatus(Task task) {
        String status = normalizeStatus(task.getStatus());
        if ("DONE".equals(status)) return "Done";
        if (isOverdue(task)) return "Overdue";
        if ("ON_PROGRESS".equals(status)) return "On Progress";
        return "Pending";
    }

    private int getStatusBackground(Task task) {
        String status = normalizeStatus(task.getStatus());
        if ("DONE".equals(status)) return R.drawable.bg_status_done;
        if (isOverdue(task)) return R.drawable.bg_status_overdue;
        if ("ON_PROGRESS".equals(status)) return R.drawable.bg_status_progress;
        return R.drawable.bg_status_default;
    }

    private String normalizeStatus(String status) {
        if (status == null) return "";
        status = status.trim().toUpperCase(Locale.US).replace(" ", "_");
        if (status.contains("DONE")) return "DONE";
        if (status.contains("PROGRESS")) return "ON_PROGRESS";
        if (status.contains("OVERDUE")) return "OVERDUE";
        return status;
    }

    private boolean isOverdue(Task task) {
        if (task == null || isEmpty(task.getDue())) return false;
        if ("DONE".equals(normalizeStatus(task.getStatus()))) return false;

        try {
            Date due = firebaseDateFormat.parse(task.getDue());
            if (due == null) return false;
            return due.before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isSameDay(Calendar first, Calendar second) {
        return first.get(Calendar.YEAR) == second.get(Calendar.YEAR)
                && first.get(Calendar.DAY_OF_YEAR) == second.get(Calendar.DAY_OF_YEAR);
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String capitalize(String value) {
        if (isEmpty(value)) return "";
        return value.substring(0, 1).toUpperCase(localeId) + value.substring(1);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}

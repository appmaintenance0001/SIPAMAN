package com.sipaman.maintenance;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationActivity extends AppCompatActivity {

    private final List<NotificationItem> notificationList = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
    private final SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMM yyyy", Locale.US);

    private NotificationAdapter adapter;
    private RecyclerView rvNotification;
    private TextView tvEmptyNotification;
    private DatabaseReference notificationRef;
    private ValueEventListener notificationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        tvEmptyNotification = findViewById(R.id.tvEmptyNotification);
        rvNotification = findViewById(R.id.rvNotification);
        rvNotification.setLayoutManager(new LinearLayoutManager(this));
        rvNotification.setHasFixedSize(true);
        adapter = new NotificationAdapter(notificationList);
        rvNotification.setAdapter(adapter);

        loadNotifications();
    }

    private void loadNotifications() {
        notificationRef = FirebaseDatabase.getInstance().getReference("tasks");
        notificationListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (isFinishing() || isDestroyed()) return;

                        notificationList.clear();

                        for (DataSnapshot data : snapshot.getChildren()) {
                            Task task = data.getValue(Task.class);
                            if (task == null) continue;
                            task.setId(data.getKey());

                            addNotificationForTask(task);
                        }

                        adapter.notifyDataSetChanged();
                        rvNotification.setVisibility(notificationList.isEmpty() ? View.GONE : View.VISIBLE);
                        tvEmptyNotification.setVisibility(notificationList.isEmpty() ? View.VISIBLE : View.GONE);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        if (isFinishing() || isDestroyed()) return;
                        Toast.makeText(NotificationActivity.this, "Gagal load notifikasi", Toast.LENGTH_SHORT).show();
                    }
                };

        notificationRef.addValueEventListener(notificationListener);
    }

    @Override
    protected void onDestroy() {
        // Audit: listener notifikasi dilepas untuk menghindari callback ke Activity lama.
        if (notificationRef != null && notificationListener != null) {
            notificationRef.removeEventListener(notificationListener);
        }
        super.onDestroy();
    }

    private void addNotificationForTask(Task task) {
        String title = isEmpty(task.getJudul()) ? "Task Maintenance" : task.getJudul();
        String due = formatDate(task.getDue());
        String status = normalizeStatus(task.getStatus());

        if ("DONE".equals(status)) {
            notificationList.add(new NotificationItem(
                    "Task selesai",
                    title + " telah selesai dikerjakan",
                    due,
                    "DONE",
                    task
            ));
            return;
        }

        if (isOverdue(task.getDue())) {
            notificationList.add(new NotificationItem(
                    "Task overdue",
                    title + " sudah melewati due date",
                    due,
                    "OVERDUE",
                    task
            ));
            return;
        }

        if (isTomorrow(task.getDue())) {
            notificationList.add(new NotificationItem(
                    "Reminder H-1",
                    title + " jatuh tempo besok",
                    due,
                    "REMINDER",
                    task
            ));
            return;
        }

        notificationList.add(new NotificationItem(
                "Task baru",
                title + " masuk daftar pekerjaan",
                due,
                "NEW",
                task
        ));
    }

    private boolean isTomorrow(String dateText) {
        if (isEmpty(dateText)) return false;

        try {
            Date due = dateFormat.parse(dateText);
            if (due == null) return false;

            Calendar dueCal = Calendar.getInstance();
            dueCal.setTime(due);

            Calendar tomorrow = Calendar.getInstance();
            tomorrow.add(Calendar.DAY_OF_MONTH, 1);

            return dueCal.get(Calendar.YEAR) == tomorrow.get(Calendar.YEAR)
                    && dueCal.get(Calendar.DAY_OF_YEAR) == tomorrow.get(Calendar.DAY_OF_YEAR);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isOverdue(String dateText) {
        if (isEmpty(dateText)) return false;

        try {
            Date due = dateFormat.parse(dateText);
            return due != null && due.before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    private String normalizeStatus(String status) {
        if (status == null) return "";
        status = status.trim().toUpperCase(Locale.US).replace(" ", "_");
        if (status.contains("DONE")) return "DONE";
        if (status.contains("PROGRESS")) return "ON_PROGRESS";
        return status;
    }

    private String formatDate(String dateText) {
        if (isEmpty(dateText)) return "-";

        try {
            Date date = dateFormat.parse(dateText);
            return date == null ? "-" : displayFormat.format(date);
        } catch (Exception e) {
            return "-";
        }
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}

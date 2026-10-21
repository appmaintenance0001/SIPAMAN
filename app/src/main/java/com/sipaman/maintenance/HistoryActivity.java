package com.sipaman.maintenance;

import android.os.Bundle;
import android.widget.Toast;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private final List<ActivityLog> allLogs = new ArrayList<>();
    private final List<ActivityLog> logList = new ArrayList<>();
    private TextView btnHistoryActive;
    private TextView btnHistoryArchive;
    private boolean showingArchive = false;
    private static final long ARCHIVE_AFTER_MILLIS = 90L * 24 * 60 * 60 * 1000;
    private HistoryAdapter adapter;
    private DatabaseReference historyRef;
    private ValueEventListener historyListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnHistoryActive = findViewById(R.id.btnHistoryActive);
        btnHistoryArchive = findViewById(R.id.btnHistoryArchive);
        btnHistoryActive.setOnClickListener(v -> showArchive(false));
        btnHistoryArchive.setOnClickListener(v -> showArchive(true));

        RecyclerView rvHistory = findViewById(R.id.rvHistory);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        rvHistory.setHasFixedSize(true);
        adapter = new HistoryAdapter(logList);
        rvHistory.setAdapter(adapter);

        showArchive(false);
        loadHistory();
    }

    private void loadHistory() {
        historyRef = FirebaseDatabase.getInstance().getReference("activity_logs");
        historyListener = new ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {
                if (isFinishing() || isDestroyed()) return;

                allLogs.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    ActivityLog log = data.getValue(ActivityLog.class);
                    if (log == null) continue;
                    log.setId(data.getKey());
                    allLogs.add(log);
                }
                Collections.sort(allLogs, new Comparator<ActivityLog>() {
                    @Override
                    public int compare(ActivityLog first, ActivityLog second) {
                        return Long.compare(second.getTimestamp(), first.getTimestamp());
                    }
                });
                refreshVisibleLogs();
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {
                if (isFinishing() || isDestroyed()) return;
                Toast.makeText(HistoryActivity.this, "Gagal load riwayat", Toast.LENGTH_SHORT).show();
            }
        };

        historyRef.addValueEventListener(historyListener);
    }

    private void showArchive(boolean archive) {
        showingArchive = archive;
        if (btnHistoryActive != null) {
            btnHistoryActive.setBackgroundResource(archive
                    ? R.drawable.bg_history_tab_inactive
                    : R.drawable.bg_history_tab_active);
            btnHistoryActive.setTextColor(android.graphics.Color.parseColor(
                    archive ? "#64748B" : "#16803D"));
        }
        if (btnHistoryArchive != null) {
            btnHistoryArchive.setBackgroundResource(archive
                    ? R.drawable.bg_history_tab_active
                    : R.drawable.bg_history_tab_inactive);
            btnHistoryArchive.setTextColor(android.graphics.Color.parseColor(
                    archive ? "#16803D" : "#64748B"));
        }
        refreshVisibleLogs();
    }

    private void refreshVisibleLogs() {
        logList.clear();
        long cutoff = System.currentTimeMillis() - ARCHIVE_AFTER_MILLIS;
        for (ActivityLog log : allLogs) {
            boolean isArchived = log.getTimestamp() < cutoff;
            if (isArchived == showingArchive) logList.add(log);
        }
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        // Audit: listener riwayat dilepas saat Activity tidak lagi aktif.
        if (historyRef != null && historyListener != null) {
            historyRef.removeEventListener(historyListener);
        }
        super.onDestroy();
    }
}

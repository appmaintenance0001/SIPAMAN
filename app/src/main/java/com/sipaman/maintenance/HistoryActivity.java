package com.sipaman.maintenance;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private final List<ActivityLog> logList = new ArrayList<>();
    private HistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        RecyclerView rvHistory = findViewById(R.id.rvHistory);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistoryAdapter(logList);
        rvHistory.setAdapter(adapter);

        loadHistory();
    }

    private void loadHistory() {
        FirebaseDatabase.getInstance()
                .getReference("activity_logs")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {
                        logList.clear();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            ActivityLog log = data.getValue(ActivityLog.class);
                            if (log == null) continue;
                            log.setId(data.getKey());
                            logList.add(log);
                        }
                        Collections.sort(logList, new Comparator<ActivityLog>() {
                            @Override
                            public int compare(ActivityLog first, ActivityLog second) {
                                return Long.compare(second.getTimestamp(), first.getTimestamp());
                            }
                        });
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {
                        Toast.makeText(HistoryActivity.this, "Gagal load riwayat", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

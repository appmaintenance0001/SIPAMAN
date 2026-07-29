package com.sipaman.maintenance;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;


public class ProjectDetailActivity extends AppCompatActivity {

    private TextView tvProjectName;
    private TextView tvLocation;
    private TextView tvTotalTask;
    private TextView tvProgress;
    private TextView tvDone;
    private TextView tvOverdue;

    private RecyclerView rvTask;
    private FloatingActionButton fabAddTask;

    private final List<Task> taskList = new ArrayList<>();
    private TaskAdapter adapter;

    private DatabaseReference projectRef;
    private DatabaseReference taskRef;

    private String projectId;
    private Project currentProject;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_detail);

        projectId = getIntent().getStringExtra("projectId");

        tvProjectName = findViewById(R.id.tvProjectName);
        tvLocation = findViewById(R.id.tvLocation);
        tvTotalTask = findViewById(R.id.tvTotalTask);
        tvProgress = findViewById(R.id.tvProgress);
        tvDone = findViewById(R.id.tvDone);
        tvOverdue = findViewById(R.id.tvOverdue);

        rvTask = findViewById(R.id.rvTask);
        fabAddTask = findViewById(R.id.fabAddTask);

        rvTask.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TaskAdapter(taskList);

        rvTask.setAdapter(adapter);

        projectRef = FirebaseDatabase.getInstance().getReference("projects");
        taskRef = FirebaseDatabase.getInstance().getReference("tasks");

        loadProject();

        loadTasks();

        fabAddTask.setOnClickListener(v -> {

            Intent i = new Intent(this, AddTaskActivity.class);

            i.putExtra("projectId", projectId);

            startActivity(i);

        });

    }

    private void loadProject() {

        projectRef.child(projectId)
                .get()
                .addOnSuccessListener(snapshot -> {

                    currentProject = snapshot.getValue(Project.class);

                    if (currentProject == null)
                        return;

                    tvProjectName.setText(currentProject.getName());

                    tvLocation.setText(currentProject.getLocation());

                });

    }

    private void loadTasks() {

        fabAddTask.setOnClickListener(v -> {

            Intent intent = new Intent(ProjectDetailActivity.this, AddTaskActivity.class);

            intent.putExtra("projectId", projectId);

            startActivity(intent);

        });

        taskRef.get().addOnSuccessListener(snapshot -> {

            taskList.clear();

            int progress = 0;
            int done = 0;
            int overdue = 0;

            for (DataSnapshot ds : snapshot.getChildren()) {

                Task task = ds.getValue(Task.class);

                if (task == null)
                    continue;

                if (!projectId.equals(task.getProjectId()))
                    continue;

                taskList.add(task);

                if ("ON_PROGRESS".equals(task.getStatus()))
                    progress++;

                else if ("DONE".equals(task.getStatus()))
                    done++;

                else
                    overdue++;

            }

            adapter.notifyDataSetChanged();

            tvTotalTask.setText(taskList.size() + " Task");

            tvProgress.setText("ON_PROGRESS : " + progress);

            tvDone.setText("DONE : " + done);

            tvOverdue.setText("OVERDUE : " + overdue);

        });

    }
}

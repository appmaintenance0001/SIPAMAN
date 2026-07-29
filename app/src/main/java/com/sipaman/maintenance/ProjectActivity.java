package com.sipaman.maintenance;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjectActivity extends AppCompatActivity {

    private final List<Project> allProjects = new ArrayList<>();
    private final List<Project> filteredProjects = new ArrayList<>();
    private final Map<String, Integer> taskCountMap = new HashMap<>();

    private DatabaseReference projectRef;
    private DatabaseReference taskRef;

    private ProjectAdapter adapter;

    private EditText etProjectSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);

        projectRef = FirebaseDatabase.getInstance().getReference("projects");
        taskRef = FirebaseDatabase.getInstance().getReference("tasks");

        etProjectSearch = findViewById(R.id.etProjectSearch);

        RecyclerView rvProject = findViewById(R.id.rvProject);
        rvProject.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ProjectAdapter(
                filteredProjects,
                taskCountMap,
                new ProjectAdapter.ProjectActionListener() {

                    @Override
                    public void onClick(Project project) {

                        Intent intent =
                                new Intent(ProjectActivity.this,
                                        ProjectDetailActivity.class);

                        intent.putExtra("projectId", project.getId());
                        startActivity(intent);

                    }

                    @Override
                    public void onEdit(Project project) {

                        showProjectDialog(project);

                    }

                    @Override
                    public void onDelete(Project project) {

                        confirmDelete(project);

                    }
                });

        rvProject.setAdapter(adapter);

        findViewById(R.id.btnAddProject)
                .setOnClickListener(v -> showProjectDialog(null));

        findViewById(R.id.btnHistory)
                .setOnClickListener(v ->
                        startActivity(
                                new Intent(
                                        this,
                                        HistoryActivity.class)));

        etProjectSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s,
                                          int start,
                                          int count,
                                          int after) {
            }

            @Override
            public void onTextChanged(CharSequence s,
                                      int start,
                                      int before,
                                      int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {

                applySearch();

            }
        });

        setupBottomNav();

        loadData();
    }

    private void setupBottomNav() {

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

        bottomNav.setSelectedItemId(R.id.nav_project);

        bottomNav.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.nav_dashboard) {

                startActivity(new Intent(ProjectActivity.this, DashboardActivity.class));
                finish();
                return true;

            }

            if (id == R.id.nav_task) {

                startActivity(new Intent(ProjectActivity.this, TaskActivity.class));
                finish();
                return true;

            }

            if (id == R.id.nav_calendar) {

                startActivity(new Intent(ProjectActivity.this, CalendarActivity.class));
                finish();
                return true;

            }

            if (id == R.id.nav_project) {

                return true;

            }

            if (id == R.id.nav_setting) {

                startActivity(new Intent(ProjectActivity.this, SettingsActivity.class));
                return true;

            }

            return false;

        });

    }

    private void loadData() {

        // =======================
        // HITUNG JUMLAH TASK
        // =======================

        taskRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                taskCountMap.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {

                    Task task = ds.getValue(Task.class);

                    if (task == null)
                        continue;

                    String projectName = task.getProject();

                    if (projectName == null)
                        continue;

                    int total = taskCountMap.containsKey(projectName)
                            ? taskCountMap.get(projectName)
                            : 0;

                    taskCountMap.put(projectName, total + 1);

                }

                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });

        // =======================
        // LOAD PROJECT
        // =======================

        projectRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                allProjects.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {

                    Project project = ds.getValue(Project.class);

                    if (project == null)
                        continue;

                    project.setId(ds.getKey());

                    allProjects.add(project);

                }

                applySearch();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(
                        ProjectActivity.this,
                        "Gagal memuat Project",
                        Toast.LENGTH_SHORT
                ).show();

            }

        });

    }

    private void applySearch() {

        String keyword = etProjectSearch
                .getText()
                .toString()
                .trim()
                .toLowerCase();

        filteredProjects.clear();

        for (Project project : allProjects) {

            String name = project.getName() == null
                    ? ""
                    : project.getName().toLowerCase();

            String location = project.getLocation() == null
                    ? ""
                    : project.getLocation().toLowerCase();

            if (keyword.isEmpty()
                    || name.contains(keyword)
                    || location.contains(keyword)) {

                filteredProjects.add(project);

            }

        }

        adapter.notifyDataSetChanged();

    }

    private void showProjectDialog(Project project) {

        boolean isEdit = project != null;

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 30, 40, 10);

        // =====================
        // KODE PROJECT
        // =====================

        EditText etCode = new EditText(this);
        etCode.setHint("Kode Project (Contoh : OFC)");
        etCode.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(5)
        });
        layout.addView(etCode);

        // =====================
        // NAMA PROJECT
        // =====================

        EditText etName = new EditText(this);
        etName.setHint("Nama Project");
        layout.addView(etName);

        // =====================
        // LOKASI
        // =====================

        EditText etLocation = new EditText(this);
        etLocation.setHint("Lokasi");
        layout.addView(etLocation);

        // =====================
        // WARNA
        // =====================

        EditText etColor = new EditText(this);
        etColor.setHint("Warna (#173A6A)");
        layout.addView(etColor);

        // =====================
        // ICON
        // =====================

        EditText etIcon = new EditText(this);
        etIcon.setHint("Icon");
        layout.addView(etIcon);

        if (isEdit) {

            etCode.setText(project.getId());
            etCode.setEnabled(false);

            etName.setText(project.getName());
            etLocation.setText(project.getLocation());
            etColor.setText(project.getColor());
            etIcon.setText(project.getIcon());

        } else {

            etColor.setText("#173A6A");

        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(isEdit ? "Edit Project" : "Tambah Project")
                .setView(layout)
                .setPositiveButton(isEdit ? "Update" : "Simpan", null)
                .setNegativeButton("Batal", null)
                .create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {

                    String code = etCode.getText().toString().trim().toUpperCase();
                    String name = etName.getText().toString().trim();
                    String location = etLocation.getText().toString().trim();
                    String color = etColor.getText().toString().trim();
                    String icon = etIcon.getText().toString().trim();

                    if (code.isEmpty()) {
                        etCode.setError("Kode wajib diisi");
                        return;
                    }

                    if (name.isEmpty()) {
                        etName.setError("Nama wajib diisi");
                        return;
                    }

                    if (color.isEmpty()) {
                        color = "#173A6A";
                    }

                    Project newProject = new Project(
                            code,
                            name,
                            location,
                            color,
                            icon,
                            isEdit ? project.getCreatedAt() : System.currentTimeMillis()
                    );

                    if (isEdit) {

                        projectRef.child(project.getId())
                                .setValue(newProject)
                                .addOnSuccessListener(unused -> {

                                    Toast.makeText(
                                            ProjectActivity.this,
                                            "Project berhasil diupdate",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    dialog.dismiss();

                                })
                                .addOnFailureListener(e ->

                                        Toast.makeText(
                                                ProjectActivity.this,
                                                e.getMessage(),
                                                Toast.LENGTH_LONG
                                        ).show());

                    } else {

                        projectRef.child(code)
                                .get()
                                .addOnSuccessListener(snapshot -> {

                                    if (snapshot.exists()) {

                                        etCode.setError("Kode sudah digunakan");
                                        return;

                                    }

                                    projectRef.child(code)
                                            .setValue(newProject)
                                            .addOnSuccessListener(unused -> {

                                                Toast.makeText(
                                                        ProjectActivity.this,
                                                        "Project berhasil ditambahkan",
                                                        Toast.LENGTH_SHORT
                                                ).show();

                                                dialog.dismiss();

                                            })
                                            .addOnFailureListener(e ->

                                                    Toast.makeText(
                                                            ProjectActivity.this,
                                                            e.getMessage(),
                                                            Toast.LENGTH_LONG
                                                    ).show());

                                });

                    }

                });

    }


    private void confirmDelete(Project project) {

        int totalTask = taskCountMap.containsKey(project.getName())
                ? taskCountMap.get(project.getName())
                : 0;

        if (totalTask > 0) {

            Toast.makeText(
                    this,
                    "Project masih digunakan oleh " + totalTask + " Task",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Hapus Project")
                .setMessage(
                        "Yakin ingin menghapus Project\n\n" +
                                project.getName() +
                                " ?"
                )
                .setPositiveButton("Hapus", (dialog, which) -> {

                    projectRef.child(project.getId())
                            .removeValue()
                            .addOnSuccessListener(unused ->

                                    Toast.makeText(
                                            ProjectActivity.this,
                                            "Project berhasil dihapus",
                                            Toast.LENGTH_SHORT
                                    ).show()

                            )
                            .addOnFailureListener(e ->

                                    Toast.makeText(
                                            ProjectActivity.this,
                                            e.getMessage(),
                                            Toast.LENGTH_LONG
                                    ).show()

                            );

                })
                .setNegativeButton("Batal", null)
                .show();

    }
}


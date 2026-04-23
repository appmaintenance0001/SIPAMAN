package com.sipaman.maintenance;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AddTaskActivity extends AppCompatActivity {

    AutoCompleteTextView spProject, spJenis, spPriority, spPic;
    EditText etMulai, etDue;
    Button btnSimpan;

    DatabaseReference database;
    String taskId;

    // 🔥 FOTO GRID
    RecyclerView rvBefore, rvAfter;
    List<Uri> listBefore = new ArrayList<>();
    List<Uri> listAfter = new ArrayList<>();

    PhotoAdapter adapterBefore, adapterAfter;

    boolean isTaskDone = false;

    int currentType = 0; // 0 = before, 1 = after

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        // 🔹 INIT VIEW
        spProject = findViewById(R.id.spProject);
        spJenis = findViewById(R.id.spJenis);
        spPriority = findViewById(R.id.spPriority);
        spPic = findViewById(R.id.spPic);

        etMulai = findViewById(R.id.etMulai);
        etDue = findViewById(R.id.etDue);
        btnSimpan = findViewById(R.id.btnSimpan);

        rvBefore = findViewById(R.id.rvBefore);
        rvAfter = findViewById(R.id.rvAfter);

        // 🔥 GRID SETUP
        rvBefore.setLayoutManager(new GridLayoutManager(this, 3));
        rvAfter.setLayoutManager(new GridLayoutManager(this, 3));

        adapterBefore = new PhotoAdapter(this, listBefore, true, 0);
        adapterAfter = new PhotoAdapter(this, listAfter, false, 1); // disable awal

        rvBefore.setAdapter(adapterBefore);
        rvAfter.setAdapter(adapterAfter);

        rvAfter.setAlpha(0.5f); // efek disable

        // 🔹 PROJECT
        String[] projectList = {"Gedung Produksi", "Gudang", "Office"};
        spProject.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, projectList));
        spProject.setOnClickListener(v -> spProject.showDropDown());

        // 🔹 JENIS
        String[] jenisList = {"Preventive Maintenance", "Corrective Maintenance"};
        spJenis.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, jenisList));
        spJenis.setOnClickListener(v -> spJenis.showDropDown());

        // 🔹 PRIORITY
        String[] priorityList = {"High Priority", "Medium Priority", "Low Priority"};
        spPriority.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, priorityList));
        spPriority.setOnClickListener(v -> spPriority.showDropDown());

        // 🔹 PIC (SEARCHABLE)
        String[] teknisi = {"Mahmud Djafar", "Rahmat Otoluwa", "Maulana", "Muliadi"};
        spPic.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, teknisi));

        // 🔹 DATE PICKER
        etMulai.setOnClickListener(v -> showDatePicker(etMulai));
        etDue.setOnClickListener(v -> showDatePicker(etDue));

        etMulai.setFocusable(false);
        etDue.setFocusable(false);

        // 🔹 FIREBASE
        database = FirebaseDatabase.getInstance().getReference("tasks");

        // 🔹 SIMPAN
        btnSimpan.setOnClickListener(v -> {

            String project = spProject.getText().toString();
            String jenis = spJenis.getText().toString();
            String priority = spPriority.getText().toString();
            String mulai = etMulai.getText().toString();
            String due = etDue.getText().toString();

            if (project.isEmpty() || jenis.isEmpty() || priority.isEmpty()
                    || mulai.isEmpty() || due.isEmpty()) {

                Toast.makeText(this, "Semua field harus diisi!", Toast.LENGTH_SHORT).show();
                return;
            }

            String status = getStatusOtomatis(due);

            // 🔥 AKTIFKAN FOTO AFTER JIKA SELESAI

            if (status.equals("DONE")) {
                isTaskDone = true;
                adapterAfter = new PhotoAdapter(this, listAfter, true, 1);
                rvAfter.setAdapter(adapterAfter);
                rvAfter.setAlpha(1f);
            } else {
                rvAfter.setAlpha(0.5f);
            }

            String id = (taskId != null) ? taskId : database.push().getKey();

            Task task = new Task(
                    id,
                    project,
                    jenis,
                    mulai,
                    due,
                    status,
                    null
            );

            database.child(id).setValue(task);

            Toast.makeText(this, "Data berhasil disimpan!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    // 🔥 BUKA GALLERY
    public void openGallery(int type) {
        if (type == 1 && !isTaskDone) {
            Toast.makeText(this, "Foto after hanya untuk task selesai", Toast.LENGTH_SHORT).show();
            return;
        }

        currentType = type;

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");

        startActivityForResult(intent, 100);
    }

    // 🔥 HASIL FOTO
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {

            Uri uri = data.getData();

            if (currentType == 0) {
                listBefore.add(uri);
                adapterBefore.notifyDataSetChanged();
            } else {
                listAfter.add(uri);
                adapterAfter.notifyDataSetChanged();
            }
        }
    }

    // 🔥 DATE PICKER
    private void showDatePicker(EditText editText) {
        Calendar calendar = Calendar.getInstance();

        new DatePickerDialog(this,
                (view, year, month, day) ->
                        editText.setText(day + "/" + (month + 1) + "/" + year),
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    // 🔥 STATUS OTOMATIS
    private String getStatusOtomatis(String dueDate) {
        try {
            String[] parts = dueDate.split("/");

            Calendar due = Calendar.getInstance();
            due.set(
                    Integer.parseInt(parts[2]),
                    Integer.parseInt(parts[1]) - 1,
                    Integer.parseInt(parts[0])
            );

            if (Calendar.getInstance().after(due)) {
                return "OVERDUE";
            } else {
                return "ON PROGRESS";
            }

        } catch (Exception e) {
            return "ON PROGRESS";
        }
    }
}
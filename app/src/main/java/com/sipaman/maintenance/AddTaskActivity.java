package com.sipaman.maintenance;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class AddTaskActivity extends AppCompatActivity {

    AutoCompleteTextView spProject, spJenis, spPriority;
    EditText etMulai, etDue;
    Button btnSimpan;

    DatabaseReference database;
    String taskId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        // 🔹 INIT VIEW
        spProject = findViewById(R.id.spProject);
        spJenis = findViewById(R.id.spJenis);
        spPriority = findViewById(R.id.spPriority);

        etMulai = findViewById(R.id.etMulai);
        etDue = findViewById(R.id.etDue);
        btnSimpan = findViewById(R.id.btnSimpan);

        // 🔹 DROPDOWN PROJECT
        String[] projectList = {"Gedung Produksi", "Gudang", "Office"};
        ArrayAdapter<String> projectAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                projectList
        );
        spProject.setAdapter(projectAdapter);
        spProject.setOnClickListener(v -> spProject.showDropDown());

        // 🔹 DROPDOWN JENIS
        String[] jenisList = {
                "Preventive Maintenance",
                "Corrective Maintenance"
        };
        ArrayAdapter<String> jenisAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                jenisList
        );
        spJenis.setAdapter(jenisAdapter);
        spJenis.setOnClickListener(v -> spJenis.showDropDown());

        // 🔹 DROPDOWN PRIORITY
        String[] priorityList = {
                "High Priority",
                "Medium Priority",
                "Low Priority"
        };
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                priorityList
        );
        spPriority.setAdapter(priorityAdapter);
        spPriority.setOnClickListener(v -> spPriority.showDropDown());

        AutoCompleteTextView spPic = findViewById(R.id.spPic);

        String[] teknisi = {
                "Mahmud Djafar",
                "Rahmat Otoluwa",
                "Maulana",
                "Muliadi"
        };

        ArrayAdapter<String> picAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                teknisi
        );

        spPic.setAdapter(picAdapter);

        // 🔹 DATE PICKER
        etMulai.setOnClickListener(v -> showDatePicker(etMulai));
        etDue.setOnClickListener(v -> showDatePicker(etDue));

        etMulai.setFocusable(false);
        etDue.setFocusable(false);

        // 🔹 FIREBASE
        database = FirebaseDatabase.getInstance().getReference("tasks");

        // 🔹 MODE EDIT
        if (getIntent().hasExtra("id")) {
            taskId = getIntent().getStringExtra("id");

            spProject.setText(getIntent().getStringExtra("project"));
            spJenis.setText(getIntent().getStringExtra("jenis"));
            etMulai.setText(getIntent().getStringExtra("mulai"));
            etDue.setText(getIntent().getStringExtra("due"));

            btnSimpan.setText("UPDATE TASK");
        }

        // 🔹 SIMPAN DATA
        btnSimpan.setOnClickListener(v -> {

            String project = spProject.getText().toString();
            String jenis = spJenis.getText().toString();
            String priority = spPriority.getText().toString();
            String mulai = etMulai.getText().toString();
            String due = etDue.getText().toString();

            // 🔥 VALIDASI
            if (project.isEmpty() || jenis.isEmpty() || priority.isEmpty()
                    || mulai.isEmpty() || due.isEmpty()) {

                Toast.makeText(this, "Semua field harus diisi!", Toast.LENGTH_SHORT).show();
                return;
            }

            String status = getStatusOtomatis(due);

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

    // 🔥 DATE PICKER
    private void showDatePicker(EditText editText) {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, day) -> {

                    String date = day + "/" + (month + 1) + "/" + year;
                    editText.setText(date);

                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    // 🔥 STATUS OTOMATIS
    private String getStatusOtomatis(String dueDate) {
        try {
            String[] parts = dueDate.split("/");

            int day = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]) - 1;
            int year = Integer.parseInt(parts[2]);

            Calendar due = Calendar.getInstance();
            due.set(year, month, day);

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
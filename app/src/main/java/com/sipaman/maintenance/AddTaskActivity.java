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

    EditText etProject, etMulai, etDue;
    AutoCompleteTextView jenis;
    Button btnSimpan;

    DatabaseReference database;
    String taskId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        // 🔹 INIT VIEW
        etProject = findViewById(R.id.etProject);
        etMulai = findViewById(R.id.etMulai);
        etDue = findViewById(R.id.etDue);
        jenis = findViewById(R.id.spinnerJenis);
        btnSimpan = findViewById(R.id.btnSimpan);

        // 🔹 DROPDOWN JENIS
        String[] items = {
                "Preventive Maintenance",
                "Corrective Maintenance"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                items
        );

        jenis.setAdapter(adapter);
        jenis.setOnClickListener(v -> jenis.showDropDown());

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

            etProject.setText(getIntent().getStringExtra("project"));
            jenis.setText(getIntent().getStringExtra("jenis"));
            etMulai.setText(getIntent().getStringExtra("mulai"));
            etDue.setText(getIntent().getStringExtra("due"));

            btnSimpan.setText("UPDATE TASK");
        }

        // 🔹 SIMPAN DATA
        btnSimpan.setOnClickListener(v -> {

            String project = etProject.getText().toString();
            String jenisText = jenis.getText().toString();
            String mulai = etMulai.getText().toString();
            String due = etDue.getText().toString();

            // 🔥 VALIDASI
            if (project.isEmpty() || jenisText.isEmpty() || mulai.isEmpty() || due.isEmpty()) {
                Toast.makeText(this, "Semua field harus diisi!", Toast.LENGTH_SHORT).show();
                return;
            }

            String status = getStatusOtomatis(due);

            String id = (taskId != null) ? taskId : database.push().getKey();

            Task task = new Task(
                    id,
                    project,
                    jenisText,
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

    // 🔥 DATE PICKER FUNCTION
    private void showDatePicker(EditText editText) {
        Calendar calendar = Calendar.getInstance();

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {

                    String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    editText.setText(date);

                }, year, month, day);

        datePickerDialog.show();
    }

    // 🔥 AUTO STATUS
    private String getStatusOtomatis(String dueDate) {
        try {
            String[] parts = dueDate.split("/");

            int day = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]) - 1;
            int year = Integer.parseInt(parts[2]);

            Calendar due = Calendar.getInstance();
            due.set(year, month, day);

            Calendar today = Calendar.getInstance();

            if (today.after(due)) {
                return "OVERDUE";
            } else {
                return "ON PROGRESS";
            }

        } catch (Exception e) {
            return "ON PROGRESS";
        }
    }
}
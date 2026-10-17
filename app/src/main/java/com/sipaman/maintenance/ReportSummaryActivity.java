package com.sipaman.maintenance;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReportSummaryActivity extends AppCompatActivity {

    private static final String PERIOD_ALL = "Semua Periode";
    private static final String STATUS_ALL = "Semua Status";
    private static final String PROJECT_ALL = "Semua Project";
    private static final String PIC_ALL = "Semua PIC";
    private static final String EXPORT_PDF = "PDF";
    private static final String EXPORT_EXCEL = "EXCEL";

    private final SimpleDateFormat taskDateFormat =
            new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
    private final SimpleDateFormat reportDateFormat =
            new SimpleDateFormat("dd MMM yyyy, HH:mm", new Locale("id", "ID"));
    private final SimpleDateFormat fileDateFormat =
            new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private final List<Task> allTasks = new ArrayList<>();
    private final List<Task> filteredTasks = new ArrayList<>();
    private final Map<String, String> projectNameById = new HashMap<>();
    private final ExecutorService exportExecutor = Executors.newSingleThreadExecutor();

    private DatabaseReference taskRef;
    private DatabaseReference projectRef;
    private ValueEventListener taskListener;
    private ValueEventListener projectListener;

    private MaterialAutoCompleteTextView filterPeriod;
    private MaterialAutoCompleteTextView filterStatus;
    private MaterialAutoCompleteTextView filterProject;
    private MaterialAutoCompleteTextView filterPic;
    private View btnApplyReportFilter;
    private View btnClearReportFilter;
    private View btnExportPdf;
    private View btnExportExcel;
    private View layoutExportStatus;
    private LinearProgressIndicator progressExport;
    private TextView tvExportStatus;
    private TextView tvReportFilterResult;
    private TextView tvReportTotalTask;
    private TextView tvReportDone;
    private TextView tvReportProgress;
    private TextView tvReportOverdue;
    private TextView tvReportPending;
    private TextView tvReportProject;
    private TextView tvReportCompletion;
    private TextView tvReportGeneratedAt;
    private TextView tvReportDonePercent;
    private TextView tvReportProgressPercent;
    private TextView tvReportPendingPercent;
    private TextView tvReportOverduePercent;
    private View layoutReportEmpty;

    private LinearProgressIndicator progressCompletion;
    private LinearProgressIndicator progressDone;
    private LinearProgressIndicator progressProgress;
    private LinearProgressIndicator progressPending;
    private LinearProgressIndicator progressOverdue;
    private boolean exportInProgress;

    // SAF lets users choose a destination without broad storage permissions.
    private final ActivityResultLauncher<String> createPdfDocument =
            registerForActivityResult(
                    new ActivityResultContracts.CreateDocument("application/pdf"),
                    uri -> handleExportDestination(uri, EXPORT_PDF)
            );
    private final ActivityResultLauncher<String> createExcelDocument =
            registerForActivityResult(
                    new ActivityResultContracts.CreateDocument(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    ),
                    uri -> handleExportDestination(uri, EXPORT_EXCEL)
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_summary);

        bindViews();
        setupFilterControls();
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        loadReportData();
    }

    private void bindViews() {
        filterPeriod = findViewById(R.id.filterReportPeriod);
        filterStatus = findViewById(R.id.filterReportStatus);
        filterProject = findViewById(R.id.filterReportProject);
        filterPic = findViewById(R.id.filterReportPic);
        btnApplyReportFilter = findViewById(R.id.btnApplyReportFilter);
        btnClearReportFilter = findViewById(R.id.btnClearReportFilter);
        btnExportPdf = findViewById(R.id.btnExportPdf);
        btnExportExcel = findViewById(R.id.btnExportExcel);
        layoutExportStatus = findViewById(R.id.layoutExportStatus);
        progressExport = findViewById(R.id.progressExport);
        tvExportStatus = findViewById(R.id.tvExportStatus);
        tvReportFilterResult = findViewById(R.id.tvReportFilterResult);

        tvReportTotalTask = findViewById(R.id.tvReportTotalTask);
        tvReportDone = findViewById(R.id.tvReportDone);
        tvReportProgress = findViewById(R.id.tvReportProgress);
        tvReportOverdue = findViewById(R.id.tvReportOverdue);
        tvReportPending = findViewById(R.id.tvReportPending);
        tvReportProject = findViewById(R.id.tvReportProject);
        tvReportCompletion = findViewById(R.id.tvReportCompletion);
        tvReportGeneratedAt = findViewById(R.id.tvReportGeneratedAt);
        tvReportDonePercent = findViewById(R.id.tvReportDonePercent);
        tvReportProgressPercent = findViewById(R.id.tvReportProgressPercent);
        tvReportPendingPercent = findViewById(R.id.tvReportPendingPercent);
        tvReportOverduePercent = findViewById(R.id.tvReportOverduePercent);
        layoutReportEmpty = findViewById(R.id.layoutReportEmpty);

        progressCompletion = findViewById(R.id.progressCompletion);
        progressDone = findViewById(R.id.progressDone);
        progressProgress = findViewById(R.id.progressProgress);
        progressPending = findViewById(R.id.progressPending);
        progressOverdue = findViewById(R.id.progressOverdue);
    }

    private void setupFilterControls() {
        setFilterAdapter(filterPeriod, listOf(
                PERIOD_ALL,
                "7 Hari Terakhir",
                "30 Hari Terakhir",
                "Bulan Ini",
                "Tahun Ini"
        ), PERIOD_ALL);
        setFilterAdapter(filterStatus, listOf(
                STATUS_ALL,
                "Selesai",
                "On Progress",
                "Pending",
                "Overdue"
        ), STATUS_ALL);
        setFilterAdapter(filterProject, listOf(PROJECT_ALL), PROJECT_ALL);
        setFilterAdapter(filterPic, listOf(PIC_ALL), PIC_ALL);

        btnApplyReportFilter.setOnClickListener(v -> applyFilters());
        btnClearReportFilter.setOnClickListener(v -> resetFilters());
        btnExportPdf.setOnClickListener(v -> requestExport(EXPORT_PDF));
        btnExportExcel.setOnClickListener(v -> requestExport(EXPORT_EXCEL));
    }

    private List<String> listOf(String... values) {
        List<String> result = new ArrayList<>();
        Collections.addAll(result, values);
        return result;
    }

    private void setFilterAdapter(
            MaterialAutoCompleteTextView view,
            List<String> options,
            String defaultValue) {
        String previousValue = view.getText() == null ? "" : view.getText().toString().trim();
        view.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, options));
        if (isEmpty(previousValue) || !options.contains(previousValue)) {
            view.setText(defaultValue, false);
        }
    }

    private void loadReportData() {
        taskRef = FirebaseDatabase.getInstance().getReference("tasks");
        projectRef = FirebaseDatabase.getInstance().getReference("projects");

        taskListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (isActivityUnavailable()) return;

                allTasks.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Task task = data.getValue(Task.class);
                    if (task == null) continue;

                    if (isEmpty(task.getId())) {
                        task.setId(data.getKey());
                    }
                    allTasks.add(task);
                }

                refreshDynamicFilterOptions();
                applyFilters();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                showDataLoadFailure();
            }
        };

        projectListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (isActivityUnavailable()) return;

                projectNameById.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Project project = data.getValue(Project.class);
                    if (project == null) continue;

                    String projectId = isEmpty(project.getId()) ? data.getKey() : project.getId();
                    if (!isEmpty(projectId) && !isEmpty(project.getName())) {
                        projectNameById.put(projectId, project.getName().trim());
                    }
                }

                refreshDynamicFilterOptions();
                applyFilters();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                if (!isActivityUnavailable()) {
                    Toast.makeText(
                            ReportSummaryActivity.this,
                            "Daftar project tidak dapat dimuat; filter project memakai data task.",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }
        };

        taskRef.addValueEventListener(taskListener);
        projectRef.addValueEventListener(projectListener);
    }

    private void refreshDynamicFilterOptions() {
        Set<String> projects = new HashSet<>(projectNameById.values());
        Set<String> pics = new HashSet<>();

        for (Task task : allTasks) {
            String projectName = getProjectName(task);
            if (!isEmpty(projectName)) projects.add(projectName);
            if (!isEmpty(task.getPic())) pics.add(task.getPic().trim());
        }

        List<String> projectOptions = new ArrayList<>(projects);
        List<String> picOptions = new ArrayList<>(pics);
        Collections.sort(projectOptions, String.CASE_INSENSITIVE_ORDER);
        Collections.sort(picOptions, String.CASE_INSENSITIVE_ORDER);
        projectOptions.add(0, PROJECT_ALL);
        picOptions.add(0, PIC_ALL);

        setFilterAdapter(filterProject, projectOptions, PROJECT_ALL);
        setFilterAdapter(filterPic, picOptions, PIC_ALL);
    }

    private void resetFilters() {
        filterPeriod.setText(PERIOD_ALL, false);
        filterStatus.setText(STATUS_ALL, false);
        filterProject.setText(PROJECT_ALL, false);
        filterPic.setText(PIC_ALL, false);
        applyFilters();
    }

    private void applyFilters() {
        String period = getSelectedValue(filterPeriod, PERIOD_ALL);
        String status = getSelectedValue(filterStatus, STATUS_ALL);
        String project = getSelectedValue(filterProject, PROJECT_ALL);
        String pic = getSelectedValue(filterPic, PIC_ALL);

        filteredTasks.clear();
        for (Task task : allTasks) {
            if (task == null) continue;
            if (!matchesPeriod(task, period)) continue;
            if (!matchesStatus(task, status)) continue;
            if (!matchesText(project, PROJECT_ALL, getProjectName(task))) continue;
            if (!matchesText(pic, PIC_ALL, task.getPic())) continue;
            filteredTasks.add(task);
        }

        updateReport(calculateSummary(filteredTasks));
        tvReportFilterResult.setText("Menampilkan " + filteredTasks.size()
                + " task | " + getFilterDescription());
    }

    private String getSelectedValue(MaterialAutoCompleteTextView view, String fallback) {
        if (view.getText() == null || isEmpty(view.getText().toString())) return fallback;
        return view.getText().toString().trim();
    }

    private boolean matchesPeriod(Task task, String period) {
        if (PERIOD_ALL.equals(period)) return true;

        Date startDate = parseTaskDate(task.getMulai());
        if (startDate == null) return false;

        Calendar selected = Calendar.getInstance();
        selected.setTime(startDate);
        Calendar now = Calendar.getInstance();

        if ("Bulan Ini".equals(period)) {
            return selected.get(Calendar.YEAR) == now.get(Calendar.YEAR)
                    && selected.get(Calendar.MONTH) == now.get(Calendar.MONTH);
        }
        if ("Tahun Ini".equals(period)) {
            return selected.get(Calendar.YEAR) == now.get(Calendar.YEAR);
        }

        int days = "7 Hari Terakhir".equals(period) ? 7 : 30;
        Calendar beginning = Calendar.getInstance();
        beginning.set(Calendar.HOUR_OF_DAY, 0);
        beginning.set(Calendar.MINUTE, 0);
        beginning.set(Calendar.SECOND, 0);
        beginning.set(Calendar.MILLISECOND, 0);
        beginning.add(Calendar.DAY_OF_YEAR, -(days - 1));
        return !startDate.before(beginning.getTime()) && !startDate.after(now.getTime());
    }

    private boolean matchesStatus(Task task, String status) {
        return STATUS_ALL.equals(status) || status.equals(getDisplayStatus(task));
    }

    private boolean matchesText(String selected, String allValue, String actualValue) {
        return allValue.equals(selected)
                || (!isEmpty(actualValue) && selected.equalsIgnoreCase(actualValue.trim()));
    }

    private MaintenanceReportExporter.Summary calculateSummary(List<Task> tasks) {
        int doneTask = 0;
        int progressTask = 0;
        int pendingTask = 0;
        int overdueTask = 0;
        Set<String> projectKeys = new HashSet<>();

        for (Task task : tasks) {
            if (task == null) continue;

            String projectKey = !isEmpty(task.getProjectId())
                    ? task.getProjectId()
                    : getProjectName(task);
            if (!isEmpty(projectKey)) {
                projectKeys.add(projectKey);
            }

            String status = normalizeStatus(task.getStatus());
            if ("DONE".equals(status)) {
                doneTask++;
            } else if (isOverdue(task)) {
                overdueTask++;
            } else if ("ON_PROGRESS".equals(status)) {
                progressTask++;
            } else {
                pendingTask++;
            }
        }

        return new MaintenanceReportExporter.Summary(
                tasks.size(),
                doneTask,
                progressTask,
                pendingTask,
                overdueTask,
                projectKeys.size()
        );
    }

    private void updateReport(MaintenanceReportExporter.Summary summary) {
        tvReportTotalTask.setText(String.valueOf(summary.getTotalTask()));
        tvReportDone.setText(String.valueOf(summary.getDoneTask()));
        tvReportProgress.setText(String.valueOf(summary.getProgressTask()));
        tvReportOverdue.setText(String.valueOf(summary.getOverdueTask()));
        tvReportPending.setText(String.valueOf(summary.getPendingTask()));
        tvReportProject.setText(String.valueOf(summary.getTotalProject()));
        tvReportCompletion.setText(summary.getCompletionRate() + "%");
        tvReportGeneratedAt.setText("Diperbarui " + reportDateFormat.format(new Date()));

        setProgress(progressCompletion, summary.getCompletionRate());
        setStatusProgress(progressDone, tvReportDonePercent, summary.getDoneTask(), summary.getTotalTask());
        setStatusProgress(progressProgress, tvReportProgressPercent,
                summary.getProgressTask(), summary.getTotalTask());
        setStatusProgress(progressPending, tvReportPendingPercent,
                summary.getPendingTask(), summary.getTotalTask());
        setStatusProgress(progressOverdue, tvReportOverduePercent,
                summary.getOverdueTask(), summary.getTotalTask());

        layoutReportEmpty.setVisibility(summary.getTotalTask() == 0 ? View.VISIBLE : View.GONE);
    }

    private void requestExport(String exportType) {
        if (exportInProgress) return;
        if (filteredTasks.isEmpty()) {
            Toast.makeText(this, "Tidak ada task untuk diekspor.", Toast.LENGTH_SHORT).show();
            return;
        }

        String fileDate = fileDateFormat.format(new Date());
        if (EXPORT_PDF.equals(exportType)) {
            createPdfDocument.launch("Laporan_Maintenance_" + fileDate + ".pdf");
        } else {
            createExcelDocument.launch("Laporan_Maintenance_" + fileDate + ".xlsx");
        }
    }

    private void handleExportDestination(Uri uri, String exportType) {
        if (uri == null) {
            Toast.makeText(this, "Penyimpanan laporan dibatalkan.", Toast.LENGTH_SHORT).show();
            return;
        }

        final List<Task> taskSnapshot = new ArrayList<>(filteredTasks);
        final MaintenanceReportExporter.Summary summary = calculateSummary(taskSnapshot);
        final String generatedAt = reportDateFormat.format(new Date());
        final String filters = getFilterDescription();

        setExportState(true, "Membuat file " + (EXPORT_PDF.equals(exportType) ? "PDF..." : "Excel..."));
        exportExecutor.execute(() -> {
            Exception exportError = null;
            try {
                OutputStream outputStream = getContentResolver().openOutputStream(uri);
                if (outputStream == null) {
                    throw new IllegalStateException("Lokasi penyimpanan tidak dapat dibuka.");
                }
                try (OutputStream stream = outputStream) {
                    if (EXPORT_PDF.equals(exportType)) {
                        MaintenanceReportExporter.writePdf(
                                stream, taskSnapshot, summary, generatedAt, filters
                        );
                    } else {
                        MaintenanceReportExporter.writeExcel(
                                stream, taskSnapshot, summary, generatedAt, filters
                        );
                    }
                }
            } catch (Exception error) {
                exportError = error;
                deleteIncompleteDocument(uri);
            }

            Exception finalExportError = exportError;
            runOnUiThread(() -> {
                if (isActivityUnavailable()) return;
                setExportState(false, null);
                if (finalExportError == null) {
                    Toast.makeText(
                            ReportSummaryActivity.this,
                            (EXPORT_PDF.equals(exportType) ? "PDF" : "Excel")
                                    + " berhasil dibuat di lokasi yang dipilih.",
                            Toast.LENGTH_LONG
                    ).show();
                } else {
                    Toast.makeText(
                            ReportSummaryActivity.this,
                            "Gagal membuat " + (EXPORT_PDF.equals(exportType) ? "PDF" : "Excel")
                                    + ". Silakan coba lagi.",
                            Toast.LENGTH_LONG
                    ).show();
                }
            });
        });
    }

    private void deleteIncompleteDocument(Uri uri) {
        try {
            getContentResolver().delete(uri, null, null);
        } catch (Exception ignored) {
            // Some document providers do not allow deleting a newly created failed document.
        }
    }

    private void setExportState(boolean inProgress, String message) {
        exportInProgress = inProgress;
        btnExportPdf.setEnabled(!inProgress);
        btnExportExcel.setEnabled(!inProgress);
        layoutExportStatus.setVisibility(inProgress ? View.VISIBLE : View.GONE);
        progressExport.setIndeterminate(inProgress);
        if (inProgress) {
            tvExportStatus.setText(message);
        }
    }

    private String getFilterDescription() {
        List<String> parts = new ArrayList<>();
        String period = getSelectedValue(filterPeriod, PERIOD_ALL);
        String status = getSelectedValue(filterStatus, STATUS_ALL);
        String project = getSelectedValue(filterProject, PROJECT_ALL);
        String pic = getSelectedValue(filterPic, PIC_ALL);

        if (!PERIOD_ALL.equals(period)) parts.add("Periode: " + period);
        if (!STATUS_ALL.equals(status)) parts.add("Status: " + status);
        if (!PROJECT_ALL.equals(project)) parts.add("Project: " + project);
        if (!PIC_ALL.equals(pic)) parts.add("PIC: " + pic);
        return parts.isEmpty() ? "Semua data task" : join(parts, " | ");
    }

    private String join(List<String> values, String delimiter) {
        StringBuilder result = new StringBuilder();
        for (String value : values) {
            if (result.length() > 0) result.append(delimiter);
            result.append(value);
        }
        return result.toString();
    }

    private void showDataLoadFailure() {
        if (isActivityUnavailable()) return;
        Toast.makeText(
                ReportSummaryActivity.this,
                "Gagal memuat ringkasan laporan.",
                Toast.LENGTH_SHORT
        ).show();
        tvReportFilterResult.setText("Data laporan belum dapat dimuat.");
    }

    private void setStatusProgress(
            LinearProgressIndicator progress,
            TextView value,
            int current,
            int total) {
        int percentage = percent(current, total);
        setProgress(progress, percentage);
        value.setText(current + " task  |  " + percentage + "%");
    }

    private void setProgress(LinearProgressIndicator progress, int value) {
        progress.setMax(100);
        progress.setProgress(value);
    }

    private int percent(int value, int total) {
        return total == 0 ? 0 : Math.round((value * 100f) / total);
    }

    private String getProjectName(Task task) {
        if (task == null) return "";
        if (!isEmpty(task.getProject())) return task.getProject().trim();
        if (!isEmpty(task.getProjectId())) {
            String name = projectNameById.get(task.getProjectId());
            return !isEmpty(name) ? name : task.getProjectId();
        }
        return "";
    }

    private String getDisplayStatus(Task task) {
        String status = normalizeStatus(task.getStatus());
        if ("DONE".equals(status)) return "Selesai";
        if (isOverdue(task)) return "Overdue";
        if ("ON_PROGRESS".equals(status)) return "On Progress";
        return "Pending";
    }

    private String normalizeStatus(String status) {
        if (status == null) return "";
        String normalized = status.trim().toUpperCase(Locale.US).replace(" ", "_");
        if (normalized.contains("DONE")) return "DONE";
        if (normalized.contains("PROGRESS")) return "ON_PROGRESS";
        if (normalized.contains("OVERDUE")) return "OVERDUE";
        return normalized;
    }

    private boolean isOverdue(Task task) {
        return task != null
                && !"DONE".equals(normalizeStatus(task.getStatus()))
                && isOverdue(task.getDue());
    }

    private boolean isOverdue(String dueDate) {
        Date due = parseTaskDate(dueDate);
        if (due == null) return false;

        Calendar dueCalendar = Calendar.getInstance();
        dueCalendar.setTime(due);
        dueCalendar.set(Calendar.HOUR_OF_DAY, 0);
        dueCalendar.set(Calendar.MINUTE, 0);
        dueCalendar.set(Calendar.SECOND, 0);
        dueCalendar.set(Calendar.MILLISECOND, 0);

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        return dueCalendar.before(today);
    }

    private Date parseTaskDate(String date) {
        if (isEmpty(date)) return null;
        try {
            return taskDateFormat.parse(date);
        } catch (Exception ignored) {
            return null;
        }
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean isActivityUnavailable() {
        return isFinishing() || isDestroyed();
    }

    @Override
    protected void onDestroy() {
        // Firebase listeners and the export executor are released with the Activity lifecycle.
        if (taskRef != null && taskListener != null) {
            taskRef.removeEventListener(taskListener);
        }
        if (projectRef != null && projectListener != null) {
            projectRef.removeEventListener(projectListener);
        }
        exportExecutor.shutdown();
        super.onDestroy();
    }
}

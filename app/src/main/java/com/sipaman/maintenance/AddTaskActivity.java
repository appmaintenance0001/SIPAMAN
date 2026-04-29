
package com.sipaman.maintenance;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.UploadCallback;
import com.cloudinary.android.callback.ErrorInfo;

import java.util.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class AddTaskActivity extends AppCompatActivity {


    ShimmerFrameLayout shimmer;

    AutoCompleteTextView spProject, spJenis, spPriority, spPic;
    EditText etDeskripsi, etMulai, etDue;
    Button btnSimpan, btnAddBefore, btnAddAfter;

    RecyclerView rvBefore, rvAfter;

    List<Uri> listBefore = new ArrayList<>();
    List<Uri> listAfter = new ArrayList<>();

    PhotoUriAdapter adapterBefore, adapterAfter;

    DatabaseReference database;

    Uri cameraUri;
    int currentType = 0;

    String id, mode;
    boolean isEdit;

    ProgressDialog dialog;

    ProgressBar progressBar;
    TextView txtProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        // ================= DROPDOWN DATA =================
        String[] projectList = {"Office", "Intake", "Dryer"};
        String[] jenisList = {"Preventive", "Corrective", "Emergency"};
        String[] priorityList = {"Low", "Medium", "High"};
        String[] picList = {"Mahmud", "Maulana", "Rahmat"};

        ShimmerFrameLayout shimmer = findViewById(R.id.shimmerLayout);

        progressBar = findViewById(R.id.progressBar);
        txtProgress = findViewById(R.id.txtProgress);

        progressBar.setVisibility(View.GONE);
        txtProgress.setVisibility(View.GONE);

        id = getIntent().getStringExtra("id");
        mode = getIntent().getStringExtra("mode");
        isEdit = id != null;

        spProject = findViewById(R.id.spProject);
        spJenis = findViewById(R.id.spJenis);
        spPriority = findViewById(R.id.spPriority);
        spPic = findViewById(R.id.spPic);
        etDeskripsi = findViewById(R.id.etDeskripsi);

        ArrayAdapter<String> projectAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, projectList);

        ArrayAdapter<String> jenisAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, jenisList);

        ArrayAdapter<String> priorityAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, priorityList);


// set adapter
        spProject.setAdapter(projectAdapter);
        spJenis.setAdapter(jenisAdapter);
        spPriority.setAdapter(priorityAdapter);
        ArrayAdapter<String> picAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, picList);

        spPic.setAdapter(picAdapter);
        spPic.setThreshold(1); // mulai search dari 1 huruf
        spPic.setOnClickListener(v -> spPic.showDropDown());


        etMulai = findViewById(R.id.etMulai);
        etDue = findViewById(R.id.etDue);
        btnSimpan = findViewById(R.id.btnSimpan);

        rvBefore = findViewById(R.id.rvBefore);
        rvAfter = findViewById(R.id.rvAfter);

        btnAddBefore = findViewById(R.id.btnAddBefore);
        btnAddAfter = findViewById(R.id.btnAddAfter);

        rvBefore.setLayoutManager(new GridLayoutManager(this, 3));
        rvAfter.setLayoutManager(new GridLayoutManager(this, 3));

        adapterBefore = new PhotoUriAdapter(this, listBefore);
        adapterAfter = new PhotoUriAdapter(this, listAfter);

        rvBefore.setAdapter(adapterBefore);
        rvAfter.setAdapter(adapterAfter);


        dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading...");
        dialog.setCancelable(false);

        // MODE AFTER
        if ("after".equals(mode)) {
            btnAddAfter.setVisibility(View.VISIBLE);
            rvAfter.setVisibility(View.VISIBLE);
        } else {
            btnAddAfter.setVisibility(View.GONE);
            rvAfter.setVisibility(View.GONE);
        }

        database = FirebaseDatabase.getInstance().getReference("tasks");

        // DATE
        etMulai.setOnClickListener(v -> showDatePicker(etMulai));
        etDue.setOnClickListener(v -> showDatePicker(etDue));
        etMulai.setFocusable(false);
        etDue.setFocusable(false);

        btnAddBefore.setOnClickListener(v -> openGallery(0));
        btnAddAfter.setOnClickListener(v -> openGallery(1));

        btnSimpan.setOnClickListener(v -> saveTask());
    }

    // ================= SAVE =================
    private void saveTask() {

        String project = spProject.getText().toString();
        String jenis = spJenis.getText().toString();
        String mulai = etMulai.getText().toString();
        String due = etDue.getText().toString();

        // 🔥 WAJIB FOTO AFTER jika mode AFTER
        if ("after".equals(mode)) {
            if (listAfter == null || listAfter.isEmpty()) {
                Toast.makeText(this, "Foto AFTER wajib diisi", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (project.isEmpty() || jenis.isEmpty() || mulai.isEmpty() || due.isEmpty()) {
            Toast.makeText(this, "Lengkapi semua field!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (id == null) id = database.push().getKey();

        Task task = new Task(
                id,
                project,
                jenis,
                mulai,
                due,
                "ON_PROGRESS",
                spPic.getText().toString(),
                spPriority.getText().toString(),
                etDeskripsi.getText().toString(),
                new ArrayList<>(),
                new ArrayList<>()
        );

        database.child(id).setValue(task);

        shimmer.setVisibility(View.VISIBLE);
        shimmer.startShimmer();
        progressBar.setVisibility(View.VISIBLE);
        txtProgress.setVisibility(View.VISIBLE);

        dialog.show();

        // 🔥 UPLOAD BEFORE
        uploadImagesCloudinary(listBefore, "before", urls -> {
            database.child(id).child("beforeUrls").setValue(urls);
        });




        // 🔥 UPLOAD AFTER
        uploadImagesCloudinary(listAfter, "after", urls -> {
            database.child(id).child("afterUrls").setValue(urls);
            dialog.dismiss(); // selesai
            Toast.makeText(this, "Task berhasil disimpan", Toast.LENGTH_SHORT).show();
            finish();
        });
    }



    // ================= UPLOAD =================
    private void uploadImagesCloudinary(List<Uri> list, String folder, OnUploadComplete callback) {

        List<String> urls = new ArrayList<>();

        if (list == null || list.isEmpty()) {

            runOnUiThread(() -> {
                shimmer.stopShimmer();
                shimmer.setVisibility(View.GONE);

                progressBar.setVisibility(View.GONE);
                txtProgress.setVisibility(View.GONE);
            });
            callback.onComplete(urls);
            return;
        }

        int total = list.size();
        int[] count = {0};

        for (Uri uri : list) {

            Uri compressed = compressImage(uri);

            progressBar.setVisibility(View.VISIBLE);
            txtProgress.setVisibility(View.VISIBLE);
            progressBar.setMax(list.size());

            MediaManager.get().upload(compressed)
                    .unsigned("sipaman_preset")
                    .option("folder", "sipaman/" + folder)
                    .callback(new UploadCallback() {

                        @Override
                        public void onSuccess(String requestId, Map resultData) {

                            progressBar.setProgress(count[0]);
                            txtProgress.setText("Upload " + count[0] + "/" + total);

                            String url = resultData.get("secure_url").toString();
                            urls.add(url);

                            count[0]++;

                            if (count[0] == total) {
                                callback.onComplete(urls);
                            }

                            progressBar.setVisibility(View.GONE);
                            txtProgress.setVisibility(View.GONE);
                        }

                        @Override public void onStart(String requestId) {}
                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {

                            int percent = (int) ((bytes * 100) / totalBytes);

                            runOnUiThread(() -> {
                                progressBar.setProgress(percent);
                                txtProgress.setText("Upload " + percent + "%");
                            });
                        }
                        @Override
                        public void onError(String requestId, ErrorInfo error) {

                            new AlertDialog.Builder(AddTaskActivity.this)
                                    .setTitle("Upload gagal")
                                    .setMessage("Coba lagi?")
                                    .setPositiveButton("Retry", (d, w) -> {
                                        uploadImagesCloudinary(list, folder, callback);
                                    })
                                    .setNegativeButton("Batal", null)
                                    .show();
                        }
                        @Override public void onReschedule(String requestId, ErrorInfo error) {}

                    }).dispatch();
        }
    }

    interface OnUploadComplete {

        void onComplete(List<String> urls);
    }


    // ================= COMPRESS =================
    private Uri compressImage(Uri uri) {
        try {
            InputStream input = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input);

            File file = new File(getCacheDir(), "IMG_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream out = new FileOutputStream(file);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, out);
            out.flush();
            out.close();

            return Uri.fromFile(file);

        } catch (Exception e) {
            return uri;
        }
    }

    // ================= PILIH FOTO =================
    public void openGallery(int type) {

        currentType = type;

        String[] options = {"Kamera", "Galeri"};

        new AlertDialog.Builder(this)
                .setTitle("Pilih Foto")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) openCamera();
                    else openGalleryOnly();
                }).show();
    }

    private void openGalleryOnly() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 100);
    }

    private void openCamera() {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File file = new File(getExternalCacheDir(),
                "IMG_" + System.currentTimeMillis() + ".jpg");

        cameraUri = FileProvider.getUriForFile(
                this,
                getPackageName() + ".provider",
                file
        );

        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
        startActivityForResult(intent, 200);
    }

    // ================= RESULT =================
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) return;

        Uri uri = (requestCode == 100 && data != null) ? data.getData() : cameraUri;
        if (uri == null) return;

        if (currentType == 0) {
            if (listBefore.size() >= 2) return;
            listBefore.add(uri);
            adapterBefore.notifyDataSetChanged();
            if (listBefore.size() == 2) btnAddBefore.setVisibility(View.GONE);
        }

        if (currentType == 1) {
            if (listAfter.size() >= 2) return;
            listAfter.add(uri);
            adapterAfter.notifyDataSetChanged();
            if (listAfter.size() == 2) btnAddAfter.setVisibility(View.GONE);
        }
    }

    private void showDatePicker(EditText editText) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this,
                (view, y, m, d) -> editText.setText(d + "/" + (m + 1) + "/" + y),
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        ).show();
    }
}
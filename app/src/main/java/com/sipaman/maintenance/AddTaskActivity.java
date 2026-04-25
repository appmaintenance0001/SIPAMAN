package com.sipaman.maintenance;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.*;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.content.pm.PackageManager;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.UploadCallback;
import com.cloudinary.android.callback.ErrorInfo;

import java.util.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;

public class AddTaskActivity extends AppCompatActivity {

    AutoCompleteTextView spProject, spJenis, spPriority, spPic;
    EditText etMulai, etDue;
    Button btnSimpan;

    RecyclerView rvBefore, rvAfter;

    List<Uri> listBefore = new ArrayList<>();
    List<Uri> listAfter = new ArrayList<>();

    PhotoAdapter adapterBefore, adapterAfter;

    DatabaseReference database;

    Uri cameraUri;
    int currentType = 0; // 0 before, 1 after

    interface OnUploadComplete {
        void onComplete(List<String> urls);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        // INIT VIEW
        spProject = findViewById(R.id.spProject);
        spJenis = findViewById(R.id.spJenis);
        spPriority = findViewById(R.id.spPriority);
        spPic = findViewById(R.id.spPic);

        etMulai = findViewById(R.id.etMulai);
        etDue = findViewById(R.id.etDue);
        btnSimpan = findViewById(R.id.btnSimpan);

        rvBefore = findViewById(R.id.rvBefore);
        rvAfter = findViewById(R.id.rvAfter);

        // RecyclerView
        rvBefore.setLayoutManager(new GridLayoutManager(this, 3));
        rvAfter.setLayoutManager(new GridLayoutManager(this, 3));

        adapterBefore = new PhotoAdapter(this, listBefore, true, 0);
        adapterAfter = new PhotoAdapter(this, listAfter, true, 1);

        rvBefore.setAdapter(adapterBefore);
        rvAfter.setAdapter(adapterAfter);

        // Firebase
        database = FirebaseDatabase.getInstance().getReference("tasks");


        // Dropdown
        spProject.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                new String[]{"Gedung Produksi", "Gudang", "Office"}));

        spJenis.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                new String[]{"Preventive", "Corrective"}));

        spPriority.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                new String[]{"High", "Medium", "Low"}));

        spPic.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                new String[]{"Mahmud", "Rahmat", "Maulana", "Muliadi"}));

        // Date
        etMulai.setOnClickListener(v -> showDatePicker(etMulai));
        etDue.setOnClickListener(v -> showDatePicker(etDue));

        etMulai.setFocusable(false);
        etDue.setFocusable(false);

        // Save
        btnSimpan.setOnClickListener(v -> saveTask());
    }

    // ================= SAVE TASK =================
    private void saveTask() {

        String project = spProject.getText().toString();
        String jenis = spJenis.getText().toString();
        String priority = spPriority.getText().toString();
        String mulai = etMulai.getText().toString();
        String due = etDue.getText().toString();

        if (project.isEmpty() || jenis.isEmpty() || priority.isEmpty()
                || mulai.isEmpty() || due.isEmpty()) {

            Toast.makeText(this, "Lengkapi semua field!", Toast.LENGTH_SHORT).show();
            return;
        }

        String id = database.push().getKey();
        String status = getStatus(due);

        // 🔥 SIMPAN DULU TANPA FOTO
        Task task = new Task(
                id,
                project,
                jenis,
                mulai,
                due,
                status,
                new ArrayList<>(),
                new ArrayList<>()
        );

        database.child(id).setValue(task);

        Toast.makeText(this, "Task disimpan, upload berjalan...", Toast.LENGTH_SHORT).show();

        // 🔥 UPLOAD BACKGROUND
        uploadImagesCloudinary(listBefore, "before", urls -> {
            database.child(id).child("beforeUrls").setValue(urls);
        });

        uploadImagesCloudinary(listAfter, "after", urls -> {
            database.child(id).child("afterUrls").setValue(urls);
        });

        finish();
    }


    // ================= UPLOAD =================
    private void uploadImagesCloudinary(List<Uri> list, String folder, OnUploadComplete callback) {

        List<String> urls = new ArrayList<>();

        if (list.isEmpty()) {
            callback.onComplete(urls);
            return;
        }

        int total = list.size();
        int[] count = {0};

        for (Uri uri : list) {

            Uri compressed = compressImage(uri);

            MediaManager.get().upload(uri)
                    .unsigned("sipaman_preset")
                    .option("folder", "sipaman/" + folder)
                    .option("resource_type", "image")
                    .option("quality", "auto:low")
                    .option("fetch_format", "auto")

                    .callback(new UploadCallback() {

                        @Override
                        public void onStart(String requestId) {
                            Log.d("UPLOAD", "Mulai upload");
                        }

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {
                        }

                        @Override
                        public void onSuccess(String requestId, Map resultData) {

                            String url = resultData.get("secure_url").toString();
                            urls.add(url);

                            count[0]++;

                            if (count[0] == total) {
                                callback.onComplete(urls);
                            }
                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            Log.e("UPLOAD", "Error: " + error.getDescription());
                        }

                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) {
                        }

                    }).dispatch();
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

        // 🔥 CEK PERMISSION CAMERA
        if (checkSelfPermission(android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{android.Manifest.permission.CAMERA}, 10);
            return;
        }

        Log.d("CAMERA", "openCamera dipanggil"); // 🔥 DEBUG

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


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 10) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // 🔥 ulangi buka kamera setelah diizinkan
                openCamera();

            } else {
                Toast.makeText(this, "Izin kamera ditolak", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) return;

        Uri uri = null;

        // 🔥 GALERI
        if (requestCode == 100 && data != null) {
            uri = data.getData();
        }

        // 🔥 KAMERA
        if (requestCode == 200) {
            uri = cameraUri;
        }

        if (uri == null) return;

        // 🔥 CEK LIMIT
        if (isLimitReached()) return;

        // 🔥 COMPRESS
        uri = compressImage(uri);

        // 🔥 MASUK LIST TANPA ELSE RIBET
        if (currentType == 0) {
            listBefore.add(uri);
            adapterBefore.notifyDataSetChanged();
        }

        if (currentType == 1) {
            listAfter.add(uri);
            adapterAfter.notifyDataSetChanged();
        }

        // ===============================
        // 🔥 AFTER (MAX 2)
        // ===============================
        else {

            if (listAfter.size() >= 2) {
                Toast.makeText(this, "Maksimal 2 foto AFTER", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d("TYPE", "currentType = " + currentType);
            listAfter.add(uri);
            adapterAfter.notifyDataSetChanged();
            adapterAfter.notifyItemInserted(listAfter.size() - 1);
        }
    }

    private boolean isLimitReached() {

        if (currentType == 0 && listBefore.size() >= 2) {
            Toast.makeText(this, "Maksimal 2 foto BEFORE", Toast.LENGTH_SHORT).show();
            return true;
        }

        if (currentType == 1 && listAfter.size() >= 2) {
            Toast.makeText(this, "Maksimal 2 foto AFTER", Toast.LENGTH_SHORT).show();
            return true;
        }

        return false;
    }

    private Uri compressImage(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap original = BitmapFactory.decodeStream(inputStream);

            // 🔥 RESIZE (max width 1024)
            int maxWidth = 1024;
            int width = original.getWidth();
            int height = original.getHeight();

            if (width > maxWidth) {
                float ratio = (float) height / width;
                width = maxWidth;
                height = (int) (width * ratio);
            }

            Bitmap resized = Bitmap.createScaledBitmap(original, width, height, true);

            // 🔥 COMPRESS (70%)
            File file = new File(getCacheDir(), "IMG_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream fos = new FileOutputStream(file);

            resized.compress(Bitmap.CompressFormat.JPEG, 70, fos);

            fos.flush();
            fos.close();

            return Uri.fromFile(file);

        } catch (Exception e) {
            e.printStackTrace();
            return uri; // fallback kalau gagal
        }
    }


    // ================= DATE =================
    private void showDatePicker(EditText editText) {

        Calendar c = Calendar.getInstance();

        new DatePickerDialog(this,
                (view, y, m, d) -> editText.setText(d + "/" + (m + 1) + "/" + y),
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    // ================= STATUS =================
    private String getStatus(String dueDate) {

        try {
            String[] p = dueDate.split("/");

            Calendar due = Calendar.getInstance();
            due.set(
                    Integer.parseInt(p[2]),
                    Integer.parseInt(p[1]) - 1,
                    Integer.parseInt(p[0])
            );

            return Calendar.getInstance().after(due) ? "OVERDUE" : "ON PROGRESS";

        } catch (Exception e) {
            return "ON PROGRESS";
        }
    }
}
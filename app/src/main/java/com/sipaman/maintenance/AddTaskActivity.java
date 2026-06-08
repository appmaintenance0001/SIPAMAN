
package com.sipaman.maintenance;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.*;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.UploadCallback;
import com.cloudinary.android.callback.ErrorInfo;

import java.text.SimpleDateFormat;
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
    Button btnSimpan,
            btnAddBefore,
            btnAddAfter,
            btnTanggalSelesai;

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

    TextView txtTanggalSelesai;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        // ================= DROPDOWN DATA =================
        String[] projectList = {"Office", "Intake", "Dryer"};
        String[] jenisList = {"Preventive", "Corrective", "Emergency"};
        String[] priorityList = {"Low", "Medium", "High"};
        String[] picList = {"Mahmud", "Maulana", "Rahmat"};

        shimmer = findViewById(R.id.shimmerLayout);

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
        txtTanggalSelesai = findViewById(R.id.txtTanggalSelesai);

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
        btnTanggalSelesai = findViewById(R.id.btnTanggalSelesai);

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

            btnTanggalSelesai.setVisibility(View.VISIBLE);

            btnAddAfter.setEnabled(true);

        } else {

            btnAddAfter.setVisibility(View.GONE);

            rvAfter.setVisibility(View.GONE);

            btnTanggalSelesai.setVisibility(View.GONE);
        }


        database = FirebaseDatabase.getInstance().getReference("tasks");


        if ("after".equals(mode) && id != null) {

            database.child(id).get().addOnSuccessListener(snapshot -> {

                Task oldTask = snapshot.getValue(Task.class);

                if (oldTask == null) return;

                // readonly mode
                spProject.setEnabled(false);
                spJenis.setEnabled(false);
                spPriority.setEnabled(false);
                spPic.setEnabled(false);

                etMulai.setEnabled(false);
                etDue.setEnabled(false);
                etDeskripsi.setEnabled(false);


                // ================= TAMPILKAN FOTO BEFORE =================

                btnAddBefore.setVisibility(View.GONE);

                rvBefore.setVisibility(View.VISIBLE);

                if (oldTask.getBeforeUrls() != null
                        && !oldTask.getBeforeUrls().isEmpty()) {

                    rvBefore.setAdapter(

                            new PhotoAdapter(
                                    AddTaskActivity.this,
                                    oldTask.getBeforeUrls()
                            )
                    );
                }

                spProject.setText(oldTask.getProject(), false);

                spJenis.setText(oldTask.getJenis(), false);

                spPriority.setText(oldTask.getPriority(), false);

                spPic.setText(oldTask.getPic(), false);

                etMulai.setText(oldTask.getMulai());

                etDue.setText(oldTask.getDue());

                etDeskripsi.setText(oldTask.getDeskripsi());
            });
        }


        // DATE
        etMulai.setOnClickListener(v -> showDatePicker(etMulai));
        etDue.setOnClickListener(v -> showDatePicker(etDue));
        etMulai.setFocusable(false);
        etDue.setFocusable(false);

        btnAddBefore.setOnClickListener(v -> openGallery(0));
        btnAddAfter.setOnClickListener(v -> openGallery(1));

        btnTanggalSelesai.setOnClickListener(v -> {

            pickTanggalSelesai(null);

        });

        btnSimpan.setOnClickListener(v -> saveTask());


    }

    String tanggalSelesaiManual = "";

    private void pickTanggalSelesai(Runnable onSelected) {

        Calendar c = Calendar.getInstance();

        new DatePickerDialog(this,
                (view, y, m, d) -> {

                    tanggalSelesaiManual = d + "/" + (m + 1) + "/" + y;

                    txtTanggalSelesai.setVisibility(View.VISIBLE);

                    txtTanggalSelesai.setText(
                            "Tanggal selesai : "
                                    + tanggalSelesaiManual
                    );

                    if (onSelected != null) onSelected.run();

                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        ).show();
    }


    // ================= SAVE =================
    private void saveTask() {

        String project = spProject.getText().toString();
        String jenis = spJenis.getText().toString();
        String mulai = etMulai.getText().toString();
        String due = etDue.getText().toString();

        FirebaseUser user =
                FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {

            Toast.makeText(
                    this,
                    "User belum login",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // ================= CREATE =================
        if (!"after".equals(mode)) {

            if (project.isEmpty()
                    || jenis.isEmpty()
                    || mulai.isEmpty()
                    || due.isEmpty()) {

                Toast.makeText(
                        this,
                        "Lengkapi semua field!",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }
        }

        // ================= AFTER =================
        else {

            // wajib 2 foto
            if (listAfter.size() < 2) {

                Toast.makeText(
                        this,
                        "Upload 2 foto AFTER",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            // wajib tanggal
            if (tanggalSelesaiManual.isEmpty()) {

                Toast.makeText(
                        this,
                        "Pilih tanggal selesai",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }
        }

        String uid = user.getUid();

        startUpload(
                uid,
                project,
                jenis,
                mulai,
                due
        );
    }

    private void startUpload(String uid,
                             String project,
                             String jenis,
                             String mulai,
                             String due) {

        if (id == null) {
            id = database.push().getKey();
        }

        if (shimmer != null) {

            shimmer.setVisibility(View.VISIBLE);

            shimmer.startShimmer();
        }

        if (progressBar != null) {

            progressBar.setVisibility(View.VISIBLE);

            progressBar.setIndeterminate(false);

            progressBar.setProgress(0);
        }

        if (txtProgress != null) {

            txtProgress.setVisibility(View.VISIBLE);

            txtProgress.setText("Preparing upload...");
        }

        btnSimpan.setEnabled(false);

        dialog.show();

        lanjutUpload(
                uid,
                project,
                jenis,
                mulai,
                due
        );
    }



    // ================= UPLOAD BERURUTAN =================
    private void lanjutUpload(String uid,
                              String project,
                              String jenis,
                              String mulai,
                              String due) {

        uploadImagesCloudinary(listBefore, "before", beforeUrls -> {

            uploadImagesCloudinary(listAfter, "after", afterUrls -> {

                final List<String> finalBeforeUrls =
                        beforeUrls != null
                                ? new ArrayList<>(beforeUrls)
                                : new ArrayList<>();

                final List<String> finalAfterUrls =
                        afterUrls != null
                                ? new ArrayList<>(afterUrls)
                                : new ArrayList<>();


                // ================= VALIDASI AFTER =================
                if ("after".equals(mode) && finalAfterUrls.isEmpty()) {

                    runOnUiThread(() -> {

                        Toast.makeText(
                                AddTaskActivity.this,
                                "Foto AFTER wajib",
                                Toast.LENGTH_SHORT
                        ).show();


                        if (progressBar != null)
                            progressBar.setVisibility(View.GONE);

                        if (txtProgress != null)
                            txtProgress.setVisibility(View.GONE);

                        if (shimmer != null) {
                            shimmer.stopShimmer();
                            shimmer.setVisibility(View.GONE);
                        }

                        dialog.dismiss();
                        btnSimpan.setEnabled(true);
                    });

                    return;
                }


                // ================= MODE AFTER =================
                if ("after".equals(mode)) {

                    database.child(id)
                            .get()
                            .addOnSuccessListener(snapshot -> {

                                Task oldTask = snapshot.getValue(Task.class);


                                if (oldTask == null) {

                                    dialog.dismiss();

                                    btnSimpan.setEnabled(true);


                                    if (progressBar != null)
                                        progressBar.setVisibility(View.GONE);

                                    if (txtProgress != null)
                                        txtProgress.setVisibility(View.GONE);

                                    if (shimmer != null) {
                                        shimmer.stopShimmer();
                                        shimmer.setVisibility(View.GONE);
                                    }


                                    Toast.makeText(
                                            AddTaskActivity.this,
                                            "Task tidak ditemukan",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    return;
                                }

                                // BEFORE lama
                                ArrayList<String> beforeLama =
                                        oldTask.getBeforeUrls() != null
                                                ? new ArrayList<>(oldTask.getBeforeUrls())
                                                : new ArrayList<>();

                                // AFTER lama
                                ArrayList<String> afterLama =
                                        oldTask.getAfterUrls() != null
                                                ? new ArrayList<>(oldTask.getAfterUrls())
                                                : new ArrayList<>();

                                // gabung AFTER lama + baru
                                afterLama.addAll(finalAfterUrls);

                                // simpan lagi
                                oldTask.setBeforeUrls(beforeLama);
                                oldTask.setAfterUrls(afterLama);

                                // status DONE
                                oldTask.setStatus("DONE");

                                // pilih tanggal selesai
                                        oldTask.setTanggalSelesai(
                                                tanggalSelesaiManual
                                        );

                                        database.child(id)
                                                .setValue(oldTask)
                                                .addOnSuccessListener(aVoid -> {

                                                    dialog.dismiss();

                                                    btnSimpan.setEnabled(true);

                                                    if (progressBar != null)
                                                        progressBar.setVisibility(View.GONE);

                                                    if (txtProgress != null)
                                                        txtProgress.setVisibility(View.GONE);

                                                    if (shimmer != null) {

                                                        shimmer.stopShimmer();

                                                        shimmer.setVisibility(View.GONE);
                                                    }

                                                    Toast.makeText(
                                                            AddTaskActivity.this,
                                                            "Task selesai",
                                                            Toast.LENGTH_SHORT
                                                    ).show();

                                                    finish();
                                                })
                                                .addOnFailureListener(e -> {

                                                    dialog.dismiss();

                                                    btnSimpan.setEnabled(true);

                                                    Toast.makeText(
                                                            AddTaskActivity.this,
                                                            "Gagal update",
                                                            Toast.LENGTH_SHORT
                                                    ).show();
                                                });
                                        });


                            }



                // ================= MODE CREATE =================
                else {

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
                            finalBeforeUrls,
                            finalAfterUrls,
                            uid
                    );

                    database.child(id)
                            .setValue(task)
                            .addOnSuccessListener(aVoid -> {

                                dialog.dismiss();
                                btnSimpan.setEnabled(true);

                                listBefore.clear();
                                listAfter.clear();

                                adapterBefore.notifyDataSetChanged();
                                adapterAfter.notifyDataSetChanged();


                                if (progressBar != null)
                                    progressBar.setVisibility(View.GONE);

                                if (txtProgress != null)
                                    txtProgress.setVisibility(View.GONE);

                                if (shimmer != null) {
                                    shimmer.stopShimmer();
                                    shimmer.setVisibility(View.GONE);
                                }


                                Toast.makeText(
                                        AddTaskActivity.this,
                                        "Task berhasil disimpan",
                                        Toast.LENGTH_SHORT
                                ).show();

                                finish();
                            })
                            .addOnFailureListener(e -> {

                                dialog.dismiss();
                                btnSimpan.setEnabled(true);

                                Toast.makeText(
                                        AddTaskActivity.this,
                                        "Gagal simpan",
                                        Toast.LENGTH_SHORT
                                ).show();
                            });
                }

            });

        });
    }


    // ================= UPLOAD =================
    private void uploadImagesCloudinary(
            List<Uri> list,
            String folder,
            OnUploadComplete callback
    ) {

        List<String> urls = new ArrayList<>();

        // jika kosong
        if (list == null || list.isEmpty()) {

            callback.onComplete(urls);

            return;
        }

        int total = list.size();

        int[] count = {0};

        for (Uri uri : list) {

            boolean isAfter =
                    folder.equals("after");

            Uri compressed =
                    compressImage(uri, isAfter);

            Log.d(
                    "UPLOAD_START",
                    compressed.toString()
            );

            MediaManager.get()
                    .upload(compressed)
                    .unsigned("sipaman_preset")
                    .option("folder", "sipaman/" + folder)
                    .option("resource_type", "image")
                    .callback(new UploadCallback() {

                        @Override
                        public void onStart(String requestId) {

                            runOnUiThread(() -> {

                                if (progressBar != null) {

                                    progressBar.setVisibility(View.VISIBLE);

                                    progressBar.setIndeterminate(true);
                                }

                                if (txtProgress != null) {

                                    txtProgress.setVisibility(View.VISIBLE);

                                    txtProgress.setText("Starting upload...");
                                }
                            });
                        }

                        @Override
                        public void onSuccess(
                                String requestId,
                                Map resultData
                        ) {

                            if (resultData != null
                                    && resultData.get("secure_url") != null) {

                                String url =
                                        resultData
                                                .get("secure_url")
                                                .toString();

                                urls.add(url);

                                Log.d(
                                        "CLOUDINARY_REALTIME",
                                        url
                                );
                            }

                            count[0]++;

                            updateProgress(
                                    count[0],
                                    total
                            );

                            if (count[0] == total) {

                                new Handler(Looper.getMainLooper()).post(() -> {
                                    callback.onComplete(urls);
                                });
                            }
                        }

                        @Override
                        public void onProgress(
                                String requestId,
                                long bytes,
                                long totalBytes
                        ) {

                            if (totalBytes <= 0) return;

                            int percent =
                                    (int) (
                                            (bytes * 100)
                                                    / totalBytes
                                    );

                            runOnUiThread(() -> {

                                if (progressBar != null) {

                                    progressBar.setVisibility(View.VISIBLE);

                                    progressBar.setProgress(percent);
                                }

                                if (txtProgress != null) {

                                    txtProgress.setVisibility(View.VISIBLE);

                                    txtProgress.setText(
                                            "Uploading "
                                                    + percent
                                                    + "%"
                                    );
                                }
                            });
                        }

                        @Override
                        public void onError(
                                String requestId,
                                ErrorInfo error
                        ) {

                            Log.e(
                                    "UPLOAD_ERROR",
                                    error.getDescription()
                            );

                            count[0]++;

                            if (count[0] == total) {

                                callback.onComplete(urls);
                            }
                        }

                        @Override
                        public void onReschedule(
                                String requestId,
                                ErrorInfo error
                        ) {

                        }

                    }).dispatch();
        }
    }

    private void updateProgress(int current, int total) {

        runOnUiThread(() -> {

            if (progressBar != null) {

                progressBar.setVisibility(View.VISIBLE);

                int percent =
                        (int) (((float) current / total) * 100);

                progressBar.setIndeterminate(false);

                progressBar.setProgress(percent);
            }

            if (txtProgress != null) {

                txtProgress.setVisibility(View.VISIBLE);

                txtProgress.setText(
                        "Uploading "
                                + current
                                + " / "
                                + total
                );
            }
        });
    }

    private void finishUpload(OnUploadComplete callback,
                              List<String> urls) {

        callback.onComplete(urls);
    }

    interface OnUploadComplete {

        void onComplete(List<String> urls);
    }


    // ================= COMPRESS =================
    private Uri compressImage(Uri uri, boolean isAfter) {

        try {

            InputStream input =
                    getContentResolver().openInputStream(uri);

            BitmapFactory.Options options =
                    new BitmapFactory.Options();

            options.inJustDecodeBounds = true;

            BitmapFactory.decodeStream(input, null, options);

            input.close();

            int photoW = options.outWidth;
            int photoH = options.outHeight;

            int scaleFactor = Math.max(
                    1,
                    Math.min(photoW / 1280, photoH / 1280)
            );

            BitmapFactory.Options options2 =
                    new BitmapFactory.Options();

            options2.inSampleSize = scaleFactor;

            InputStream input2 =
                    getContentResolver().openInputStream(uri);

            Bitmap bitmap =
                    BitmapFactory.decodeStream(
                            input2,
                            null,
                            options2
                    );

            input2.close();

            if (bitmap == null) return uri;

            int maxWidth;
            int maxHeight;
            int quality;

            // AFTER → super kecil
            if (isAfter) {

                maxWidth = 480;
                maxHeight = 480;
                quality = 40;

            }

            // BEFORE → lebih bagus
            else {

                maxWidth = 900;
                maxHeight = 900;
                quality = 50;
            }

            int width = bitmap.getWidth();
            int height = bitmap.getHeight();

            float ratio =
                    Math.min(
                            (float) maxWidth / width,
                            (float) maxHeight / height
                    );

            int finalWidth = Math.round(width * ratio);
            int finalHeight = Math.round(height * ratio);

            Bitmap resized =
                    Bitmap.createScaledBitmap(
                            bitmap,
                            finalWidth,
                            finalHeight,
                            true
                    );

            File file = new File(
                    getCacheDir(),
                    "IMG_" + System.currentTimeMillis() + ".jpg"
            );

            FileOutputStream out =
                    new FileOutputStream(file);

            resized.compress(
                    Bitmap.CompressFormat.JPEG,
                    quality,
                    out
            );

            out.flush();
            out.close();

            bitmap.recycle();

            resized.recycle();

            return Uri.fromFile(file);


        } catch (Exception e) {

            e.printStackTrace();

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

        Intent intent =
                new Intent(Intent.ACTION_OPEN_DOCUMENT);

        intent.setType("image/*");

        intent.putExtra(
                Intent.EXTRA_ALLOW_MULTIPLE,
                true
        );

        intent.addCategory(Intent.CATEGORY_OPENABLE);

        startActivityForResult(
                intent,
                100
        );
    }

    private void openCamera() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    200);

            return;
        }

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

        if (requestCode == 200) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera(); // ulang buka kamera
            } else {
                Toast.makeText(this, "Izin kamera ditolak", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ================= RESULT =================
    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data
    ) {

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) return;

        // CAMERA
        if (requestCode == 200) {

            if (cameraUri == null) return;

            if (currentType == 0) {

                if (listBefore.size() >= 2) return;

                listBefore.add(cameraUri);

                adapterBefore.notifyDataSetChanged();

                if (listBefore.size() >= 2) {
                    btnAddBefore.setVisibility(View.GONE);
                }
            } else {

                if (listAfter.size() >= 2) {

                    Toast.makeText(
                            this,
                            "Max 2 foto AFTER",
                            Toast.LENGTH_SHORT
                    ).show();

                    return;
                }

                // AFTER


                getContentResolver().notifyChange(cameraUri, null);
                listAfter.add(cameraUri);

                adapterAfter =
                        new PhotoUriAdapter(
                                this,
                                listAfter
                        );

                rvAfter.setAdapter(adapterAfter);

                adapterAfter.notifyDataSetChanged();

                rvAfter.setVisibility(View.VISIBLE);

                if (listAfter.size() >= 2) {

                    btnAddAfter.setVisibility(View.GONE);

                    Toast.makeText(
                            this,
                            "2 foto AFTER siap",
                            Toast.LENGTH_SHORT
                    ).show();

                } else {

                    btnAddAfter.setVisibility(View.VISIBLE);

                    Toast.makeText(
                            this,
                            "Tambah 1 foto lagi",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            return;
        }

        // GALLERY
        if (requestCode == 100 && data != null) {

            List<Uri> selectedUris = new ArrayList<>();

            // MULTIPLE
            if (data.getClipData() != null) {

                int total =
                        data.getClipData().getItemCount();

                for (int i = 0; i < total; i++) {

                    Uri uri =
                            data.getClipData()
                                    .getItemAt(i)
                                    .getUri();

                    selectedUris.add(uri);
                }
            }

            // SINGLE
            else if (data.getData() != null) {

                selectedUris.add(data.getData());
            }

            // ================= BEFORE =================
            if (currentType == 0) {

                for (Uri uri : selectedUris) {

                    if (listBefore.size() >= 2) break;

                    listBefore.add(uri);
                }

                adapterBefore.notifyDataSetChanged();

                if (listBefore.size() >= 2) {

                    btnAddBefore.setVisibility(View.GONE);

                } else {

                    btnAddBefore.setVisibility(View.VISIBLE);
                }
            }

            // ================= AFTER =================
            else {

                for (Uri uri : selectedUris) {

                    if (listAfter.size() >= 2) break;

                    listAfter.add(uri);
                }

                adapterAfter =
                        new PhotoUriAdapter(
                                this,
                                listAfter
                        );

                rvAfter.setAdapter(adapterAfter);

                adapterAfter.notifyDataSetChanged();

                rvAfter.setVisibility(View.VISIBLE);

                if (listAfter.size() >= 2) {

                    btnAddAfter.setVisibility(View.GONE);

                    Toast.makeText(
                            this,
                            "2 foto AFTER siap",
                            Toast.LENGTH_SHORT
                    ).show();

                } else {

                    btnAddAfter.setVisibility(View.VISIBLE);

                    Toast.makeText(
                            this,
                            "Tambah 1 foto lagi",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }
        }
    }

        private void showDatePicker (EditText editText){
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this,
                    (view, y, m, d) -> editText.setText(d + "/" + (m + 1) + "/" + y),
                    c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH)
            ).show();
        }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        listBefore.clear();
        listAfter.clear();
    }
    }

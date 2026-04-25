package com.sipaman.maintenance;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class PreviewActivity extends AppCompatActivity {

    ImageView imgPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        imgPreview = findViewById(R.id.imgPreview);

        String uri = getIntent().getStringExtra("image");

        Glide.with(this)
                .load(uri)
                .into(imgPreview);
    }
}
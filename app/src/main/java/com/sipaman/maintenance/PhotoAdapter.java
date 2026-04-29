package com.sipaman.maintenance;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolder> {

    Context context;
    List<String> urlList;

    public PhotoAdapter(Context context, List<String> urlList) {
        this.context = context;
        this.urlList = (urlList == null) ? new ArrayList<>() : urlList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_photo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        String url = urlList.get(position);

        Glide.with(context)
                .load(url)
                .into(holder.imgPhoto);

        // 🔥 PREVIEW
        holder.imgPhoto.setOnClickListener(v -> {
            Intent intent = new Intent(context, PreviewActivity.class);
            intent.putExtra("image", url);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return urlList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPhoto;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPhoto = itemView.findViewById(R.id.imgPhoto);
        }
    }
}
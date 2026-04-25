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

import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolder> {

    Context context;
    List<Uri> list;
    boolean isAddButton;
    int type;

    public PhotoAdapter(Context context, List<Uri> list, boolean isAddButton, int type) {
        this.context = context;
        this.list = list;
        this.isAddButton = isAddButton;
        this.type = type;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_photo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        if (position < list.size()) {

            Uri uri = list.get(position);

            holder.btnDelete.setVisibility(View.VISIBLE);

            Glide.with(context)
                    .load(uri)
                    .into(holder.imgPhoto);

            // 🔥 PREVIEW
            holder.imgPhoto.setOnClickListener(v -> {
                Intent intent = new Intent(context, PreviewActivity.class);
                intent.putExtra("image", uri.toString());
                context.startActivity(intent);
            });

            // 🔥 DELETE
            holder.btnDelete.setOnClickListener(v -> {
                list.remove(position);
                notifyDataSetChanged();
            });

        } else {

            holder.btnDelete.setVisibility(View.GONE);
            holder.imgPhoto.setImageResource(android.R.drawable.ic_input_add);

            holder.imgPhoto.setOnClickListener(v -> {
                ((AddTaskActivity) context).openGallery(type);
            });
        }
    }

    @Override
    public int getItemCount() {
        return list.size() + 1;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgPhoto, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPhoto = itemView.findViewById(R.id.imgPhoto);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
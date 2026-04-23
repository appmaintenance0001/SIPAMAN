package com.sipaman.maintenance;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolder> {

    private List<Uri> list;
    private Context context;
    private boolean isEnabled;
    private int type; // 🔥 0 = before, 1 = after

    public PhotoAdapter(Context context, List<Uri> list, boolean isEnabled, int type) {
        this.context = context;
        this.list = list;
        this.isEnabled = isEnabled;
        this.type = type;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_photo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        // 🔥 FOTO BIASA
        if (position < list.size()) {

            holder.imgPhoto.setImageURI(list.get(position));
            holder.btnDelete.setVisibility(View.VISIBLE);

            holder.btnDelete.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    list.remove(pos);
                    notifyItemRemoved(pos);
                }
            });

        } else {
            // 🔥 TOMBOL TAMBAH
            holder.imgPhoto.setImageResource(android.R.drawable.ic_input_add);
            holder.btnDelete.setVisibility(View.GONE);

            holder.imgPhoto.setOnClickListener(v -> {
                if (isEnabled && context instanceof AddTaskActivity) {
                    ((AddTaskActivity) context).openGallery(type);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return list.size() < 4 ? list.size() + 1 : 4;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPhoto, btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            imgPhoto = itemView.findViewById(R.id.imgPhoto);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
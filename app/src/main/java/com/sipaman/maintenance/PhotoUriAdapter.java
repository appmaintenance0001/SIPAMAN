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

public class PhotoUriAdapter extends RecyclerView.Adapter<PhotoUriAdapter.ViewHolder> {

    private final Context context;
    private final List<Uri> uriList;

    public PhotoUriAdapter(
            Context context,
            List<Uri> uriList,
            boolean showDelete,
            OnDeleteClick deleteClick
    ) {
        this.context = context;
        this.uriList = (uriList == null) ? new ArrayList<>() : uriList;
        this.showDelete = showDelete;
        this.deleteClick = deleteClick;
    }

    private final boolean showDelete;

    public interface OnDeleteClick {
        void onDelete(int position);
    }

    private final OnDeleteClick deleteClick;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Audit: gunakan parent context agar lifecycle Glide mengikuti item view.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Uri uri = uriList.get(position);

        Glide.with(holder.itemView)
                .load(uri)
                .into(holder.imgPhoto);

        if (showDelete) {
            holder.btnDelete.setVisibility(View.VISIBLE);
        } else {
            holder.btnDelete.setVisibility(View.GONE);
        }

        holder.btnDelete.setOnClickListener(v -> {
            int adapterPosition = holder.getBindingAdapterPosition();
            // Audit: posisi RecyclerView bisa invalid saat item sedang dianimasikan/dihapus.
            if (deleteClick != null && adapterPosition != RecyclerView.NO_POSITION) {
                deleteClick.onDelete(adapterPosition);
            }
        });

        holder.imgPhoto.setOnClickListener(v -> {

            Intent intent =
                    new Intent(v.getContext(), PreviewActivity.class);

            intent.putExtra(
                    "image",
                    uri.toString()
            );

            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return uriList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPhoto;
        ImageView btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPhoto = itemView.findViewById(R.id.imgPhoto);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}

package com.sipaman.maintenance;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OverdueAdapter extends RecyclerView.Adapter<OverdueAdapter.ViewHolder> {

    private final List<Task> list;
    private final SimpleDateFormat sourceFormat = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
    private final SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMM yyyy", Locale.US);

    public OverdueAdapter(List<Task> list) {
        this.list = list;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtTanggal, txtDue;

        public ViewHolder(View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtTanggal = itemView.findViewById(R.id.txtTanggal);
            txtDue = itemView.findViewById(R.id.txtDue);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task_overdue, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Task task = list.get(position);

        holder.txtTitle.setText(isEmpty(task.getJudul()) ? "Task Maintenance" : task.getJudul());
        holder.txtTanggal.setText(isEmpty(task.getProject()) ? "-" : task.getProject());
        holder.txtDue.setText(formatDate(task.getDue()));

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), DetailTaskActivity.class);
            intent.putExtra("id", task.getId());
            intent.putExtra("judul", task.getJudul());
            intent.putExtra("project", task.getProject());
            intent.putExtra("jenis", task.getJenis());
            intent.putExtra("mulai", task.getMulai());
            intent.putExtra("due", task.getDue());
            intent.putExtra("status", task.getStatus());
            intent.putExtra("tanggalSelesai", task.getTanggalSelesai());
            intent.putExtra("pic", task.getPic());
            intent.putExtra("priority", task.getPriority());
            intent.putExtra("deskripsi", task.getDeskripsi());
            v.getContext().startActivity(intent);
        });
    }

    private String formatDate(String value) {
        try {
            Date date = sourceFormat.parse(value);
            return date == null ? "-" : displayFormat.format(date);
        } catch (Exception e) {
            return "-";
        }
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }
}

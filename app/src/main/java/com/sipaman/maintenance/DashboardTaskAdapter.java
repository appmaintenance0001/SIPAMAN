package com.sipaman.maintenance;

import android.content.Intent;
import android.graphics.Color;
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

public class DashboardTaskAdapter extends RecyclerView.Adapter<DashboardTaskAdapter.ViewHolder> {

    private final List<Task> list;
    private final boolean overdueMode;
    private final SimpleDateFormat sourceFormat = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
    private final SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMM yyyy", Locale.US);

    public DashboardTaskAdapter(List<Task> list, boolean overdueMode) {
        this.list = list;
        this.overdueMode = overdueMode;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTaskIcon, tvTaskTitle, tvTaskMeta, tvTaskSub, tvTaskBadge, tvTaskDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTaskIcon = itemView.findViewById(R.id.tvTaskIcon);
            tvTaskTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvTaskMeta = itemView.findViewById(R.id.tvTaskMeta);
            tvTaskSub = itemView.findViewById(R.id.tvTaskSub);
            tvTaskBadge = itemView.findViewById(R.id.tvTaskBadge);
            tvTaskDate = itemView.findViewById(R.id.tvTaskDate);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dashboard_task, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Task task = list.get(position);
        holder.tvTaskTitle.setText(isEmpty(task.getJudul()) ? "Task Maintenance" : task.getJudul());
        holder.tvTaskMeta.setText(safe(task.getProject()) + " - PIC: " + safe(task.getPic()));
        holder.tvTaskDate.setText(formatDate(task.getDue()));

        if (overdueMode) {
            holder.tvTaskIcon.setText("!");
            holder.tvTaskIcon.setTextColor(Color.parseColor("#E11D48"));
            holder.tvTaskIcon.setBackgroundResource(R.drawable.bg_task_icon_overdue);
            holder.tvTaskBadge.setText("OVERDUE");
            holder.tvTaskBadge.setBackgroundResource(R.drawable.bg_status_overdue);
            holder.tvTaskSub.setText("Terlambat " + Math.max(1, Math.abs(daysFromNow(task.getDue()))) + " hari");
            holder.tvTaskSub.setTextColor(Color.parseColor("#E11D48"));
        } else {
            holder.tvTaskIcon.setText("D");
            holder.tvTaskIcon.setTextColor(Color.parseColor("#CA8A04"));
            holder.tvTaskIcon.setBackgroundResource(R.drawable.bg_task_icon_progress);
            int days = Math.max(0, daysFromNow(task.getDue()));
            holder.tvTaskBadge.setText("DUE IN " + days + " DAYS");
            holder.tvTaskBadge.setBackgroundResource(R.drawable.bg_status_progress);
            holder.tvTaskSub.setText(days == 0 ? "Jatuh tempo hari ini" : "Jatuh tempo dalam " + days + " hari");
            holder.tvTaskSub.setTextColor(Color.parseColor("#CA8A04"));
        }

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

    private int daysFromNow(String value) {
        try {
            Date date = sourceFormat.parse(value);
            if (date == null) return 0;
            long diff = date.getTime() - System.currentTimeMillis();
            return (int) Math.ceil(diff / (1000d * 60 * 60 * 24));
        } catch (Exception e) {
            return 0;
        }
    }

    private String safe(String value) {
        return isEmpty(value) ? "-" : value;
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }
}

package com.sipaman.maintenance;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private final List<NotificationItem> list;

    public NotificationAdapter(List<NotificationItem> list) {
        this.list = list;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNotifIcon, tvNotifTitle, tvNotifMessage, tvNotifDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNotifIcon = itemView.findViewById(R.id.tvNotifIcon);
            tvNotifTitle = itemView.findViewById(R.id.tvNotifTitle);
            tvNotifMessage = itemView.findViewById(R.id.tvNotifMessage);
            tvNotifDate = itemView.findViewById(R.id.tvNotifDate);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationItem item = list.get(position);

        holder.tvNotifTitle.setText(item.getTitle());
        holder.tvNotifMessage.setText(item.getMessage());
        holder.tvNotifDate.setText(item.getDate());
        applyType(holder, item.getType());

        holder.itemView.setOnClickListener(v -> {
            Task task = item.getTask();
            if (task == null) return;

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

    private void applyType(ViewHolder holder, String type) {
        if ("DONE".equals(type)) {
            holder.tvNotifIcon.setText("OK");
            holder.tvNotifIcon.setTextColor(Color.parseColor("#16A34A"));
            holder.tvNotifIcon.setBackgroundResource(R.drawable.bg_task_icon_done);
        } else if ("OVERDUE".equals(type)) {
            holder.tvNotifIcon.setText("!");
            holder.tvNotifIcon.setTextColor(Color.parseColor("#E11D48"));
            holder.tvNotifIcon.setBackgroundResource(R.drawable.bg_task_icon_overdue);
        } else if ("REMINDER".equals(type)) {
            holder.tvNotifIcon.setText("H-1");
            holder.tvNotifIcon.setTextColor(Color.parseColor("#CA8A04"));
            holder.tvNotifIcon.setBackgroundResource(R.drawable.bg_task_icon_progress);
        } else {
            holder.tvNotifIcon.setText("NEW");
            holder.tvNotifIcon.setTextColor(Color.parseColor("#173A6A"));
            holder.tvNotifIcon.setBackgroundResource(R.drawable.bg_calendar_task_day);
        }
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }
}

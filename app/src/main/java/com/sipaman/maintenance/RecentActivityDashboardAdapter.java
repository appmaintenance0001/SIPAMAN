package com.sipaman.maintenance;

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

public class RecentActivityDashboardAdapter extends RecyclerView.Adapter<RecentActivityDashboardAdapter.ViewHolder> {

    private final List<ActivityLog> list;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.US);

    public RecentActivityDashboardAdapter(List<ActivityLog> list) {
        this.list = list;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvActivityIcon, tvActivityMessage, tvActivityUser, tvActivityTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvActivityIcon = itemView.findViewById(R.id.tvActivityIcon);
            tvActivityMessage = itemView.findViewById(R.id.tvActivityMessage);
            tvActivityUser = itemView.findViewById(R.id.tvActivityUser);
            tvActivityTime = itemView.findViewById(R.id.tvActivityTime);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recent_activity_dashboard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ActivityLog log = list.get(position);
        holder.tvActivityMessage.setText(isEmpty(log.getMessage()) ? log.getType() : log.getMessage());
        holder.tvActivityUser.setText(isEmpty(log.getUserName()) ? "-" : log.getUserName());
        holder.tvActivityTime.setText(dateFormat.format(new Date(log.getTimestamp())));

        String type = log.getType() == null ? "" : log.getType().toLowerCase();
        if (type.contains("selesai") || type.contains("done")) {
            holder.tvActivityIcon.setText("OK");
            holder.tvActivityIcon.setTextColor(Color.parseColor("#16A34A"));
            holder.tvActivityIcon.setBackgroundResource(R.drawable.bg_task_icon_done);
        } else if (type.contains("overdue")) {
            holder.tvActivityIcon.setText("!");
            holder.tvActivityIcon.setTextColor(Color.parseColor("#E11D48"));
            holder.tvActivityIcon.setBackgroundResource(R.drawable.bg_task_icon_overdue);
        } else {
            holder.tvActivityIcon.setText("+");
            holder.tvActivityIcon.setTextColor(Color.parseColor("#1D4ED8"));
            holder.tvActivityIcon.setBackgroundResource(R.drawable.bg_calendar_task_day);
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

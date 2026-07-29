package com.sipaman.maintenance;

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

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private final List<ActivityLog> list;
    private final SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.US);

    public HistoryAdapter(List<ActivityLog> list) {
        this.list = list;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvHistoryType, tvHistoryMessage, tvHistoryMeta;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHistoryType = itemView.findViewById(R.id.tvHistoryType);
            tvHistoryMessage = itemView.findViewById(R.id.tvHistoryMessage);
            tvHistoryMeta = itemView.findViewById(R.id.tvHistoryMeta);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ActivityLog log = list.get(position);
        holder.tvHistoryType.setText(log.getType());
        holder.tvHistoryMessage.setText(log.getMessage());
        holder.tvHistoryMeta.setText(log.getUserName() + " - " + format.format(new Date(log.getTimestamp())));
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }
}

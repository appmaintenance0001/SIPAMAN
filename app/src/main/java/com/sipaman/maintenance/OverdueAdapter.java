package com.sipaman.maintenance;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class OverdueAdapter extends RecyclerView.Adapter<OverdueAdapter.ViewHolder> {

    List<Task> list;

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

        Task t = list.get(position);

        holder.txtTitle.setText(t.getProject());
        holder.txtTanggal.setText(t.getMulai());
        holder.txtDue.setText(t.getDue());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}

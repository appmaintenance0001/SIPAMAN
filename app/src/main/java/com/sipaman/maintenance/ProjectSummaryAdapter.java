package com.sipaman.maintenance;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ProjectSummaryAdapter
        extends RecyclerView.Adapter<ProjectSummaryAdapter.ViewHolder> {

    private final List<ProjectSummary> list;

    public ProjectSummaryAdapter(List<ProjectSummary> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_project_summary,
                        parent,
                        false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position) {

        ProjectSummary item = list.get(position);
        int maxTotal = 1;
        for (ProjectSummary summary : list) {
            if (summary.getTotalTask() > maxTotal) {
                maxTotal = summary.getTotalTask();
            }
        }
        int percent = Math.round((item.getTotalTask() * 100f) / maxTotal);

        holder.tvProject.setText(item.getProjectName());

        holder.tvTotal.setText(
                item.getTotalTask() + " Task");

        holder.tvDone.setText(
                "Done: " + item.getDoneTask());

        holder.tvProgress.setText(
                "Progress: " + item.getProgressTask());

        holder.tvOverdue.setText(
                "Overdue: " + item.getOverdueTask());
        holder.progressProject.setProgress(percent);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvProject;
        TextView tvTotal;
        TextView tvDone;
        TextView tvProgress;
        TextView tvOverdue;
        ProgressBar progressProject;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvProject = itemView.findViewById(R.id.tvProject);
            tvTotal = itemView.findViewById(R.id.tvTotal);
            tvDone = itemView.findViewById(R.id.tvDone);
            tvProgress = itemView.findViewById(R.id.tvProgress);
            tvOverdue = itemView.findViewById(R.id.tvOverdue);
            progressProject = itemView.findViewById(R.id.progressProject);
        }
    }
}

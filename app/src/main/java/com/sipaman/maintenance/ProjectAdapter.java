package com.sipaman.maintenance;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Map;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ViewHolder> {

    public interface ProjectActionListener {
        void onClick(Project project);
        void onEdit(Project project);
        void onDelete(Project project);
    }

    private final List<Project> list;
    private final Map<String, Integer> taskCountMap;
    private final ProjectActionListener listener;

    public ProjectAdapter(List<Project> list, Map<String, Integer> taskCountMap, ProjectActionListener listener) {
        this.list = list;
        this.taskCountMap = taskCountMap;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvProjectIcon, tvProjectName, tvProjectLocation, tvTaskCount, btnProjectMore;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProjectIcon = itemView.findViewById(R.id.tvProjectIcon);
            tvProjectName = itemView.findViewById(R.id.tvProjectName);
            tvProjectLocation = itemView.findViewById(R.id.tvProjectLocation);
            tvTaskCount = itemView.findViewById(R.id.tvTaskCount);
            btnProjectMore = itemView.findViewById(R.id.btnProjectMore);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_project, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Project project = list.get(position);
        String name = project.getName() == null ? "Project" : project.getName();
        int count = taskCountMap.containsKey(name) ? taskCountMap.get(name) : 0;

        holder.tvProjectName.setText(name);
        holder.tvProjectLocation.setText(project.getLocation() == null ? "-" : project.getLocation());
        holder.tvTaskCount.setText(count + " Task");
        holder.tvProjectIcon.setText(project.getIcon() == null || project.getIcon().trim().isEmpty()
                ? getProjectInitial(name)
                : project.getIcon());

        try {
            holder.tvProjectIcon.setTextColor(Color.parseColor(project.getColor()));
        } catch (Exception e) {
            holder.tvProjectIcon.setTextColor(Color.parseColor("#173A6A"));
        }

        holder.itemView.setOnClickListener(v -> {
            listener.onClick(project);
        });

        holder.btnProjectMore.setOnClickListener(v -> {
            PopupMenu menu = new PopupMenu(v.getContext(), holder.btnProjectMore);
            menu.getMenu().add("Edit");
            menu.getMenu().add("Hapus");
            menu.setOnMenuItemClickListener(item -> {
                if ("Edit".contentEquals(item.getTitle())) {
                    listener.onEdit(project);
                } else {
                    listener.onDelete(project);
                }
                return true;
            });
            menu.show();
        });
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    private String getProjectInitial(String name) {
        // Audit: nama project kosong tidak boleh membuat substring crash.
        if (name == null || name.trim().isEmpty()) {
            return "P";
        }
        return name.trim().substring(0, 1).toUpperCase();
    }
}

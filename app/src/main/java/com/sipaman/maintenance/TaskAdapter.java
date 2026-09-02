package com.sipaman.maintenance;

import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
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

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {

    private final List<Task> taskList;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private String highlightedTaskId = null;
    private OnItemClickListener listener;
    private final Runnable clearHighlightRunnable = () -> {
        String previousId = highlightedTaskId;
        highlightedTaskId = null;
        notifyTaskChanged(previousId);
    };

    public interface OnItemClickListener {
        void onItemClick(Task task);
    }

    public TaskAdapter(List<Task> taskList) {
        this.taskList = taskList;
    }

    public TaskAdapter(List<Task> list, OnItemClickListener listener) {
        this.taskList = list;
        this.listener = listener;
    }

    public void setHighlightedTask(String taskId) {
        String previousId = highlightedTaskId;
        this.highlightedTaskId = taskId;
        mainHandler.removeCallbacks(clearHighlightRunnable);
        notifyTaskChanged(previousId);
        notifyTaskChanged(taskId);

        // Audit: delayed callback disimpan agar bisa dibatalkan saat RecyclerView detach.
        mainHandler.postDelayed(clearHighlightRunnable, 3000);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtJudul, txtId, txtProject, txtJenis, txtMulai, txtDue, txtStatus, txtSelesai, txtStatusIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtJudul = itemView.findViewById(R.id.txtJudul);
            txtStatusIcon = itemView.findViewById(R.id.txtStatusIcon);
            txtId = itemView.findViewById(R.id.txtId);
            txtProject = itemView.findViewById(R.id.txtProject);
            txtJenis = itemView.findViewById(R.id.txtJenis);
            txtMulai = itemView.findViewById(R.id.txtMulai);
            txtDue = itemView.findViewById(R.id.txtDue);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            txtSelesai = itemView.findViewById(R.id.txtSelesai);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Task task = taskList.get(position);

        holder.txtJudul.setText(isEmpty(task.getJudul()) ? "Task Maintenance" : task.getJudul());
        holder.txtId.setText("ID: " + safeText(task.getId()));
        holder.txtProject.setText(isEmpty(task.getProject()) ? "-" : task.getProject());
        holder.txtJenis.setText(isEmpty(task.getJenis()) ? "-" : task.getJenis());
        holder.txtMulai.setText("Mulai: " + safeText(task.getMulai()));
        holder.txtDue.setText(formatDisplayDate(task.getDue()));
        holder.txtSelesai.setText("Selesai: " + safeText(task.getTanggalSelesai()));

        applyStatusStyle(holder, task);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(task);
                return;
            }

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

        if (task.getId() != null && task.getId().equals(highlightedTaskId)) {
            holder.itemView.setBackgroundResource(R.drawable.bg_highlight);
        } else {
            holder.itemView.setBackgroundResource(R.drawable.bg_task_list_card);
        }
    }

    private void applyStatusStyle(ViewHolder holder, Task task) {
        String status = normalizeStatus(task.getStatus());

        if ("DONE".equals(status)) {
            holder.txtStatus.setText("Done");
            holder.txtStatus.setTextColor(Color.parseColor("#16A34A"));
            holder.txtStatus.setBackgroundResource(R.drawable.bg_status_done_light);
            holder.txtStatusIcon.setText("OK");
            holder.txtStatusIcon.setTextColor(Color.parseColor("#16A34A"));
            holder.txtStatusIcon.setBackgroundResource(R.drawable.bg_task_icon_done);
        } else if (isOverdue(task.getDue())) {
            holder.txtStatus.setText("Overdue");
            holder.txtStatus.setTextColor(Color.parseColor("#E11D48"));
            holder.txtStatus.setBackgroundResource(R.drawable.bg_status_overdue_light);
            holder.txtStatusIcon.setText("!");
            holder.txtStatusIcon.setTextColor(Color.parseColor("#E11D48"));
            holder.txtStatusIcon.setBackgroundResource(R.drawable.bg_task_icon_overdue);
        } else if ("ON_PROGRESS".equals(status)) {
            holder.txtStatus.setText("On Progress");
            holder.txtStatus.setTextColor(Color.parseColor("#CA8A04"));
            holder.txtStatus.setBackgroundResource(R.drawable.bg_status_progress_light);
            holder.txtStatusIcon.setText("IP");
            holder.txtStatusIcon.setTextColor(Color.parseColor("#CA8A04"));
            holder.txtStatusIcon.setBackgroundResource(R.drawable.bg_task_icon_progress);
        } else {
            holder.txtStatus.setText("Pending");
            holder.txtStatus.setTextColor(Color.parseColor("#64748B"));
            holder.txtStatus.setBackgroundResource(R.drawable.bg_status_pending_light);
            holder.txtStatusIcon.setText("P");
            holder.txtStatusIcon.setTextColor(Color.parseColor("#64748B"));
            holder.txtStatusIcon.setBackgroundResource(R.drawable.bg_task_icon_pending);
        }
    }

    private boolean isOverdue(String dueDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
            Date due = sdf.parse(dueDate);
            return due != null && due.before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    private String normalizeStatus(String status) {
        if (status == null) return "";
        status = status.trim().toUpperCase(Locale.US).replace(" ", "_");
        if (status.contains("DONE")) return "DONE";
        if (status.contains("PROGRESS")) return "ON_PROGRESS";
        if (status.contains("OVERDUE")) return "OVERDUE";
        return status;
    }

    private String formatDisplayDate(String dateText) {
        if (isEmpty(dateText)) return "-";

        try {
            SimpleDateFormat source = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
            SimpleDateFormat target = new SimpleDateFormat("dd MMM yyyy", Locale.US);
            Date date = source.parse(dateText);
            return date == null ? dateText : target.format(date);
        } catch (Exception e) {
            return dateText;
        }
    }

    private String safeText(String value) {
        return isEmpty(value) ? "-" : value;
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    @Override
    public int getItemCount() {
        return taskList == null ? 0 : taskList.size();
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        // Audit: cegah Handler menahan adapter setelah list tidak dipakai.
        mainHandler.removeCallbacks(clearHighlightRunnable);
        super.onDetachedFromRecyclerView(recyclerView);
    }

    private void notifyTaskChanged(String taskId) {
        int position = findTaskPosition(taskId);
        if (position >= 0) {
            notifyItemChanged(position);
        }
    }

    private int findTaskPosition(String taskId) {
        if (taskId == null || taskList == null) return -1;
        for (int i = 0; i < taskList.size(); i++) {
            Task task = taskList.get(i);
            if (task != null && taskId.equals(task.getId())) {
                return i;
            }
        }
        return -1;
    }
}

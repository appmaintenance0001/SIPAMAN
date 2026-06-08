package com.sipaman.maintenance;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import android.graphics.Color;
import android.app.AlertDialog;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import java.util.Calendar;

import android.content.Intent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.List;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;



public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {

    List<Task> taskList;

    public TaskAdapter(List<Task> taskList) {
        this.taskList = taskList;
    }

    private String highlightedTaskId = null;

    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Task task);
    }


    public TaskAdapter(List<Task> list, OnItemClickListener listener) {
        this.taskList = list;
        this.listener = listener;
    }

    public void setHighlightedTask(String taskId) {
        this.highlightedTaskId = taskId;
        notifyDataSetChanged();

        new android.os.Handler().postDelayed(() -> {
            highlightedTaskId = null;
            notifyDataSetChanged();
        }, 3000); // hilang setelah 3 detik
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtId, txtProject, txtJenis, txtMulai, txtDue, txtStatus, txtSelesai;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

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

        holder.txtId.setText("ID: " + task.getId());
        holder.txtProject.setText("Project: " + task.getProject());
        holder.txtJenis.setText(task.getJenis());
        holder.txtMulai.setText("Mulai: " + task.getMulai());

        // 🔥 FIX txtDue
        holder.txtDue.setText("Due: " + task.getDue());
        holder.txtDue.setTextColor(Color.RED);

        // 🔥 RESET STATE (PENTING)

        holder.txtStatus.setBackgroundResource(0);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(taskList.get(position));
            }
        });

        // 🔥 TANGGAL SELESAI
        // =========================
        if (task.getTanggalSelesai() != null) {
            holder.txtSelesai.setText("Selesai: " + task.getTanggalSelesai());
        } else {
            holder.txtSelesai.setText("Selesai: -");
        }

        // =========================
        // 🔥 CLICK EDIT
        // =========================
        holder.itemView.setOnClickListener(v -> {

            Intent intent = new Intent(v.getContext(), DetailTaskActivity.class);

            intent.putExtra("id", task.getId());
            intent.putExtra("project", task.getProject());
            intent.putExtra("jenis", task.getJenis());
            intent.putExtra("mulai", task.getMulai());
            intent.putExtra("due", task.getDue());
            intent.putExtra("status", task.getStatus());
            intent.putExtra("tanggalSelesai", task.getTanggalSelesai());

            // 🔥 TAMBAHAN (PENTING)
            intent.putExtra("pic", task.getPic());
            intent.putExtra("priority", task.getPriority());
            intent.putExtra("deskripsi", task.getDeskripsi());

            v.getContext().startActivity(intent);
        });

        // =========================
        // 🔥 STATUS LOGIC
        // =========================
        if ("DONE".equals(task.getStatus())) {

            holder.txtStatus.setText("DONE");
            holder.txtStatus.setBackgroundResource(R.drawable.bg_status_done);


        } else if (task.getDue() != null && isOverdue(task.getDue())) {

            holder.txtStatus.setText("OVERDUE");
            holder.txtStatus.setBackgroundResource(R.drawable.bg_status_overdue);

        } else {

            holder.txtStatus.setText("ON PROGRESS");
            holder.txtStatus.setBackgroundResource(R.drawable.bg_status_progress);
        }

        // =========================
        // 🔥 HIGHLIGHT ANIMATION
        // =========================
        if (task.getId().equals(highlightedTaskId)) {

            holder.itemView.setBackgroundResource(R.drawable.bg_highlight);

            holder.itemView.setScaleX(0.9f);
            holder.itemView.setScaleY(0.9f);

            holder.itemView.animate()
                    .scaleX(1.05f)
                    .scaleY(1.05f)
                    .setDuration(300)
                    .withEndAction(() ->
                            holder.itemView.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(200)
                                    .start()
                    ).start();

        } else {
            holder.itemView.setBackgroundResource(android.R.color.transparent);
        }
    }

    // ================= OVERDUE CHECK =================
    private boolean isOverdue(String dueDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
            Date due = sdf.parse(dueDate);
            return due.before(new Date());
        } catch (Exception e) {
            return false;
        }
    }


    @Override
    public int getItemCount() {
        return taskList == null ? 0 : taskList.size();
    }
}
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
        Button btnDelete, btnDone;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtId = itemView.findViewById(R.id.txtId);
            txtProject = itemView.findViewById(R.id.txtProject);
            txtJenis = itemView.findViewById(R.id.txtJenis);
            txtMulai = itemView.findViewById(R.id.txtMulai);
            txtDue = itemView.findViewById(R.id.txtDue);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            txtSelesai = itemView.findViewById(R.id.txtSelesai);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnDone = itemView.findViewById(R.id.btnDone);
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
        holder.btnDone.setVisibility(View.VISIBLE);
        holder.txtStatus.setBackgroundResource(0);

        // =========================
        // 🔥 DELETE
        // =========================
        holder.btnDelete.setOnClickListener(v -> {

            new AlertDialog.Builder(v.getContext())
                    .setTitle("Hapus Data")
                    .setMessage("Yakin ingin menghapus task ini?")
                    .setPositiveButton("Hapus", (dialog, which) -> {

                        FirebaseDatabase.getInstance()
                                .getReference("tasks")
                                .child(task.getId())
                                .removeValue();

                        Toast.makeText(v.getContext(), "Data berhasil dihapus", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Batal", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        // =========================
        // 🔥 DONE + DATE PICKER
        // =========================
        holder.btnDone.setOnClickListener(v -> {

            Calendar calendar = Calendar.getInstance();

            new DatePickerDialog(
                    v.getContext(),
                    (view, year, month, dayOfMonth) -> {

                        String tanggalSelesai = dayOfMonth + "/" + (month + 1) + "/" + year;

                        DatabaseReference db = FirebaseDatabase.getInstance().getReference("tasks");

                        db.child(task.getId()).child("status").setValue("DONE");
                        db.child(task.getId()).child("tanggalSelesai").setValue(tanggalSelesai);

                        Toast.makeText(v.getContext(), "Task selesai ✔", Toast.LENGTH_SHORT).show();
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        // =========================
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

            Intent intent = new Intent(v.getContext(), AddTaskActivity.class);

            intent.putExtra("id", task.getId());
            intent.putExtra("project", task.getProject());
            intent.putExtra("jenis", task.getJenis());
            intent.putExtra("mulai", task.getMulai());
            intent.putExtra("due", task.getDue());
            intent.putExtra("status", task.getStatus());

            v.getContext().startActivity(intent);
        });

        // =========================
        // 🔥 STATUS LOGIC
        // =========================
        String status = task.getStatus();

        if (status != null) {
            status = status.replace(" ", "_").toUpperCase();
        }

        if ("DONE".equals(status)) {

            holder.txtStatus.setText("DONE");
            holder.txtStatus.setBackgroundResource(R.drawable.bg_status_done);
            holder.btnDone.setVisibility(View.GONE);
            holder.btnDone.setEnabled(false);

        } else {

            try {
                SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
                Date dueDate = sdf.parse(task.getDue());
                Date today = new Date();

                if (dueDate != null && today.after(dueDate)) {
                    holder.txtStatus.setText("OVERDUE");
                    holder.txtStatus.setBackgroundResource(R.drawable.bg_status_overdue);
                } else {
                    holder.txtStatus.setText("ON PROGRESS");
                    status = "ON_PROGRESS";
                    holder.txtStatus.setBackgroundResource(R.drawable.bg_status_progress);
                }

            } catch (Exception e) {
                holder.txtStatus.setText("UNKNOWN");
            }
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


    @Override
    public int getItemCount() {
        return taskList == null ? 0 : taskList.size();
    }
}
package com.sipaman.maintenance;

import java.util.List;

public class Task {

    public String id;
    public String project;
    public String jenis;
    public String mulai;
    public String due;
    public String status;
    public String tanggalSelesai;

    public List<String> beforeUrls;
    public List<String> afterUrls;

    // 🔥 WAJIB kosong (Firebase butuh ini)
    public Task() {
    }

    public Task(String id, String project, String jenis,
                String mulai, String due, String status,
                String tanggalSelesai,
                List<String> beforeUrls,
                List<String> afterUrls) {

        this.id = id;
        this.project = project;
        this.jenis = jenis;
        this.mulai = mulai;
        this.due = due;
        this.status = status;
        this.tanggalSelesai = tanggalSelesai;
        this.beforeUrls = beforeUrls;
        this.afterUrls = afterUrls;
    }

    // 🔥 GETTER (INI YANG KURANG)
    public String getId() { return id; }

    public String getProject() { return project; }

    public String getJenis() { return jenis; }

    public String getMulai() { return mulai; }

    public String getDue() { return due; }

    public String getStatus() { return status; }

    public String getTanggalSelesai() { return tanggalSelesai; }

    public List<String> getBeforeUrls() { return beforeUrls; }

    public List<String> getAfterUrls() { return afterUrls; }
}
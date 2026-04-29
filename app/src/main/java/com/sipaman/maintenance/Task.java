package com.sipaman.maintenance;

import java.util.ArrayList;
import java.util.List;

public class Task {

    private String id;
    private String project;
    private String jenis;
    private String mulai;
    private String due;
    private String status;
    private String tanggalSelesai;

    private String pic;
    private String priority;
    private String deskripsi;

    private List<String> beforeUrls;
    private List<String> afterUrls;

    // 🔥 WAJIB Firebase
    public Task() {beforeUrls = new ArrayList<>();
        afterUrls = new ArrayList<>();}

    // 🔥 Constructor lengkap
    public Task(String id, String project, String jenis, String mulai,
                String due, String status,
                String pic, String priority, String deskripsi,
                List<String> beforeUrls, List<String> afterUrls) {

        this.id = id;
        this.project = project;
        this.jenis = jenis;
        this.mulai = mulai;
        this.due = due;
        this.status = status;
        this.pic = pic;
        this.priority = priority;
        this.deskripsi = deskripsi;
        this.beforeUrls = beforeUrls;
        this.afterUrls = afterUrls;
    }

    // 🔥 GETTER
    public String getId() { return id; }
    public String getProject() { return project; }
    public String getJenis() { return jenis; }
    public String getMulai() { return mulai; }
    public String getDue() { return due; }
    public String getStatus() { return status; }
    public String getTanggalSelesai() { return tanggalSelesai; }

    public String getPic() { return pic; }
    public String getPriority() { return priority; }
    public String getDeskripsi() { return deskripsi; }

    public List<String> getBeforeUrls() { return beforeUrls; }
    public List<String> getAfterUrls() { return afterUrls; }

    // 🔥 SETTER (WAJIB BIAR FIREBASE BISA MAP)
    public void setId(String id) { this.id = id; }
    public void setProject(String project) { this.project = project; }
    public void setJenis(String jenis) { this.jenis = jenis; }
    public void setMulai(String mulai) { this.mulai = mulai; }
    public void setDue(String due) { this.due = due; }
    public void setStatus(String status) { this.status = status; }
    public void setTanggalSelesai(String tanggalSelesai) { this.tanggalSelesai = tanggalSelesai; }

    public void setPic(String pic) { this.pic = pic; }
    public void setPriority(String priority) { this.priority = priority; }
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }

    public void setBeforeUrls(List<String> beforeUrls) { this.beforeUrls = beforeUrls; }
    public void setAfterUrls(List<String> afterUrls) { this.afterUrls = afterUrls; }
}
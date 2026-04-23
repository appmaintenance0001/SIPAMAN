package com.sipaman.maintenance;

public class Task {

    private String id;
    private String project;
    private String jenis;
    private String mulai;
    private String due;
    private String status;
    private String tanggalSelesai;
    public Task() {
    }

    // CONSTRUCTOR
    public Task(String Id, String Project, String jenis, String mulai, String due, String status, String tanggalSelesai) {
        this.id = Id;
        this.project = Project;
        this.jenis = jenis;
        this.mulai = mulai;
        this.due = due;
        this.status = status;
        this.tanggalSelesai = tanggalSelesai;
    }

    // GETTER
    public String getId() {
        return id;
    }
    public String getProject() {
        return project;
    }
    public String getJenis() {
        return jenis;
    }

    public String getMulai() {
        return mulai;
    }

    public String getDue() {
        return due;
    }

    public String getStatus() {
        return status;
    }

    public String getTanggalSelesai() { return tanggalSelesai; }
}
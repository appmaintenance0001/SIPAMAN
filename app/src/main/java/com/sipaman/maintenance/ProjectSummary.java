package com.sipaman.maintenance;

public class ProjectSummary {

    private String projectId;
    private String projectName;

    private int totalTask;
    private int doneTask;
    private int progressTask;
    private int overdueTask;

    public ProjectSummary() {
    }

    public ProjectSummary(String projectId, String projectName) {
        this.projectId = projectId;
        this.projectName = projectName;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public int getTotalTask() {
        return totalTask;
    }

    public int getDoneTask() {
        return doneTask;
    }

    public int getProgressTask() {
        return progressTask;
    }

    public int getOverdueTask() {
        return overdueTask;
    }

    public void increaseTotal() {
        totalTask++;
    }

    public void increaseDone() {
        doneTask++;
    }

    public void increaseProgress() {
        progressTask++;
    }

    public void increaseOverdue() {
        overdueTask++;
    }
}
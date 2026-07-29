package com.sipaman.maintenance;

public class ActivityLog {
    private String id;
    private String uid;
    private String userName;
    private String type;
    private long timestamp;
    private String taskId;
    private String taskTitle;
    private String message;

    public ActivityLog() {
    }

    public ActivityLog(String id, String uid, String userName, String type, long timestamp, String taskId, String taskTitle, String message) {
        this.id = id;
        this.uid = uid;
        this.userName = userName;
        this.type = type;
        this.timestamp = timestamp;
        this.taskId = taskId;
        this.taskTitle = taskTitle;
        this.message = message;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }
    public String getTaskTitle() { return taskTitle; }
    public void setTaskTitle(String taskTitle) { this.taskTitle = taskTitle; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}

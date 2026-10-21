package com.sipaman.maintenance;

public class ActivityLog {
    private String id;
    private String uid;
    private String userName;
    private String type;
    private long timestamp;

    // Field lama dipertahankan untuk kompatibilitas log task.
    private String taskId;
    private String taskTitle;
    private String message;

    // Field tambahan untuk mencatat aktivitas task maupun project.
    private String entityType; // "task" atau "project"
    private String entityId;
    private String entityName;
    private String changes;    // Ringkasan nilai lama -> nilai baru

    public ActivityLog() {
        // Wajib untuk Firebase Realtime Database.
    }

    // Konstruktor lama: jangan dihapus karena masih dipakai kode yang ada.
    public ActivityLog(String id, String uid, String userName, String type,
                       long timestamp, String taskId, String taskTitle, String message) {
        this.id = id;
        this.uid = uid;
        this.userName = userName;
        this.type = type;
        this.timestamp = timestamp;
        this.taskId = taskId;
        this.taskTitle = taskTitle;
        this.message = message;
        this.entityType = "task";
        this.entityId = taskId;
        this.entityName = taskTitle;
    }

    // Konstruktor baru untuk aktivitas task/project dengan rincian perubahan.
    public ActivityLog(String id, String uid, String userName, String type,
                       long timestamp, String entityType, String entityId,
                       String entityName, String message, String changes) {
        this.id = id;
        this.uid = uid;
        this.userName = userName;
        this.type = type;
        this.timestamp = timestamp;
        this.entityType = entityType;
        this.entityId = entityId;
        this.entityName = entityName;
        this.message = message;
        this.changes = changes;

        if ("task".equalsIgnoreCase(entityType)) {
            this.taskId = entityId;
            this.taskTitle = entityName;
        }
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

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }

    public String getEntityName() { return entityName; }
    public void setEntityName(String entityName) { this.entityName = entityName; }

    public String getChanges() { return changes; }
    public void setChanges(String changes) { this.changes = changes; }
}

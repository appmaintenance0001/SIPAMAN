package com.sipaman.maintenance;

public class NotificationItem {
    private final String title;
    private final String message;
    private final String date;
    private final String type;
    private final Task task;

    public NotificationItem(String title, String message, String date, String type, Task task) {
        this.title = title;
        this.message = message;
        this.date = date;
        this.type = type;
        this.task = task;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getDate() {
        return date;
    }

    public String getType() {
        return type;
    }

    public Task getTask() {
        return task;
    }
}

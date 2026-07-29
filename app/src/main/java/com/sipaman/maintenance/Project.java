package com.sipaman.maintenance;

public class Project {

    private String id;
    private String name;
    private String location;
    private String color;
    private String icon;
    private long createdAt;

    public Project() {
    }

    public Project(String id, String name, String location, String color, String icon, long createdAt) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.color = color;
        this.icon = icon;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}

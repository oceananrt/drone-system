package com.drone.dronesystem.entity;

import com.drone.dronesystem.constant.TaskType; // 确保导入

public class DroneTask {

    private TaskType type;       // 注意：这里用的是枚举
    private double lat;
    private double lng;
    private int height;
    private int delaySeconds;

    // ======================
    // 🔥 必须加上 Getter
    // ======================
    public TaskType getType() { return type; }
    public double getLat() { return lat; }
    public double getLng() { return lng; }
    public int getHeight() { return height; }
    public int getDelaySeconds() { return delaySeconds; }

    // Setter（如果需要传参设置也可以加）
    public void setType(TaskType type) { this.type = type; }
    public void setLat(double lat) { this.lat = lat; }
    public void setLng(double lng) { this.lng = lng; }
    public void setHeight(int height) { this.height = height; }
    public void setDelaySeconds(int delaySeconds) { this.delaySeconds = delaySeconds; }
}
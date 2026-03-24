package com.drone.dronesystem.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 真机飞行日志（云端存储）
 */
@Entity
@Table(name = "drone_flight_log")
public class DroneFlightLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 无人机状态
    private double lat;
    private double lng;
    private float altitude;
    private int battery;
    private String flyStatus;

    // 日志时间
    private LocalDateTime createTime = LocalDateTime.now();

    // getter & setter
    public Long getId() { return id; }
    public double getLat() { return lat; }
    public double getLng() { return lng; }
    public float getAltitude() { return altitude; }
    public int getBattery() { return battery; }
    public String getFlyStatus() { return flyStatus; }
    public LocalDateTime getCreateTime() { return createTime; }

    public void setLat(double lat) { this.lat = lat; }
    public void setLng(double lng) { this.lng = lng; }
    public void setAltitude(float altitude) { this.altitude = altitude; }
    public void setBattery(int battery) { this.battery = battery; }
    public void setFlyStatus(String flyStatus) { this.flyStatus = flyStatus; }
}
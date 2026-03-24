package com.drone.dronesystem.entity;

/**
 * 真机 GPS 位置
 */
public class DroneLocation {

    // 经纬度
    private double lat;
    private double lng;

    // 高度、速度
    private float altitude;
    private float speed;

    // 时间戳
    private long timestamp;

    // 必须加 getter + setter
    public double getLat() { return lat; }
    public double getLng() { return lng; }
    public float getAltitude() { return altitude; }
    public float getSpeed() { return speed; }
    public long getTimestamp() { return timestamp; }

    public void setLat(double lat) { this.lat = lat; }
    public void setLng(double lng) { this.lng = lng; }
    public void setAltitude(float altitude) { this.altitude = altitude; }
    public void setSpeed(float speed) { this.speed = speed; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
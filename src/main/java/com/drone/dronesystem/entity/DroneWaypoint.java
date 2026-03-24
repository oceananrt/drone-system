package com.drone.dronesystem.entity;

public class DroneWaypoint {
    private double lat;
    private double lng;
    private int height;
    private int speed;
    private int hoverTime;

    public double getLat() { return lat; }
    public double getLng() { return lng; }
    public int getHeight() { return height; }
    public int getSpeed() { return speed; }
    public int getHoverTime() { return hoverTime; }

    public void setLat(double lat) { this.lat = lat; }
    public void setLng(double lng) { this.lng = lng; }
    public void setHeight(int height) { this.height = height; }
    public void setSpeed(int speed) { this.speed = speed; }
    public void setHoverTime(int hoverTime) { this.hoverTime = hoverTime; }
}
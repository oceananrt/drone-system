package com.drone.dronesystem.entity;

/**
 * 真实无人机实时数据
 * 对应飞控上传的：GPS、姿态、电压、高度、速度、经纬度
 */
public class DroneRealData {

    // 无人机基本信息
    private Long droneId;
    private String droneName;

    // 飞行姿态
    private float altitude;      // 相对高度
    private float speed;        // 水平速度
    private float voltage;      // 电池电压
    private int battery;        // 电量

    // GPS
    private double lat;         // 纬度
    private double lng;         // 经度
    private float gpsNum;       // 卫星数

    // 状态
    private String flyStatus;   // 飞行状态
    private boolean isConnected;// 是否连接

    public DroneRealData() {
        this.droneId = 1L;
        this.droneName = "真机-01";
        this.isConnected = false;
        this.flyStatus = "DISCONNECTED";
    }

    // GET SET 自动生成即可
    public Long getDroneId() { return droneId; }
    public void setDroneId(Long droneId) { this.droneId = droneId; }
    public String getDroneName() { return droneName; }
    public void setDroneName(String droneName) { this.droneName = droneName; }
    public float getAltitude() { return altitude; }
    public void setAltitude(float altitude) { this.altitude = altitude; }
    public float getSpeed() { return speed; }
    public void setSpeed(float speed) { this.speed = speed; }
    public float getVoltage() { return voltage; }
    public void setVoltage(float voltage) { this.voltage = voltage; }
    public int getBattery() { return battery; }
    public void setBattery(int battery) { this.battery = battery; }
    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }
    public double getLng() { return lng; }
    public void setLng(double lng) { this.lng = lng; }
    public float getGpsNum() { return gpsNum; }
    public void setGpsNum(float gpsNum) { this.gpsNum = gpsNum; }
    public String getFlyStatus() { return flyStatus; }
    public void setFlyStatus(String flyStatus) { this.flyStatus = flyStatus; }
    public boolean isConnected() { return isConnected; }
    public void setConnected(boolean connected) { isConnected = connected; }
}
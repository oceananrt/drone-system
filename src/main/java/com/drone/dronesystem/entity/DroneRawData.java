package com.drone.dronesystem.entity;

/**
 * 真实无人机 原始数据
 * 直接从飞控 / 串口读取
 */
public class DroneRawData {

    // 帧头
    public byte head1;
    public byte head2;

    // 飞行数据
    public int altitude;      // 高度（cm）
    public int speed;         // 速度（cm/s）
    public int voltage;       // 电压（mV）
    public int battery;       // 电量（%）
    public int flyMode;       // 飞行模式
    public int gpsSat;        // GPS卫星数
    public double lat;        // 纬度
    public double lng;        // 经度

    // 校验
    public byte checkSum;
    public byte tail1;
    public byte tail2;
}

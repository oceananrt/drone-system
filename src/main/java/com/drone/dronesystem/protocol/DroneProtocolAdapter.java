package com.drone.dronesystem.protocol;

import com.drone.dronesystem.entity.DroneRealData;

/**
 * 无人机协议适配器接口
 * 所有协议实现（自定义协议、MAVLink、DJI Cloud API）都实现此接口
 * 服务层通过此接口与飞控通信，不关心底层协议细节
 */
public interface DroneProtocolAdapter {

    /**
     * 获取协议名称
     */
    String getProtocolName();

    /**
     * 连接飞控
     * @return true 连接成功
     */
    boolean connect();

    /**
     * 断开连接
     */
    void disconnect();

    /**
     * 是否已连接
     */
    boolean isConnected();

    /**
     * 读取实时遥测数据
     * 由守护线程每秒调用一次
     * @return 最新的无人机实时数据
     */
    DroneRealData readRealData();

    /**
     * 起飞
     * @param height 目标高度（米）
     */
    boolean takeoff(int height);

    /**
     * 降落
     */
    boolean land();

    /**
     * 悬停
     */
    boolean hover();

    /**
     * 一键返航
     */
    boolean returnHome();

    /**
     * 设置目标高度
     * @param height 目标高度（米）
     */
    boolean setAltitude(int height);

    /**
     * 飞至 GPS 坐标点
     * @param lat 纬度
     * @param lng 经度
     * @param height 目标高度（米）
     */
    boolean goTo(double lat, double lng, int height);

    /**
     * 发送心跳包（保持连接）
     */
    default void sendHeartbeat() {}
}

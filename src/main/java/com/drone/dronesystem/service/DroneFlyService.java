package com.drone.dronesystem.service;

public interface DroneFlyService {

    // 起飞
    boolean takeoff(int height);

    // 降落
    boolean land();

    // 悬停
    boolean hover();

    // 一键返航
    boolean returnHome();

    // 设置高度
    boolean setHeight(int height);

    // 飞到GPS坐标
    boolean goTo(double lat, double lng, int height);
}
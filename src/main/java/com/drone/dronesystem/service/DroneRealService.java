package com.drone.dronesystem.service;

import com.drone.dronesystem.entity.DroneRealData;

public interface DroneRealService {

    // 获取真机实时数据
    DroneRealData getRealData();

    // 连接无人机
    boolean connectDrone();

    // 起飞
    boolean takeoff();

    // 降落
    boolean land();

    // 返航
    boolean returnHome();

    boolean hover();

}
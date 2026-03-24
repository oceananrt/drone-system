package com.drone.dronesystem.service;

import com.drone.dronesystem.entity.DroneLocation;
import java.util.List;

public interface DroneMapService {

    // 获取当前位置
    DroneLocation getCurrentLocation();

    // 获取历史轨迹
    List<DroneLocation> getHistoryPath();

    // 清空轨迹
    void clearPath();
}
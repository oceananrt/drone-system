package com.drone.dronesystem.service;

import com.drone.dronesystem.entity.DroneRealData;

public interface DroneLogService {
    // 保存飞行数据到云端
    void saveFlightLog(DroneRealData data);
}
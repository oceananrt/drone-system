package com.drone.dronesystem.service;

import com.drone.dronesystem.entity.DroneRealData;

public interface DroneSafetyService {
    // 全面安全检查
    boolean checkAllSafety(DroneRealData data);

    // 低电量保护
    boolean checkBattery(DroneRealData data);

    // GPS保护
    boolean checkGps(DroneRealData data);

    // 高度保护
    boolean checkAltitude(DroneRealData data);
}
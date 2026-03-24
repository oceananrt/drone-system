package com.drone.dronesystem.constant;

public enum DroneState {
    IDLE,        // 空闲
    TAKEOFF,     // 起飞中
    FLYING,      // 飞行中
    HOVER,       // 悬停
    LANDING,     // 降落中
    RETURN_HOME, // 返航中
    ERROR        // 异常
}
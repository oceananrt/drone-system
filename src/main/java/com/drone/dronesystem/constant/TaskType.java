package com.drone.dronesystem.constant;

public enum TaskType {
    TAKEOFF,      // 起飞
    LAND,         // 降落
    HOVER,        // 悬停
    RETURN_HOME,  // 返航
    GO_TO_GPS,    // 飞至GPS点
    TAKE_PHOTO,   // 拍照
    RECORD_VIDEO  // 录像
}
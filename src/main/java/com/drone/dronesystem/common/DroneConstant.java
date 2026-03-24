package com.drone.dronesystem.common;

public class DroneConstant {
    // 安全参数
    public static final int MAX_HEIGHT = 150;
    public static final int MAX_DISTANCE = 1000;
    public static final int BATTERY_WARNING = 20;
    public static final int BATTERY_RETURN = 15;
    public static final int BATTERY_LAND = 10;

    // 模式
    public static final String MODE_MANUAL = "手动模式";
    public static final String MODE_GESTURE = "手势模式";
    public static final String MODE_TRACKING = "跟踪模式";
    public static final String MODE_MISSION = "航线模式";
}
package com.drone.dronesystem.protocol;

/**
 * 真实无人机指令
 * 直接下发给飞控
 */
public enum DroneCmd {

    CONNECT(0x01),    // 连接飞控
    TAKEOFF(0x02),    // 起飞
    LAND(0x03),       // 降落
    RETURN_HOME(0x04),// 一键返航
    HOVER(0x05),      // 悬停
    SET_ALT(0x06),    // 设置目标高度
    SET_SPEED(0x07),  // 设置速度
    SET_GPS(0x08);    // 设置GPS坐标点

    private final byte code;

    DroneCmd(int code) {
        this.code = (byte) code;
    }

    public byte getCode() {
        return code;
    }
}
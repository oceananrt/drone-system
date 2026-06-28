package com.drone.dronesystem.protocol;

/**
 * MAVLink 协议工具
 * 用于对接 Pixhawk / ArduPilot / PX4 等开源飞控
 *
 * 真机对接时需完成以下步骤：
 * 1. 引入 MAVLink Java 生成库（mavlink-generator）
 * 2. 根据飞控固件版本选择对应 dialect（common / ardupilotmega）
 * 3. 通过串口（SerialPortUtil）或 UDP 收发 MAVLink 消息
 *
 * 参考：https://mavlink.io/en/
 *
 * TODO 真机对接：以下方法需替换为真实 MAVLink 消息收发
 */
public class MavLinkUtil {

    /**
     * 建立 MAVLink 心跳连接
     * 发送 HEARTBEAT 消息，等待飞控响应
     */
    public static void connect() {
        // TODO 真机对接：发送 MAVLink HEARTBEAT (msg_id=0)
        // MAVLink dialect: common.Heartbeat
        // 等待飞控 HEARTBEAT 回复确认连接
        System.out.println("[MavLink] connect() - 待实现");
    }

    /**
     * 起飞指令
     * 发送 COMMAND_LONG，command=MAV_CMD_NAV_TAKEOFF
     */
    public static void takeoff() {
        // TODO 真机对接：发送 COMMAND_LONG (msg_id=76)
        // command: MAV_CMD_NAV_TAKEOFF (22)
        // param7: 目标高度(m)
        System.out.println("[MavLink] takeoff() - 待实现");
    }

    /**
     * 降落指令
     * 发送 COMMAND_LONG，command=MAV_CMD_NAV_LAND
     */
    public static void land() {
        // TODO 真机对接：发送 COMMAND_LONG (msg_id=76)
        // command: MAV_CMD_NAV_LAND (21)
        System.out.println("[MavLink] land() - 待实现");
    }

    /**
     * 返航指令
     * 发送 COMMAND_LONG，command=MAV_CMD_NAV_RETURN_TO_LAUNCH
     */
    public static void returnHome() {
        // TODO 真机对接：发送 COMMAND_LONG (msg_id=76)
        // command: MAV_CMD_NAV_RETURN_TO_LAUNCH (20)
        System.out.println("[MavLink] returnHome() - 待实现");
    }

    /**
     * 设置目标高度
     * 发送 COMMAND_LONG，command=MAV_CMD_NAV_WAYPOINT
     */
    public static void setAltitude(float alt) {
        // TODO 真机对接：发送 COMMAND_LONG (msg_id=76)
        // command: MAV_CMD_NAV_WAYPOINT (16)
        // param7: 目标高度(m)
        System.out.println("[MavLink] setAltitude(" + alt + ") - 待实现");
    }

    /**
     * 飞至 GPS 坐标点
     * 发送 SET_POSITION_TARGET_GLOBAL_INT (msg_id=86)
     */
    public static void goToGps(double lat, double lng, float alt) {
        // TODO 真机对接：发送 SET_POSITION_TARGET_GLOBAL_INT (msg_id=86)
        // lat_int: 纬度 * 1e7
        // lon_int: 经度 * 1e7
        // alt: 目标高度(m)
        System.out.println("[MavLink] goToGps(" + lat + ", " + lng + ", " + alt + ") - 待实现");
    }
}

package com.drone.dronesystem.protocol;

/**
 * 真机心跳包
 * 用于保持连接、判断无人机是否在线
 */
public class DroneHeartbeat {

    // 心跳指令
    private static final byte[] HEARTBEAT = {
            (byte)0xAA, (byte)0x55,
            (byte)0x00,
            (byte)0x0D, (byte)0x0A
    };

    public static void sendHeartbeat() {
        if (SerialPortUtil.isConnected()) {
            SerialPortUtil.send(HEARTBEAT);
        }
    }
}
package com.drone.dronesystem.protocol;

/**
 * 真实无人机指令发送器
 * 最终通过 串口 / TCP / MavLink 发给飞控
 */
public class DroneSender {

    /**
     * 发送指令到真机
     */
    public static boolean send(DroneCmd cmd) {
        byte[] packet = new byte[]{
                (byte) 0xAA,         // 帧头
                (byte) 0x55,
                cmd.getCode(),       // 指令
                (byte) 0x00,         // 数据位
                (byte) 0x00,
                (byte) 0x00,
                (byte) 0x00,
                (byte) 0x0D, (byte) 0x0A  // 帧尾
        };

        // 真实发送：串口 / 网络
        return SerialPortUtil.send(packet);
    }

    /**
     * 发送带参数的指令（如设置高度、GPS）
     */
    public static boolean sendWithData(DroneCmd cmd, int data) {
        byte[] packet = new byte[]{
                (byte) 0xAA,
                (byte) 0x55,
                cmd.getCode(),
                (byte) (data >> 8),
                (byte) (data & 0xFF),
                (byte) 0x00,
                (byte) 0x00,
                (byte) 0x0D, (byte) 0x0A
        };
        return SerialPortUtil.send(packet);
    }

    /**
     * 发送GPS坐标指令
     * 协议格式：AA 55 08 [lat:4字节] [lng:4字节] [height:2字节] 0D 0A
     * 经纬度以 1e7 度为单位（与解析端一致）
     */
    public static boolean sendGpsCommand(double lat, double lng, int height) {
        int latInt = (int) (lat * 10000000);
        int lngInt = (int) (lng * 10000000);

        byte[] packet = new byte[]{
                (byte) 0xAA,
                (byte) 0x55,
                DroneCmd.SET_GPS.getCode(),
                (byte) (latInt >> 24),
                (byte) (latInt >> 16),
                (byte) (latInt >> 8),
                (byte) (latInt & 0xFF),
                (byte) (lngInt >> 24),
                (byte) (lngInt >> 16),
                (byte) (lngInt >> 8),
                (byte) (lngInt & 0xFF),
                (byte) (height >> 8),
                (byte) (height & 0xFF),
                (byte) 0x0D,
                (byte) 0x0A
        };
        return SerialPortUtil.send(packet);
    }
}

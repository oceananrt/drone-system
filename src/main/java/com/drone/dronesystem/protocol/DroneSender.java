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
}
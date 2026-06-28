package com.drone.dronesystem.protocol;

import com.fazecast.jSerialComm.SerialPort;

/**
 * 真机串口通信工具
 * 真实连接 USB / TTL / 飞控 / 数传电台
 */
public class SerialPortUtil {

    // 串口对象
    private static SerialPort serialPort;

    /**
     * 打开串口（真实）
     * COM3  Windows
     * /dev/ttyUSB0  Linux
     */
    public static boolean open() {
        try {
            // 自动搜索无人机串口
            SerialPort[] ports = SerialPort.getCommPorts();
            if (ports.length == 0) {
                System.err.println("未找到串口设备");
                return false;
            }

            // 默认使用第一个串口（可改为指定）
            serialPort = ports[0];
            serialPort.setBaudRate(115200);   // 无人机常用波特率
            serialPort.setNumDataBits(8);
            serialPort.setNumStopBits(1);
            serialPort.setParity(SerialPort.NO_PARITY);

            // 打开
            return serialPort.openPort();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 打开指定串口
     * @param portName 串口名（如 COM3、/dev/ttyUSB0）
     * @param baudRate 波特率
     */
    public static boolean openPort(String portName, int baudRate) {
        try {
            serialPort = SerialPort.getCommPort(portName);
            serialPort.setBaudRate(baudRate);
            serialPort.setNumDataBits(8);
            serialPort.setNumStopBits(1);
            serialPort.setParity(SerialPort.NO_PARITY);
            return serialPort.openPort();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 关闭串口
     */
    public static void close() {
        if (serialPort != null) {
            serialPort.closePort();
        }
    }

    /**
     * 发送指令到真机（真实）
     */
    public static boolean send(byte[] cmd) {
        if (serialPort == null || !serialPort.isOpen()) {
            return false;
        }
        serialPort.writeBytes(cmd, cmd.length);
        return true;
    }

    /**
     * 读取无人机数据（真实）
     */
    public static byte[] read() {
        if (serialPort == null || !serialPort.isOpen()) {
            return null;
        }

        try {
            byte[] buffer = new byte[256];
            int len = serialPort.readBytes(buffer, buffer.length);
            if (len <= 0) return null;

            // 裁剪有效数据
            byte[] result = new byte[len];
            System.arraycopy(buffer, 0, result, 0, len);
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 是否已连接
     */
    public static boolean isConnected() {
        return serialPort != null && serialPort.isOpen();
    }
}
package com.drone.dronesystem.protocol;

import com.drone.dronesystem.entity.DroneRawData;

/**
 * 真机协议解析器
 * 把串口收到的 byte[] 转为真实无人机数据
 */
public class DroneParser {

    /**
     * 解析真机数据
     * 真实飞控上行数据格式
     */
    public static DroneRawData parse(byte[] buffer) {
        DroneRawData data = new DroneRawData();

        try {
            // 帧头
            data.head1 = buffer[0];
            data.head2 = buffer[1];

            // 高度 (2字节)
            data.altitude = ((buffer[2] & 0xFF) << 8) | (buffer[3] & 0xFF);

            // 速度 (2字节)
            data.speed = ((buffer[4] & 0xFF) << 8) | (buffer[5] & 0xFF);

            // 电压 (2字节)
            data.voltage = ((buffer[6] & 0xFF) << 8) | (buffer[7] & 0xFF);

            // 电量
            data.battery = buffer[8] & 0xFF;

            // 飞行模式
            data.flyMode = buffer[9] & 0xFF;

            // GPS卫星数
            data.gpsSat = buffer[10] & 0xFF;

            // 经纬度（真实格式）
            data.lat = ((buffer[11] << 24) | (buffer[12] << 16) | (buffer[13] << 8) | buffer[14]) / 10000000.0;
            data.lng = ((buffer[15] << 24) | (buffer[16] << 16) | (buffer[17] << 8) | buffer[18]) / 10000000.0;

            // 校验和 + 帧尾
            data.checkSum = buffer[19];
            data.tail1 = buffer[20];
            data.tail2 = buffer[21];

        } catch (Exception e) {
            return null;
        }

        return data;
    }
}
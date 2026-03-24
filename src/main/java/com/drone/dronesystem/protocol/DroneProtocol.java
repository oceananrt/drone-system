package com.drone.dronesystem.protocol;

import com.drone.dronesystem.entity.DroneRealData;

public class DroneProtocol {

    // 真实无人机指令
    public static final byte CONNECT      = 0x01;
    public static final byte TAKEOFF      = 0x02;
    public static final byte LAND         = 0x03;
    public static final byte RETURN_HOME  = 0x04;

    /**
     * 【真机专用】
     * 解析飞控上传的原始二进制数据
     */
    public static DroneRealData parse(byte[] buffer) {
        DroneRealData data = new DroneRealData();

        try {
            // 真实协议解析格式
            data.setAltitude(((buffer[2] & 0xFF) << 8 | (buffer[3] & 0xFF)) / 100.0f);
            data.setSpeed(((buffer[4] & 0xFF) << 8 | (buffer[5] & 0xFF)) / 100.0f);
            data.setBattery(buffer[6] & 0xFF);
            data.setVoltage(((buffer[7] & 0xFF) << 8 | (buffer[8] & 0xFF)) / 100.0f);
            data.setConnected(true);

            int status = buffer[9] & 0xFF;
            switch (status) {
                case 0: data.setFlyStatus("STANDBY"); break;
                case 1: data.setFlyStatus("FLYING"); break;
                case 2: data.setFlyStatus("LANDING"); break;
                case 3: data.setFlyStatus("RETURNING"); break;
                default: data.setFlyStatus("ERROR");
            }

        } catch (Exception e) {
            data.setConnected(false);
        }

        return data;
    }
}
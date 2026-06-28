package com.drone.dronesystem.protocol.adapter;

import com.drone.dronesystem.entity.DroneRealData;
import com.drone.dronesystem.entity.DroneRawData;
import com.drone.dronesystem.protocol.*;

/**
 * 自定义协议适配器
 * 封装已有的 SerialPortUtil + DroneParser + DroneSender 静态调用
 * 帧格式：帧头 AA55 + 指令 + 数据 + 帧尾 0D0A
 * 上行帧 22 字节，下行帧 9/15 字节
 */
public class CustomProtocolAdapter implements DroneProtocolAdapter {

    private final DroneRealData droneData = new DroneRealData();

    @Override
    public String getProtocolName() {
        return "custom";
    }

    @Override
    public boolean connect() {
        boolean ok = SerialPortUtil.open();
        droneData.setConnected(ok);
        return ok;
    }

    @Override
    public void disconnect() {
        SerialPortUtil.close();
        droneData.setConnected(false);
    }

    @Override
    public boolean isConnected() {
        return SerialPortUtil.isConnected();
    }

    @Override
    public DroneRealData readRealData() {
        if (!isConnected()) {
            droneData.setConnected(false);
            return droneData;
        }

        byte[] buffer = SerialPortUtil.read();
        if (buffer == null || buffer.length < 22) {
            return droneData;
        }

        DroneRawData raw = DroneParser.parse(buffer);
        if (raw == null) {
            return droneData;
        }

        droneData.setAltitude(raw.altitude / 100.0f);
        droneData.setSpeed(raw.speed / 100.0f);
        droneData.setVoltage(raw.voltage / 1000.0f);
        droneData.setBattery(raw.battery);
        droneData.setGpsNum(raw.gpsSat);
        droneData.setLat(raw.lat);
        droneData.setLng(raw.lng);
        droneData.setConnected(true);

        switch (raw.flyMode) {
            case 0: droneData.setFlyStatus("STANDBY"); break;
            case 1: droneData.setFlyStatus("FLYING"); break;
            case 2: droneData.setFlyStatus("LANDING"); break;
            case 3: droneData.setFlyStatus("RETURNING"); break;
            case 4: droneData.setFlyStatus("HOVER"); break;
            default: droneData.setFlyStatus("ERROR"); break;
        }

        return droneData;
    }

    @Override
    public boolean takeoff(int height) {
        return DroneSender.sendWithData(DroneCmd.TAKEOFF, height);
    }

    @Override
    public boolean land() {
        return DroneSender.send(DroneCmd.LAND);
    }

    @Override
    public boolean hover() {
        return DroneSender.send(DroneCmd.HOVER);
    }

    @Override
    public boolean returnHome() {
        return DroneSender.send(DroneCmd.RETURN_HOME);
    }

    @Override
    public boolean setAltitude(int height) {
        return DroneSender.sendWithData(DroneCmd.SET_ALT, height);
    }

    @Override
    public boolean goTo(double lat, double lng, int height) {
        return DroneSender.sendGpsCommand(lat, lng, height);
    }

    @Override
    public void sendHeartbeat() {
        DroneHeartbeat.sendHeartbeat();
    }
}

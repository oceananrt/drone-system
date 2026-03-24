package com.drone.dronesystem.service.impl;

import com.drone.dronesystem.protocol.DroneCmd;
import com.drone.dronesystem.protocol.DroneSender;
import com.drone.dronesystem.service.DroneFlyService;
import org.springframework.stereotype.Service;

@Service
public class DroneFlyServiceImpl implements DroneFlyService {

    @Override
    public boolean takeoff(int height) {
        // 下发起飞指令 + 目标高度
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
    public boolean setHeight(int height) {
        return DroneSender.sendWithData(DroneCmd.SET_ALT, height);
    }

    @Override
    public boolean goTo(double lat, double lng, int height) {
        // 真实项目：GPS坐标组包
        return false;
    }
}
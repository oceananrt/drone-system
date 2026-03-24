package com.drone.dronesystem.service.impl;

import com.drone.dronesystem.constant.DroneSafetyConstant;
import com.drone.dronesystem.entity.DroneRealData;
import com.drone.dronesystem.service.DroneFlyService;
import com.drone.dronesystem.service.DroneSafetyService;
import org.springframework.stereotype.Service;

@Service
public class DroneSafetyServiceImpl implements DroneSafetyService {

    private final DroneFlyService flyService;

    public DroneSafetyServiceImpl(DroneFlyService flyService) {
        this.flyService = flyService;
    }

    @Override
    public boolean checkAllSafety(DroneRealData data) {
        if (!data.isConnected()) return false;

        boolean batterySafe = checkBattery(data);
        boolean gpsSafe = checkGps(data);
        boolean altSafe = checkAltitude(data);

        return batterySafe && gpsSafe && altSafe;
    }

    @Override
    public boolean checkBattery(DroneRealData data) {
        if (data.getBattery() <= DroneSafetyConstant.CRITICAL_BATTERY) {
            flyService.land(); // 强制降落
            return false;
        }
        if (data.getBattery() <= DroneSafetyConstant.LOW_BATTERY) {
            flyService.returnHome(); // 自动返航
        }
        return true;
    }

    @Override
    public boolean checkGps(DroneRealData data) {
        if (data.getGpsNum() < DroneSafetyConstant.MIN_GPS_SAT) {
            flyService.hover(); // GPS弱 → 悬停
            return false;
        }
        return true;
    }

    @Override
    public boolean checkAltitude(DroneRealData data) {
        if (data.getAltitude() > DroneSafetyConstant.MAX_ALTITUDE) {
            flyService.setHeight(100); // 超高压降
            return false;
        }
        return true;
    }
}
package com.drone.dronesystem.service.impl;

import com.drone.dronesystem.entity.DroneFlightLog;
import com.drone.dronesystem.entity.DroneRealData;
import com.drone.dronesystem.repository.DroneFlightLogRepository;
import com.drone.dronesystem.service.DroneLogService;
import org.springframework.stereotype.Service;

@Service
public class DroneLogServiceImpl implements DroneLogService {

    private final DroneFlightLogRepository repository;

    public DroneLogServiceImpl(DroneFlightLogRepository repository) {
        this.repository = repository;
    }

    @Override
    public void saveFlightLog(DroneRealData data) {
        if (data == null || !data.isConnected()) {
            return;
        }

        DroneFlightLog log = new DroneFlightLog();
        log.setLat(data.getLat());
        log.setLng(data.getLng());
        log.setAltitude(data.getAltitude());
        log.setBattery(data.getBattery());
        log.setFlyStatus(data.getFlyStatus());

        // 保存到数据库（云端）
        repository.save(log);
    }
}
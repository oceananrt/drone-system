package com.drone.dronesystem.service.impl;

import com.drone.dronesystem.entity.DroneLocation;
import com.drone.dronesystem.entity.DroneRealData;
import com.drone.dronesystem.service.DroneMapService;
import com.drone.dronesystem.service.DroneRealService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DroneMapServiceImpl implements DroneMapService {

    private final DroneRealService droneRealService;
    private final List<DroneLocation> pathList = new ArrayList<>();

    public DroneMapServiceImpl(DroneRealService droneRealService) {
        this.droneRealService = droneRealService;
    }

    @Override
    public DroneLocation getCurrentLocation() {
        DroneRealData real = droneRealService.getRealData();

        DroneLocation loc = new DroneLocation();
        loc.setLat(real.getLat());
        loc.setLng(real.getLng());
        loc.setAltitude(real.getAltitude());
        loc.setSpeed(real.getSpeed());
        loc.setTimestamp(System.currentTimeMillis());

        // 加入轨迹
        pathList.add(loc);
        return loc;
    }

    @Override
    public List<DroneLocation> getHistoryPath() {
        return new ArrayList<>(pathList);
    }

    @Override
    public void clearPath() {
        pathList.clear();
    }
}
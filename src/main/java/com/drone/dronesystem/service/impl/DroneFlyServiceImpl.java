package com.drone.dronesystem.service.impl;

import com.drone.dronesystem.protocol.DroneProtocolAdapter;
import com.drone.dronesystem.service.DroneFlyService;
import org.springframework.stereotype.Service;

@Service
public class DroneFlyServiceImpl implements DroneFlyService {

    private final DroneProtocolAdapter adapter;

    public DroneFlyServiceImpl(DroneProtocolAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public boolean takeoff(int height) {
        return adapter.takeoff(height);
    }

    @Override
    public boolean land() {
        return adapter.land();
    }

    @Override
    public boolean hover() {
        return adapter.hover();
    }

    @Override
    public boolean returnHome() {
        return adapter.returnHome();
    }

    @Override
    public boolean setHeight(int height) {
        return adapter.setAltitude(height);
    }

    @Override
    public boolean goTo(double lat, double lng, int height) {
        return adapter.goTo(lat, lng, height);
    }
}
